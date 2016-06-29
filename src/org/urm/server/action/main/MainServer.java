package org.urm.server.action.main;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.jmx.ServerCommandCall;
import org.urm.common.jmx.ServerMBean;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class MainServer {

	ActionBase action;
	ServerEngine engine;
	ServerMBean controller; 
	boolean running = false;

	public CommandMeta[] executors = null;
	
	Map<String,ServerCommandCall> calls;
	
	int sessionSequence = 0;
	
	public MainServer( ActionBase action , ServerEngine engine ) {
		this.action = action;
		this.engine = engine;
		
		controller = new ServerMBean( action , this ); 
		calls = new HashMap<String,ServerCommandCall>();
	}
	
	public void start() throws Exception {
		CommandBuilder builder = new CommandBuilder( action.context.session.clientrc , action.context.session.execrc );
		executors = builder.getExecutors( true , true );
		controller.start();
		
		action.info( "server successfully started, accepting connections." );
		synchronized( this ) {
			running = true;
			wait();
			running = false;
		}
	}

	public synchronized int createSessionId() {
		sessionSequence++;
		return( sessionSequence );
	}
	
	public void stop() throws Exception {
		action.info( "stopping server ..." );
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

}
