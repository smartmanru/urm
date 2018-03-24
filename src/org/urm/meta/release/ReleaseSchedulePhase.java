package org.urm.meta.release;

import java.util.Date;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.meta.engine.LifecyclePhase;

public class ReleaseSchedulePhase {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_DAYS = "days";
	public static String PROPERTY_NORMALDAYS = "normaldays";
	public static String PROPERTY_UNLIMITED = "unlimited";
	public static String PROPERTY_STARTDATE = "startdate";
	public static String PROPERTY_RELEASESTAGE = "stage";
	public static String PROPERTY_STAGEPOS = "stagepos";
	public static String PROPERTY_FINISHED = "finished";
	public static String PROPERTY_FINISHDATE = "finishdate";

	Release release;
	ReleaseSchedule schedule;

	public int ID;
	public DBEnumLifecycleStageType STAGETYPE;
	public int STAGE_POS;
	public String NAME;
	public String DESC;
	public int DAYS;
	public int NORMAL_DAYS;
	public boolean FINISHED;
	public boolean UNLIMITED;
	public Date START_DATE;
	public Date FINISH_DATE;
	public int RV;
	
	private Date deadlineStart;
	private Date deadlineFinish;
	private Date bestStart;
	private Date bestFinish;
	
	public ReleaseSchedulePhase( Release release , ReleaseSchedule schedule ) {
		this.release = release;
		this.schedule = schedule;
		
		STAGE_POS = 0;
		DAYS = 0;
		NORMAL_DAYS = 0;
		FINISHED = false;
	}
	
	public ReleaseSchedulePhase copy( Release rrelease , ReleaseSchedule rschedule ) throws Exception {
		ReleaseSchedulePhase r = new ReleaseSchedulePhase( rrelease , rschedule );
		
		r.ID = ID;
		r.STAGETYPE = STAGETYPE;
		r.STAGE_POS = STAGE_POS;
		r.NAME = NAME;
		r.DESC = DESC;
		r.DAYS = DAYS;
		r.NORMAL_DAYS = NORMAL_DAYS;
		r.FINISHED = FINISHED;
		r.UNLIMITED = UNLIMITED;
		r.START_DATE = START_DATE;
		r.FINISH_DATE = FINISH_DATE;
		r.RV = RV;
		
		r.deadlineStart = deadlineStart;
		r.deadlineFinish = deadlineFinish;
		r.bestStart = bestStart;
		r.bestFinish = bestFinish;
		
		return( r );
	}

	public void create( LifecyclePhase lcPhase , int pos ) throws Exception {
		this.STAGETYPE = lcPhase.LIFECYCLESTAGE_TYPE;
		this.STAGE_POS = pos;
		this.NAME = lcPhase.NAME;
		this.DESC = lcPhase.DESC;
		this.DAYS = lcPhase.getDuration();
		this.NORMAL_DAYS = this.DAYS;
		this.FINISHED = false;
		this.UNLIMITED = lcPhase.isUnlimited();
		this.START_DATE = null;
		this.FINISH_DATE = null;
	}

	public boolean isRelease() {
		return( STAGETYPE == DBEnumLifecycleStageType.RELEASE );
	}
	
	public boolean isDeploy() {
		if( isRelease() )
			return( false );
		return( true );
	}
	
	public boolean isStarted() {
		if( START_DATE != null )
			return( true );
		return( false );
	}
	
	public boolean isFinished() {
		return( FINISHED );
	}
	
	public int getDaysPassed() {
		if( START_DATE == null )
			return( -1 );
		Date currentDate = Common.getDateCurrentDay();
		int ndays = Common.getDateDiffDays( START_DATE , currentDate );
		if( requireStartDay() )
			ndays++;
		return( ndays );
	}
	
	public int getDaysExpected() {
		return( DAYS );
	}
	
	public int getDaysBest() {
		return( NORMAL_DAYS );
	}
	
	public Date getDeadlineStart() {
		return( deadlineStart );
	}

	public Date getBestStart() {
		return( bestStart );
	}

	public Date getDeadlineFinish() {
		return( deadlineFinish );
	}

	public Date getBestFinish() {
		return( bestFinish );
	}

