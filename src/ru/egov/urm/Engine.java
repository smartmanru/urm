package ru.egov.urm;

import ru.egov.urm.action.CommandBuilder;
import ru.egov.urm.action.CommandExecutor;

public class Engine {

	public static void main(String[] args) {
		
		CommandBuilder builder = new CommandBuilder();
		try {
			CommandExecutor executor = builder.buildCommand( args ); 
			if( executor == null )
				System.exit( 1 );
				
			boolean res = builder.run( executor );
			System.exit( ( res )? 0 : 1 );
		}
		catch( Throwable e ) {
			ExitException ex = Common.getExitException( e );
			
			boolean debug = true;
			if( builder.context != null )
				debug = builder.context.CTX_SHOWALL;
			
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
