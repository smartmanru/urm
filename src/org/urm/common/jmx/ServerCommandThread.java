package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;

public class ServerCommandThread implements Runnable {

	public String sessionId;
	public ServerCommandMBean command;
	public String action;
	public ActionData data;

	public Controller controller;
	
	public ServerCommandThread( String sessionId , ServerCommandMBean command , String action , ActionData data ) {
		this.sessionId = sessionId;
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
    		command.engine.runClientRemote( sessionId , command.meta , method , data );
    	}
    	catch( Throwable e ) {
        	command.notifyLog( sessionId , "exception: " + e.getMessage() );
    	}
    	
    	command.notifyStop( sessionId );
    	controller.threadStopped( this );
    }
    
}
