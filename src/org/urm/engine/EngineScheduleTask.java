package org.urm.engine;

import java.util.Calendar;
import java.util.Date;

import org.urm.action.ActionBase;

public class EngineScheduleTask {

	EngineSchedule schedule;
	
	public enum ScheduleType {
		SPECIFIC ,
		WEEKLY ,
		DAILY ,
		HOURLY ,
		INTERVAL
	};
	
	public ScheduleType scheduleType;
	public Date specificDateTime;
	public boolean regularFromEnd;
	public long regularInterval;
	public long betweenInterval;

	public Date lastStarted;
	public Date lastFinished;
	
	public EngineScheduleTask( EngineSchedule schedule ) {
		this.schedule = schedule;
		
		this.scheduleType = ScheduleType.INTERVAL;
		this.specificDateTime = null;
		this.regularFromEnd = true;
		this.regularInterval = schedule.getTimeInterval( 0 , 5 , 0 );
		this.betweenInterval = 0;
	}
	
	public void createSingle( ActionBase action , Date specificDateTime ) throws Exception {
		this.scheduleType = ScheduleType.SPECIFIC;
		this.specificDateTime = specificDateTime;
		this.regularFromEnd = false;
		this.regularInterval = 0;
		this.betweenInterval = 0;
	}
	
	public void createWeekly( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleType.WEEKLY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createDaily( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleType.DAILY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createHourly( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleType.HOURLY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createIntervalFromStart( ActionBase action , long regularInterval , long betweenInterval ) throws Exception {
		this.scheduleType = ScheduleType.INTERVAL;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = regularInterval;
		this.betweenInterval = betweenInterval;
	}
	
	public void createIntervalFromEnd( ActionBase action , long regularInterval ) throws Exception {
		this.scheduleType = ScheduleType.INTERVAL;
		this.specificDateTime = null;
		this.regularFromEnd = true;
		this.regularInterval = regularInterval;
		this.betweenInterval = 0;
	}

	public Date getFirstStart() {
		Date currentDate = new Date();
		if( scheduleType == ScheduleType.SPECIFIC ) {
			if( specificDateTime.before( currentDate ) )
				return( null );
			return( specificDateTime );
		}
		
		if( scheduleType == ScheduleType.WEEKLY ) {
			Calendar cal = Calendar.getInstance();
			int weekDay = cal.get( Calendar.DAY_OF_WEEK );
			int weekShift = ( weekDay == Calendar.SUNDAY )? 6 : ( weekDay - 2 );
			cal.roll( Calendar.HOUR , -weekShift * 24 );
			Date startDate = cal.getTime();
			startDate.setTime( startDate.getTime() + regularInterval );
			
			if( startDate.before( currentDate ) )
				startDate.setTime( startDate.getTime() + 7 * 24 * 60 * 60 * 1000L );
			return( startDate );
		}
		
		if( scheduleType == ScheduleType.DAILY ) {
			long timeMillis = currentDate.getTime();
			currentDate.setTime( timeMillis - ( timeMillis % ( 24 * 60 * 60 * 1000L ) ) + regularInterval );
			if( currentDate.before( currentDate ) )
				currentDate.setTime( currentDate.getTime() + 24 * 60 * 60 * 1000L );
			return( currentDate );
		}
		
		if( scheduleType == ScheduleType.HOURLY ) {
			long timeMillis = currentDate.getTime();
			currentDate.setTime( timeMillis - ( timeMillis % ( 60 * 60 * 1000L ) ) + regularInterval );
			if( currentDate.before( currentDate ) )
				currentDate.setTime( currentDate.getTime() + 60 * 60 * 1000L );
			return( currentDate );
		}
		
		if( scheduleType == ScheduleType.INTERVAL )
			return( currentDate );
		
		return( null );
	}
	
	public Date getNextStart( Date dateStart , Date dateEnd ) {
		if( scheduleType == ScheduleType.SPECIFIC )
			return( null );
		
		if( scheduleType == ScheduleType.WEEKLY ||
			scheduleType == ScheduleType.DAILY ||
			scheduleType == ScheduleType.HOURLY ) {
			return( getFirstStart() );
		}

		if( scheduleType == ScheduleType.INTERVAL ) {
			Date currentDate = new Date();
			Date nextDate = new Date();
			if( regularFromEnd ) {
				nextDate.setTime( dateEnd.getTime() + regularInterval );
				if( nextDate.before( currentDate ) )
					nextDate = currentDate;
			}
			else {
				nextDate.setTime( dateStart.getTime() + regularInterval );
				if( nextDate.before( currentDate ) )
					nextDate = currentDate;
				
				Date limitDate = new Date();
				limitDate.setTime( dateEnd.getTime() + betweenInterval );
				if( nextDate.before( limitDate ) )
					nextDate = limitDate;
			}
			
			return( nextDate );
		}
		
		return( null );
	}
	
}
