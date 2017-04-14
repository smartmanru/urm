package org.urm.engine;

import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;
import org.urm.engine.executor.MainExecutor;

public class Main {

	public static void main(String[] args) {
		try {
			boolean res = runArgs( args );
			System.exit( ( res )? 0 : 1 );
		}
		catch( Throwable e ) {
			RunError ex = Common.getExitException( e );
			output( e , ex );
			System.exit( 1 );
		}
		
		System.exit( 3 );
	}
	
	private static void output( Throwable e , RunError ex ) {
		e.printStackTrace();
		if( ex != null )
			System.err.println( ex.getMessage() );
	}

	public static boolean runArgs( String[] args ) throws Exception {
		// server environment
		RunContext execrc = new RunContext();
		execrc.load();
		if( !execrc.isMain() )
			Common.exit0( _Error.MainExecutorExpected0 , "only main executor id expected" );

		ServerEngine engine = new ServerEngine( execrc );
		engine.init();
		MainExecutor serverExecutor = MainExecutor.createExecutor( engine );
		
		// server run options
		OptionsMeta meta = new OptionsMeta();
		CommandBuilder builder = new CommandBuilder( execrc , execrc , meta );
		CommandOptions options = serverExecutor.createOptionsByArgs( builder , args );
		if( options == null )
			return( false );

		return( engine.runServerExecutor( serverExecutor , options ) );
	}
	
}
