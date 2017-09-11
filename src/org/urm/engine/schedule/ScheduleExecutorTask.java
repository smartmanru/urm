package org.urm.engine.schedule;

import org.urm.engine.EngineExecutorTask;

public class ScheduleExecutorTask extends EngineExecutorTask {
	
	EngineScheduler scheduler;
	int id;
	
	public ScheduleExecutorTask( EngineScheduler scheduler , int id ) {
		super( "schedule-executor-" + id );
		this.scheduler = scheduler;
		this.id = id;
	}

	public void execute() {
		scheduler.engine.debug( "EXECUTOR=" + id + ": started" );
		ScheduleTask task = scheduler.getNextTask( this );
		if( task == null )
			return;
		
		try {
			scheduler.engine.debug( "EXECUTOR=" + id + ": start task=" + task.name );
			task.start();
			task.execute();
		}
		catch( Throwable e ) {
			scheduler.engine.log( "schedule executor task" , e );
		}

		task.finish();
		scheduler.release( this , task );
	}
	
}
