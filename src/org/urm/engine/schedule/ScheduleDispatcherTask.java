package org.urm.engine.schedule;

import org.urm.engine.EngineExecutorTask;

public class ScheduleDispatcherTask extends EngineExecutorTask {

	EngineScheduler scheduler;
	
	public ScheduleDispatcherTask( EngineScheduler scheduler ) {
		super( "scheduler-dispatcher" );
		this.scheduler = scheduler;
	}

	@Override
	public void execute() {
		scheduler.waitDispatch();
	}
	
}
