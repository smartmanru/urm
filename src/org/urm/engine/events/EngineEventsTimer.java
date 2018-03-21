package org.urm.engine.events;

import org.urm.common.Common;
import org.urm.engine.EventService;
import org.urm.engine.run.EngineExecutorTask;

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
	
	public EngineEventsTimer( EventService events ) {
		super( events , "urm.timer" );
		task = new ServerExecutorTaskTimer(); 
	}

	@Override
	public EngineEventsState getState() {
		return( null );
	}
	
	private void cycle() {
		Common.sleep( 1000 );
		super.notify( EventService.OWNER_ENGINE , EventService.EVENT_SECONDTIMER , null );
	}

	public void start() {
		events.engine.executor.executeCycle( task );
	}

	public void stop() {
		events.engine.executor.stopTask( task );
	}
	
}
