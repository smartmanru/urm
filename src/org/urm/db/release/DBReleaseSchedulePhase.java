package org.urm.db.release;

import java.sql.ResultSet;

import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseSchedule;
import org.urm.meta.release.ReleaseSchedulePhase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
	
	public static void loaddbReleaseSchedulePhase( EngineLoader loader , Release release , ReleaseSchedule schedule , ResultSet rs ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleasePhase;
		
		ReleaseSchedulePhase phase = new ReleaseSchedulePhase( release , schedule );
		phase.ID = entity.loaddbId( rs );
		phase.RV = entity.loaddbVersion( rs );
		phase.create(
				DBEnumLifecycleStageType.getValue( entity.loaddbEnum( rs , ReleaseSchedulePhase.PROPERTY_RELEASESTAGE ) , true ) ,
				entity.loaddbInt( rs , ReleaseSchedulePhase.PROPERTY_STAGEPOS ) ,
				entity.loaddbString( rs , ReleaseSchedulePhase.PROPERTY_NAME ) ,
				entity.loaddbString( rs , ReleaseSchedulePhase.PROPERTY_DESC ) ,
				entity.loaddbInt( rs , ReleaseSchedulePhase.PROPERTY_DAYS ) ,
				entity.loaddbInt( rs , ReleaseSchedulePhase.PROPERTY_NORMALDAYS ) ,
				entity.loaddbBoolean( rs , ReleaseSchedulePhase.PROPERTY_FINISHED ) ,
				entity.loaddbBoolean( rs , ReleaseSchedulePhase.PROPERTY_UNLIMITED ) ,
				entity.loaddbDate( rs , ReleaseSchedulePhase.PROPERTY_STARTDATE ) ,
				entity.loaddbDate( rs , ReleaseSchedulePhase.PROPERTY_FINISHDATE )
				);
		schedule.addPhase( phase );
	}
	
	public static void importxmlReleaseSchedulePhase( EngineLoader loader , Release release , ReleaseSchedule schedule , Node node ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleasePhase;
		
		ReleaseSchedulePhase phase = new ReleaseSchedulePhase( release , schedule );
		phase.create(
				DBEnumLifecycleStageType.getValue( entity.importxmlEnumAttr( node , ReleaseSchedulePhase.PROPERTY_RELEASESTAGE ) , true ) ,
				entity.importxmlIntAttr( node , ReleaseSchedulePhase.PROPERTY_STAGEPOS ) ,
				entity.importxmlStringAttr( node , ReleaseSchedulePhase.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( node , ReleaseSchedulePhase.PROPERTY_DESC ) ,
				entity.importxmlIntAttr( node , ReleaseSchedulePhase.PROPERTY_DAYS ) ,
				entity.importxmlIntAttr( node , ReleaseSchedulePhase.PROPERTY_NORMALDAYS ) ,
				entity.importxmlBooleanAttr( node , ReleaseSchedulePhase.PROPERTY_FINISHED , false ) ,
				entity.importxmlBooleanAttr( node , ReleaseSchedulePhase.PROPERTY_UNLIMITED , false ) ,
				entity.importxmlDateAttr( node , ReleaseSchedulePhase.PROPERTY_STARTDATE ) ,
				entity.importxmlDateAttr( node , ReleaseSchedulePhase.PROPERTY_FINISHDATE )
				);
		
		modifySchedulePhase( c , release , schedule , phase , true );
		schedule.addPhase( phase );
	}
	
	public static void exportxmlReleaseSchedulePhase( EngineLoader loader , Release release , ReleaseSchedulePhase phase , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleasePhase;
		
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlEnum( phase.STAGETYPE ) ,
				entity.exportxmlInt( phase.STAGE_POS ) ,
				entity.exportxmlString( phase.NAME ) ,
				entity.exportxmlString( phase.DESC ) ,
				entity.exportxmlInt( phase.DAYS ) ,
				entity.exportxmlInt( phase.NORMAL_DAYS ) ,
				entity.exportxmlBoolean( phase.FINISHED ) ,
				entity.exportxmlBoolean( phase.UNLIMITED ) ,
				entity.exportxmlDate( phase.START_DATE ) ,
				entity.exportxmlDate( phase.FINISH_DATE )
		} , true );
	}
	
}
