package org.urm.engine.schedule;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

public class EngineScheduler extends EngineObject {

	public enum ScheduleTaskCategory {
		MONITORING
	};
	
	public enum ScheduleTaskType {
		SPECIFIC ,
		WEEKLY ,
		DAILY ,
		HOURLY ,
		INTERVAL
	};
	
	Engine engine;
	Map<ScheduleTaskCategory,ScheduleTaskSet> sets;
	List<ScheduleTask> tasks;
	
	public EngineScheduler( Engine engine ) {
		super( null );
		this.engine = engine;
		
		sets = new HashMap<ScheduleTaskCategory,ScheduleTaskSet>();
		tasks = new LinkedList<ScheduleTask>();
	}
	
	@Override
	public String getName() {
		return( "engine-scheduler" );
	}
	
	public void init() {
		sets.put( ScheduleTaskCategory.MONITORING , new ScheduleTaskSet( this ) );
	}
	
	public void start( ActionBase action ) {
	}
	
	public void stop() {
	}

	public long getTimeInterval( int hours , int minutes , int seconds ) {
		return( ( ( hours * 60 + minutes ) * 60 + seconds ) * 1000 );
	}

	public ScheduleTask findTask( ScheduleTaskCategory category , String name ) {
		ScheduleTaskSet set = sets.get( category );
		if( set == null )
			return( null );
		
		return( set.findTask( name ) );
	}

	public void addTask( ActionBase action , ScheduleTaskCategory category , ScheduleTask task , ScheduleProperties schedule ) {
	}
	
}
