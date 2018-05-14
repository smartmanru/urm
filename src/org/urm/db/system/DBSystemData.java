package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppProductMonitoringItem;
import org.urm.meta.system.AppProductMonitoringTarget;
import org.urm.meta.system.AppProductPolicy;
import org.urm.meta.system.AppSystem;
import org.urm.meta.system.ProductDump;
import org.urm.meta.system.ProductDumpMask;

public class DBSystemData {

	public static String TABLE_SYSTEM = "urm_system";
	public static String TABLE_PRODUCT = "urm_product";
	public static String TABLE_POLICY = "urm_product_policy";
	public static String TABLE_POLICYCYCLE = "urm_product_lifecycle";
	public static String TABLE_DUMP = "urm_product_dbdump";
	public static String TABLE_DUMPMASK = "urm_product_tablemask";
	public static String TABLE_MONTARGET = "urm_product_montarget";
	public static String TABLE_MONITEM = "urm_product_monitem";
	public static String FIELD_SYSTEM_ID = "system_id";
	public static String FIELD_SYSTEM_DESC = "xdesc";
	public static String FIELD_SYSTEM_MATCHED = "matched";
	public static String FIELD_PRODUCT_SYSTEM_ID = "system_id";
	public static String FIELD_PRODUCT_ID = "product_id";
	public static String FIELD_PRODUCT_DESC = "xdesc";
	public static String FIELD_PRODUCT_MONITORING_ENABLED = "monitoring_enabled";
	public static String FIELD_PRODUCT_LAST_MAJOR1 = "last_major1";
	public static String FIELD_PRODUCT_LAST_MAJOR2 = "last_major2";
	public static String FIELD_PRODUCT_LAST_MINOR1 = "last_minor1";
	public static String FIELD_PRODUCT_LAST_MINOR2 = "last_minor2";
	public static String FIELD_PRODUCT_NEXT_MAJOR1 = "next_major1";
	public static String FIELD_PRODUCT_NEXT_MAJOR2 = "next_major2";
	public static String FIELD_PRODUCT_NEXT_MINOR1 = "next_minor1";
	public static String FIELD_PRODUCT_NEXT_MINOR2 = "next_minor2";
	public static String FIELD_POLICY_ID = "product_id";
	public static String FIELD_POLICY_LCURGENTALL = "lcurgent_any";
	public static String FIELD_LIFECYCLE_PRODUCT = "product_id";
	public static String FIELD_LIFECYCLE_ID = "lifecycle_id";
	public static String FIELD_DUMP_ID = "dump_id";
	public static String FIELD_DUMP_PRODUCT_ID = "product_id";
	public static String FIELD_DUMP_DESC = "xdesc";
	public static String FIELD_DUMP_FKENV = "db_fkenv";
	public static String FIELD_DUMP_FKSG = "db_fksg";
	public static String FIELD_DUMP_FKSERVER = "db_fkserver";
	public static String FIELD_DUMP_SERVER_ID = "db_fkid";
	public static String FIELD_DUMP_EXPORT = "modeexport";
	public static String FIELD_DUMP_SETDBENV = "remote_setdbenv";
	public static String FIELD_DUMP_DATAPUMPDIR = "database_datapumpdir";
	public static String FIELD_DUMP_STANDBY = "usestandby";
	public static String FIELD_DUMP_NFS = "usenfs";
	public static String FIELD_DUMPMASK_ID = "tablemask_id";
	public static String FIELD_DUMPMASK_PRODUCT_ID = "product_id";
	public static String FIELD_DUMPMASK_DUMP_ID = "dump_id";
	public static String FIELD_DUMPMASK_INCLUDE = "modeinclude";
	public static String FIELD_DUMPMASK_SCHEMA_ID = "schema_fkid";
	public static String FIELD_DUMPMASK_SCHEMA_NAME = "schema_fkname";
	public static String FIELD_DUMPMASK_MASK = "tablemask";
	public static String FIELD_MONTARGET_ID = "montarget_id";
	public static String FIELD_MONTARGET_SEGMENT_ID = "target_fkid";
	public static String FIELD_MONTARGET_FKENV = "target_fkenv";
	public static String FIELD_MONTARGET_FKSG = "target_fksg";
	public static String FIELD_MONTARGET_MAJOR_ENABLED = "major_enabled";
	public static String FIELD_MONTARGET_MAJOR_SCHEDULE = "major_schedule";
	public static String FIELD_MONTARGET_MAJOR_MAXTIME = "major_maxtime";
	public static String FIELD_MONTARGET_MINOR_ENABLED = "minor_enabled";
	public static String FIELD_MONTARGET_MINOR_SCHEDULE = "minor_schedule";
	public static String FIELD_MONTARGET_MINOR_MAXTIME = "minor_maxtime";
	public static String FIELD_MONITEM_ID = "monitem_id";
	public static String FIELD_MONITEM_TARGET_ID = "montarget_id";
	public static String FIELD_MONITEM_TYPE = "monitem_type";
	public static String FIELD_MONITEM_DESC = "xdesc";
	
