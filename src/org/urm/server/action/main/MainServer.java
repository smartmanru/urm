package org.urm.server.action.main;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethod;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.common.jmx.ServerMBean;
import org.urm.server.CommandExecutor;
import org.urm.server.ServerEngine;
import org.urm.server.SessionContext;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;

public class MainServer {

	ActionBase action;
	ServerEngine engine;
	ServerMBean controller; 
	boolean running = false;
	boolean stop = false;

	public CommandMeta[] executors = null;
	
	Map<String,ServerCommandCall> calls;
	Map<Integer,ActionInit> actions;
	
	int sessionSequence = 0;
	
	public MainServer( ActionBase action , ServerEngine engine ) {
		this.action = action;
		this.engine = engine;
		
		controller = new ServerMBean( action , this ); 
		calls = new HashMap<String,ServerCommandCall>();
		actions = new HashMap<Integer,ActionInit>(); 
	}
	
	public void start() throws Exception {
		CommandBuilder builder = new CommandBuilder( action.context.session.clientrc , action.context.session.execrc );
		executors = builder.getExecutors( true , true );
		controller.start();
		
		action.info( "server successfully started, accepting connections." );
		synchronized( this ) {
			running = true;
			while( !stop )
				wait();
			running = false;
		}
		
		waitAllActions();
	}

	public boolean runClientRemote( ServerCommandCall call , CommandMethod method , ActionData data ) throws Exception {
		if( !running ) {
			engine.serverAction.error( "server is in progress of shutdown" );
			return( false );
		}
		
		CommandBuilder builder = new CommandBuilder( data.clientrc , engine.execrc );
		
		CommandOptions options = new CommandOptions( engine.serverAction.context.options.meta );
		options.setAction( call.command.meta.name , method , data );
		
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null )
			return( false );
		
		CommandExecutor executor = engine.createExecutor( commandInfo );
		SessionContext session = new SessionContext( engine , data.clientrc , call.sessionId );
		session.setServerClientLayout( engine.serverSession );
		
		ActionInit action = engine.createAction( options , executor , session , "remote-" + data.clientrc.productDir , call );
		if( action == null )
			return( false );

		return( runClientAction( session , executor , action ) );
	}

	public boolean runClientJmx( int sessionId , String productDir , CommandMeta meta , CommandOptions options ) throws Exception {
		if( !running ) {
			engine.serverAction.error( "server is in progress of shutdown" );
			return( false );
		}
		
		CommandExecutor executor = engine.createExecutor( meta );
		SessionContext session = new SessionContext( engine , engine.execrc , sessionId );
		session.setServerProductLayout( productDir );
		
		ActionInit action = engine.createAction( options , executor , session , "jmx-" + engine.execrc.productDir , null );
		if( action == null )
			return( false );

		return( runClientAction( session , executor , action ) );
	}
	
	public boolean runClientAction( SessionContext session , CommandExecutor executor , ActionInit clientAction ) {
		engine.serverAction.debug( "run client action workFolder=" + clientAction.artefactory.workFolder.folderPath + " ..." );
		
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
		engine.serverAction.debug( "client action workFolder=" + clientAction.artefactory.workFolder.folderPath + ", status=" + res );
		
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
		}

		return( res );
	}

	public synchronized int createSessionId() {
		sessionSequence++;
		return( sessionSequence );
	}
	
	public void stop() throws Exception {
		action.info( "stopping server ..." );
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
		action.debug( "thread started: sessionId=" + thread.sessionId );
	}

	public synchronized void threadStopped( ServerCommandCall thread ) {
		calls.remove( "" + thread.sessionId );
		action.debug( "thread stopped: sessionId=" + thread.sessionId );
	}

	private void waitAllActions() throws Exception {
		while( true ) {
			synchronized( this ) {
				if( actions.size() == 0 )
					return;
			}
		}
	}
	
}
