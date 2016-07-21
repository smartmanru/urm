package org.urm.common.jmx;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethodMeta;
import org.urm.server.SessionController;
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

	public SessionController server;
	
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
    		CommandMethodMeta method = command.meta.getAction( actionName );
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
    	notifyStop( "disconnected" );
    	server.threadStopped( this );
    }

    public void addLog( String message ) {
    	if( !waitConnectMode ) {
    		command.notifyLog( sessionId , message );
    		return;
    	}
    }

	public void runInteractive( ActionBase action , ShellInteractive shell ) throws Exception {
		shellInteractive = shell;
		waitConnectMode = true;
		action.setTimeoutUnlimited();
		shell.runInteractive( action );
	}
    
	public void executeInteractiveCommand( String input ) throws Exception {
		if( shellInteractive == null )
			return;
		
		action.trace( shellInteractive.name + " execute: " + input );
		if( shellInteractive.executeCommand( action , input ) )
			notifyCommandFinished( "OK" );
		else
			notifyCommandFinished( "FAILED" );
	}
	
	public void stop() throws Exception {
		if( shellInteractive == null )
			return;
		
		action.trace( shellInteractive.name + " stopping ..." );
		shellInteractive.stop( action );
	}
	
	public boolean waitConnect() throws Exception {
		action.trace( "wait to connect ..." );
		
		synchronized( this ) {
			if( !waitConnectFinished )
				wait();
			waitConnectMode = false;
		}
		
		if( waitConnectSucceeded )
    		notifyConnected( "successfully connected to " + shellInteractive.account.getPrintName() );
		else
			notifyStop( "unable to connect to " + shellInteractive.account.getPrintName() );
		
		return( waitConnectSucceeded );
	}

	public void connectFinished( boolean connected ) throws Exception {
		synchronized( this ) {
			waitConnectFinished = true;
			waitConnectSucceeded = connected;
			notifyAll();
		}
	}
	
	public void notifyStop( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionId , clientId , msg ); 
			n.setStopEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	public void notifyConnected( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionId , clientId , msg ); 
			n.setConnectedEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
	public void notifyCommandFinished( String msg ) {
		try {
			int notificationSequence = command.getNextSequence();
			ActionNotification n = new ActionNotification( command , notificationSequence , sessionId , clientId , msg ); 
			n.setCommandFinishedEvent();
			command.sendNotification( n );
		}
		catch( Throwable e ) {
		}
	}
	
}
