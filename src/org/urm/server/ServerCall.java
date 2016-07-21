package org.urm.server;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionInit;
import org.urm.server.shell.ShellInteractive;

public abstract class ServerCall implements Runnable {

	public ServerEngine engine;
	public int sessionId;
	public String clientId;
	public CommandMeta command;
	public String actionName;
	public ActionData data;
	
	public SessionController sessionController;
	public ActionInit action;

	public ShellInteractive shellInteractive;
	public boolean waitConnectMode = false;
	public boolean waitConnectFinished = false;
	public boolean waitConnectSucceeded = false;

	abstract protected void notifyStop( String msg );
	abstract protected void notifyConnected( String msg );
	abstract protected void notifyCommandFinished( String msg );
	abstract protected void notifyLog( String msg );
	abstract protected void notifyLog( Throwable e );
	
	public ServerCall( ServerEngine engine , int sessionId , String clientId , CommandMeta command , String actionName , ActionData data ) {
		this.engine = engine;
		this.sessionId = sessionId;
		this.clientId = clientId;
		this.command = command;
		this.actionName = actionName;
		this.data = data;
		
		sessionController = engine.sessionController;
	}
	
	public boolean start() {
    	try {
    		CommandMethodMeta method = command.getAction( actionName );
    		action = sessionController.createRemoteAction( this , method , data );
    	}
    	catch( Throwable e ) {
    		notifyLog( e );
        	return( false );
    	}
    	
    	if( action == null )
    		return( false );
    	
        Thread thread = new Thread( null , this , getClass().getSimpleName() );
        sessionController.threadStarted( this );
        thread.start();
        
        return( true );
    }

    @Override
    public void run() {
    	sessionController.runClientAction( action );
    	notifyStop( "disconnected" );
    	sessionController.threadStopped( this );
    }

    public void addLog( String message ) {
    	if( !waitConnectMode ) {
    		notifyLog( message );
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
	
}
