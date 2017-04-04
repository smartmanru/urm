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
		
		if( execrc.isClientMode() )
			res = runClientMode( builder , commandInfo , options );
		else
		if( localRun )
			res = runLocalServerMode( builder , commandInfo , options );
		else
			res = runRemoteServerMode( builder , commandInfo , options );
		return( res );
	}

	private boolean runLocalServerMode( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		ClientCallLocal call = new ClientCallLocal();
		return( call.runClient( builder , commandInfo , options ) );
	}
	
	private boolean runRemoteServerMode( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		RemoteCall call = new RemoteCall( options );
		ClientAuth auth = new ClientAuth( this );
		if( !auth.getAuth( builder , options ) )
			return( false );
		
		return( call.runClient( builder , commandInfo , auth ) );
	}

	private boolean runClientMode( CommandBuilder builder , CommandMeta commandInfo , CommandOptions options ) throws Exception {
		boolean res = false;
		if( options.method.equals( "auth" ) ) {
			ClientAuth auth = new ClientAuth( this );
			res = auth.setAuth( builder , options );
		}
		return( res );
	}

}
