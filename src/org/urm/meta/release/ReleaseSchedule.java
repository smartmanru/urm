package org.urm.meta.release;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist._Error;
import org.urm.meta.engine.LifecyclePhase;
import org.urm.meta.engine.ReleaseLifecycle;

public class ReleaseSchedule {

	public static String PROPERTY_STARTED = "started";
	public static String PROPERTY_RELEASEDATE = "releasedate";
	public static String PROPERTY_RELEASEDATEACTUAL = "releaseactual";
	public static String PROPERTY_COMPLETEDATEACTUAL = "completeactual";
	public static String PROPERTY_RELEASEDSTATUS = "released";
	public static String PROPERTY_COMPLETEDSTATUS = "completed";
	public static String PROPERTY_PHASE = "phase";
	
	public Release release;
	
	public int ID;
	public Date DATE_STARTED;
	public Date RELEASE_DATE;
	public Date RELEASE_DATE_ACTUAL;
	public Date COMPLETE_DATE_ACTUAL;
	public boolean RELEASED;
	public boolean COMPLETED;
	public int CURRENT_PHASE;
	public int RV;

	public int releasePhaseCount;
	public int deployPhaseCount;
	
	public List<ReleaseSchedulePhase> phases;
	
	public ReleaseSchedule( Release release ) {
		this.release = release;
		
		CURRENT_PHASE = -1;
		RELEASED = false;
		COMPLETED = false;
		
		releasePhaseCount = 0;
		deployPhaseCount = 0;
		phases = new LinkedList<ReleaseSchedulePhase>();
	}
	
	public void copy( Release rrelease , ReleaseSchedule r ) throws Exception {
		r.ID = ID;
		r.DATE_STARTED = DATE_STARTED;
		r.RELEASE_DATE = RELEASE_DATE;
		r.RELEASE_DATE_ACTUAL = RELEASE_DATE_ACTUAL;
		r.COMPLETE_DATE_ACTUAL = COMPLETE_DATE_ACTUAL;
		r.RELEASED = RELEASED;
		r.COMPLETED = COMPLETED;
		r.CURRENT_PHASE = CURRENT_PHASE;
		r.RV = RV;
		
		for( ReleaseSchedulePhase phase : phases ) {
			ReleaseSchedulePhase rphase = phase.copy( rrelease , r );
			r.addPhase( rphase );
		}
	}

	public void create( Date DATE_STARTED , Date RELEASE_DATE , Date RELEASE_DATE_ACTUAL , Date COMPLETE_DATE_ACTUAL ,
			boolean RELEASED , boolean COMPLETED , int CURRENT_PHASE ) throws Exception {
		this.DATE_STARTED = DATE_STARTED;
		this.RELEASE_DATE = RELEASE_DATE;
		this.RELEASE_DATE_ACTUAL = RELEASE_DATE_ACTUAL;
		this.COMPLETE_DATE_ACTUAL = COMPLETE_DATE_ACTUAL;
		this.RELEASED = RELEASED;
		this.COMPLETED = COMPLETED;
		this.CURRENT_PHASE = CURRENT_PHASE;
	}

	public void addPhase( ReleaseSchedulePhase phase ) {
		phases.add( phase );
	}
	
	public void sortPhases() throws Exception {
		getPhaseCounts();
		
		Map<String,ReleaseSchedulePhase> map = new HashMap<String,ReleaseSchedulePhase>();
		for( ReleaseSchedulePhase phase : phases ) {
			String key = ( phase.isRelease() )? "1" : "2";
			key += "-" + Common.getZeroPadded( phase.STAGE_POS , 10 );
			map.put( key , phase );
		}
		
		phases.clear();
		for( String key : Common.getSortedKeys( map ) ) {
			ReleaseSchedulePhase phase = map.get( key );
			int expectedIndex = ( phase.isRelease() )? phase.STAGE_POS : releasePhaseCount + phase.STAGE_POS;
			if( phases.size() != expectedIndex )
				Common.exitUnexpected();
			phases.add( phase );
		}
	}
	
	public void getPhaseCounts() {
		releasePhaseCount = 0; 
		deployPhaseCount = 0;
		
		for( ReleaseSchedulePhase phase : phases ) {
			if( phase.isRelease() )
				releasePhaseCount++;
			else
				deployPhaseCount++;
		}
	}
	
	public void createMaster() {
		DATE_STARTED = new Date();
		RELEASE_DATE = null;
		RELEASE_DATE_ACTUAL = null;
		COMPLETE_DATE_ACTUAL = null;  
		RELEASED = false;
		COMPLETED = false;
		CURRENT_PHASE = -1;
		
		releasePhaseCount = 0;
		deployPhaseCount = 0;
	}

