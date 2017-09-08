package org.urm.engine.schedule;

import java.util.Date;

public class EngineSchedulerTask {

	EngineScheduler schedule;

	public Date lastStarted;
	public Date lastFinished;
	
	public EngineSchedulerTask( EngineScheduler schedule ) {
		this.schedule = schedule;
	}
	
}
