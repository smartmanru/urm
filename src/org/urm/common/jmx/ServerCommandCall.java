package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;
import org.urm.server.MainServer;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;
import org.urm.server.shell.ShellInteractive;

public class ServerCommandCall implements Runnable {

	public int sessionId;
	public String clientId;
	public ServerCommandMBean command;
	public String actionName;
	public ActionData data;
	public ActionInit action;

	public MainServer server;
	
	public ShellInteractive shellInteractive;
	public boolean waitConnectMode = false;
	public boolean waitConnectFinished = false;
	public boolean waitConnectSucceeded = false;
	
	public ServerCommandCall( int sessionId , String clientId , ServerCommandMBean command , String actionName , ActionData data ) {
		this.sessionId = sessionId;
		this.clientId = clientId;
		this.command = command;
		this.actionName = actionName;
		this.data = data;
		
		server = command.controller.server; 
	}
	
	public boolean start() {
    	try {
    		CommandMethod method = command.meta.getAction( actionName );
    		action = server.createRemoteAction( this , method , data );
    	}
    	catch( Throwable e ) {
    		command.notifyLog( sessionId , e );
        	return( false );
    	}
    	
    	if( action == null )
    		return( false );
    	
        Thread thread = new Thread( null , this , getClass().getSimpleName() );
        server.threadStarted( this );
        thread.start();
        
        return( true );
    }

    @Override
    public void run() {
		server.runClientAction( action );
    	command.notifyStop( sessionId );
    	server.threadStopped( this );
    }

    public void addLog( String message ) {
    	if( !waitConnectMode ) {
    		command.notifyLog( sessionId , message );
    		return;
    	}
    	
    	if( message.equals( ShellInteractive.CONNECT_MARKER ) ) {
    		waitConnectFinished = true;
    		waitConnectSucceeded = true;
    		synchronized( this ) {
    			notifyAll();
    		}
    	}
    }

	public void runInteractive( ActionBase action , ShellInteractive shell ) throws Exception {
		shellInteractive = shell;
		waitConnectMode = true;
		shell.start( action );
		waitConnect();
	}
    
	public void addInput( String input ) throws Exception {
		if( shellInteractive == null )
			return;
		
		action.trace( shellInteractive.name + " execute: " + input );
		shellInteractive.addInput( action , input );
	}
	
	public boolean waitConnect() throws Exception {
		action.trace( "wait to connect ..." );
		
		synchronized( this ) {
			wait();
		}
		return( false );
	}
	
}
