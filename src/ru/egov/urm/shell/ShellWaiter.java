package ru.egov.urm.shell;

import ru.egov.urm.action.ActionBase;

public class ShellWaiter {

	ShellExecutor shell;
	WaiterCommand command;
	protected boolean windowsHelper = false;
	
	public ShellWaiter( ShellExecutor shell , WaiterCommand command ) {
		this.shell = shell;
		this.command = command;
	}

	public void setWindowsHelper() {
		windowsHelper = true;
		command.setWindowsHelper();
	}
	
	public boolean wait( ActionBase action , int timeoutMillis ) {
		try {
			action.trace( "wait for command=" + command.getClass().getSimpleName() + "(timeout " + timeoutMillis + "ms) ..." );
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
			try {
				action.trace( "timeout command=" + command.getClass().getSimpleName() );
				action.log( e );
			}
			catch( Throwable ep ) {
				System.out.println( "unable to log exception:" );
				ep.printStackTrace();
			}
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
    	shell.restart( action );
    }
	
}
