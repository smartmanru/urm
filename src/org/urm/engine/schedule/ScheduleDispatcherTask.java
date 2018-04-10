package org.urm.engine.schedule;

import org.urm.engine.ScheduleService;
import org.urm.engine.run.EngineExecutorTask;

public class ScheduleDispatcherTask extends EngineExecutorTask {

	ScheduleService scheduler;
	
	public ScheduleDispatcherTask( ScheduleService scheduler ) {
		super( "scheduler-dispatcher" );
		this.scheduler = scheduler;
	}

	@Override
	public void execute() {
		scheduler.waitDispatch();
	}
	
}
