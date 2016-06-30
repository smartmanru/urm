package org.urm.server.action.main;

import org.urm.common.jmx.RemoteCall;
import org.urm.server.MainServer;
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
	
	private boolean serverConnect( RemoteCall call ) {
		String host = ( context.CTX_HOST.isEmpty() )? "localhost" : context.CTX_HOST;
		int port = ( context.CTX_PORT > 0 )? context.CTX_PORT : RemoteCall.DEFAULT_SERVER_PORT;
		String serverHostPort = host + ":" + port;
		return( call.serverConnect( serverHostPort ) );	
	}
	
	private void executeServerStop() throws Exception {
		info( "stopping server ..." );
		RemoteCall call = new RemoteCall();
		if( !serverConnect( call ) )
			info( "server is not running on url=" + call.URL );
		else {
			String status = call.serverCall( "status" );
			if( !status.equals( "running" ) ) {
				call.serverDisconnect();
				exit( "server is in unknown state (url=" + call.URL + ")" );
				return;
			}
			
			status = call.serverCall( "stop" );
			call.serverDisconnect();
			if( !status.equals( "ok" ) )
				exit( "unable to stop server: " + status + " (url=" + call.URL + ")" );
			
			info( "server is successfully stopped" );
		}
	}
	
	private void executeServerStatus() throws Exception {
		info( "check server status ..." );
		RemoteCall call = new RemoteCall();
		if( !serverConnect( call ) )
			info( "server not running on url=" + call.URL );
		else {
			String status = call.serverCall( "status" );
			if( !status.equals( "running" ) )
				info( "server is in unknown state  (url=" + call.URL + ")" );
			else
				info( "server is running (url=" + call.URL + ")" );
			call.serverDisconnect();
		}
	}
	
}
