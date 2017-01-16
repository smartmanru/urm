package org.urm.engine;

import org.urm.common.Common;

public class ServerEventsTimer extends ServerEventsSource implements Runnable {

	private Thread thread;
	private boolean started = false;
	private boolean stopped = false;
	private boolean stopping = false;
	
	public ServerEventsTimer( ServerEvents events ) {
		super( events , "timer" );
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void run() {
		started = true;
		try {
			while( !stopping )
				cycle();
		}
		catch( Throwable e ) {
			events.engine.handle( "events timer error" , e );
		}
		
		synchronized( this ) {
			thread = null;
			stopped = true;
			notifyAll();
		}
	}

	private void cycle() {
		Common.sleep( 1000 );
		super.trigger( ServerEvents.EVENT_SECONDTIMER , null );
	}
	
	public void start() {
		if( started )
			return;
		
		events.engine.info( "start events timer ..." );
		stopping = false;
        thread = new Thread( null , this , "timer" );
        thread.start();
	}

	public synchronized void stop() {
		if( started == false || stopped )
			return;
		
		events.engine.info( "stop events timer ..." );
		stopping = true;
		try {
			wait();
		}
		catch( Throwable e ) {
			events.engine.log( "events timer stop" , e );
		}
	}
	
}
