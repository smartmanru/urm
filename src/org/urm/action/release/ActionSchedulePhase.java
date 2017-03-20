package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.dist.ReleaseSchedule;

public class ActionSchedulePhase extends ActionBase {

	public Dist dist;
	
	boolean cmdNext = false;
	boolean cmdPhaseDeadline = false;
	boolean cmdPhaseDuration = false;
	
	String PHASE;
	Date deadlineDate;
	int duration;

	public ActionSchedulePhase( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Proceed to next phase release=" + dist.RELEASEDIR );
		this.dist = dist;
		cmdNext = true;
	}

	public ActionSchedulePhase( ActionBase action , String stream , Dist dist , String PHASE , Date deadlineDate ) {
		super( action , stream , "Reschedule phase deadline release=" + dist.RELEASEDIR + ", phase=" + PHASE );
		this.dist = dist;
		this.PHASE = PHASE;
		this.deadlineDate = deadlineDate;
		cmdPhaseDeadline = true;
	}

	public ActionSchedulePhase( ActionBase action , String stream , Dist dist , String PHASE , int duration ) {
		super( action , stream , "Reschedule phase duration release=" + dist.RELEASEDIR + ", phase=" + PHASE );
		this.dist = dist;
		this.PHASE = PHASE;
		this.duration = duration;
		cmdPhaseDuration = true;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		ReleaseSchedule schedule = dist.release.schedule;
		if( cmdNext && schedule.currentPhase >= 0 ) {
			if( schedule.currentPhase == schedule.releasePhases - 1 ) {
				dist.finish( this );
				return( SCOPESTATE.RunSuccess );
			}
			
			if( schedule.currentPhase == schedule.getPhaseCount() - 1 ) {
				dist.complete( this );
				return( SCOPESTATE.RunSuccess );
			}
		}
		
		DISTSTATE state = open();
		if( cmdNext )
			schedule.nextPhase( this );
		else
		if( cmdPhaseDeadline )
			schedule.setPhaseDeadline( this , PHASE , deadlineDate );
		else
		if( cmdPhaseDuration )
			schedule.setPhaseDuration( this , PHASE , duration );
	
		close( state );
		return( SCOPESTATE.RunSuccess );
	}

	private DISTSTATE open() throws Exception {
		DISTSTATE state = dist.getState();
		if( dist.isFinalized() )
			dist.openForControl( this );
		else
			dist.openForDataChange( this );
		return( state );
	}

	private void close( DISTSTATE state ) throws Exception {
		dist.saveReleaseXml( this );
		if( dist.isFinalized() )
			dist.closeControl( this , state );
		else
			dist.closeDataChange( this );
	}
	
}