	public Date getStartDate() {
		return( START_DATE );
	}
	
	public Date getFinishDate() {
		return( FINISH_DATE );
	}
	
	public Date getDateBeforePhaseExpected() {
		if( !requireStartDay() )
			return( deadlineStart );
		
		return( Common.addDays( deadlineStart , -1 ) );
	}
	
	public Date getDateBeforePhaseBest() {
		if( !requireStartDay() )
			return( bestStart );
		
		return( Common.addDays( bestStart , -1 ) );
	}
	
	public boolean requireStartDay() {
		return( ( UNLIMITED || NORMAL_DAYS > 0 )? true : false );
	}
	
	public int getDaysActually() {
		if( START_DATE == null || FINISH_DATE == null )
			return( -1 );
			
		int diff = Common.getDateDiffDays( START_DATE.getTime() , FINISH_DATE.getTime() );
		if( requireStartDay() )
			diff++;
			
		return( diff );
	}

	public void setDeadlineDateExpected( Date deadlineFinish ) {
		this.deadlineFinish = deadlineFinish;
		
		if( DAYS > 0 )
			this.deadlineStart = Common.addDays( deadlineFinish , -(DAYS-1) );
		else
			this.deadlineStart = deadlineFinish;
	}

	public void setDeadlineDateBest( Date bestFinish ) {
		this.bestFinish = bestFinish;
		
		if( NORMAL_DAYS > 0 )
			this.bestStart = Common.addDays( bestFinish , -(NORMAL_DAYS-1) );
		else
			this.bestStart = bestFinish;
	}

	public void setStartDateExpected( Date deadlineStart ) {
		if( requireStartDay() )
			this.deadlineStart = Common.addDays( deadlineStart , 1 );
		else
			this.deadlineStart = deadlineStart;
		
		if( DAYS > 0 )
			this.deadlineFinish = Common.addDays( this.deadlineStart , (DAYS-1) );
		else
			this.deadlineFinish = this.deadlineStart;
	}

	public void setStartDateBest( Date bestStart ) {
		if( requireStartDay() )
			this.bestStart = Common.addDays( bestStart , 1 );
		else
			this.bestStart = bestStart;
		
		if( NORMAL_DAYS > 0 )
			this.bestFinish = Common.addDays( this.bestStart , (NORMAL_DAYS-1) );
		else
			this.bestFinish = this.bestStart;
	}

	public void startPhase( Date date ) throws Exception {
		START_DATE = date;
		FINISHED = false;
		FINISH_DATE = null;
	}
	
	public void finishPhase( Date date ) throws Exception {
		FINISHED = true;
		FINISH_DATE = date;
	}
	
	public void reopenPhase() throws Exception {
		FINISHED = false;
		FINISH_DATE = null;
	}
	
	public void clearPhase() throws Exception {
		START_DATE = null;
		FINISHED = false;
		FINISH_DATE = null;
	}

	public void setDuration( int duration ) throws Exception {
		this.DAYS = duration;
	}

	private void changeDays() {
		DAYS = Common.getDateDiffDays( deadlineStart , deadlineFinish );
		if( requireStartDay() )
			DAYS++;
	}
	
	public void setFinishDeadline( Date deadlineDate , boolean shiftStart ) throws Exception {
		deadlineFinish = deadlineDate;
		if( shiftStart ) {
			if( DAYS > 0 )
				deadlineStart = Common.addDays( deadlineFinish , -(DAYS-1) );
			else
				deadlineStart = deadlineFinish;
		}
		else
			changeDays();
	}
	
	public void setStartDeadline( Date deadlineDate , boolean shiftFinish ) throws Exception {
		deadlineStart = deadlineDate;
		if( shiftFinish ) {
			if( DAYS > 0 )
				deadlineFinish = Common.addDays( deadlineStart , DAYS - 1 );
			else
				deadlineFinish = deadlineStart;
		}
		else
			changeDays();
	}

	public void setDeadlines( Date deadlineStart , Date deadlineFinish ) throws Exception {
		this.deadlineStart = deadlineStart;
		this.deadlineFinish = deadlineFinish;
		changeDays();
	}
	
}
