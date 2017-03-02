package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
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
		dist.openForControl( this );
		
		if( cmdNext ) {
			ReleaseSchedule schedule = dist.release.schedule;
			schedule.nextPhase( this );
		}
		else
		if( cmdPhaseDeadline ) {
			ReleaseSchedule schedule = dist.release.schedule;
			schedule.setPhaseDeadline( this , PHASE , deadlineDate );
		}
		else
		if( cmdPhaseDuration ) {
			ReleaseSchedule schedule = dist.release.schedule;
			schedule.setPhaseDuration( this , PHASE , duration );
		}
		
		dist.saveReleaseXml( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
