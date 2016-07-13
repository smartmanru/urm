package org.urm.client;

import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.jmx.RemoteCall;

public class ClientEngine {

	public ClientEngine() {
	}

	public boolean runArgs( String[] args ) throws Exception {
		RunContext execrc = new RunContext();
		execrc.load();
		
		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		CommandMeta commandInfo = builder.buildCommand( args ); 
		if( commandInfo == null )
			return( false );

		boolean localRun = false;
		if( builder.isLocalRun() )
			localRun = true;
		else
		if( !execrc.isRemoteMode() )
			localRun = true;
		
		boolean res = false;
		if( localRun )
			res = runClientMode( builder , commandInfo );
		else
			res = runServerMode( builder , commandInfo );
		return( res );
	}

	private boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		ClientCallLocal call = new ClientCallLocal();
		return( call.runClient( builder , commandInfo ) );
	}
	
	private boolean runServerMode( CommandBuilder builder , CommandMeta commandInfo ) throws Exception {
		RemoteCall call = new RemoteCall( builder.options );
		return( call.runClient( builder , commandInfo ) );
	}

}
