package org.urm.engine;

import org.urm.common.Common;

public class ServerEventsTimer extends ServerEventsSource implements Runnable {

	private ServerThread thread;
	
	public ServerEventsTimer( ServerEvents events ) {
		super( events , "urm.timer" );
		thread = new ServerThread( events.engine , this , "events second timer" , true ); 
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void run() {
		Common.sleep( 1000 );
		super.trigger( ServerEvents.EVENT_SECONDTIMER , null );
	}

	public void start() {
		thread.start();
	}

	public synchronized void stop() {
		thread.stop();
	}
	
}
