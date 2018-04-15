package org.urm.db.system;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.*;
import org.urm.db.engine.DBEngineEntities;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppProductPolicy;
import org.urm.meta.engine.AppSystem;

public class DBSystemData {

	public static String TABLE_SYSTEM = "urm_system";
	public static String TABLE_PRODUCT = "urm_product";
	public static String TABLE_POLICY = "urm_product_policy";
	public static String TABLE_POLICYCYCLE = "urm_product_lifecycle";
	public static String FIELD_SYSTEM_ID = "system_id";
	public static String FIELD_SYSTEM_DESC = "xdesc";
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
	
	public static PropertyEntity makeEntityDirectorySystem( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.APPSYSTEM , DBEnumObjectVersionType.SYSTEM , TABLE_SYSTEM , FIELD_SYSTEM_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AppSystem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AppSystem.PROPERTY_DESC , FIELD_SYSTEM_DESC , AppSystem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( AppSystem.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBoolean( AppSystem.PROPERTY_MATCHED , "State of matched to core" , false , true )
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
				EntityVar.metaStringVar( AppProduct.PROPERTY_DESC , FIELD_PRODUCT_DESC , AppProduct.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( AppProduct.PROPERTY_PATH , "Path" , true , null ) ,
				EntityVar.metaBoolean( AppProduct.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBooleanVar( AppProduct.PROPERTY_MONITORING_ENABLED , FIELD_PRODUCT_MONITORING_ENABLED , AppProduct.PROPERTY_MONITORING_ENABLED , "Monitoring enabled" , false , false ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MAJOR_FIRST , FIELD_PRODUCT_LAST_MAJOR1 , AppProduct.PROPERTY_LAST_MAJOR_FIRST , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MAJOR_SECOND , FIELD_PRODUCT_LAST_MAJOR2 , AppProduct.PROPERTY_LAST_MAJOR_SECOND , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MINOR_FIRST , FIELD_PRODUCT_LAST_MINOR1 , AppProduct.PROPERTY_LAST_MINOR_FIRST , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_LAST_MINOR_SECOND , FIELD_PRODUCT_LAST_MINOR2 , AppProduct.PROPERTY_LAST_MINOR_SECOND , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MAJOR_FIRST , FIELD_PRODUCT_NEXT_MAJOR1 , AppProduct.PROPERTY_NEXT_MAJOR_FIRST , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MAJOR_SECOND , FIELD_PRODUCT_NEXT_MAJOR2 , AppProduct.PROPERTY_NEXT_MAJOR_SECOND , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MINOR_FIRST , FIELD_PRODUCT_NEXT_MINOR1 , AppProduct.PROPERTY_NEXT_MINOR_FIRST , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerVar( AppProduct.PROPERTY_NEXT_MINOR_SECOND , FIELD_PRODUCT_NEXT_MINOR2 , AppProduct.PROPERTY_NEXT_MINOR_SECOND , "Minor next version, second number" , true , null )
		} ) );
	}

	public static PropertyEntity makeEntityProductPolicy( DBConnection c , boolean upgrade ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_POLICY , DBEnumParamEntityType.PRODUCT_POLICY , DBEnumObjectVersionType.SYSTEM , TABLE_POLICY , FIELD_POLICY_ID , false );
		if( !upgrade ) {
			DBSettings.loaddbAppEntity( c , entity );
			return( entity );
		}
		
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaBooleanVar( AppProductPolicy.PROPERTY_RELEASELC_URGENTANY , FIELD_POLICY_LCURGENTALL , AppProductPolicy.PROPERTY_RELEASELC_URGENTANY , "Any urgent lifecycle enabled" , true , false )
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

	public static void dropSystemData( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = c.getEntities();
		boolean res = true;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_UNMATCHPRODUCTS0 ) : false;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMVALUES0 ) : false;
		res = ( res )? c.modify( DBQueries.MODIFY_APP_DROP_SYSTEMPARAMS0 ) : false;
		if( !res )
			Common.exitUnexpected();
		
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicyLifecycle );
		DBEngineEntities.dropAppObjects( c , entities.entityAppProductPolicy );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDirectoryProduct );
		DBEngineEntities.dropAppObjects( c , entities.entityAppDirectorySystem );
	}
	
}
