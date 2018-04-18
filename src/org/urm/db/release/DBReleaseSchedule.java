package org.urm.db.release;

import java.sql.ResultSet;
import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.run.EngineMethod;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBReleaseSchedule {

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
	
	public static void importxmlReleaseSchedule( EngineLoader loader , Release release , ReleaseSchedule schedule , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseSchedule;

		schedule.create(
				entity.importxmlDateAttr( root , ReleaseSchedule.PROPERTY_STARTED ) ,
				entity.importxmlDateAttr( root , ReleaseSchedule.PROPERTY_RELEASEDATE ) ,
				entity.importxmlDateAttr( root , ReleaseSchedule.PROPERTY_RELEASEDATEACTUAL ) ,
				entity.importxmlDateAttr( root , ReleaseSchedule.PROPERTY_COMPLETEDATEACTUAL ) ,
				entity.importxmlBooleanAttr( root , ReleaseSchedule.PROPERTY_RELEASEDSTATUS , false ) ,
				entity.importxmlBooleanAttr( root , ReleaseSchedule.PROPERTY_COMPLETEDSTATUS , false ) ,
				entity.importxmlIntAttr( root , ReleaseSchedule.PROPERTY_PHASE )
				);
		modifySchedule( c , release , schedule , false );
		
		// delete old phases in database
		DBEngineEntities.dropAppObjects( c , entities.entityAppReleasePhase , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
	}
	
	public static void exportxmlReleaseSchedule( EngineLoader loader , Release release , ReleaseSchedule schedule , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseSchedule;
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlDate( schedule.DATE_STARTED ) ,
				entity.exportxmlDate( schedule.RELEASE_DATE ) ,
				entity.exportxmlDate( schedule.RELEASE_DATE_ACTUAL ) ,
				entity.exportxmlDate( schedule.COMPLETE_DATE_ACTUAL ) ,
				entity.exportxmlBoolean( schedule.RELEASED ) ,
				entity.exportxmlBoolean( schedule.COMPLETED ) ,
				entity.exportxmlInt( schedule.CURRENT_PHASE )
		} , true );
	}
	
	public static ReleaseSchedule createReleaseSchedule( EngineMethod method , ActionBase action , Release release ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		ReleaseSchedule schedule = release.getSchedule();
		
		// save to database
		modifySchedule( c , release , schedule , true );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , true );
		
		return( schedule );
	}
	
	public static void changeReleaseDate( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );
		
		// change schedule
		release.setReleaseDate( action , releaseDate , lc );

		// delete old phases in database
		boolean insert = ( lc == null )? false : true; 
		if( insert ) {
			EngineEntities entities = c.getEntities();
			DBEngineEntities.dropAppObjects( c , entities.entityAppReleasePhase , DBQueries.FILTER_REL_RELEASE1 , new String[] { EngineDB.getObject( release.ID ) } );
		}
		
		// save in database
		modifySchedule( c , release , schedule , false );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , insert );
	}

	public static void scheduleSetAllDates( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule , Date[] dates ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		// change
		schedule.setAllDates( action , dates );
		
		// save in database
		modifySchedule( c , release , schedule , false );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , false );
	}
	
	public static void scheduleNextPhase( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		// change
		ReleaseSchedulePhase phaseOld = schedule.getCurrentPhase();
		schedule.nextPhase( action );
		ReleaseSchedulePhase phaseNew = schedule.getCurrentPhase();
		
		// save in database
		modifySchedule( c , release , schedule , false );
		if( phaseOld != null )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phaseOld , false );
		if( phaseOld != null )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phaseNew , false );
	}
	
	public static void setPhaseDeadline( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule , ReleaseSchedulePhase phase , Date deadlineDate ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		// change
		schedule.setPhaseDeadline( action , phase , deadlineDate );
		
		// save in database
		for( ReleaseSchedulePhase phaseUpdate : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phaseUpdate , false );
	}
	
	public static void setPhaseDuration( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule , ReleaseSchedulePhase phase , int duration ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		// change
		schedule.setPhaseDuration( action , phase , duration );
		
		// save in database
		for( ReleaseSchedulePhase phaseUpdate : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phaseUpdate , false );
	}

	public static void finish( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		schedule.finish( action );
		modifySchedule( c , release , schedule , false );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , false );
	}
	
	public static void complete( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		schedule.complete( action );
		modifySchedule( c , release , schedule , false );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , false );
	}
	
	public static void reopen( EngineMethod method , ActionBase action , Release release , ReleaseSchedule schedule ) throws Exception {
		DBConnection c = method.getMethodConnection( action );
		method.checkUpdateRelease( release );

		schedule.reopen( action );
		modifySchedule( c , release , schedule , false );
		for( ReleaseSchedulePhase phase : schedule.getPhases() )
			DBReleaseSchedulePhase.modifySchedulePhase( c , release , schedule , phase , false );
	}
	
}
