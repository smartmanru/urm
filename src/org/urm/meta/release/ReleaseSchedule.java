package org.urm.meta.release;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.dist._Error;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.engine.LifecyclePhase;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedule {

	public static String PROPERTY_STARTED = "started";
	public static String PROPERTY_RELEASEDATE = "releasedate";
	public static String PROPERTY_RELEASEDATEACTUAL = "releaseactual";
	public static String PROPERTY_COMPLETEDATEACTUAL = "completeactual";
	public static String PROPERTY_RELEASEDSTATUS = "released";
	public static String PROPERTY_COMPLETEDSTATUS = "completed";
	public static String PROPERTY_PHASE = "phase";
	
	public Meta meta;
	public Release release;
	
	public Date started;
	public Date releaseDate;
	public Date releaseDateActual;
	public Date completeDateActual;
	public int currentPhase;
	public int releasePhases;
	public int deployPhases;
	public boolean released;
	public boolean completed;
	
	public List<ReleaseSchedulePhase> phases;
	
	public ReleaseSchedule( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
		phases = new LinkedList<ReleaseSchedulePhase>();
		currentPhase = -1;
		releasePhases = 0;
		deployPhases = 0;
		released = false;
		completed = false;
	}
	
	public ReleaseSchedule copy( ActionBase action , Meta meta , Release release , boolean createProd ) throws Exception {
		ReleaseSchedule r = new ReleaseSchedule( meta , release );
		r.started = started;
		r.releaseDate = ( createProd )? null : releaseDate;
		r.releaseDateActual = ( createProd )? null : releaseDateActual;
		r.completeDateActual = ( createProd )? null : completeDateActual;  
		r.currentPhase = ( createProd )? -1 : currentPhase;
		r.releasePhases = ( createProd )? 0 : releasePhases; 
		r.deployPhases = ( createProd )? 0 : deployPhases; 
		r.released = ( createProd )? false : released;
		r.completed = ( createProd )? false : completed;
		
		if( !createProd ) {
			for( ReleaseSchedulePhase phase : phases ) {
				ReleaseSchedulePhase rphase = phase.copy( action , meta , r );
				r.phases.add( rphase );
			}
		}
		
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		phases.clear();
		
		Node node = ConfReader.xmlGetFirstChild( root , Release.ELEMENT_SCHEDULE );
		if( node == null ) {
			started = new Date();
			releaseDate = null;
			releaseDateActual = null;
			completeDateActual = null;
			currentPhase = -1;
			releasePhases = 0; 
			deployPhases = 0; 
			released = false;
			completed = false;
			return;
		}
		
		started = Common.getDateValue( ConfReader.getAttrValue( node , PROPERTY_STARTED ) );
		if( started == null )
			started = new Date();
			
		releaseDate = Common.getDateValue( ConfReader.getAttrValue( node , PROPERTY_RELEASEDATE ) );
		releaseDateActual = Common.getDateValue( ConfReader.getAttrValue( node , PROPERTY_RELEASEDATEACTUAL ) );
		completeDateActual = Common.getDateValue( ConfReader.getAttrValue( node , PROPERTY_COMPLETEDATEACTUAL ) );
		currentPhase = ConfReader.getIntegerAttrValue( node , PROPERTY_PHASE , 0 );
		released = ConfReader.getBooleanAttrValue( node , PROPERTY_RELEASEDSTATUS , false );
		completed = ConfReader.getBooleanAttrValue( node , PROPERTY_COMPLETEDSTATUS , false );
		
		Node[] items = ConfReader.xmlGetChildren( node , Release.ELEMENT_PHASE );
		if( items == null )
			return;
		
		int pos = 0;
		for( Node phaseNode : items ) {
			ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
			phase.load( action , phaseNode , pos , currentPhase );
			phases.add( phase );
			pos++;
		}
		
		getPhaseCounts();
		setDeadlines();
	}
	
	private void getPhaseCounts() {
		releasePhases = 0; 
		deployPhases = 0; 
		for( ReleaseSchedulePhase phase : phases ) {
			if( phase.isRelease() )
				releasePhases++;
			else
				deployPhases++;
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , Release.ELEMENT_SCHEDULE );
		Common.xmlSetElementAttr( doc , node , PROPERTY_STARTED , Common.getDateValue( started ) );
		Common.xmlSetElementAttr( doc , node , PROPERTY_RELEASEDATE , Common.getDateValue( releaseDate ) );
		Common.xmlSetElementAttr( doc , node , PROPERTY_RELEASEDATEACTUAL , Common.getDateValue( releaseDateActual ) );
		Common.xmlSetElementAttr( doc , node , PROPERTY_COMPLETEDATEACTUAL , Common.getDateValue( completeDateActual ) );
		Common.xmlSetElementAttr( doc , node , PROPERTY_PHASE , "" + currentPhase );
		Common.xmlSetElementAttr( doc , node , PROPERTY_RELEASEDSTATUS , Common.getBooleanValue( released ) );
		Common.xmlSetElementAttr( doc , node , PROPERTY_COMPLETEDSTATUS , Common.getBooleanValue( completed ) );
		
		for( ReleaseSchedulePhase phase : phases ) {
			Element phaseElement = Common.xmlCreateElement( doc , node , Release.ELEMENT_PHASE );
			phase.save( action , doc , phaseElement );
		}
	}

	public void createProd( ActionBase action ) throws Exception {
		releasePhases = 0;
		deployPhases = 0;
		released = false;
		completed = false;
		phases.clear();
	}

	public void create( ActionBase action ) throws Exception {
		started = new Date();
		currentPhase = -1;
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		currentPhase = ( lc == null )? -1 : 0;
		phases.clear();
		
		if( lc != null ) {
			if( !lc.ENABLED )
				action.exit1( _Error.DisabledLifecycle1 , "Release lifecycle " + lc.NAME + " is currently disabled" , lc.NAME );
			
			int pos = 0;
			for( LifecyclePhase lcPhase : lc.getPhases() ) {
				ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
				phase.create( action , lcPhase , pos );
				phases.add( phase );
				pos++;
			}
			
			getPhaseCounts();
			if( releasePhases > 0 ) {
				ReleaseSchedulePhase phase = getPhase( 0 );
				phase.startPhase( action , started );
			}
		}
		
		changeReleaseSchedule( action , releaseDate );
	}
	
	public int getDaysToRelease() {
		Date currentDate = Common.getDateCurrentDay();
		return( Common.getDateDiffDays( currentDate , releaseDate ) + 1 );
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate ) throws Exception {
		if( released )
			action.exit1( _Error.AlreadyReleased1 , "Release " + release.RELEASEVER + " is already released" , release.RELEASEVER );
			
		this.releaseDate = releaseDate;
		setDeadlines();
		
		int daysBeforeRelease = getDaysToRelease();
		if( daysBeforeRelease <= 0 ) {
			if( !action.isForced() )
				action.exit1( _Error.DisabledLifecycle1 , "Release " + release.RELEASEVER + " is trying to release in the past" , release.RELEASEVER );
		}
		
		if( releasePhases == 0 )
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
		if( releasePhases > currentPhase + 1 ) {
			ReleaseSchedulePhase phaseNext = getPhase( currentPhase + 1 );
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
		if( currentPhase >= 0 )
			return( phases.get( currentPhase ) );
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
			if( name.equals( phase.name.toLowerCase() ) )
				return( phase );
		}
		return( null );
	}

	private void setDeadlines() {
		setDeadlinesBest();
		setDeadlinesExpected();
	}
	
	private void setDeadlinesBest() {
		Date dateBest = releaseDate;
		for( int k = releasePhases - 1; k >= 0; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setDeadlineDateBest( dateBest );
			dateBest = phase.getDateBeforePhaseBest();
		}
		
		dateBest = releaseDate;
		for( int k = releasePhases; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setStartDateBest( dateBest );
			dateBest = phase.getBestFinish();
		}
	}

	private void setDeadlinesExpected() {
		Date dateDeadline = releaseDate;
		for( int k = releasePhases - 1; k >= 0; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setDeadlineDateExpected( dateDeadline );
			dateDeadline = phase.getDateBeforePhaseExpected();
		}
		
		dateDeadline = releaseDate;
		for( int k = releasePhases; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			phase.setStartDateExpected( dateDeadline );
			dateDeadline = phase.getDeadlineFinish();
		}
	}
	
	public Date getDateReleased() {
		return( releaseDateActual );
	}
	
	public Date getDateCompleted() {
		return( completeDateActual );
	}
	
	public void finish( ActionBase action ) throws Exception {
		released = true;
		
		Date date = Common.getDateCurrentDay();
		releaseDateActual = date;
		
		for( int k = 0; k < releasePhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( !phase.isFinished() ) {
				if( !phase.isStarted() )
					phase.startPhase( action , date );
				phase.finishPhase( action , date );
			}
		}

		currentPhase = releasePhases;
		if( deployPhases > 0 ) {
			ReleaseSchedulePhase phase = getPhase( releasePhases );
			phase.startPhase( action , date );
		}
		else
			complete( action );
	}
	
	public void complete( ActionBase action ) throws Exception {
		if( phases.isEmpty() ) {
			completed = true;
			return;
		}
		
		if( currentPhase >= 0 && currentPhase < releasePhases )
			action.exit0( _Error.DistributiveNotReleased1 , "Release is not finished" );
		
		completed = true;
		
		if( currentPhase >= 0 ) {
			Date date = Common.getDateCurrentDay();
			completeDateActual = date;
			
			for( int k = currentPhase; k < phases.size(); k++ ) {
				ReleaseSchedulePhase phase = getPhase( k );
				if( !phase.isFinished() ) {
					if( !phase.isStarted() )
						phase.startPhase( action , date );
					phase.finishPhase( action , date );
				}
			}
		}
		
		currentPhase = -1;
	}
	
	public void reopen( ActionBase action ) throws Exception {
		released = false;
		releaseDateActual = null;
		
		Date date = Common.getDateCurrentDay();
		for( int k = 0; k < deployPhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( releasePhases + k );
			if( phase.isStarted() )
				phase.clearPhase( action );
		}
		
		if( releasePhases > 0 ) {
			currentPhase = releasePhases - 1;
			ReleaseSchedulePhase phase = getPhase( releasePhases - 1 );
			if( phase.isFinished() )
				phase.reopenPhase( action );
		}
		else {
			if( deployPhases > 0 ) {
				currentPhase = 0;
				ReleaseSchedulePhase phase = getPhase( 0 );
				phase.startPhase( action , date );
			}
		}
	}

	private void squizeSchedule( ActionBase action , int days , int currentDaysRemained ) throws Exception {
		for( int k = currentPhase; k < releasePhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( k == currentPhase ) {
				phase.setDuration( action , phase.getDaysExpected() - currentDaysRemained );
				days -= currentDaysRemained;
			}
			else {
				int expected = phase.getDaysExpected();
				int reduce = expected;
				if( reduce > days )
					reduce = days;
				phase.setDuration( action , expected - reduce );
				days -= reduce;
			}
		}
		
		setDeadlines();
	}
	
	private void extendSchedule( ActionBase action , int days ) throws Exception {
		for( int k = releasePhases - 1; k >= currentPhase; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( phase.getDaysExpected() >= phase.getDaysBest() )
				continue;
			
			int increase = phase.getDaysBest() - phase.getDaysExpected();
			if( increase > days )
				increase = days;
			
			phase.setDuration( action , phase.getDaysExpected() + increase );
			days -= increase;
		}
		
		setDeadlines();
	}

	public void nextPhase( ActionBase action ) throws Exception {
		if( currentPhase < 0 )
			action.exit0( _Error.NoCurrentPhase0 , "Release has no current phase" );
		
		if( currentPhase == releasePhases - 1 )
			finish( action );
		else
		if( currentPhase == phases.size() - 1 )
			complete( action );
		else {
			ReleaseSchedulePhase phase = getCurrentPhase();
			phase.finishPhase( action , Common.getDateCurrentDay() );
			currentPhase++;
			
			ReleaseSchedulePhase phaseNext = getCurrentPhase();
			Date date = phase.getFinishDate();
			if( phaseNext.requireStartDay() )
				date = Common.addDays( date , 1 );
				
			phaseNext.startPhase( action , date );
		}
	}

	public void setPhaseDeadline( ActionBase action , String PHASE , Date deadlineDate ) throws Exception {
		ReleaseSchedulePhase phase = getPhase( action , PHASE );
		if( phase.isFinished() )
			action.exit2( _Error.PhaseFinished2 , "Phase finished, cannot be modified, release=" + release.RELEASEVER + " phase=" + PHASE , release.RELEASEVER , PHASE );
		
		if( deadlineDate.before( started ) ) {
			String DATE = Common.getDateValue( started );
			action.exit2( _Error.DateEalierThanReleaseStarted2 , "Date cannot be before date when release has been started, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		if( phase.pos > 0 && deadlineDate.before( phase.getDeadlineStart() ) ) {
			String DATE = Common.getDateValue( phase.getDeadlineStart() );
			action.exit2( _Error.DateEalierThanPhaseStart2 , "Date cannot be before date when phase expected to start, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		Date currentDate = Common.getDateCurrentDay();
		if( deadlineDate.before( currentDate ) ) {
			String DATE = Common.getDateValue( currentDate );
			action.exit2( _Error.DateEalierThanToday2 , "Date cannot be before current date, release=" + release.RELEASEVER + " date=" + DATE , release.RELEASEVER , DATE );
		}
		
		ReleaseSchedulePhase phaseNext = null;
		if( phase.pos < phases.size() - 1 ) {
			phaseNext = getPhase( phase.pos + 1 );
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
					phaseNext.setStartDeadline( action , Common.addDays( deadlineDate , 1 ) , false );
				else
					phaseNext.setStartDeadline( action , deadlineDate , false );
			}
			else 
			if( deadlineDate.before( phase.getDeadlineFinish() ) )
				phaseNext.setStartDeadline( action , deadlineDate , false );
		}
		
		phase.setFinishDeadline( action , deadlineDate , false );
		if( phase.pos == releasePhases - 1 ) {
			releaseDate = deadlineDate;
			setDeadlinesBest();
		}
	}

	public void setPhaseDuration( ActionBase action , String PHASE , int duration ) throws Exception {
		ReleaseSchedulePhase phase = getPhase( action , PHASE );
		if( phase.isFinished() )
			action.exit2( _Error.PhaseFinished2 , "Phase finished, cannot be modified, release=" + release.RELEASEVER + " phase=" + PHASE , release.RELEASEVER , PHASE );

		phase.setDuration( action , duration );
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
				
			phase.setDeadlines( action , startDate , finishDate );
			
			if( k == releasePhases - 1 )
				releaseDate = finishDate;
		}
	}
	
}