package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;

public class Main {

	public static void main(String[] args) {
		ServerEngine engine = new ServerEngine();
		
		try {
			boolean res = engine.runArgs( args );
			System.exit( ( res )? 0 : 1 );
		}
		catch( Throwable e ) {
			ExitException ex = Common.getExitException( e );
			output( e , ex.getMessage() );
			System.exit( 1 );
		}
		
		System.exit( 3 );
	}
	
	private static void output( Throwable e , String msg ) {
		e.printStackTrace();
		if( msg != null )
			System.err.println( msg );
	}

}
