package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;
import org.urm.server.action.CommandExecutor;
import org.urm.server.meta.MetaEngineProduct;

public class SessionController {

	ActionBase serverAction;
	ServerEngine engine;
	
	boolean running = false;
	boolean stop = false;
	boolean started = false;

	public CommandMeta[] executors = null;
	
	Map<String,ServerCommandCall> calls;
	Map<Integer,ActionInit> actions;
	
	int sessionSequence = 0;
	
	public SessionController( ActionBase serverAction , ServerEngine engine ) {
		this.serverAction = serverAction;
		this.engine = engine;
		
		calls = new HashMap<String,ServerCommandCall>();
		actions = new HashMap<Integer,ActionInit>(); 
	}
	
	public void start() throws Exception {
		CommandBuilder builder = new CommandBuilder( serverAction.context.session.clientrc , serverAction.context.session.execrc );
		executors = builder.getExecutors( true , true );
	}
	
	public void waitFinished() throws Exception {
		synchronized( this ) {
			started = true;
			notifyAll();
			
			running = true;
			while( !stop )
				wait();
			running = false;
		}
		
		waitAllActions();
	}

	public ActionInit createRemoteAction( ServerCommandCall call , CommandMethodMeta method , ActionData data ) throws Exception {
		if( !running ) {
			engine.serverAction.error( "server is in progress of shutdown" );
			return( null );
		}
		
		CommandBuilder builder = new CommandBuilder( data.clientrc , engine.execrc );
		
		CommandOptions options = new CommandOptions( serverAction.context.options.meta );
		options.setAction( method , data );
		
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null )
			return( null );
		
		CommandExecutor actionExecutor = engine.createExecutor( commandInfo , options );
		SessionContext session = new SessionContext( engine , data.clientrc , call.sessionId );
		session.setServerRemoteLayout( engine.serverSession );
		
		ActionInit action = engine.createAction( actionExecutor , session , "rjmx-" + data.clientrc.product , call );
		if( action == null )
			return( null );

		return( action );
	}

	public boolean runWebJmx( int sessionId , String productName , CommandMeta meta , CommandOptions options ) throws Exception {
		if( !running ) {
			serverAction.error( "server is in progress of shutdown" );
			return( false );
		}
		
		CommandExecutor actionExecutor = engine.createExecutor( meta , options );
		SessionContext session = new SessionContext( engine , engine.execrc , sessionId );
		MetaEngineProduct product = engine.getProductMeta( productName ); 
		session.setServerProductLayout( product.NAME , product.PATH );
		
		ActionInit action = engine.createAction( actionExecutor , session , "cjmx-" + engine.execrc.product , null );
		if( action == null )
			return( false );

		return( runClientAction( action ) );
	}
	
	public boolean runClientAction( ActionInit clientAction ) {
		SessionContext session = clientAction.session;
		CommandExecutor executor = clientAction.executor;
		
		serverAction.debug( "run client action sessionId=" + session.sessionId + ", workFolder=" + clientAction.artefactory.workFolder.folderPath + " ..." );
		
		synchronized( this ) {
			actions.put( session.sessionId , clientAction );
		}
		
		// execute
		try {
			engine.startAction( clientAction );
			clientAction.meta.loadProduct( clientAction );
			executor.runAction( clientAction );
		}
		catch( Throwable e ) {
			clientAction.log( e );
		}

		boolean res = ( session.isFailed() )? false : true;
		
		if( res )
			clientAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			clientAction.commentExecutor( "COMMAND FAILED" );

		try {
			engine.finishAction( clientAction );
		}
		catch( Throwable e ) {
			clientAction.log( e );
		}
		
		synchronized( this ) {
			actions.remove( session.sessionId );
			serverAction.debug( "finished client action sessionId=" + session.sessionId + ", status=" + res );
		}

		return( res );
	}

	public synchronized int createSessionId() {
		sessionSequence++;
		return( sessionSequence );
	}
	
	public void stop() throws Exception {
		serverAction.info( "stopping server ..." );
		stop = true;
		engine.stop();
		
		synchronized( this ) {
			notifyAll();
		}
	}
	
	public boolean isRunning() {
		return( running );
	}
	
	public ServerCommandCall getCall( int sessionId ) {
		ServerCommandCall call = calls.get( "" + sessionId );
		return( call );
	}
	
	public synchronized void threadStarted( ServerCommandCall thread ) {
		calls.put( "" + thread.sessionId , thread );
		serverAction.debug( "thread started: sessionId=" + thread.sessionId );
	}

	public synchronized void threadStopped( ServerCommandCall thread ) {
		calls.remove( "" + thread.sessionId );
		serverAction.debug( "thread stopped: sessionId=" + thread.sessionId );
	}

	private void waitAllActions() throws Exception {
		synchronized( this ) {
			if( actions.size() == 0 )
				return;
			
			serverAction.info( "waiting for " + actions.size() + " action(s) to complete ..." );
		}
		
		while( true ) {
			synchronized( this ) {
				if( actions.size() == 0 )
					return;
			}
		}
	}

	public void executeInteractiveCommand( String sessionId , String input ) throws Exception {
		ServerCommandCall call = calls.get( "" + sessionId );
		if( call == null )
			serverAction.exit( "unknown call session=" + sessionId );
		
		call.executeInteractiveCommand( input );
	}

	public void stopSession( String sessionId ) throws Exception {
		ServerCommandCall call = calls.get( "" + sessionId );
		if( call == null )
			serverAction.exit( "unknown call session=" + sessionId );
		
		call.stop();
	}

	public boolean waitConnect( String sessionId ) throws Exception {
		ServerCommandCall call = calls.get( "" + sessionId );
		if( call == null )
			serverAction.exit( "unknown call session=" + sessionId );
		
		return( call.waitConnect() );
	}

	public boolean waitStart() {
		synchronized( this ) {
			if( started )
				return( true );
			
			try {
				wait( 30000 );
				return( true );
			}
			catch( Throwable e ) {
				serverAction.log( e );
			}
			return( false );
		}
	}
	
}