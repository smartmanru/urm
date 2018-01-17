package org.urm.db.product;

import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;

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
	
	public static PropertyEntity upgradeEntityProductSettings( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTDEFS , DBEnumObjectVersionType.UNKNOWN );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_REDISTLINUX_PATH , "Linux Staging Area Path" , true , null , DBEnumOSType.LINUX ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_REDISTWIN_PATH , "Windows Staging Area Path" , true , null , DBEnumOSType.WINDOWS ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_DISTR_PATH , "Distributives Path" , true , null , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_DISTR_HOSTLOGIN , "Distributives host@login" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_UPGRADE_PATH , "Upgrade Scripts Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_BASE_PATH , "Platform Software Path" , false , null , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_MIRRORPATH , "Mirror Repositories" , false , null , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_ADM_TRACKER , "Codebase Control Tracker" , false , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_COMMIT_TRACKERLIST , "Source Task Trackers" , false , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_SOURCE_CHARSET , "Release Source Charset" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductCoreSettings.PROPERTY_SOURCE_RELEASEROOTDIR , "Release Source Root" , false , null , null ) ,
				EntityVar.metaPathRelative( MetaProductCoreSettings.PROPERTY_SOURCE_CFG_ROOTDIR , "Configuration Root" , false , null , null ) ,
				EntityVar.metaPathRelative( MetaProductCoreSettings.PROPERTY_SOURCE_CFG_LIVEROOTDIR , "Configuration Live" , false , null , null ) ,
				EntityVar.metaPathRelative( MetaProductCoreSettings.PROPERTY_SOURCE_SQL_POSTREFRESH , "Database PostRefresh" , false , null , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_CUSTOM_BUILD , "Custom Builder Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_CUSTOM_DEPLOY , "Custom Deployer Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_CUSTOM_DATABASE , "Custom Database Plugin" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityProductSettings( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTDEFS , DBEnumObjectVersionType.UNKNOWN );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityProductBuild( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTBUILD , DBEnumObjectVersionType.UNKNOWN );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMAJOR , "Last Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMAJOR , "Next Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMINOR , "Last Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMINOR , "Next Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_VERSION , "Release Build Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_APPVERSION , "Artefacts Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_BRANCHNAME , "Source Branch Name" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_GROUPFOLDER , "Release Source Group" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_ARTEFACTDIR , "Artefacts Directory" , false , null , null ) ,
				EntityVar.metaPathRelative( MetaProductBuildSettings.PROPERTY_LOGPATH , "Build Log Path" , false , null , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO , "Nexus Repository" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO_THIRDPARTY , "Nexus Thirdparty Repository" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_MAVEN_CFGFILE , "Maven Settings" , false , null , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityProductBuild( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTBUILD , DBEnumObjectVersionType.UNKNOWN );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityProductContext( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTCTX , DBEnumObjectVersionType.UNKNOWN );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaProductSettings.PROPERTY_PRODUCT_NAME , "Product Name" , true , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_PRODUCT_HOME , "Product Home" , true , null , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_LAST_MAJOR_FIRST , "Version Major Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_LAST_MAJOR_SECOND , "Version Minor Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_NEXT_MAJOR_FIRST , "Next Major Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_NEXT_MAJOR_SECOND , "Next Minor Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_LAST_MINOR_FIRST , "Last Tag Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_LAST_MINOR_SECOND , "Last Urgent Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_NEXT_MINOR_FIRST , "Next Tag Number" , false , null ) ,
				EntityVar.metaInteger( MetaProductSettings.PROPERTY_NEXT_MINOR_SECOND , "Next Urgent Number" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityProductContext( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTCTX , DBEnumObjectVersionType.UNKNOWN );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMeta( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT , DBEnumObjectVersionType.PRODUCT , TABLE_META , FIELD_META_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_PRODUCT_ID , "Application product id" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_NAME , "Application product name" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_MATCHED , "Product match status" , false , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR1 , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MAJOR2 , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR1 , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_LAST_MINOR2 , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR1 , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MAJOR2 , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR1 , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_NEXT_MINOR2 , "Minor next version, second number" , true , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMeta( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT , DBEnumObjectVersionType.PRODUCT , TABLE_META , FIELD_META_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}

	public static PropertyEntity upgradeEntityMetaVersion( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_VERSION , DBEnumObjectVersionType.PRODUCT );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_LAST_MAJOR_FIRST , "Major last version, first number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_LAST_MAJOR_SECOND , "Major last version, last number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_LAST_MINOR_FIRST , "Minor last version, first number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_LAST_MINOR_SECOND , "Minor last version, last number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_NEXT_MAJOR_FIRST , "Major next version, first number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_NEXT_MAJOR_SECOND , "Major next version, last number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_NEXT_MINOR_FIRST , "Minor next version, first number" , true , null ) ,
				EntityVar.metaIntegerXmlOnly( MetaProductSettings.PROPERTY_NEXT_MINOR_SECOND , "Minor next version, second number" , true , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaVersion( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_VERSION , DBEnumObjectVersionType.PRODUCT );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaMonitoring( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_MONITORING , DBEnumObjectVersionType.PRODUCT );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaProductCoreSettings.PROPERTY_MONITORING_RESOURCE_URL , "Monitoring Resources URL" , true , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_RES , "Monitoring Resources Path" , true , null , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_DATA , "Monitoring Database Path" , true , null , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_REPORTS , "Monitoring Reports Path" , true , null , null ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_LOGS , "Monitoring Logs" , true , null , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaMonitoring( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.META , DBEnumParamEntityType.PRODUCT_MONITORING , DBEnumObjectVersionType.PRODUCT );
		DBSettings.loaddbAppEntity( c , entity );
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
