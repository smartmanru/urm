package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;
import org.urm.server.MainServer;
import org.urm.server.shell.ShellExecutor;

public class ServerCommandCall implements Runnable {

	public int sessionId;
	public String clientId;
	public ServerCommandMBean command;
	public String action;
	public ActionData data;

	public MainServer server;
	
	public ShellExecutor interactiveExecutor;

	public ServerCommandCall( int sessionId , String clientId , ServerCommandMBean command , String action , ActionData data ) {
		this.sessionId = sessionId;
		this.clientId = clientId;
		this.command = command;
		this.action = action;
		this.data = data;
		
		server = command.controller.server; 
	}
	
    public void start() {
        Thread thread = new Thread( null , this , getClass().getSimpleName() );
        server.threadStarted( this );
        thread.start();
    }

    @Override
    public void run() {
    	try {
    		CommandMethod method = command.meta.getAction( action );
    		server.runClientRemote( this , method , data );
    	}
    	catch( Throwable e ) {
        	command.notifyLog( sessionId , e );
    	}
    	
    	command.notifyStop( sessionId );
    	server.threadStopped( this );
    }

    public void addLog( String message ) {
    	command.notifyLog( sessionId , message );
    }

	public void createCommunication( ShellExecutor executor ) throws Exception {
		interactiveExecutor = executor;
	}

	public void closeCommunication() throws Exception {
		interactiveExecutor = null;
	}

	public void addInput( String input ) throws Exception {
		interactiveExecutor.addInput( input );
	}
	
}
