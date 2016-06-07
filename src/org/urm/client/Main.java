package org.urm.client;

import org.urm.common.Common;
import org.urm.common.ExitException;

public class Main {

	public static void main(String[] args) {
		ClientEngine engine = new ClientEngine();
		
		try {
			boolean res = engine.runArgs( args );
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

}
