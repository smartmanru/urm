package org.urm.db.engine;

import java.sql.ResultSet;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumLifecycleStageType;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.engine.LifecyclePhase;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.loader.EngineLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBEngineLifecycles {

	public static String ELEMENT_LIFECYCLE = "lifecycle";
	public static String ELEMENT_PHASE = "phase";
	public static String TABLE_LIFECYCLE = "urm_release_lifecycle";
	public static String TABLE_PHASE = "urm_lifecycle_phase";
	public static String FIELD_LIFECYCLE_ID = "lifecycle_id";
	public static String FIELD_LIFECYCLE_DESC = "xdesc";
	public static String FIELD_LIFECYCLE_TYPE = "lifecycle_type";
	public static String FIELD_LIFECYCLE_RELEASEDAYS = "days_to_release";
	public static String FIELD_LIFECYCLE_DEPLOYDAYS = "days_to_deploy";
	public static String FIELD_LIFECYCLE_SHIFTDAYS = "shift_days";
	public static String FIELD_PHASE_LIFECYCLE = "lifecycle_id";
	public static String FIELD_PHASE_ID = "phase_id";
	public static String FIELD_PHASE_DESC = "xdesc";
	public static String FIELD_PHASE_STAGE = "lifecyclestage_type";
	public static String FIELD_PHASE_STAGE_POS = "stage_pos";
	public static String FIELD_PHASE_STAGE_STARTDAY = "start_day";
	public static String XMLPROP_LIFECYCLE_NAME = "id";
	public static String XMLPROP_PHASE_NAME = "id";
	
	
	public static PropertyEntity makeEntityReleaseLifecycle( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.LIFECYCLE , DBEnumParamEntityType.LIFECYCLE , DBEnumObjectVersionType.CORE , TABLE_LIFECYCLE , FIELD_LIFECYCLE_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaStringVar( ReleaseLifecycle.PROPERTY_NAME , ReleaseLifecycle.PROPERTY_NAME , XMLPROP_LIFECYCLE_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( ReleaseLifecycle.PROPERTY_DESC , FIELD_LIFECYCLE_DESC , ReleaseLifecycle.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( ReleaseLifecycle.PROPERTY_TYPE , FIELD_LIFECYCLE_TYPE , ReleaseLifecycle.PROPERTY_TYPE , "Type" , false , DBEnumLifecycleType.UNKNOWN ) ,
				EntityVar.metaBoolean( ReleaseLifecycle.PROPERTY_ENABLED , "Enabled" , false , false ) ,
				EntityVar.metaBoolean( ReleaseLifecycle.PROPERTY_REGULAR , "Regular" , false , false ) ,
				EntityVar.metaIntegerVar( ReleaseLifecycle.PROPERTY_DAYS_TO_RELEASE , FIELD_LIFECYCLE_RELEASEDAYS , ReleaseLifecycle.PROPERTY_DAYS_TO_RELEASE , "Days to release" , false , 0 ) ,
				EntityVar.metaIntegerVar( ReleaseLifecycle.PROPERTY_DAYS_TO_DEPLOY , FIELD_LIFECYCLE_DEPLOYDAYS , ReleaseLifecycle.PROPERTY_DAYS_TO_DEPLOY , "Days_to_deploy" , false , 0 ) ,
				EntityVar.metaIntegerVar( ReleaseLifecycle.PROPERTY_SHIFT_DAYS , FIELD_LIFECYCLE_SHIFTDAYS , ReleaseLifecycle.PROPERTY_SHIFT_DAYS , "Shift days" , false , 0 ) ,
		} ) );
	}

	public static PropertyEntity makeEntityLifecyclePhase( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.LIFECYCLEPHASE , DBEnumParamEntityType.LIFECYCLEPHASE , DBEnumObjectVersionType.CORE , TABLE_PHASE , FIELD_PHASE_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_PHASE_LIFECYCLE , "Lifecycle" , true , null ) ,
				EntityVar.metaStringVar( LifecyclePhase.PROPERTY_NAME , LifecyclePhase.PROPERTY_NAME , XMLPROP_PHASE_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( LifecyclePhase.PROPERTY_DESC , FIELD_PHASE_DESC , LifecyclePhase.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( LifecyclePhase.PROPERTY_STAGE , FIELD_PHASE_STAGE , LifecyclePhase.PROPERTY_STAGE , "Lifecycle stage" , false , DBEnumLifecycleStageType.UNKNOWN ) ,
				EntityVar.metaIntegerVar( LifecyclePhase.PROPERTY_STAGE_POS , FIELD_PHASE_STAGE_POS , LifecyclePhase.PROPERTY_STAGE_POS , "Stage position" , false , 0 ) ,
				EntityVar.metaBoolean( LifecyclePhase.PROPERTY_UNLIMITED , "Unlimited duration" , false , false ) ,
				EntityVar.metaBooleanVar( LifecyclePhase.PROPERTY_START_DAY , FIELD_PHASE_STAGE_STARTDAY , LifecyclePhase.PROPERTY_START_DAY , "Start new day" , false , true ) ,
				EntityVar.metaInteger( LifecyclePhase.PROPERTY_DAYS , "Number of days" , false , 0 )
		} ) );
	}

	public static void importxml( EngineLoader loader , EngineLifecycles lifecycles , Node root ) throws Exception {
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_LIFECYCLE );
		if( list != null ) {
			for( Node node : list ) {
				ReleaseLifecycle lc = importxmlLifecycle( loader , lifecycles , node );
				lifecycles.addLifecycle( lc );
			}
		}
	}
	
	private static ReleaseLifecycle importxmlLifecycle( EngineLoader loader , EngineLifecycles lifecycles , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseLifecycle;
		
		ReleaseLifecycle lc = new ReleaseLifecycle( lifecycles );
		lc.createLifecycle(
				entity.importxmlStringAttr( root , ReleaseLifecycle.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , ReleaseLifecycle.PROPERTY_DESC ) ,
				DBEnumLifecycleType.getValue( entity.importxmlEnumAttr( root , ReleaseLifecycle.PROPERTY_TYPE ) , true ) );
		lc.setEnabled( entity.importxmlBooleanAttr( root , ReleaseLifecycle.PROPERTY_ENABLED , false ) );
		lc.setLifecycleData(
				entity.importxmlBooleanAttr( root , ReleaseLifecycle.PROPERTY_REGULAR , false ) ,
				entity.importxmlIntAttr( root , ReleaseLifecycle.PROPERTY_DAYS_TO_RELEASE ) ,
				entity.importxmlIntAttr( root , ReleaseLifecycle.PROPERTY_DAYS_TO_DEPLOY ) ,
				entity.importxmlIntAttr( root , ReleaseLifecycle.PROPERTY_SHIFT_DAYS ) );
		modifyLifecycle( c , lc , true );
		
		Node[] list = ConfReader.xmlGetChildren( root , ELEMENT_PHASE );
		if( list != null ) {
			for( Node node : list ) {
				LifecyclePhase phase = importxmlPhase( loader , lifecycles , lc , node );
				lc.addPhase( phase );
			}
		}
		
		return( lc );
	}
	
	private static LifecyclePhase importxmlPhase( EngineLoader loader , EngineLifecycles lifecycles , ReleaseLifecycle lc , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppLifecyclePhase;
		
		LifecyclePhase phase = new LifecyclePhase( lc );
		phase.createPhase(
				entity.importxmlStringAttr( root , LifecyclePhase.PROPERTY_NAME ) ,
				entity.importxmlStringAttr( root , LifecyclePhase.PROPERTY_DESC ) ,
				DBEnumLifecycleStageType.getValue( entity.importxmlEnumAttr( root , LifecyclePhase.PROPERTY_STAGE ) , true ) ,
				entity.importxmlIntAttr( root , LifecyclePhase.PROPERTY_STAGE_POS ) ,
				entity.importxmlBooleanAttr( root , LifecyclePhase.PROPERTY_UNLIMITED , false ) ,
				entity.importxmlBooleanAttr( root , LifecyclePhase.PROPERTY_START_DAY , false ) ,
				entity.importxmlIntAttr( root , LifecyclePhase.PROPERTY_DAYS )
				);
		modifyPhase( c , phase , true );
		
		return( phase );
	}
	
	private static void modifyLifecycle( DBConnection c , ReleaseLifecycle lc , boolean insert ) throws Exception {
		if( insert )
			lc.ID = DBNames.getNameIndex( c , DBVersions.CORE_ID , lc.NAME , DBEnumParamEntityType.LIFECYCLE );
		else
			DBNames.updateName( c , DBVersions.CORE_ID , lc.NAME , lc.ID , DBEnumParamEntityType.LIFECYCLE );
		
		lc.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppReleaseLifecycle , lc.ID , lc.CV , new String[] {
				EngineDB.getString( lc.NAME ) , 
				EngineDB.getString( lc.DESC ) ,
				EngineDB.getEnum( lc.LIFECYCLE_TYPE ) ,
				EngineDB.getBoolean( lc.ENABLED ) ,
				EngineDB.getBoolean( lc.REGULAR ) ,
				EngineDB.getInteger( lc.DAYS_TO_RELEASE ) ,
				EngineDB.getInteger( lc.DAYS_TO_DEPLOY ) ,
				EngineDB.getInteger( lc.SHIFT_DAYS )
				} , insert );
	}

	private static void modifyPhase( DBConnection c , LifecyclePhase phase , boolean insert ) throws Exception {
		if( insert )
			phase.ID = DBNames.getNameIndex( c , phase.lc.ID , phase.NAME , DBEnumParamEntityType.LIFECYCLEPHASE );
		else
			DBNames.updateName( c , phase.lc.ID , phase.NAME , phase.ID , DBEnumParamEntityType.LIFECYCLEPHASE );
		
		phase.CV = c.getNextCoreVersion();
		EngineEntities entities = c.getEntities();
		DBEngineEntities.modifyAppObject( c , entities.entityAppLifecyclePhase , phase.ID , phase.CV , new String[] {
				EngineDB.getInteger( phase.lc.ID ) ,
				EngineDB.getString( phase.NAME ) , 
				EngineDB.getString( phase.DESC ) ,
				EngineDB.getEnum( phase.LIFECYCLESTAGE_TYPE ) ,
				EngineDB.getInteger( phase.STAGE_POS ) ,
				EngineDB.getBoolean( phase.UNLIMITED ) ,
				EngineDB.getBoolean( phase.START_DAY ) ,
				EngineDB.getInteger( phase.DAYS )
				} , insert );
	}

	public static void exportxml( EngineLoader loader , EngineLifecycles lifecycles , Document doc , Element root ) throws Exception {
		for( String name : lifecycles.getLifecycleNames() ) {
			ReleaseLifecycle lc = lifecycles.getLifecycle( name );
			Element node = Common.xmlCreateElement( doc , root , ELEMENT_LIFECYCLE );
			exportxmlLifecycle( loader , lc , doc , node );
		}
	}

	public static void exportxmlLifecycle( EngineLoader loader , ReleaseLifecycle lc , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppReleaseLifecycle;
		DBEngineEntities.exportxmlAppObject( doc , root , entity , new String[] {
				entity.exportxmlString( lc.NAME ) ,
				entity.exportxmlString( lc.DESC ) ,
				entity.exportxmlEnum( lc.LIFECYCLE_TYPE ) ,
				entity.exportxmlBoolean( lc.ENABLED ) ,
				entity.exportxmlBoolean( lc.REGULAR ) ,
				entity.exportxmlInt( lc.DAYS_TO_RELEASE ) ,
				entity.exportxmlInt( lc.DAYS_TO_DEPLOY ) ,
				entity.exportxmlInt( lc.SHIFT_DAYS )
		} , true );
		
		for( LifecyclePhase phase : lc.getPhases() ) {
			Element element = Common.xmlCreateElement( doc , root , ELEMENT_PHASE );
			exportxmlPhase( loader , phase , doc , element );
		}
	}

	public static void exportxmlPhase( EngineLoader loader , LifecyclePhase phase , Document doc , Element root ) throws Exception {
		EngineEntities entities = loader.getEntities();
		PropertyEntity entity = entities.entityAppLifecyclePhase;
		DBEngineEntities.exportxmlAppObject( doc , root , entities.entityAppLifecyclePhase , new String[] {
				entity.exportxmlString( phase.NAME ) ,
				entity.exportxmlString( phase.DESC ) ,
				entity.exportxmlEnum( phase.LIFECYCLESTAGE_TYPE ) ,
				entity.exportxmlInt( phase.STAGE_POS ) ,
				entity.exportxmlBoolean( phase.UNLIMITED ) ,
				entity.exportxmlBoolean( phase.START_DAY ) ,
				entity.exportxmlInt( phase.DAYS )
		} , true );
	}

	public static void loaddb( EngineLoader loader , EngineLifecycles lifecycles ) throws Exception {
		loaddbLifecycles( loader , lifecycles );
		loaddbPhases( loader , lifecycles );
		
		for( ReleaseLifecycle lc : lifecycles.getLifecycles() )
			lc.rebuild();
	}

	public static void loaddbLifecycles( EngineLoader loader , EngineLifecycles lifecycles ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppReleaseLifecycle;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				ReleaseLifecycle lc = new ReleaseLifecycle( lifecycles );
				lc.ID = entity.loaddbId( rs );
				lc.CV = entity.loaddbVersion( rs );
				lc.createLifecycle( 
						entity.loaddbString( rs , ReleaseLifecycle.PROPERTY_NAME ) , 
						entity.loaddbString( rs , ReleaseLifecycle.PROPERTY_DESC ) ,
						DBEnumLifecycleType.getValue( entity.loaddbEnum( rs , ReleaseLifecycle.PROPERTY_TYPE ) , true ) );
				lc.setEnabled( entity.loaddbBoolean( rs , ReleaseLifecycle.PROPERTY_ENABLED ) );
				lc.setLifecycleData(
						entity.loaddbBoolean( rs , ReleaseLifecycle.PROPERTY_REGULAR ) ,
						entity.loaddbInt( rs , ReleaseLifecycle.PROPERTY_DAYS_TO_RELEASE ) ,
						entity.loaddbInt( rs , ReleaseLifecycle.PROPERTY_DAYS_TO_DEPLOY ) ,
						entity.loaddbInt( rs , ReleaseLifecycle.PROPERTY_SHIFT_DAYS ) );
				lifecycles.addLifecycle( lc );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static void loaddbPhases( EngineLoader loader , EngineLifecycles lifecycles ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		PropertyEntity entity = entities.entityAppLifecyclePhase;
		
		ResultSet rs = DBEngineEntities.listAppObjects( c , entity );
		try {
			while( rs.next() ) {
				int lifecycleId = entity.loaddbInt( rs , FIELD_PHASE_LIFECYCLE );
				ReleaseLifecycle lc = lifecycles.getLifecycle( lifecycleId );
				
				LifecyclePhase phase = new LifecyclePhase( lc );
				phase.ID = entity.loaddbId( rs );
				phase.CV = entity.loaddbVersion( rs );
				phase.createPhase( 
						entity.loaddbString( rs , LifecyclePhase.PROPERTY_NAME ) , 
						entity.loaddbString( rs , LifecyclePhase.PROPERTY_DESC ) ,
						DBEnumLifecycleStageType.getValue( entity.loaddbEnum( rs , LifecyclePhase.PROPERTY_STAGE ) , true ) ,
						entity.loaddbInt( rs , LifecyclePhase.PROPERTY_STAGE_POS ) ,
						entity.loaddbBoolean( rs , LifecyclePhase.PROPERTY_UNLIMITED ) ,
						entity.loaddbBoolean( rs , LifecyclePhase.PROPERTY_START_DAY ) ,
						entity.loaddbInt( rs , LifecyclePhase.PROPERTY_DAYS ) );
				lifecycles.addPhase( phase );
			}
		}
		finally {
			c.closeQuery();
		}
	}
	
	public static ReleaseLifecycle createLifecycle( EngineTransaction transaction , EngineLifecycles lifecycles , String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( lifecycles.findLifecycle( name ) != null )
			transaction.exitUnexpectedState();
		
		ReleaseLifecycle lc = new ReleaseLifecycle( lifecycles );
		lc.createLifecycle( name , desc , type );
		lc.setEnabled( false );
		lc.setLifecycleData( regular , daysRelease , daysDeploy , shiftDays );
		modifyLifecycle( c , lc , true );
		
		lifecycles.addLifecycle( lc );
		return( lc );
	}
	
	public static void modifyLifecycle( EngineTransaction transaction , EngineLifecycles lifecycles , ReleaseLifecycle lc , String name , String desc , DBEnumLifecycleType type , boolean regular , int daysRelease , int daysDeploy , int shiftDays ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		lc.modifyLifecycle( name , desc , type );
		lc.setLifecycleData( regular , daysRelease , daysDeploy , shiftDays );
		modifyLifecycle( c , lc , false );
		
		lifecycles.updateLifecycle( lc );
	}
	
	public static void deleteLifecycle( EngineTransaction transaction , EngineLifecycles lifecycles , ReleaseLifecycle lc ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppLifecyclePhase , DBQueries.FILTER_LIFECYCLE_ID1 , new String[] { EngineDB.getInteger( lc.ID ) } );
		DBEngineEntities.deleteAppObject( c , entities.entityAppReleaseLifecycle , lc.ID , c.getNextCoreVersion() );
		
		lifecycles.removeLifecycle( lc );
		lc.deleteObject();
	}

	public static ReleaseLifecycle copyLifecycle( EngineTransaction transaction , EngineLifecycles lifecycles , ReleaseLifecycle lc , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( lifecycles.findLifecycle( name ) != null )
			transaction.exitUnexpectedState();
		
		ReleaseLifecycle lcNew = lc.copy( lifecycles );
		lcNew.modifyLifecycle( name , desc , lc.LIFECYCLE_TYPE );
		lcNew.setEnabled( false );
		modifyLifecycle( c , lcNew , true );
		
		lifecycles.addLifecycle( lcNew );
		return( lcNew );
	}

	public static void enableLifecycle( EngineTransaction transaction , EngineLifecycles lifecycles , ReleaseLifecycle lc , boolean enabled ) throws Exception {
		DBConnection c = transaction.getConnection();
		lc.setEnabled( enabled );
		modifyLifecycle( c , lc , false );
	}
	
	public static void changePhases( EngineTransaction transaction , EngineLifecycles lifecycles , ReleaseLifecycle lc , LifecyclePhase[] phases ) throws Exception {
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		
		LifecyclePhase[] phasesNew = new LifecyclePhase[ phases.length ];
		for( int k = 0; k < phases.length; k++ ) {
			LifecyclePhase phase = phases[ k ].copy( lc );
			phasesNew[ k ] = phase;
		}
		lc.setPhases( phasesNew );
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppLifecyclePhase , DBQueries.FILTER_LIFECYCLE_ID1 , new String[] { EngineDB.getInteger( lc.ID ) } );
		
		for( LifecyclePhase phase : lc.getPhases() )
			modifyPhase( c , phase , true );
	}
	
}