	public void createNormal( Date date ) {
		DATE_STARTED = new Date();
		CURRENT_PHASE = -1;
		phases.clear();
		RELEASE_DATE = date;
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		CURRENT_PHASE = ( lc == null )? -1 : 0;
		phases.clear();
		
		if( lc != null ) {
			if( !lc.ENABLED )
				Common.exit1( _Error.DisabledLifecycle1 , "Release lifecycle " + lc.NAME + " is currently disabled" , lc.NAME );
			
			for( LifecyclePhase lcPhase : lc.getPhases() ) {
				ReleaseSchedulePhase phase = new ReleaseSchedulePhase( release , this );
				phase.create( lcPhase );
				phases.add( phase );
			}
			
			getPhaseCounts();
			if( releasePhaseCount > 0 ) {
				ReleaseSchedulePhase phase = getPhase( 0 );
				phase.startPhase( DATE_STARTED );
			}
		}
		
		changeReleaseSchedule( action , releaseDate );
	}
	
	public int getDaysToRelease() {
		Date currentDate = Common.getDateCurrentDay();
		return( Common.getDateDiffDays( currentDate , RELEASE_DATE ) + 1 );
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate ) throws Exception {
		if( RELEASED )
			action.exit1( _Error.AlreadyReleased1 , "Release " + release.RELEASEVER + " is already released" , release.RELEASEVER );
			
		this.RELEASE_DATE = releaseDate;
		setDeadlines();
		
		int daysBeforeRelease = getDaysToRelease();
		if( daysBeforeRelease <= 0 ) {
			if( !action.isForced() )
				action.exit1( _Error.DisabledLifecycle1 , "Release " + release.RELEASEVER + " is trying to release in the past" , release.RELEASEVER );
		}
		
		if( releasePhaseCount == 0 )
			return;
		
		ReleaseSchedulePhase phase = getCurrentPhase();
		int phaseDaysPassed = phase.getDaysPassed();
		if( phaseDaysPassed < 0 )
			action.exitUnexpectedState();
		
		int currentPhaseRequired = phase.getDaysExpected();
		int currentDaysRemained = ( currentPhaseRequired == 0 )? 0 : currentPhaseRequired - phaseDaysPassed;
		if( currentDaysRemained < 0 )
			currentDaysRemained = 0;
		
		int nextDaysRequired = 0;
		if( releasePhaseCount > CURRENT_PHASE + 1 ) {
			ReleaseSchedulePhase phaseNext = getPhase( CURRENT_PHASE + 1 );
			nextDaysRequired = Common.getDateDiffDays( phaseNext.getDeadlineStart() , releaseDate ) + 1;
		}
		
		int daysDiff = daysBeforeRelease - ( currentDaysRemained + nextDaysRequired );
		if( daysDiff < 0 ) {
			if( !action.isForced() )
				action.exit1( _Error.DisabledLifecycle1 , "Release " + release.RELEASEVER + " does not fit lifecycle" , release.RELEASEVER );
			
			squizeSchedule( action , -daysDiff , currentDaysRemained );
		}
		else
		if( daysDiff > 0 )
			extendSchedule( action , daysDiff );
	}

	public ReleaseSchedulePhase getCurrentPhase() {
		if( phases.isEmpty() )
			return( null );
		if( CURRENT_PHASE >= 0 )
			return( phases.get( CURRENT_PHASE ) );
		return( null );
	}
	
	public ReleaseSchedulePhase[] getPhases() {
		return( phases.toArray( new ReleaseSchedulePhase[0] ) );
	}

	public int getPhaseCount() {
		return( phases.size() );
	}
	
	public ReleaseSchedulePhase getPhase( int index ) {
		return( phases.get( index ) );
	}

	public ReleaseSchedulePhase getPhase( ActionBase action , String PHASE ) throws Exception {
		ReleaseSchedulePhase phase = findPhase( PHASE );
		if( phase == null )
			action.exit2( _Error.UnknownReleasePhase2 , "Unknown release=" + release.RELEASEVER + " phase=" + PHASE , release.RELEASEVER , PHASE );
		return( phase );
	}
	
	public ReleaseSchedulePhase findPhase( String PHASE ) {
		if( PHASE.matches( "[0-9]+" ) ) {
			int pos = Integer.parseInt( PHASE );
			if( pos < 1 || pos > phases.size() )
				return( null );
			
			return( getPhase( pos - 1 ) );
		}
		
		String name = PHASE.toLowerCase();
		for( ReleaseSchedulePhase phase : phases ) {
			if( name.equals( phase.NAME.toLowerCase() ) )
				return( phase );
		}
		return( null );
	}

