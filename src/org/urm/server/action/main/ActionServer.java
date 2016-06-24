package org.urm.server.action.main;

import org.urm.common.jmx.RemoteCall;
import org.urm.server.action.ActionBase;

public class ActionServer extends ActionBase {

	String OP;
	
	public ActionServer( ActionBase action , String stream , String OP ) {
		super( action , stream );
		this.OP = OP;
	}

	@Override protected boolean executeSimple() throws Exception {
		if( OP.equals( "start" ) )
			executeServerStart();
		else if( OP.equals( "stop" ) )
			executeServerStop();
		else if( OP.equals( "status" ) )
			executeServerStatus();
		else
			exit( "unexpected action=" + OP );
		
		return( true );
	}

	private void executeServerStart() throws Exception {
		info( "start server ..." );
		
		MainServer server = new MainServer( this , executor.engine );
		server.start();
	}
	
	private void executeServerStop() throws Exception {
		info( "stopping server ..." );
		RemoteCall call = new RemoteCall();
		if( !call.serverConnect( context.execrc ) )
			info( "server is already stopped (url=" + call.URL + ")" );
		else {
			String status = call.serverCall( this , "status" );
			if( !status.equals( "running" ) ) {
				call.serverDisconnect();
				exit( "server is in unknown state (url=" + call.URL + ")" );
				return;
			}
			
			status = call.serverCall( this , "stop" );
			call.serverDisconnect();
			if( !status.equals( "ok" ) )
				exit( "unable to stop server: " + status + " (url=" + call.URL + ")" );
			
			info( "server is successfull stopped" );
		}
	}
	
	private void executeServerStatus() throws Exception {
		info( "check server status ..." );
		RemoteCall call = new RemoteCall();
		if( !call.serverConnect( context.execrc ) )
			info( "server is stopped (url=" + call.URL + ")" );
		else {
			String status = call.serverCall( this , "status" );
			if( !status.equals( "running" ) )
				info( "server is in unknown state  (url=" + call.URL + ")" );
			else
				info( "server is running (url=" + call.URL + ")" );
			call.serverDisconnect();
		}
	}
	
}
