package org.urm.server.shell;

import org.urm.server.action.ActionBase;

public class ShellWaiter {

	WaiterCommand command;
	protected boolean windowsHelper = false;
	
	public ShellWaiter( WaiterCommand command ) {
		this.command = command;
	}

	public void setWindowsHelper() {
		windowsHelper = true;
		command.setWindowsHelper();
	}
	
	public boolean wait( ActionBase action , int timeoutMillis ) {
		try {
			action.trace( "wait for command=" + command.getClass().getSimpleName() + " (timeout " + timeoutMillis + "ms) ..." );
            Thread thread = new Thread( null , command , command.getClass().getSimpleName() );
            command.action = action;
            command.thread = thread; 
            
            thread.start();
            waitTimeout( action , timeoutMillis );
            if( command.finished )
            	return( true );
            
			action.trace( "wait failed for command=" + command.getClass().getSimpleName() );
            cleanup( action );
		}
		catch( Throwable e ) {
			action.log( "timeout command=" + command.getClass().getSimpleName() , e );
		}

		return( false );
	}
	
	private void waitTimeout( ActionBase action , long timeoutMillis ) throws Exception {
		long finishRun = System.currentTimeMillis() + timeoutMillis;

		while( !command.finished ) {
			long now = System.currentTimeMillis();
	        	
			if( timeoutMillis == 0 ) {
				synchronized ( command ) {
					command.wait( 0 );
				}
			}
			else {
				synchronized ( command ) {
					if( finishRun <= now )
						return;
					
					command.wait( finishRun - now );
				}
			}
		}
	}

    private void cleanup( ActionBase action ) throws Exception {
    	command.thread.interrupt();
    	Thread.yield();
    }
	
}
