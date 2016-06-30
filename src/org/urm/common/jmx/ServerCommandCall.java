package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;
import org.urm.server.action.main.MainServer;

public class ServerCommandCall implements Runnable {

	public int sessionId;
	public String clientId;
	public ServerCommandMBean command;
	public String action;
	public ActionData data;

	public MainServer server;
	
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
    
}
