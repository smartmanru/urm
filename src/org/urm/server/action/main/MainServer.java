package org.urm.server.action.main;

import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.jmx.Controller;
import org.urm.server.ServerEngine;
import org.urm.server.action.ActionBase;

public class MainServer {

	ServerEngine engine;
	Controller controller; 

	public CommandMeta[] executors = null;
	
	public MainServer( ServerEngine engine ) {
		engine = this.engine;
		controller = new Controller( this ); 
	}
	
	public void start( ActionBase action ) throws Exception {
		CommandBuilder builder = new CommandBuilder( action.context.clientrc , action.context.execrc );
		executors = builder.getExecutors( true , true );
		controller.start( action );
	}

}
