package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.server.action.CommandExecutor;
import org.urm.server.executor.MainExecutor;

public class Main {

	public static void main(String[] args) {
		try {
			boolean res = runArgs( args );
			System.exit( ( res )? 0 : 1 );
		}
		catch( Throwable e ) {
			ExitException ex = Common.getExitException( e );
			output( e , ex );
			System.exit( 1 );
		}
		
		System.exit( 3 );
	}
	
	private static void output( Throwable e , ExitException ex ) {
		e.printStackTrace();
		if( ex != null )
			System.err.println( ex.getMessage() );
	}

	public static boolean runArgs( String[] args ) throws Exception {
		// server environment
		RunContext execrc = new RunContext();
		execrc.load();
		if( !execrc.isMain() )
			throw new ExitException( "only main executor id expected" );

		// server run options
		ServerEngine engine = new ServerEngine();
		CommandBuilder builder = new CommandBuilder( execrc , execrc );
		CommandExecutor serverExecutor = MainExecutor.createByArgs( engine , builder , args );
		if( serverExecutor == null )
			return( false );

		return( engine.runServerExecutor( execrc , serverExecutor ) );
	}
	
}
