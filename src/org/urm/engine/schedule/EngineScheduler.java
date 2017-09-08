package org.urm.engine.schedule;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

public class EngineScheduler extends EngineObject {

	public enum ScheduleTaskType {
		SPECIFIC ,
		WEEKLY ,
		DAILY ,
		HOURLY ,
		INTERVAL
	};
	
	Engine engine;
	List<EngineSchedulerTask> data;
	
	public EngineScheduler( Engine engine ) {
		super( null );
		this.engine = engine;
		
		data = new LinkedList<EngineSchedulerTask>(); 
	}
	
	@Override
	public String getName() {
		return( "engine-schedule" );
	}
	
	public void init() {
	}
	
	public void start( ActionBase action ) {
	}
	
	public void stop() {
	}

	public long getTimeInterval( int hours , int minutes , int seconds ) {
		return( ( ( hours * 60 + minutes ) * 60 + seconds ) * 1000 );
	}
	
}
