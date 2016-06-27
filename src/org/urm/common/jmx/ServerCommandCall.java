package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;

public class ServerCommandCall implements Runnable {

	public int sessionId;
	public String clientId;
	public ServerCommandMBean command;
	public String action;
	public ActionData data;

	public ServerMBean controller;
	
	public ServerCommandCall( int sessionId , String clientId , ServerCommandMBean command , String action , ActionData data ) {
		this.sessionId = sessionId;
		this.clientId = clientId;
		this.command = command;
		this.action = action;
		this.data = data;
		
		controller = command.controller; 
	}
	
    public void start() {
        Thread thread = new Thread( null , this , getClass().getSimpleName() );
        controller.threadStarted( this );
        thread.start();
    }

    @Override
    public void run() {
    	try {
    		CommandMethod method = command.meta.getAction( action );
    		command.engine.runClientRemote( this , method , data );
    	}
    	catch( Throwable e ) {
        	command.notifyLog( sessionId , "exception: " + e.getMessage() );
    	}
    	
    	command.notifyStop( sessionId );
    	controller.threadStopped( this );
    }

    public void addLog( String message ) {
    	command.notifyLog( sessionId , message );
    }
    
}