	public static PropertyEntity makeEntityDirectorySystem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.APPSYSTEM , DBEnumObjectVersionType.SYSTEM , TABLE_SYSTEM , FIELD_SYSTEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AppSystem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AppSystem.PROPERTY_DESC , FIELD_SYSTEM_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( AppSystem.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBooleanDatabaseOnly( FIELD_SYSTEM_MATCHED , "State of matched to core" , false , true )
		} ) );
	}

	public static PropertyEntity makeEntityDirectoryProduct( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT , DBEnumParamEntityType.APPPRODUCT , DBEnumObjectVersionType.SYSTEM , TABLE_PRODUCT , FIELD_PRODUCT_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_PRODUCT_SYSTEM_ID , "System" , DBEnumObjectType.APPSYSTEM , true ) ,
				EntityVar.metaString( AppProduct.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AppProduct.PROPERTY_DESC , FIELD_PRODUCT_DESC , "Description" , false , null ) ,
				EntityVar.metaString( AppProduct.PROPERTY_PATH , "Path" , true , null ) ,
				EntityVar.metaBoolean( AppProduct.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBooleanVar( AppProduct.PROPERTY_MONITORING_ENABLED , FIELD_PRODUCT_MONITORING_ENABLED , "Monitoring enabled" , false , false ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MAJOR_FIRST , FIELD_PRODUCT_LAST_MAJOR1 , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MAJOR_SECOND , FIELD_PRODUCT_LAST_MAJOR2 , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MINOR_FIRST , FIELD_PRODUCT_LAST_MINOR1 , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MINOR_SECOND , FIELD_PRODUCT_LAST_MINOR2 , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MAJOR_FIRST , FIELD_PRODUCT_NEXT_MAJOR1 , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MAJOR_SECOND , FIELD_PRODUCT_NEXT_MAJOR2 , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MINOR_FIRST , FIELD_PRODUCT_NEXT_MINOR1 , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MINOR_SECOND , FIELD_PRODUCT_NEXT_MINOR2 , "Minor next version, second number" , true , null )
		} ) );
	}

	public static PropertyEntity makeEntityProductPolicy( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_POLICY , DBEnumParamEntityType.PRODUCT_POLICY , DBEnumObjectVersionType.SYSTEM , TABLE_POLICY , FIELD_POLICY_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaBooleanVar( AppProductPolicy.PROPERTY_RELEASELC_URGENTANY , FIELD_POLICY_LCURGENTALL , "Any urgent lifecycle enabled" , true , false )
		} ) );
	}

	public static PropertyEntity makeEntityProductPolicyLifecycle( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppAssociativeEntity( DBEnumObjectType.META_POLICYCYCLE , DBEnumParamEntityType.PRODUCT_POLICYCYCLE , DBEnumObjectVersionType.SYSTEM , TABLE_POLICYCYCLE , false , 2 );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaObjectDatabaseOnly( FIELD_LIFECYCLE_PRODUCT , "product id" , DBEnumObjectType.META , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_LIFECYCLE_ID , "lifecycle id" , DBEnumObjectType.LIFECYCLE , false ) ,
		} ) );
	}

	public static PropertyEntity makeEntityDump( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT_DUMP , DBEnumParamEntityType.APPPRODUCT_DUMP , DBEnumObjectVersionType.SYSTEM , TABLE_DUMP , FIELD_DUMP_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMP_PRODUCT_ID , "product id" , DBEnumObjectType.APPPRODUCT , true ) ,
				EntityVar.metaString( ProductDump.PROPERTY_NAME , "name" , true , null ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_DESC , FIELD_DUMP_DESC , "description" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMP_SERVER_ID , "server id" , DBEnumObjectType.ENVIRONMENT_SERVER , true ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_ENV , FIELD_DUMP_FKENV , "environment name" , false , null ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_SEGMENT , FIELD_DUMP_FKSG , "segment name" , false , null ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_SERVER , FIELD_DUMP_FKSERVER , "server name" , false , null ) ,
				EntityVar.metaBooleanVar( ProductDump.PROPERTY_EXPORT , FIELD_DUMP_EXPORT , "export direction" , true , true ) ,
				EntityVar.metaString( ProductDump.PROPERTY_DATASET , "dataset folder" , true , null ) ,
				EntityVar.metaBoolean( ProductDump.PROPERTY_OWNTABLESET , "own table set" , true , false ) ,
				EntityVar.metaString( ProductDump.PROPERTY_DUMPDIR , "dump directory to put/get files" , false , null ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_SETDBENV , FIELD_DUMP_SETDBENV , "context setup script" , false , null ) ,
				EntityVar.metaStringVar( ProductDump.PROPERTY_DATAPUMPDIR , FIELD_DUMP_DATAPUMPDIR , "dump directory seen from database" , false , null ) ,
				EntityVar.metaString( ProductDump.PROPERTY_POSTREFRESH , "product data folder where there are postrefresh scripts to be applied after import" , false , null ) ,
				EntityVar.metaString( ProductDump.PROPERTY_SCHEDULE , "schedule to perform dump" , false , null ) ,
				EntityVar.metaBooleanVar( ProductDump.PROPERTY_STANDBY , FIELD_DUMP_STANDBY , "use stand by node to export dump" , true , false ) ,
				EntityVar.metaBooleanVar( ProductDump.PROPERTY_NFS , FIELD_DUMP_NFS , "assume nfs is used and avoid copy dump files" , true , false ) ,
				EntityVar.metaBoolean( ProductDump.PROPERTY_OFFLINE , "dump operation is offline" , true , true )
		} ) );
	}

	public static PropertyEntity makeEntityDumpMask( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT_DUMPMASK , DBEnumParamEntityType.APPPRODUCT_DUMPMASK , DBEnumObjectVersionType.SYSTEM , TABLE_DUMPMASK , FIELD_DUMPMASK_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMPMASK_PRODUCT_ID , "product id" , DBEnumObjectType.APPPRODUCT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMPMASK_DUMP_ID , "dump id" , DBEnumObjectType.APPPRODUCT_DUMP , true ) ,
				EntityVar.metaBooleanVar( ProductDumpMask.PROPERTY_INCLUDE , FIELD_DUMPMASK_INCLUDE , "if tables by mask are included" , true , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DUMPMASK_SCHEMA_ID , "database schema id" , DBEnumObjectType.DBSCHEMA , false ) ,
				EntityVar.metaStringVar( ProductDumpMask.PROPERTY_SCHEMA , FIELD_DUMPMASK_SCHEMA_NAME , "database schema name" , false , null ) ,
				EntityVar.metaStringVar( ProductDumpMask.PROPERTY_MASK , FIELD_DUMPMASK_MASK , "table mask" , false , null )
		} ) );
	}

	public static PropertyEntity makeEntityMonitoringTarget( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT_MONTARGET , DBEnumParamEntityType.APPPRODUCT_MONTARGET , DBEnumObjectVersionType.SYSTEM , TABLE_MONTARGET , FIELD_MONTARGET_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_PRODUCT_ID , "product id" , DBEnumObjectType.APPPRODUCT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONTARGET_SEGMENT_ID , "segment id" , DBEnumObjectType.ENVIRONMENT_SEGMENT , true ) ,
				EntityVar.metaStringVar( AppProductMonitoringTarget.PROPERTY_ENV , FIELD_MONTARGET_FKENV , "environment name" , true , null ) ,
				EntityVar.metaStringVar( AppProductMonitoringTarget.PROPERTY_SEGMENT , FIELD_MONTARGET_FKSG , "segment name" , true , null ) ,
				EntityVar.metaBooleanVar( AppProductMonitoringTarget.PROPERTY_MAJOR_ENABLED , FIELD_MONTARGET_MAJOR_ENABLED , "Enabled major monitoring" , true , false ) ,
				EntityVar.metaStringVar( AppProductMonitoringTarget.PROPERTY_MAJOR_SCHEDULE , FIELD_MONTARGET_MAJOR_SCHEDULE , "major schedule" , false , null ) ,
				EntityVar.metaIntegerVar( AppProductMonitoringTarget.PROPERTY_MAJOR_MAXTIME , FIELD_MONTARGET_MAJOR_MAXTIME , "major max time" , true , 300000 ) ,
				EntityVar.metaBooleanVar( AppProductMonitoringTarget.PROPERTY_MINOR_ENABLED , FIELD_MONTARGET_MINOR_ENABLED , "Enabled minor monitoring" , true , false ) ,
				EntityVar.metaStringVar( AppProductMonitoringTarget.PROPERTY_MINOR_SCHEDULE , FIELD_MONTARGET_MINOR_SCHEDULE , "minor schedule" , false , null ) ,
				EntityVar.metaIntegerVar( AppProductMonitoringTarget.PROPERTY_MINOR_MAXTIME , FIELD_MONTARGET_MINOR_MAXTIME , "minor max time" , true , 300000 ) ,
		} ) );
	}

	public static PropertyEntity makeEntityMonitoringItem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT_MONITEM , DBEnumParamEntityType.APPPRODUCT_MONITEM , DBEnumObjectVersionType.SYSTEM , TABLE_MONITEM , FIELD_MONITEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_PRODUCT_ID , "product id" , DBEnumObjectType.APPPRODUCT , true ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_MONITEM_TARGET_ID , "monitoring target id" , DBEnumObjectType.APPPRODUCT_MONTARGET , true ) ,
				EntityVar.metaStringVar( AppProductMonitoringItem.PROPERTY_DESC , FIELD_MONITEM_DESC , "description" , false , null ) ,
				EntityVar.metaEnumVar( AppProductMonitoringItem.PROPERTY_TYPE , FIELD_MONITEM_TYPE , "monitoring item type" , true , DBEnumMonItemType.UNKNOWN ) ,
				EntityVar.metaString( AppProductMonitoringItem.PROPERTY_URL , "check url" , false , null ) ,
				EntityVar.metaString( AppProductMonitoringItem.PROPERTY_WSDATA , "check request" , false , null ) ,
				EntityVar.metaString( AppProductMonitoringItem.PROPERTY_WSCHECK , "check request response" , false , null )
		} ) );
	}

	public static void dropEnvData( DBConnection c , ProductMeta storage ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		// dump data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDump , DBQueries.FILTER_DUMP_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDumpMask , DBQueries.FILTER_DUMP_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		// mon data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonItem , DBQueries.FILTER_MONTARGET_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonTarget , DBQueries.FILTER_MONTARGET_META1 , new String[] { EngineDB.getInteger( storage.ID ) } );
	}
	
	public static void dropEnvData( DBConnection c , MetaEnv env ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		// dump data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDump , DBQueries.FILTER_DUMP_ENV1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDumpMask , DBQueries.FILTER_DUMP_ENV1 , new String[] { EngineDB.getInteger( env.ID ) } );
		// mon data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonItem , DBQueries.FILTER_MONTARGET_ENV1 , new String[] { EngineDB.getInteger( env.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonTarget , DBQueries.FILTER_MONTARGET_ENV1 , new String[] { EngineDB.getInteger( env.ID ) } );
	}
	
	public static void dropSystemData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		
		boolean res = true;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_UNMATCHPRODUCTS0 ) : false;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMVALUES0 ) : false;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMS0 ) : false;
		if( !res )
			Common.exitUnexpected();
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDump );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDumpMask );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonItem );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonTarget );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicyLifecycle );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicy );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDirectoryProduct );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDirectorySystem );
	}
	
	public static void dropProductData( DBConnection c , AppProduct product ) throws Exception {
		EngineEntities entities = c.getEntities();
		
		// dump data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDump , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductDumpMask , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		// mon data
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonItem , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductMonTarget , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		// policy
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicyLifecycle , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicy , DBQueries.FILTER_PRODUCT_ID1 , new String[] { EngineDB.getInteger( product.ID ) } );
		
		DBAppProduct.deleteProduct( c , product );
	}
	
}
