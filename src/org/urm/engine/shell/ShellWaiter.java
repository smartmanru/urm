package org.urm.engine.shell;

import org.urm.action.ActionBase;

public class ShellWaiter implements Runnable {

	public ShellOutputWaiter command;

	private volatile boolean stop;
	private volatile boolean finished;
	private volatile boolean succeeded;
	private volatile boolean broken;
	
	private Thread thread;
	private ActionBase action;
	public boolean system;
	private String syncObject;
	
	public ShellWaiter( ShellOutputWaiter command ) {
		this.command = command;
		
		this.broken = false;
		this.stop = false;
		this.succeeded = false;
		this.finished = false;
		
		syncObject = new String();
	}

	@Override
    public void run() {
        finished = false;
        while( !stop )
        	runAction();
        
        finished = true;
		
        synchronized ( syncObject ) {
        	syncObject.notifyAll();
        }
    }

	public void stop( ActionBase action ) {
		synchronized( syncObject ) {
			stop = true;
			action = null;
			syncObject.notifyAll();
		}
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

	public boolean isFinished() {
		return( finished );
	}
	
	public boolean isBroken() {
		return( broken );
	}
	
	private boolean waitTimeout( ActionBase action , long timeoutMillis ) {
		long finishRun = System.currentTimeMillis() + timeoutMillis;

		if( !startAction( action ) )
			return( false );
		
		try {
			while( true ) {
				long now = System.currentTimeMillis();
		        	
				if( timeoutMillis == 0 ) {
					synchronized( syncObject ) {
						syncObject.wait( 0 );
						
						if( stop )
							return( false );
						
						if( succeeded ) {
							succeeded = false;
							return( true );
						}
					}
				}
				else {
					if( finishRun <= now )
						return( false );
					
					synchronized( syncObject ) {
						if( succeeded ) {
							succeeded = false;
							return( true );
						}
						
						syncObject.wait( finishRun - now );
						
						if( stop )
							return( false );
						
						if( succeeded ) {
							succeeded = false;
							return( true );
						}
					}
				}
			}
		}
		catch( Throwable e ) {
			action.log( "ShellWaiter" , e );
		}
		
		return( false );
	}

	private boolean startAction( ActionBase action ) {
		synchronized( syncObject ) {
			if( broken || finished || this.action != null ) {
				action.trace( "unexpected wait shell command=" + command.getClass().getSimpleName() );
				return( false );
			}
	
			this.action = action;
			
			if( thread == null ) {
				thread = new Thread( null , this , command.getClass().getSimpleName() );
				thread.start();
			}
	
			syncObject.notifyAll();
		}
		
		return( true );
	}

    private void runAction() {
    	// wait for action
		try {
			synchronized( syncObject ) {
		    	if( action == null )
		    		syncObject.wait();
		    	
				if( action == null )
					return;
			}
		}
		catch( Throwable e ) {
			return;
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
	        	action.exitUnexpectedState();
		}
		catch( Throwable e ) {
			res = false;
			action.log( "ShellWaiter" , e );
		}

		// check status
        if( !res ) {
        	broken = true;
        	stop = true;
        }
        
        // wakeup waiter
		synchronized( syncObject ) {
			if( action.context.CTX_TRACEINTERNAL )
				action.trace( "notify - action completed" );
			
	    	succeeded = true;
			action = null;
			syncObject.notifyAll();
		}
    }
	
}
