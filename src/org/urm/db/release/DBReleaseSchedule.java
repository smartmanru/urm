package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.EngineLoader;
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
	
	public static void loaddbReleaseSchedule( EngineLoader loader , Release release , ReleaseSchedule schedule , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseSchedule;
		
		schedule.ID = entity.loaddbId( rs );
		schedule.RV = entity.loaddbVersion( rs );
		schedule.create(
				entity.loaddbDate( rs , ReleaseSchedule.PROPERTY_STARTED ) ,
				entity.loaddbDate( rs , ReleaseSchedule.PROPERTY_RELEASEDATE ) ,
				entity.loaddbDate( rs , ReleaseSchedule.PROPERTY_RELEASEDATEACTUAL ) ,
				entity.loaddbDate( rs , ReleaseSchedule.PROPERTY_COMPLETEDATEACTUAL ) ,
				entity.loaddbBoolean( rs , ReleaseSchedule.PROPERTY_RELEASEDSTATUS ) ,
				entity.loaddbBoolean( rs , ReleaseSchedule.PROPERTY_COMPLETEDSTATUS ) ,
				entity.loaddbInt( rs , ReleaseSchedule.PROPERTY_PHASE )
				);
	}
	
}
