package org.urm.server.action.main;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.jmx.ServerMBean;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class MainServer {

	ActionBase action;
	ServerEngine engine;
	ServerMBean controller; 
	boolean running = false;

	public CommandMeta[] executors = null;
	
	public MainServer( ActionBase action , ServerEngine engine ) {
		this.action = action;
		this.engine = engine;
		controller = new ServerMBean( action , this ); 
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

	public void stop() throws Exception {
		action.info( "stopping server ..." );
		synchronized( this ) {
			notifyAll();
		}
	}
	
	public boolean isRunning() {
		return( running );
	}
	
}
