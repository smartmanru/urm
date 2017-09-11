package org.urm.engine.schedule;

import java.util.Date;

public class ScheduleTask {

	final String name;
	
	public Date lastStarted;
	public Date lastFinished;
	
	public ScheduleTask( String name ) {
		this.name = name;
	}
	
}
