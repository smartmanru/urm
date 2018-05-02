package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseSchedule;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;

public class ActionSchedulePhase extends ActionBase {

	public Release release;
	
	boolean cmdNext = false;
	boolean cmdPhaseDeadline = false;
	boolean cmdPhaseDuration = false;
	boolean cmdScheduleAll = false;
	
	String PHASE;
	Date deadlineDate;
	int duration;
	Date[] dates;

	public ActionSchedulePhase( ActionBase action , String stream , Release release ) {
		super( action , stream , "Proceed to next phase release=" + release.RELEASEVER );
		this.release = release;
		cmdNext = true;
	}

	public ActionSchedulePhase( ActionBase action , String stream , Release release , String PHASE , Date deadlineDate ) {
		super( action , stream , "Reschedule phase deadline release=" + release.RELEASEVER + ", phase=" + PHASE );
		this.release = release;
		this.PHASE = PHASE;
		this.deadlineDate = deadlineDate;
		cmdPhaseDeadline = true;
	}

	public ActionSchedulePhase( ActionBase action , String stream , Release release , String PHASE , int duration ) {
		super( action , stream , "Reschedule phase duration release=" + release.RELEASEVER + ", phase=" + PHASE );
		this.release = release;
		this.PHASE = PHASE;
		this.duration = duration;
		cmdPhaseDuration = true;
	}

	public ActionSchedulePhase( ActionBase action , String stream , Release release , Date[] dates ) {
		super( action , stream , "Schedule all phases release=" + release.RELEASEVER );
		this.release = release;
		this.dates = dates;
		cmdScheduleAll = true;
	}
	
	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		ReleaseSchedule schedule = release.getSchedule();
		if( cmdNext && schedule.CURRENT_PHASE >= 0 ) {
			if( schedule.CURRENT_PHASE == schedule.releasePhaseCount - 1 ) {
				try {
					ReleaseCommand.finishRelease( state , this , release );
				}
				catch( Throwable e ) {
					super.log( "finishRelease" , e );
					super.exit0( _Error.UnableFinalizeRelease0 , "Unable to finalize release" );
				}
				return( SCOPESTATE.RunSuccess );
			}
			
			if( schedule.CURRENT_PHASE == schedule.getPhaseCount() - 1 ) {
				ReleaseCommand.completeRelease( state , this , release );
				return( SCOPESTATE.RunSuccess );
			}
		}
		
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			ReleaseSchedule scheduleUpdated = releaseUpdated.getSchedule();
			
			if( cmdNext )
				DBReleaseSchedule.scheduleNextPhase( method , this , releaseUpdated , scheduleUpdated );
			else
			if( cmdPhaseDeadline ) {
				ReleaseSchedulePhase phase = scheduleUpdated.getPhase( this , PHASE );
				DBReleaseSchedule.setPhaseDeadline( method , this , releaseUpdated , scheduleUpdated , phase , deadlineDate );
			}
			else
			if( cmdPhaseDuration ) {
				ReleaseSchedulePhase phase = scheduleUpdated.getPhase( this , PHASE );
				DBReleaseSchedule.setPhaseDuration( method , this , releaseUpdated , scheduleUpdated , phase , duration );
			}
			else
			if( cmdScheduleAll )
				DBReleaseSchedule.scheduleSetAllDates( method , this , releaseUpdated , scheduleUpdated , dates );
		}
	
		return( SCOPESTATE.RunSuccess );
	}

}
