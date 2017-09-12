package org.urm.engine.schedule;

import java.util.Date;

import org.urm.engine.EngineExecutorTask;

abstract public class ScheduleTask extends EngineExecutorTask {

	final ScheduleProperties schedule;
	
	public Date expectedTime;
	public boolean dispatched;
	public boolean stopped;
	
	public Date lastStarted;
	public Date lastFinished;
	
	@Override abstract public void execute();
	
	public ScheduleTask( String name , ScheduleProperties schedule ) {
		super( name );
		this.schedule = schedule;
		dispatched = false;
		stopped = false;
	}
	
	public void start() {
		lastStarted = new Date();
		lastFinished = null;
	}
	
	public void finish() {
		lastFinished = new Date();
		dispatched = false;
	}
	
	public void setExpectedTime( Date date ) {
		expectedTime = date;
	}

	public void setDispatched() {
		dispatched = true;
	}

	public void setStopped() {
		stopped = true;
	}
	
}
