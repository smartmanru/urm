package org.urm.engine.dist;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.meta.engine.ServerReleaseLifecycle;
import org.urm.meta.engine.ServerReleaseLifecyclePhase;
import org.urm.meta.engine.ServerReleaseLifecycles;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ReleaseSchedule {

	public Meta meta;
	public Release release;
	
	public String LIFECYCLE;
	public Date started;
	public Date releaseDate;
	public int currentPhase;
	public int releasePhases;
	public int deployPhases;
	public boolean released;
	public boolean completed;
	public boolean archived;
	
	public List<ReleaseSchedulePhase> phases;
	
	public ReleaseSchedule( Meta meta , Release release ) {
		this.meta = meta;
		this.release = release;
		phases = new LinkedList<ReleaseSchedulePhase>();
		currentPhase = 0;
		releasePhases = 0;
		deployPhases = 0;
		released = false;
		completed = false;
		archived = false;
	}
	
	public ReleaseSchedule copy( ActionBase action , Meta meta , Release release ) throws Exception {
		ReleaseSchedule r = new ReleaseSchedule( meta , release );
		r.LIFECYCLE = LIFECYCLE;
		r.started = started;
		r.releaseDate = releaseDate;
		r.currentPhase = currentPhase;
		r.releasePhases = releasePhases; 
		r.deployPhases = deployPhases; 
		r.released = released;
		r.completed = completed;
		r.archived = archived;
		
		for( ReleaseSchedulePhase phase : phases ) {
			ReleaseSchedulePhase rphase = phase.copy( action , meta , r );
			r.phases.add( rphase );
		}
		return( r );
	}

	public void load( ActionBase action , Node root ) throws Exception {
		phases.clear();
		
		Node node = ConfReader.xmlGetFirstChild( root , "schedule" );
		if( node == null ) {
			LIFECYCLE = "";
			releaseDate = null;
			currentPhase = 0;
			releasePhases = 0; 
			deployPhases = 0; 
			released = false;
			completed = false;
			archived = false;
			return;
		}
		
		LIFECYCLE = ConfReader.getAttrValue( node , "lifecycle" , "" );
		started = Common.getDateValue( ConfReader.getAttrValue( node , "started" ) );
		releaseDate = Common.getDateValue( ConfReader.getAttrValue( node , "releasedate" ) );
		currentPhase = ConfReader.getIntegerAttrValue( node , "phase" , 0 );
		released = ConfReader.getBooleanAttrValue( node , "released" , false );
		completed = ConfReader.getBooleanAttrValue( node , "completed" , false );
		archived = ConfReader.getBooleanAttrValue( node , "archived" , false );
		
		Node[] items = ConfReader.xmlGetChildren( node , "phase" );
		if( items == null )
			return;
		
		int pos = 0;
		for( Node phaseNode : items ) {
			ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
			phase.load( action , phaseNode , pos );
			phases.add( phase );
			pos++;
			if( phase.release )
				releasePhases++;
			else
				deployPhases++;
		}
		
		setDeadlines();
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		Element node = Common.xmlCreateElement( doc , root , "schedule" );
		Common.xmlSetElementAttr( doc , node , "lifecycle" , LIFECYCLE );
		Common.xmlSetElementAttr( doc , node , "started" , Common.getDateValue( started ) );
		Common.xmlSetElementAttr( doc , node , "releasedate" , Common.getDateValue( releaseDate ) );
		Common.xmlSetElementAttr( doc , node , "phase" , "" + currentPhase );
		Common.xmlSetElementAttr( doc , node , "released" , Common.getBooleanValue( released ) );
		Common.xmlSetElementAttr( doc , node , "completed" , Common.getBooleanValue( completed ) );
		Common.xmlSetElementAttr( doc , node , "archived" , Common.getBooleanValue( archived ) );
		
		for( ReleaseSchedulePhase phase : phases ) {
			Element phaseElement = Common.xmlCreateElement( doc , node , "phase" );
			phase.save( action , doc , phaseElement );
		}
	}
	
	public void createReleaseSchedule( ActionBase action , Date releaseDate , ServerReleaseLifecycle lc ) throws Exception {
		this.LIFECYCLE = ( lc == null )? "" : lc.ID;
		currentPhase = 0;
		started = Common.getDateCurrentDay();
		phases.clear();
		
		if( lc != null ) {
			if( !lc.enabled )
				action.exit1( _Error.DisabledLifecycle1 , "Release lifecycle " + lc.ID + " is currently disabled" , lc.ID );
			
			int pos = 0;
			releasePhases = 0; 
			deployPhases = 0;
			
			for( ServerReleaseLifecyclePhase lcPhase : lc.getPhases() ) {
				ReleaseSchedulePhase phase = new ReleaseSchedulePhase( meta , this );
				phase.create( action , lcPhase , pos );
				phases.add( phase );
				pos++;
				
				if( phase.release )
					releasePhases++;
				else
					deployPhases++;
			}
			
			if( releasePhases > 0 ) {
				ReleaseSchedulePhase phase = getPhase( 0 );
				phase.startPhase( action , started );
			}
		}
		
		changeReleaseSchedule( action , releaseDate );
	}
	
	public void changeReleaseSchedule( ActionBase action , Date releaseDate ) throws Exception {
		if( released )
			action.exit1( _Error.AlreadyReleased1 , "Release " + release.dist.RELEASEDIR + " is already released" , release.dist.RELEASEDIR );
			
		this.releaseDate = releaseDate;
		setDeadlines();
		
		Date currentDate = Common.getDateCurrentDay();
		int daysBeforeRelease = Common.getDateDiffDays( currentDate , releaseDate ) + 1;
		if( daysBeforeRelease <= 0 ) {
			if( !action.isForced() )
				action.exit1( _Error.DisabledLifecycle1 , "Release " + release.dist.RELEASEDIR + " is trying to release in the past" , release.dist.RELEASEDIR );
		}
		
		if( releasePhases == 0 )
			return;
		
		ReleaseSchedulePhase phase = getPhase( currentPhase );
		int phaseDaysPassed = Common.getDateDiffDays( phase.startDate , currentDate ) + 1 ;
		if( phaseDaysPassed <= 0 )
			action.exitUnexpectedState();
		
		int currentPhaseRequired = phase.days;
		int currentDaysRemained = ( currentPhaseRequired == 0 )? 0 : currentPhaseRequired - phaseDaysPassed;
		if( currentDaysRemained < 0 )
			currentDaysRemained = 0;
		
		int nextDaysRequired = 0;
		if( releasePhases > currentPhase + 1 ) {
			ReleaseSchedulePhase phaseNext = getPhase( currentPhase + 1 );
			nextDaysRequired = Common.getDateDiffDays( phaseNext.deadlineStart , releaseDate ) + 1;
		}
		
		int daysDiff = daysBeforeRelease - ( currentDaysRemained + nextDaysRequired );
		if( daysDiff < 0 ) {
			if( !action.isForced() )
				action.exit1( _Error.DisabledLifecycle1 , "Release " + release.dist.RELEASEDIR + " does not fit lifecycle" , release.dist.RELEASEDIR );
			
			squizeSchedule( action , -daysDiff , currentDaysRemained );
		}
		else
		if( daysDiff > 0 )
			extendSchedule( action , daysDiff );
	}

	public ServerReleaseLifecycle getLifecycle( ActionBase action ) throws Exception {
		if( LIFECYCLE.isEmpty() )
			return( null );
		
		ServerReleaseLifecycles lifecycles = action.getServerReleaseLifecycles();
		return( lifecycles.findLifecycle( LIFECYCLE ) );
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

	private void setDeadlines() {
		int indexDeadline = 0;
		int indexBest = 0;
		for( int k = releasePhases - 1; k >= 0; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			
			int indexDeadlineTo = indexDeadline;
			indexDeadline -= phase.days;
			int indexDeadlineFrom = indexDeadline;
			if( phase.days > 0 )
				indexDeadlineFrom++;
			
			int indexBestTo = indexBest;
			indexBest -= phase.normalDays;
			int indexBestFrom = indexBest;
			if( phase.normalDays > 0 )
				indexBestFrom++;
			
			Date dateDeadlineFrom = Common.addDays( releaseDate , indexDeadlineFrom );
			Date dateDeadlineTo = Common.addDays( releaseDate , indexDeadlineTo );
			Date dateBestFrom = Common.addDays( releaseDate , indexBestFrom );
			Date dateBestTo = Common.addDays( releaseDate , indexBestTo );
			phase.setDeadlineDates( dateDeadlineFrom , dateDeadlineTo , dateBestFrom , dateBestTo );
		}
		
		indexDeadline = 1;
		indexBest = 1;
		for( int k = releasePhases; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			
			int indexDeadlineFrom = indexDeadline;
			indexDeadline += phase.days;
			int indexDeadlineTo = indexDeadline;
			if( phase.days > 0 )
				indexDeadlineTo--;
			
			int indexBestFrom = indexBest;
			indexBest += phase.normalDays;
			int indexBestTo = indexBest;
			if( phase.normalDays > 0 )
				indexBestTo--;
			
			Date dateDeadlineFrom = Common.addDays( releaseDate , indexDeadlineFrom );
			Date dateDeadlineTo = Common.addDays( releaseDate , indexDeadlineTo );
			Date dateBestFrom = Common.addDays( releaseDate , indexBestFrom );
			Date dateBestTo = Common.addDays( releaseDate , indexBestTo );
			phase.setDeadlineDates( dateDeadlineFrom , dateDeadlineTo , dateBestFrom , dateBestTo );
		}
	}
	
	public void finish( ActionBase action ) throws Exception {
		released = true;
		
		Date date = Common.getDateCurrentDay();
		for( int k = 0; k < releasePhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( !phase.finished ) {
				if( phase.startDate == null )
					phase.startPhase( action , date );
				phase.finishPhase( action , date );
			}
		}
		
		if( deployPhases > 0 ) {
			currentPhase = releasePhases;
			ReleaseSchedulePhase phase = getPhase( releasePhases );
			phase.startPhase( action , date );
		}
		else
			currentPhase = -1;
	}
	
	public void complete( ActionBase action ) throws Exception {
		if( currentPhase < releasePhases )
			action.exitUnexpectedState();
		
		completed = true;
		
		Date date = Common.getDateCurrentDay();
		for( int k = currentPhase; k < phases.size(); k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( !phase.finished ) {
				if( phase.startDate == null )
					phase.startPhase( action , date );
				phase.finishPhase( action , date );
			}
		}
		
		currentPhase = -1;
	}
	
	public void reopen( ActionBase action ) throws Exception {
		released = false;
		
		Date date = Common.getDateCurrentDay();
		for( int k = 0; k < deployPhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( releasePhases + k );
			if( phase.startDate != null )
				phase.clearPhase( action );
		}
		
		if( releasePhases > 0 ) {
			currentPhase = releasePhases - 1;
			ReleaseSchedulePhase phase = getPhase( releasePhases - 1 );
			if( phase.finished )
				phase.reopenPhase( action );
		}
		else {
			currentPhase = 0;
			ReleaseSchedulePhase phase = getPhase( 0 );
			phase.startPhase( action , date );
		}
	}

	private void squizeSchedule( ActionBase action , int days , int currentDaysRemained ) throws Exception {
		for( int k = currentPhase; k < releasePhases; k++ ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( k == currentPhase ) {
				phase.setPhaseDuration( action , phase.days - currentDaysRemained );
				days -= currentDaysRemained;
			}
			else {
				int reduce = ( days > phase.days )? phase.days : days;
				phase.setPhaseDuration( action , phase.days - reduce );
				days -= reduce;
			}
		}
		
		setDeadlines();
	}
	
	private void extendSchedule( ActionBase action , int days ) throws Exception {
		for( int k = releasePhases - 1; k >= currentPhase; k-- ) {
			ReleaseSchedulePhase phase = getPhase( k );
			if( phase.days >= phase.normalDays )
				continue;
			
			int increase = phase.normalDays - phase.days;
			if( increase > days )
				increase = days;
			
			phase.setPhaseDuration( action , phase.days + increase );
			days -= increase;
		}
		
		setDeadlines();
	}
	
}
