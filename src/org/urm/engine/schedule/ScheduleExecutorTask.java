package org.urm.engine.schedule;

import org.urm.engine.Engine;
import org.urm.engine.EngineExecutorTask;

public class ScheduleExecutorTask extends EngineExecutorTask {
	
	EngineScheduler scheduler;
	Engine engine;
	int id;
	
	public ScheduleExecutorTask( EngineScheduler scheduler , int id ) {
		super( "schedule-executor-" + id );
		this.scheduler = scheduler;
		this.engine = scheduler.engine;
		this.id = id;
	}

	public void execute() {
		ScheduleTask task = scheduler.getNextTask( this );
		if( task == null )
			return;
		
		try {
			engine.debug( "SCHEDULE executor=" + id + ": start task=" + task.name );
			task.start();
			task.execute();
			task.finishSuccessful();
		}
		catch( Throwable e ) {
			engine.log( "SCHEDULE executor task" , e );
			task.finishFailed( e );
		}

		task.finish();
		scheduler.release( this , task );
	}
	
}