	public void setDeadlines() {
		setDeadlinesBest();
		setDeadlinesExpected();
	}
	
	private void setDeadlinesBest() {
		Date dateBest = RELEASE_DATE;
		for( int k = releasePhaseCount - 1; k >= 0; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setDeadlineDateBest( dateBest );
			dateBest = phase.getDateBeforePhaseBest();
		}
		
		dateBest = RELEASE_DATE;
		for( int k = releasePhaseCount; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setStartDateBest( dateBest );
			dateBest = phase.getBestFinish();
		}
	}

	private void setDeadlinesExpected() {
		Date dateDeadline = RELEASE_DATE;
		for( int k = releasePhaseCount - 1; k >= 0; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setDeadlineDateExpected( dateDeadline );
			dateDeadline = phase.getDateBeforePhaseExpected();
		}
		
		dateDeadline = RELEASE_DATE;
		for( int k = releasePhaseCount; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setStartDateExpected( dateDeadline );
			dateDeadline = phase.getDeadlineFinish();
		}
	}
	
	public Date getDateReleased() {
		return( RELEASE_DATE_ACTUAL );
	}
	
	public Date getDateCompleted() {
		return( COMPLETE_DATE_ACTUAL );
	}
	
	public void finish( ActionBase action ) throws Exception {
		RELEASED = true;
		
		Date date = Common.getDateCurrentDay();
		RELEASE_DATE_ACTUAL = date;
		
		for( int k = 0; k < releasePhaseCount; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( !phase.isFinished() ) {
				if( !phase.isStarted() )
					phase.startPhase( date );
				phase.finishPhase( date );
			}
		}

		CURRENT_PHASE = releasePhaseCount;
		if( deployPhaseCount > 0 ) {
			ReleaseSchedulePhase phase = getPhase( releasePhaseCount );
			phase.startPhase( date );
		}
		else
			complete( action );
	}
	
	public void complete( ActionBase action ) throws Exception {
		if( phases.isEmpty() ) {
			COMPLETED = true;
			return;
		}
		
		if( CURRENT_PHASE >= 0 && CURRENT_PHASE < releasePhaseCount )
			action.exit0( _Error.DistributiveNotReleased1 , "Release is not finished" );
		
		COMPLETED = true;
		
		if( CURRENT_PHASE >= 0 ) {
			Date date = Common.getDateCurrentDay();
			COMPLETE_DATE_ACTUAL = date;
			
			for( int k = CURRENT_PHASE; k < phases.size(); k++ ) {
				ReleaseSchedulePhase phase = getPhase( k );
				if( !phase.isFinished() ) {
					if( !phase.isStarted() )
						phase.startPhase( date );
					phase.finishPhase( date );
				}
			}
		}
		
		CURRENT_PHASE = -1;
	}
	
	public void reopen( ActionBase action ) throws Exception {
		RELEASED = false;
		RELEASE_DATE_ACTUAL = null;
		
		Date date = Common.getDateCurrentDay();
		for( int k = 0; k < deployPhaseCount; k++ ) {
			ReleaseSchedulePhase phase = getPhase( releasePhaseCount + k );
			if( phase.isStarted() )
				phase.clearPhase();
		}
		
		if( releasePhaseCount > 0 ) {
			CURRENT_PHASE = releasePhaseCount - 1;
			ReleaseSchedulePhase phase = getPhase( releasePhaseCount - 1 );
			if( phase.isFinished() )
				phase.reopenPhase();
		}
		else {
			if( deployPhaseCount > 0 ) {
				CURRENT_PHASE = 0;
				ReleaseSchedulePhase phase = getPhase( 0 );
				phase.startPhase( date );
			}
		}
	}

	private void squizeSchedule( ActionBase action , int days , int currentDaysRemained ) throws Exception {
		for( int k = CURRENT_PHASE; k < releasePhaseCount; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( k == CURRENT_PHASE ) {
				phase.setDuration( phase.getDaysExpected() - currentDaysRemained );
				days -= currentDaysRemained;
			}
			else {
				int expected = phase.getDaysExpected();
				int reduce = expected;
				if( reduce > days )
					reduce = days;
				phase.setDuration( expected - reduce );
				days -= reduce;
			}
		}
		
		setDeadlines();
	}
	
	private void extendSchedule( ActionBase action , int days ) throws Exception {
		for( int k = releasePhaseCount - 1; k >= CURRENT_PHASE; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( phase.getDaysExpected() >= phase.getDaysBest() )
				continue;
			
			int increase = phase.getDaysBest() - phase.getDaysExpected();
			if( increase > days )
				increase = days;
			
			phase.setDuration( phase.getDaysExpected() + increase );
			days -= increase;
		}
		
		setDeadlines();
	}

