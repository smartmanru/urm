package org.urm.db.release;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;

public class DBReleaseSchedule {

	public static ReleaseSchedule createReleaseSchedule( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		
		// save to database
		ReleaseSchedule schedule = release.getSchedule();
		modifySchedule( c , release , schedule , true );
		
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , true );
		
		return( schedule );
	}
	
	private static void modifySchedule( DBConnection c , Release release , ReleaseSchedule schedule , boolean insert ) throws Exception {
		schedule.ID = release.ID;
		schedule.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseSchedule , schedule.ID , schedule.RV , new String[] {
				EngineDB.getDate( schedule.DATE_STARTED ) ,
				EngineDB.getDate( schedule.RELEASE_DATE ) ,
				EngineDB.getDate( schedule.RELEASE_DATE_ACTUAL ) ,
				EngineDB.getDate( schedule.COMPLETE_DATE_ACTUAL ) ,
				EngineDB.getBoolean( schedule.RELEASED ) ,
				EngineDB.getBoolean( schedule.COMPLETED ) ,
				EngineDB.getInteger( schedule.CURRENT_PHASE )
				} , insert );
	}
	
}
