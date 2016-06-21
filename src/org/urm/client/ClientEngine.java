package org.urm.client;

import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;

public class ClientEngine {

	public ClientEngine() {
	}

	public boolean runArgs( String[] args ) throws Exception {
		RunContext rc = new RunContext();
		rc.load();
		
		CommandBuilder builder = new CommandBuilder( rc );
		CommandMeta commandInfo = builder.buildCommand( args ); 
		if( commandInfo == null )
			return( false );

		boolean res = false;
		if( rc.isRemoteMode() )
			res = runServerMode( builder , commandInfo );
		else
			res = runClientMode( builder , commandInfo );
		return( res );
	}

	private boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		ClientCallLocal call = new ClientCallLocal();
		return( call.runClient( builder , commandInfo ) );
	}
	
	private boolean runServerMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		ClientCallRemote call = new ClientCallRemote();
		return( call.runClient( builder , commandInfo ) );
	}

}
