package org.urm.server.action.main;

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
	}
	
	private void executeServerStatus() throws Exception {
	}
	
}
