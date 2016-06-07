package org.urm.client;

import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.server.ServerEngine;

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
		if( rc.isServer() )
			res = runServerMode( builder , commandInfo );
		else
			res = runClientMode( builder , commandInfo );
		return( res );
	}

	private boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		ServerEngine server = new ServerEngine();
		return( server.runClientMode( builder , commandInfo ) );
	}
	
	private boolean runServerMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		throw new ExitException( "sorry, not implemented yet" );
	}

}
