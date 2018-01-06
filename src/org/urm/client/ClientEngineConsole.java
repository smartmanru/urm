package org.urm.client;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.RunError;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandMeta;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;
import org.urm.common.jmx.RemoteCall;

public class ClientEngineConsole extends ClientEngine {

	public static String RED_COLOR = "\033[31m";
	public static String GREEN_COLOR = "\033[32m";
	public static String NORMAL_COLOR = "\033[0m";
	
	RunContext execrc;
	
	public ClientEngineConsole() {
		execrc = new RunContext();
	}

	@Override
	public void output( Throwable e ) {
		e.printStackTrace();
		RunError ex = Common.getExitException( e );
		if( ex != null )
			System.err.println( ex.getMessage() );
	}

	@Override
	public void println( String s ) {
		// color processing check
		boolean isError = false;
		boolean isInfo = false;
		if( s.contains( "[ERROR]") )
			isError = true;
		else
		if( s.contains( "[INFO ]") )
			isInfo = true;
		
		if( execrc.isLinux() ) {
			if( isError )
				s = RED_COLOR + s + NORMAL_COLOR;
			else
			if( isInfo )
				s = GREEN_COLOR + s + NORMAL_COLOR;
		}
			
		if( isError )
			System.err.println( s );
		else
			System.out.println( s );
	}
	
	public boolean runArgs( String[] args ) throws Exception {
		execrc.load();
		
		OptionsMeta meta = new OptionsMeta();
		CommandBuilder builder = new CommandBuilder( execrc , execrc , meta );
		CommandOptions options = new CommandOptions( meta );
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
		RemoteCall call = new RemoteCall( this , options );
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
