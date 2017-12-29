package org.urm.client;

public class Main {

	public static void main(String[] args) {
		ClientEngineConsole engine = new ClientEngineConsole();
		
		try {
			boolean res = engine.runArgs( args );
			System.exit( ( res )? 0 : 1 );
		}
		catch( Throwable e ) {
			engine.output( e );
			System.exit( 1 );
		}
		
		System.exit( 3 );
	}
	
}
