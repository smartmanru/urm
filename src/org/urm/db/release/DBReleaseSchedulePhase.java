package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;

public class DBReleaseSchedulePhase {

	public static void modifySchedulePhase( DBConnection c , Release release , ReleaseSchedule schedule , ReleaseSchedulePhase phase , boolean insert ) throws Exception {
		if( insert )
			phase.ID = c.getNextSequenceValue();
		
		phase.RV = c.getNextReleaseVersion( release );
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleasePhase , phase.ID , phase.RV , new String[] {
				EngineDB.getObject( release.ID ) ,
				EngineDB.getEnum( phase.STAGETYPE ) ,
				EngineDB.getInteger( phase.STAGE_POS ) ,
				EngineDB.getString( phase.NAME ) ,
				EngineDB.getString( phase.DESC ) ,
				EngineDB.getInteger( phase.DAYS ) ,
				EngineDB.getInteger( phase.NORMAL_DAYS ) ,
				EngineDB.getBoolean( phase.FINISHED ) ,
				EngineDB.getBoolean( phase.UNLIMITED ) ,
				EngineDB.getDate( phase.START_DATE ) ,
				EngineDB.getDate( phase.FINISH_DATE )
				} , insert );
	}
	
}
