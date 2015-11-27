package ru.egov.urm;

import ru.egov.urm.run.CommandBuilder;
import ru.egov.urm.run.CommandExecutor;

public class Engine {

	public static void main(String[] args) {
		
		boolean debug = false;
		try {
			CommandBuilder builder = new CommandBuilder();
			CommandExecutor executor = builder.buildCommand( args ); 
			if( executor == null )
				System.exit( 1 );
				
			boolean flg = builder.options.getFlagValue( "GETOPT_SHOWALL" );
			if( flg )
				debug = true;
			
			builder.run( executor );
			System.exit( 0 );
		}
		catch( Throwable e ) {
			ExitException ex = Common.getExitException( e );
			
			if( ex != null ) {
				output( debug , e , ex.getMessage() );
				System.exit( 2 );
			}
			else
				output( debug , e , null );
		}
		
		System.exit( 3 );
	}
	
	private static void output( boolean debug , Throwable e , String msg ) {
		if( debug || msg == null )
			e.printStackTrace();
		else
			System.err.println( msg );
	}
}