	public void nextPhase( ActionBase action ) throws Exception {
		if( CURRENT_PHASE < 0 )
			action.exit0( _Error.NoCurrentPhase0 , "Release has no current phase" );
		
		if( CURRENT_PHASE == releasePhaseCount - 1 )
			finish( action );
		else
		if( CURRENT_PHASE == phases.size() - 1 )
			complete( action );
		else {
			ReleaseSchedulePhase phase = getCurrentPhase();
			phase.finishPhase( Common.getDateCurrentDay() );
			
			ReleaseSchedulePhase phaseNext = getCurrentPhase();
			Date date = phase.getFinishDate();
			if( phaseNext.requireStartDay() )
				date = Common.addDays( date , 1 );
				
			phaseNext.startPhase( date );
		}
	}

	public void setPhaseDeadline( ActionBase action , ReleaseSchedulePhase phase , Date deadlineDate ) throws Exception {
		if( phase.isFinished() )
			action.exit2( _Error.PhaseFinished2 , "Phase finished, cannot be modified, release=" + release.RELEASEVER + " phase=" + phase.NAME , release.RELEASEVER , phase.NAME );
		
		if( deadlineDate.before( DATE_STARTED ) ) {
			String DATE = Common.getDateValue( DATE_STARTED );
			action.exit2( _Error.DateEalierThanReleaseStarted2 , "Date cannot be before date when release has been started, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		if( phase.STAGE_POS > 0 && deadlineDate.before( phase.getDeadlineStart() ) ) {
			String DATE = Common.getDateValue( phase.getDeadlineStart() );
			action.exit2( _Error.DateEalierThanPhaseStart2 , "Date cannot be before date when phase expected to start, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		Date currentDate = Common.getDateCurrentDay();
		if( deadlineDate.before( currentDate ) ) {
			String DATE = Common.getDateValue( currentDate );
			action.exit2( _Error.DateEalierThanToday2 , "Date cannot be before current date, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		ReleaseSchedulePhase phaseNext = null;
		if( phase.STAGE_POS < phases.size() - 1 ) {
			phaseNext = getPhase( phase.STAGE_POS + 1 );
			String DATE = Common.getDateValue( phaseNext.getDeadlineFinish() );
			if( deadlineDate.after( phaseNext.getDeadlineFinish() ) )
				action.exit2( _Error.DateEalierThanNextPhaseDeadline2 , "Date cannot be after next phase deadline, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
			
			if( phaseNext.requireStartDay() && deadlineDate.compareTo( phaseNext.getDeadlineFinish() ) == 0 )
				action.exit2( _Error.DateEqualToNextPhaseDeadline2 , "Date should be before next phase deadline, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		// set new deadline, update current and next phases, update release date if current phase is finishing phase, shift best representation
		if( phaseNext != null ) {
			if( deadlineDate.after( phase.getDeadlineFinish() ) ) {
				if( phaseNext.requireStartDay() )
					phaseNext.setStartDeadline( Common.addDays( deadlineDate , 1 ) , false );
				else
					phaseNext.setStartDeadline( deadlineDate , false );
			}
			else 
			if( deadlineDate.before( phase.getDeadlineFinish() ) )
				phaseNext.setStartDeadline( deadlineDate , false );
		}
		
		phase.setFinishDeadline( deadlineDate , false );
		if( phase.STAGE_POS == releasePhaseCount - 1 ) {
			RELEASE_DATE = deadlineDate;
			setDeadlinesBest();
		}
	}

	public void setPhaseDuration( ActionBase action , ReleaseSchedulePhase phase , int duration ) throws Exception {
		if( phase.isFinished() )
			action.exit2( _Error.PhaseFinished2 , "Phase finished, cannot be modified, release=" + release.RELEASEVER + " phase=" + phase.NAME , release.RELEASEVER , phase.NAME );

		phase.setDuration( duration );
		setDeadlinesExpected();
	}

	public void setAllDates( ActionBase action , Date[] dates ) throws Exception {
		if( phases.size() * 2 != dates.length )
			action.exitUnexpectedState();

		for( int k = 0; k < phases.size(); k++ ) {
			Date startDate = dates[ k * 2 ];
			Date finishDate = dates[ k * 2 + 1 ];
			ReleaseSchedulePhase phase = phases.get( k );
			
			if( phase.isFinished() )
				continue;
			
			if( phase.isStarted() )
				startDate = phase.getDeadlineStart();
				
			phase.setDeadlines( startDate , finishDate );
			
			if( k == releasePhaseCount - 1 )
				RELEASE_DATE = finishDate;
		}
	}
	
}
