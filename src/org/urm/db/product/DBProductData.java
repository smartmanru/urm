package org.urm.db.product;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;

public class DBProductData {

	public static String TABLE_META = "urm_product_meta";
	public static String FIELD_META_ID = "meta_id";
	public static String FIELD_META_PRODUCT_ID = "product_id";
	public static String FIELD_META_PRODUCT_NAME = "name";
	public static String FIELD_META_PRODUCT_MATCHED = "matched";
	public static String FIELD_META_LAST_MAJOR1 = "last_major1";
	public static String FIELD_META_LAST_MAJOR2 = "last_major2";
	public static String FIELD_META_LAST_MINOR1 = "last_minor1";
	public static String FIELD_META_LAST_MINOR2 = "last_minor2";
	public static String FIELD_META_NEXT_MAJOR1 = "next_major1";
	public static String FIELD_META_NEXT_MAJOR2 = "next_major2";
	public static String FIELD_META_NEXT_MINOR1 = "next_minor1";
	public static String FIELD_META_NEXT_MINOR2 = "next_minor2";
	
	public static PropertyEntity upgradeEntityMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT , DBEnumObjectVersionType.PRODUCT , TABLE_META , FIELD_META_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_PRODUCT_ID , "Application product id" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_NAME , "Application product name" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_MATCHED , "Product match status" , false , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR1 , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR2 , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR1 , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR2 , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR1 , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR2 , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR1 , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR2 , "Minor next version, second number" , true , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMeta( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT , DBEnumObjectVersionType.PRODUCT , TABLE_META , FIELD_META_ID );
		DBSettings.loaddbEntity( c , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaCoreSettings( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_CORESETTINGS , DBEnumObjectVersionType.PRODUCT );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_PRODUCT_ID , "Application product id" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_NAME , "Application product name" , false , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR1 , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR2 , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR1 , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR2 , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR1 , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR2 , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR1 , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR2 , "Minor next version, second number" , true , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaCoreSettings( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_CORESETTINGS , DBEnumObjectVersionType.PRODUCT );
		DBSettings.loaddbEntity( c , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void dropProductData( EngineLoader loader ) throws Exception {
		dropProductReleasesData( loader );
		dropProductDesignData( loader );
		dropProductEnvData( loader );
		dropProductCoreData( loader );
	}

	public static void dropProductReleasesData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductDesignData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductEnvData( EngineLoader loader ) throws Exception {
	}
	
	public static void dropProductCoreData( EngineLoader loader ) throws Exception {
	}

}
