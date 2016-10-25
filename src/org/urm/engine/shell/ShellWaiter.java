package org.urm.engine.shell;

import org.urm.action.ActionBase;

public class ShellWaiter implements Runnable {

	public ShellOutputWaiter command;

	private boolean stop;
	private boolean finished;
	private boolean succeeded;
	private boolean broken;
	
	private Thread thread;
	private ActionBase action;
	public boolean system;
	
	public ShellWaiter( ShellOutputWaiter command ) {
		this.command = command;
		
		this.broken = false;
		this.stop = false;
		this.succeeded = false;
		this.finished = false;
	}

	@Override
    public void run() {
        finished = false;
        succeeded = false;

        while( !stop )
        	runAction();
        
        finished = true;
		action.trace( "finished shell waiter shell=" + command.shell.name );
		
        synchronized ( this ) {
            notifyAll();
        }
    }

	public synchronized void stop() {
		stop = true;
		action = null;
		notifyAll();
	}
	
	public boolean wait( ActionBase action , int timeoutMillis , int logLevel , boolean system ) {
		try {
			action.trace( "wait for " + command.getClass().getSimpleName() + ", shell=" + command.shell.name + " (timeout " + timeoutMillis + "ms) ..." );
			if( waitTimeout( action , timeoutMillis ) ) {
				if( !system )
					action.trace( "wait successfully finished for command=" + command.getClass().getSimpleName() );
				
				return( true );
			}
            
			if( !system )
				action.trace( "wait failed for " + command.getClass().getSimpleName() );
		}
		catch( Throwable e ) {
			if( !system )
				action.handle( "timeout wait for " + command.getClass().getSimpleName() , e );
		}

		broken = true;
		stop = true;
		return( false );
	}

	public boolean isBroken() {
		return( broken );
	}
	
	private synchronized boolean waitTimeout( ActionBase action , long timeoutMillis ) {
		long finishRun = System.currentTimeMillis() + timeoutMillis;

		if( !startAction( action ) )
			return( false );
		
		succeeded = false;
		try {
			while( !succeeded ) {
				long now = System.currentTimeMillis();
		        	
				if( timeoutMillis == 0 ) {
					wait( 0 );
				}
				else {
					if( finishRun <= now )
						return( false );
					
					wait( finishRun - now );
				}
			}
		}
		catch( Throwable e ) {
			action.log( "ShellWaiter" , e );
		}
		
		return( succeeded );
	}

	private boolean startAction( ActionBase action ) {
		if( broken || finished || this.action != null ) {
			action.trace( "unexpected wait shell command=" + command.getClass().getSimpleName() );
			return( false );
		}

		this.action = action;
		
		if( thread == null ) {
			thread = new Thread( null , this , command.getClass().getSimpleName() );
			thread.start();
		}

		notifyAll();
		return( true );
	}

    private synchronized void runAction() {
    	// wait for action
    	if( action == null ) {
    		try {
    			wait();
    			if( action == null )
    				return;
    		}
    		catch( Throwable e ) {
    			return;
    		}
    	}

    	// run action
        boolean res = true;
		try {
	        if( command.waitForCommandFinished )
	        	res = command.runWaitForCommandFinished();
	        else
	        if( command.waitForMarker )
	        	res = command.runWaitForMarker();
	        else
	        if( command.waitInifinite )
	        	command.runWaitInfinite();
	        else
	        	action.exitUnexpectedState();
		}
		catch( Throwable e ) {
			res = false;
			action.log( "ShellWaiter" , e );
		}

		// check status
		action = null;
    	succeeded = true;
        if( !res ) {
        	broken = true;
        	stop = true;
        }
        
        // wakeup waiter
        notifyAll();
    }
	
}
