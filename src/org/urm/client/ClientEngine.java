package org.urm.client;

import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.jmx.RemoteCall;

public class ClientEngine {

	public ClientEngine() {
	}

	public boolean runArgs( String[] args ) throws Exception {
		RunContext execrc = new RunContext();
		execrc.load();
		
		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		CommandOptions options = new CommandOptions();
		CommandMeta commandInfo = builder.buildCommand( args , options ); 
		if( commandInfo == null )
			return( false );

		boolean localRun = false;
		if( options.isLocalRun() )
			localRun = true;
		else
		if( !execrc.isRemoteMode() )
			localRun = true;
		
		boolean res = false;
		if( localRun )
			res = runClientMode( builder , commandInfo , options );
		else
			res = runServerMode( builder , commandInfo , options );
		return( res );
	}

	private boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		ClientCallLocal call = new ClientCallLocal();
		return( call.runClient( builder , commandInfo , options ) );
	}
	
	private boolean runServerMode( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		RemoteCall call = new RemoteCall( options );
		return( call.runClient( builder , commandInfo ) );
	}

}
