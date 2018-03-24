package org.urm.db.release;

import java.util.Date;

import org.urm.common.Common;
import org.urm.engine.dist._Error;
import org.urm.meta.engine.LifecyclePhase;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.release.ReleaseSchedulePhase;

public class DBReleaseSchedule {

	public void createReleaseSchedule( Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		CURRENT_PHASE = ( lc == null )? -1 : 0;
		phases.clear();
		
		if( lc != null ) {
			if( !lc.ENABLED )
				Common.exit1( _Error.DisabledLifecycle1 , "Release lifecycle " + lc.NAME + " is currently disabled" , lc.NAME );
			
			int pos = 0;
			for( LifecyclePhase lcPhase : lc.getPhases() ) {
				ReleaseSchedulePhase phase = new ReleaseSchedulePhase( release , this );
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
	
}
