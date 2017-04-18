package org.urm.engine;

import org.urm.common.Common;

public class ServerEventsTimer extends ServerEventsSource {

	class ServerExecutorTaskTimer extends ServerExecutorTask {
		ServerExecutorTaskTimer() {
			super( "second timer" );
		}
		
		@Override
		public void execute() {
			try {
				cycle();
			}
			catch( Throwable e ) {
				events.engine.handle( "events notifier error" , e );
			}
		}
	};		
	
	private ServerExecutorTaskTimer task;
	
	public ServerEventsTimer( ServerEvents events ) {
		super( events , "urm.timer" );
		task = new ServerExecutorTaskTimer(); 
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	private void cycle() {
		Common.sleep( 1000 );
		super.trigger( ServerEvents.EVENT_SECONDTIMER , null );
	}

	public void start() {
		events.engine.executor.executeCycle( task );
	}

	public synchronized void stop() {
		events.engine.executor.stopTask( task );
	}
	
}
