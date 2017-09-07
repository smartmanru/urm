package org.urm.engine.events;

import org.urm.common.Common;
import org.urm.engine.EngineExecutorTask;

public class EngineEventsTimer extends EngineEventsSource {

	class ServerExecutorTaskTimer extends EngineExecutorTask {
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
	
	public EngineEventsTimer( EngineEvents events ) {
		super( events , "urm.timer" );
		task = new ServerExecutorTaskTimer(); 
	}

	@Override
	public EngineEventsState getState() {
		return( null );
	}
	
	private void cycle() {
		Common.sleep( 1000 );
		super.notify( EngineEvents.EVENT_SECONDTIMER , null );
	}

	public void start() {
		events.engine.executor.executeCycle( task );
	}

	public synchronized void stop() {
		events.engine.executor.stopTask( task );
	}
	
}
