package org.urm.db.release;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumLifecycleType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.Release;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;

public class DBReleaseData {

	public static String TABLE_REPOSITORY = "urm_rel_repository";
	public static String TABLE_MAIN = "urm_rel_main";
	public static String FIELD_REPOSITORY_ID = "repo_id";
	public static String FIELD_REPOSITORY_PRODUCT_ID = "product_fkid";
	public static String FIELD_REPOSITORY_PRODUCT_NAME = "product_fkname";
	public static String FIELD_REPOSITORY_MASTER_RELEASE_ID = "master_release_id";
	public static String FIELD_MAIN_ID = "release_id";
	public static String FIELD_MAIN_REPO_ID = "repo_id";
	public static String FIELD_MAIN_DESC = "xdesc";
	public static String FIELD_MAIN_LIFECYCLE = "lifecycle_type";
	public static String FIELD_MAIN_V1 = "v1";
	public static String FIELD_MAIN_V2 = "v2";
	public static String FIELD_MAIN_V3 = "v3";
	public static String FIELD_MAIN_V4 = "v4";
	
	public static PropertyEntity upgradeEntityReleaseRepository( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_REPOSITORY , DBEnumParamEntityType.RELEASE_REPOSITORY , DBEnumObjectVersionType.PRODUCT , TABLE_REPOSITORY , FIELD_REPOSITORY_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_REPOSITORY_PRODUCT_ID , "product id" , DBEnumObjectType.APPPRODUCT , false ) ,
				EntityVar.metaStringVar( DistRepository.PROPERTY_PRODUCT , FIELD_REPOSITORY_PRODUCT_NAME , DistRepository.PROPERTY_PRODUCT , "product name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_REPOSITORY_MASTER_RELEASE_ID , "master release id" , DBEnumObjectType.RELEASE_MAIN , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseRepository( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_REPOSITORY , DBEnumParamEntityType.RELEASE_REPOSITORY , DBEnumObjectVersionType.PRODUCT , TABLE_REPOSITORY , FIELD_REPOSITORY_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityReleaseMain( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_MAIN , DBEnumParamEntityType.RELEASE_MAIN , DBEnumObjectVersionType.RELEASE , TABLE_MAIN , FIELD_MAIN_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_MAIN_REPO_ID , "repo id" , DBEnumObjectType.RELEASE_REPOSITORY , true ) ,
				EntityVar.metaString( Release.PROPERTY_NAME , "product name" , true , null ) ,
				EntityVar.metaStringVar( Release.PROPERTY_DESC , FIELD_MAIN_DESC , Release.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_MASTER , "master" , true , false ) ,
				EntityVar.metaEnumVar( Release.PROPERTY_LIFECYCLE , FIELD_MAIN_LIFECYCLE , Release.PROPERTY_LIFECYCLE , "Lifecycle type" , true , DBEnumLifecycleType.UNKNOWN ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V1 , "version number 1" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V2 , "version number 2" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V3 , "version number 3" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_MAIN_V4 , "version number 4" , true , null ) ,
				EntityVar.metaString( Release.PROPERTY_VERSION , "release version" , true , null ) ,
				EntityVar.metaBoolean( Release.PROPERTY_ARCHIVEDSTATUS , "archived" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityReleaseMain( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.RELEASE_MAIN , DBEnumParamEntityType.RELEASE_MAIN , DBEnumObjectVersionType.RELEASE , TABLE_MAIN , FIELD_MAIN_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
}
