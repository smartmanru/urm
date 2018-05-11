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
		
		long startTime = 0;
		try {
			engine.debug( "SCHEDULE executor=" + id + ": start task=" + task.name );
			startTime = System.currentTimeMillis();
			task.start();
			task.execute();
			task.finishSuccessful();
		}
		catch( Throwable e ) {
			engine.log( "SCHEDULE executor task" , e );
			task.finishFailed( e );
		}

		long finishTime = System.currentTimeMillis();
		long passTime = finishTime - startTime;
		engine.debug( "SCHEDULE executor=" + id + ": finished task=" + task.name + " (time=" + passTime + "ms)" );
		task.finish();
		scheduler.release( this , task );
	}
	
}
