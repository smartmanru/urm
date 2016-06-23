package org.urm.common.jmx;

import org.urm.common.action.ActionData;

public class ServerCommandThread implements Runnable {

	public String sessionId;
	public ServerCommandMBean command;
	public ActionData data;

	public Controller controller;
	
	public ServerCommandThread( String sessionId , ServerCommandMBean command , ActionData data ) {
		this.sessionId = sessionId;
		this.command = command;
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
    		command.engine.runClientRemote( command.meta , data );
    	}
    	catch( Throwable e ) {
        	command.notifyLog( sessionId , "exception: " + e.getMessage() );
    	}
    	
    	command.notifyStop( sessionId );
    	controller.threadStopped( this );
    }
    
}
