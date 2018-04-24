package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.RunContext;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.common.action.CommandOptions;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.EngineCall;
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.meta.engine.AuthUser;
import org.urm.meta.product.Meta;
import org.urm.engine.action.CommandExecutor;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.session.EngineSession;
import org.urm.engine.session.SessionSecurity;

public class SessionService {

	public Engine engine;
	
	boolean running = false;
	boolean stop = false;
	boolean started = false;

	public CommandMeta[] executors = null;
	
	Map<Integer,EngineSession> sessions;
	Map<String,EngineCall> calls;
	Map<Integer,ActionInit> actions;
	
	int sessionSequence = 0;
	
	public SessionService( Engine engine ) {
		this.engine = engine;
	
		sessions = new HashMap<Integer,EngineSession>(); 
		calls = new HashMap<String,EngineCall>();
		actions = new HashMap<Integer,ActionInit>(); 
	}

	public void init() throws Exception {
	}
	
	public void start( ActionBase serverAction ) throws Exception {
		stop = false;
		serverAction.debug( "start session controller ..." );
		CommandBuilder builder = new CommandBuilder( engine.serverSession.clientrc , engine.serverSession.execrc , engine.optionsMeta );
		executors = builder.getExecutors( true , true );
		serverAction.debug( "session controller has been started" );
	}
	
	public void stop( ActionBase serverAction ) throws Exception {
		serverAction.debug( "stop session controller ..." );
		
		stop = true;
		synchronized( this ) {
			notifyAll();
		}
		serverAction.debug( "session controller has been stopped" );
	}
	
	public boolean isRunning() {
		return( running );
	}
	
	public void waitFinished( ActionInit action ) throws Exception {
		synchronized( this ) {
			started = true;
			notifyAll();
			
			running = true;
			while( !stop )
				wait();
			running = false;
		}
		
		waitAllActions( action );
		
		// release all meta
		for( EngineSession session : sessions.values().toArray( new EngineSession[0] ) )
			session.releaseMeta( action );
	}

	public ActionInit createRemoteAction( ActionBase serverAction , EngineCall call , CommandMethodMeta method , ActionData data ) throws Exception {
		if( !running ) {
			engine.error( "server is in progress of shutdown" );
			return( null );
		}
		
		CommandBuilder builder = new CommandBuilder( data.clientrc , engine.execrc , engine.optionsMeta );
		CommandOptions options = new CommandOptions( serverAction.context.options.meta );
		options.setAction( method , data );
		
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null ) {
			engine.error( "unable to create root action, method=" + options.command );
			return( null );
		}
		
		EngineSession session = call.sessionContext;
		session.setServerRemoteProductLayout( engine.serverAction );
		
		ActionInit action = engine.createRootAction( RootActionType.Command , options , session , "call-" + data.clientrc.product , call , false , "Run remote command=" + commandInfo.name + "::" + options.method );
		if( action == null ) {
			engine.error( "unable to create root action, method=" + options.method );
			return( null );
		}

		action.context.loadEnv( action , false );
		return( action );
	}

	public boolean runWebJmx( EngineSession session , CommandMeta meta , CommandOptions options ) throws Exception {
		if( !running ) {
			engine.error( "server is in progress of shutdown" );
			return( false );
		}
		
		ActionBase serverAction = engine.serverAction;
		session.setServerRemoteProductLayout( serverAction );
		
		ActionInit action = engine.createRootAction( RootActionType.Command , options , session , "webjmx-" + engine.execrc.product , null , false , "Run web JMX command=" + meta.name + "::" + options.method );
		if( action == null )
			return( false );

		return( runClientAction( serverAction , action ) );
	}
	
	public boolean runClientAction( ActionBase serverAction , ActionInit clientAction ) {
		EngineSession session = clientAction.session;
		CommandExecutor executor = clientAction.executor;
		
		serverAction.debug( "run client action sessionId=" + session.sessionId + ", workFolder=" + clientAction.artefactory.workFolder.folderPath + " ..." );
		
		synchronized( this ) {
			actions.put( session.sessionId , clientAction );
		}
		
		// execute
		try {
			engine.startAction( clientAction );
			executor.runExecutor( null , clientAction , clientAction.commandAction , true );
		}
		catch( Throwable e ) {
			clientAction.handle( e );
		}

		boolean res = ( clientAction.isFailed() )? false : true;
		
		if( res )
			clientAction.commentExecutor( "COMMAND SUCCESSFUL" );
		else
			clientAction.commentExecutor( "COMMAND FAILED" );

		try {
			engine.finishAction( clientAction , true );
		}
		catch( Throwable e ) {
			clientAction.handle( e );
		}
		
		synchronized( this ) {
			actions.remove( session.sessionId );
			serverAction.debug( "finished client action sessionId=" + session.sessionId + ", status=" + res );
		}

		return( res );
	}

	public synchronized EngineSession createSession( SessionSecurity security , RunContext clientrc , boolean client ) {
		int sessionId = ++sessionSequence;
		EngineSession session = new EngineSession( this , security , clientrc , sessionId , client );
		sessions.put( session.sessionId , session );
		return( session );
	}

	public EngineCall getCall( int sessionId ) {
		EngineCall call = calls.get( "" + sessionId );
		return( call );
	}
	
	public synchronized void threadStarted( ActionBase serverAction , EngineCall thread ) {
		calls.put( "" + thread.sessionContext.sessionId , thread );
		serverAction.debug( "thread started: sessionId=" + thread.sessionContext.sessionId );
	}

	public synchronized void threadStopped( ActionBase serverAction , EngineCall thread ) {
		calls.remove( "" + thread.sessionContext.sessionId );
		serverAction.debug( "thread stopped: sessionId=" + thread.sessionContext.sessionId );
	}

	private void waitAllActions( ActionBase serverAction ) throws Exception {
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
		EngineCall call = calls.get( "" + sessionId );
		if( call == null )
			engine.exit1( _Error.UnknownCallSession1 , "unknown call session=" + sessionId , sessionId );
		
		call.executeInteractiveCommand( input );
	}

	public void stopSession( String sessionId ) throws Exception {
		EngineCall call = calls.get( "" + sessionId );
		if( call != null )
			call.stop();
	}

	public boolean waitConnect( String sessionId ) throws Exception {
		EngineCall call = calls.get( "" + sessionId );
		if( call == null )
			engine.exit1( _Error.UnknownCallSession1 , "unknown call session=" + sessionId , sessionId );
		
		return( call.waitConnect() );
	}

	public boolean waitStart( ActionBase serverAction ) {
		synchronized( this ) {
			if( started )
				return( true );
			
			try {
				wait( 30000 );
				return( true );
			}
			catch( Throwable e ) {
				serverAction.handle( e );
			}
			return( false );
		}
	}

	public void closeSession( EngineSession session ) throws Exception {
		session.close();
		synchronized( this ) {
			sessions.remove( session.sessionId );
		}
	}

	public synchronized void updatePermissions( ActionBase action , String user ) throws Exception {
		for( EngineSession session : sessions.values() ) {
			SessionSecurity security = session.getSecurity();
			AuthUser su = security.getUser();
			if( su != null ) {
				if( user.equals( su.NAME ) )
					security.setPermissions();
			}
		}
	}

	public void releaseSessionProductMetadata( ActionInit action , Meta meta ) throws Exception {
		EngineProduct ep = meta.getEngineProduct();
		ep.releaseSessionMeta( action , meta );
	}
	
}
