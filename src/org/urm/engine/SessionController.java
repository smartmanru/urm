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
import org.urm.engine.action.ActionInit.RootActionType;
import org.urm.engine.action.CommandExecutor;
import org.urm.meta.engine.ServerAuthUser;

public class SessionController {

	ServerEngine engine;
	
	boolean running = false;
	boolean stop = false;
	boolean started = false;

	public CommandMeta[] executors = null;
	
	Map<Integer,ServerSession> sessions;
	Map<String,ServerCall> calls;
	Map<Integer,ActionInit> actions;
	
	int sessionSequence = 0;
	
	public SessionController( ServerEngine engine ) {
		this.engine = engine;
	
		sessions = new HashMap<Integer,ServerSession>(); 
		calls = new HashMap<String,ServerCall>();
		actions = new HashMap<Integer,ActionInit>(); 
	}

	public void start( ActionBase serverAction ) throws Exception {
		stop = false;
		serverAction.debug( "start session controller ..." );
		CommandBuilder builder = new CommandBuilder( engine.serverSession.clientrc , engine.serverSession.execrc );
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
	
	public void waitFinished( ActionBase serverAction ) throws Exception {
		synchronized( this ) {
			started = true;
			notifyAll();
			
			running = true;
			while( !stop )
				wait();
			running = false;
		}
		
		waitAllActions( serverAction );
	}

	public ActionInit createRemoteAction( ActionBase serverAction , ServerCall call , CommandMethodMeta method , ActionData data ) throws Exception {
		if( !running ) {
			engine.error( "server is in progress of shutdown" );
			return( null );
		}
		
		CommandBuilder builder = new CommandBuilder( data.clientrc , engine.execrc );
		CommandOptions options = new CommandOptions( serverAction.context.options.meta );
		options.setAction( method , data );
		
		CommandMeta commandInfo = builder.createMeta( options.command );
		if( commandInfo == null )
			return( null );
		
		ServerSession session = call.sessionContext;
		session.setServerRemoteProductLayout( engine.serverAction );
		
		ActionInit action = engine.createAction( RootActionType.Command , options , session , "call-" + data.clientrc.product , call , false );
		if( action == null )
			return( null );

		return( action );
	}

	public boolean runWebJmx( ServerSession session , CommandMeta meta , CommandOptions options ) throws Exception {
		if( !running ) {
			engine.error( "server is in progress of shutdown" );
			return( false );
		}
		
		ActionBase serverAction = engine.serverAction;
		session.setServerRemoteProductLayout( serverAction );
		
		ActionInit action = engine.createAction( RootActionType.Command , options , session , "webjmx-" + engine.execrc.product , null , false );
		if( action == null )
			return( false );

		return( runClientAction( serverAction , action ) );
	}
	
	public boolean runClientAction( ActionBase serverAction , ActionInit clientAction ) {
		ServerSession session = clientAction.session;
		CommandExecutor executor = clientAction.executor;
		
		serverAction.debug( "run client action sessionId=" + session.sessionId + ", workFolder=" + clientAction.artefactory.workFolder.folderPath + " ..." );
		
		synchronized( this ) {
			actions.put( session.sessionId , clientAction );
		}
		
		// execute
		try {
			engine.startAction( clientAction );
			executor.runAction( clientAction );
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

	public synchronized ServerSession createSession( SessionSecurity security , RunContext clientrc , boolean client ) {
		int sessionId = ++sessionSequence;
		ServerSession session = new ServerSession( this , security , clientrc , sessionId , client );
		sessions.put( session.sessionId , session );
		return( session );
	}

	public ServerCall getCall( int sessionId ) {
		ServerCall call = calls.get( "" + sessionId );
		return( call );
	}
	
	public synchronized void threadStarted( ActionBase serverAction , ServerCall thread ) {
		calls.put( "" + thread.sessionContext.sessionId , thread );
		serverAction.debug( "thread started: sessionId=" + thread.sessionContext.sessionId );
	}

	public synchronized void threadStopped( ActionBase serverAction , ServerCall thread ) {
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
		ServerCall call = calls.get( "" + sessionId );
		if( call == null )
			engine.exit1( _Error.UnknownCallSession1 , "unknown call session=" + sessionId , sessionId );
		
		call.executeInteractiveCommand( input );
	}

	public void stopSession( String sessionId ) throws Exception {
		ServerCall call = calls.get( "" + sessionId );
		if( call != null )
			call.stop();
	}

	public boolean waitConnect( String sessionId ) throws Exception {
		ServerCall call = calls.get( "" + sessionId );
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

	public void closeSession( ServerSession session ) throws Exception {
		session.close();
		synchronized( this ) {
			sessions.remove( session.sessionId );
		}
	}

	public synchronized void updatePermissions( ActionBase action , String user ) throws Exception {
		for( ServerSession session : sessions.values() ) {
			SessionSecurity security = session.getSecurity();
			ServerAuthUser su = security.getUser();
			if( su != null ) {
				if( user.equals( su.NAME ) )
					security.setPermissions();
			}
		}
		
	}
	
}
