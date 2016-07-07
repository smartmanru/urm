package org.urm.common.jmx;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.urm.common.action.ActionData;
import org.urm.common.action.CommandMethod;
import org.urm.server.MainServer;
import org.urm.server.action.ActionInit;
import org.urm.server.shell.WaiterCommand;

public class ServerCommandCall implements Runnable {

	public int sessionId;
	public String clientId;
	public ServerCommandMBean command;
	public String actionName;
	public ActionData data;
	public ActionInit action;

	public MainServer server;
	
	public boolean waitConnectMode = false;
	public boolean waitConnectFinished = false;
	public boolean waitConnectSucceeded = false;
	public OutputStream stdin;
	public InputStream stderr;
	public InputStream stdout;
	public BufferedReader reader;
	public Writer writer;
	public BufferedReader errreader;

	public final static String CONNECT_MARKER = "URM.CONNECTED";  
	
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
    	
    	if( message.equals( CONNECT_MARKER ) ) {
    		waitConnectFinished = true;
    		waitConnectSucceeded = true;
    		synchronized( this ) {
    			notifyAll();
    		}
    	}
    }

	public void addInput( String input ) throws Exception {
		if( action.isShellLinux() )
			writer.write( input + "\n" );
		else
			writer.write( input + "\r\n" );
		writer.flush();
	}
	
	public boolean waitConnect() throws Exception {
		action.trace( "wait to connect ..." );
		
		synchronized( this ) {
			wait();
		}
		return( false );
	}
	
	public void executeInteractive( ServerCommandCall call , ProcessBuilder pb ) throws Exception {
		waitConnectMode = true;
		
		Process process = pb.start();
		
		stdin = process.getOutputStream();
		writer = new OutputStreamWriter( stdin );
		
		stderr = process.getErrorStream();
		stdout = process.getInputStream();
		
		reader = new BufferedReader( new InputStreamReader( stdout ) );
		errreader = new BufferedReader( new InputStreamReader( stderr ) );
		
		addInput( "echo " + CONNECT_MARKER );
		
		WaiterCommand waiter = new WaiterCommand( action.context.logLevelLimit , reader , errreader );
		if( waiter.waitForMarker( action , CONNECT_MARKER ) )
			waiter.waitForProcess( action , process );
		
		synchronized( this ) {
			notifyAll();
		}
	}

}
