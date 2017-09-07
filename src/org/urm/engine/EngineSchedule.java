package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.meta.EngineObject;

public class EngineSchedule extends EngineObject {

	Engine engine;
	List<EngineScheduleTask> data;
	
	public EngineSchedule( Engine engine ) {
		super( null );
		this.engine = engine;
		
		data = new LinkedList<EngineScheduleTask>(); 
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
