package org.urm.engine;

public class EngineExecutorThread implements Runnable {

	EngineExecutor executor;
	EngineExecutorTask task;
	boolean cycle;
	
	private Thread thread;
	private boolean started;
	private boolean stopping;
	private boolean stopped;
	
	public EngineExecutorThread( EngineExecutor executor , EngineExecutorTask task , boolean cycle ) {
		this.executor = executor;
		this.task = task;
		this.cycle = cycle;
		
		started = false;
		stopping = false;
		stopped = false;
	}
	
	@Override
	public void run() {
		task.setThread( this );
		while( !stopping ) {
			try {
				task.execute();
				task.finishSuccessful();
			}
			catch( Throwable e ) {
				task.finishFailed( e );
			}
			
			if( !cycle )
				break;
		}
		
		synchronized( task ) {
			stopped = true;
			started = false;
			thread = null;
			task.notifyAll();
		}
	}
	
	public boolean isRunning() {
		synchronized( task ) {
			if( stopped || stopping )
				return( false );
			return( started );
		}
	}
	
	public void start() {
		synchronized( task ) {
			executor.engine.trace( task.name + " - start thread ..." );
			
			started = true;
			stopping = false;
	        thread = new Thread( null , this , task.name );
	        thread.start();
		}
	}
	
	public void stop() {
		if( started == false || stopped )
			return;
		
		executor.engine.trace( task.name + " - stop thread ..." );
		try {
			if( started ) {
				synchronized( task ) {
					stopping = true;
					task.notifyAll();
				}

				while( true ) {
					synchronized( task ) {
						if( !stopped )
							task.wait();
						if( stopped )
							break;
					}
				}
			}
		}
		catch( Throwable e ) {
			executor.engine.handle( "exception when stopping " + task.name + " thread" , e );
		}
		
		executor.engine.trace( task.name + " thread has been stopped" );
	}

}
