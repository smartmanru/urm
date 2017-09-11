package org.urm.engine.schedule;

import java.util.HashMap;
import java.util.Map;

public class ScheduleTaskSet {

	EngineScheduler scheduler;
	
	Map<String,ScheduleTask> data;
	
	public ScheduleTaskSet( EngineScheduler scheduler ) {
		this.scheduler = scheduler;
		data = new HashMap<String,ScheduleTask>(); 
	}
	
	public ScheduleTask findTask( String name ) {
		return( data.get( name ) );
	}
	
}
