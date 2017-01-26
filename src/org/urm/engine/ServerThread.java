package org.urm.engine;

public class ServerThread implements Runnable {

	ServerEngine engine;
	Runnable runnable;
	String name;
	boolean cycle;
	
	private Thread thread;
	private boolean started;
	private boolean stopping;
	private boolean stopped;
	
	public ServerThread( ServerEngine engine , Runnable runnable , String name , boolean cycle ) {
		this.engine = engine;
		this.runnable = runnable;
		this.name = name;
		this.cycle = cycle;
		
		started = false;
		stopping = false;
		stopped = false;
	}
	
	@Override
	public void run() {
		while( !stopping ) {
			runnable.run();
			if( !cycle )
				break;
		}
		
		synchronized( this ) {
			stopped = true;
			started = false;
			thread = null;
			notifyAll();
		}
	}
	
	public synchronized boolean isRunning() {
		if( stopped || stopping )
			return( false );
		return( started );
	}
	
	public synchronized void start() {
		engine.info( name + " - start thread ..." );
		
		started = true;
		stopping = false;
        thread = new Thread( null , this , name );
        thread.start();
	}
	
	public void stop() {
		if( started == false || stopped )
			return;
		
		engine.info( name + " - stop thread ..." );
		try {
			if( started ) {
				synchronized( this ) {
					stopping = true;
					notifyAll();
				}

				while( true ) {
					synchronized( this ) {
						if( !stopped )
							wait();
						if( stopped )
							break;
					}
				}
			}
		}
		catch( Throwable e ) {
			engine.handle( "exception when stopping " + name + " thread" , e );
		}
		
		engine.debug( name + " thread has been stopped" );
	}

}
