
package org.urm.engine.schedule;

import java.util.Calendar;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.schedule.EngineScheduler.ScheduleTaskType;

public class ScheduleProperties {

	public ScheduleTaskType scheduleType;
	public Date specificDateTime;
	public boolean regularFromEnd;
	public long regularInterval;
	public long betweenInterval;

	public ScheduleProperties() {
		this.scheduleType = ScheduleTaskType.DAILY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = 0;
		this.betweenInterval = 0;
	}
	
	public void createSpecific( ActionBase action , Date specificDateTime ) throws Exception {
		this.scheduleType = ScheduleTaskType.SPECIFIC;
		this.specificDateTime = specificDateTime;
		this.regularFromEnd = false;
		this.regularInterval = 0;
		this.betweenInterval = 0;
	}
	
	public void createWeekly( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleTaskType.WEEKLY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createDaily( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleTaskType.DAILY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createHourly( ActionBase action , long intervalFromBeginning ) throws Exception {
		this.scheduleType = ScheduleTaskType.HOURLY;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = intervalFromBeginning;
		this.betweenInterval = 0;
	}
	
	public void createIntervalFromStart( ActionBase action , long regularInterval , long betweenInterval ) throws Exception {
		this.scheduleType = ScheduleTaskType.INTERVAL;
		this.specificDateTime = null;
		this.regularFromEnd = false;
		this.regularInterval = regularInterval;
		this.betweenInterval = betweenInterval;
	}
	
	public void createIntervalFromEnd( ActionBase action , long regularInterval ) throws Exception {
		this.scheduleType = ScheduleTaskType.INTERVAL;
		this.specificDateTime = null;
		this.regularFromEnd = true;
		this.regularInterval = regularInterval;
		this.betweenInterval = 0;
	}

	public Date getFirstStart() {
		Date currentDate = new Date();
		if( scheduleType == ScheduleTaskType.SPECIFIC ) {
			if( specificDateTime.before( currentDate ) )
				return( null );
			return( specificDateTime );
		}
		
		if( scheduleType == ScheduleTaskType.WEEKLY ) {
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
		
		if( scheduleType == ScheduleTaskType.DAILY ) {
			long timeMillis = currentDate.getTime();
			currentDate.setTime( timeMillis - ( timeMillis % ( 24 * 60 * 60 * 1000L ) ) + regularInterval );
			if( currentDate.before( currentDate ) )
				currentDate.setTime( currentDate.getTime() + 24 * 60 * 60 * 1000L );
			return( currentDate );
		}
		
		if( scheduleType == ScheduleTaskType.HOURLY ) {
			long timeMillis = currentDate.getTime();
			currentDate.setTime( timeMillis - ( timeMillis % ( 60 * 60 * 1000L ) ) + regularInterval );
			if( currentDate.before( currentDate ) )
				currentDate.setTime( currentDate.getTime() + 60 * 60 * 1000L );
			return( currentDate );
		}
		
		if( scheduleType == ScheduleTaskType.INTERVAL )
			return( currentDate );
		
		return( null );
	}
	
	public Date getNextStart( Date dateStart , Date dateEnd ) {
		if( scheduleType == ScheduleTaskType.SPECIFIC )
			return( null );
		
		if( scheduleType == ScheduleTaskType.WEEKLY ||
			scheduleType == ScheduleTaskType.DAILY ||
			scheduleType == ScheduleTaskType.HOURLY ) {
			return( getFirstStart() );
		}

		if( scheduleType == ScheduleTaskType.INTERVAL ) {
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

	public String getScheduleData() {
		String value = "type:" + Common.getEnumLower( scheduleType );
		if( specificDateTime != null )
			value += " specific:" + specificDateTime.getTime();
		if( scheduleType == ScheduleTaskType.INTERVAL )
			value += " endbased:" + Common.getBooleanValue( regularFromEnd );
		if( scheduleType == ScheduleTaskType.WEEKLY ||
			scheduleType == ScheduleTaskType.DAILY ||
			scheduleType == ScheduleTaskType.HOURLY ||
			scheduleType == ScheduleTaskType.INTERVAL )
			value += " interval:" + regularInterval;
		if( scheduleType == ScheduleTaskType.INTERVAL && regularFromEnd == false )
			value += " between:" + betweenInterval;
		return( value );
	}
	
	public void setScheduleData( ActionBase action , String value ) throws Exception {
		ScheduleTaskType scheduleTypeValue = null;
		Date specificDateTimeValue = null;
		boolean regularFromEndValue = false;
		long regularIntervalValue = 0;
		long betweenIntervalValue = 0;
		
		try {
			String[] items = Common.splitSpaced( value );
			for( String item : items ) {
				String[] pair = Common.split( item , ":" );
				if( pair.length != 2 )
					action.exitUnexpectedState();
				
				String var = pair[0];
				String data = pair[1];
				if( var.equals( "type" ) ) {
					scheduleTypeValue = ScheduleTaskType.valueOf( data.toUpperCase() );
				}
				else
				if( var.equals( "specific" ) ) {
					specificDateTimeValue = new Date();
					specificDateTimeValue.setTime( Long.parseLong( data ) );
				}
				else
				if( var.equals( "endbased" ) ) {
					regularFromEndValue = Common.getBooleanValue( data );
				}
				else
				if( var.equals( "interval" ) ) {
					regularIntervalValue = Long.parseLong( data );
					if( regularIntervalValue < 0 )
						action.exitUnexpectedState();
				}
				else
				if( var.equals( "between" ) ) {
					betweenIntervalValue = Long.parseLong( data );
					if( betweenIntervalValue < 0 )
						action.exitUnexpectedState();
				}
			}
		
			if( scheduleTypeValue == ScheduleTaskType.SPECIFIC ) {
				if( specificDateTimeValue == null )
					action.exitUnexpectedState();
				createSpecific( action , specificDateTimeValue );
			}
			else
			if( scheduleTypeValue == ScheduleTaskType.WEEKLY )
				createWeekly( action , regularIntervalValue );
			else
			if( scheduleTypeValue == ScheduleTaskType.DAILY )
				createDaily( action , regularIntervalValue );
			else
			if( scheduleTypeValue == ScheduleTaskType.HOURLY )
				createHourly( action , regularIntervalValue );
			else
			if( scheduleTypeValue == ScheduleTaskType.INTERVAL ) {
				if( regularFromEndValue )
					createIntervalFromEnd( action , regularIntervalValue );
				else
					createIntervalFromStart( action , regularIntervalValue , betweenIntervalValue );
			}
		}
		catch( Throwable e ) {
			action.log( "invalid schedule task data: " + value , e );
			return;
		}
	}

	public int getIntervalDays() {
		return( ( int )( regularInterval / ( 24 * 60 * 60 * 1000L ) ) );
	}
	
	public int getIntervalHours() {
		return( ( int )( regularInterval / ( 60 * 60 * 1000L ) ) );
	}
	
	public int getIntervalMinutes() {
		return( ( int )( regularInterval / ( 60 * 1000L ) ) );
	}
	
	public int getBetweenMinutes() {
		return( ( int )( betweenInterval / ( 60 * 1000L ) ) );
	}
	
}
