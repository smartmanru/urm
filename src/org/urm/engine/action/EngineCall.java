package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandMethodMeta;
import org.urm.engine.Engine;
import org.urm.engine.SessionService;
import org.urm.engine.session.EngineSession;
import org.urm.engine.shell.ShellInteractive;

public abstract class EngineCall implements Runnable {

	public Engine engine;
	public EngineSession sessionContext;
	public CommandMeta command;
	public String actionName;
	public ActionData data;
	
	public SessionService sessionController;
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
	
	public EngineCall( Engine engine , EngineSession sessionContext , CommandMeta command , String actionName , ActionData data ) {
		this.engine = engine;
		this.sessionContext = sessionContext;
		this.command = command;
		this.actionName = actionName;
		this.data = data;
		
		sessionController = engine.sessions;
	}
	
    @Override
    public void run() {
    	sessionController.runClientAction( engine.serverAction , action );
    	notifyStop( "disconnected" );
    	sessionController.threadStopped( engine.serverAction , this );
    }

	public boolean start() {
    	try {
    		CommandMethodMeta method = command.getMethod( actionName );
    		action = sessionController.createRemoteAction( engine.serverAction , this , method , data );
    	}
    	catch( Throwable e ) {
    		engine.serverAction.log( "call" , e );
    		notifyLog( e );
        	return( false );
    	}
    	
    	if( action == null ) {
    		engine.serverAction.error( "unknown action=" + actionName );
    		notifyLog( "unknown action=" + actionName );
    		return( false );
    	}
    	
        Thread thread = new Thread( null , this , getClass().getSimpleName() );
        sessionController.threadStarted( engine.serverAction , this );
        thread.start();
        
        return( true );
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
		shell.runInteractive( action );
	}
    
	public void executeInteractiveCommand( String input ) throws Exception {
		if( shellInteractive == null )
			return;
		
		action.trace( shellInteractive.name + " execute: " + input );
		if( shellInteractive.executeInteractiveCommand( action , input ) )
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
