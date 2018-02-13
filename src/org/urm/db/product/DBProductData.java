package org.urm.db.product;

import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBQueries;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.db.core.DBSettings;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.ProductMeta;

public class DBProductData {

	public static String TABLE_META = "urm_product_meta";
	public static String TABLE_UNIT = "urm_product_unit";
	public static String TABLE_SCHEMA = "urm_product_schema";
	public static String TABLE_SOURCESET = "urm_source_set";
	public static String TABLE_SOURCEPROJECT = "urm_source_project";
	public static String TABLE_SOURCEITEM = "urm_source_item";
	public static String TABLE_DOC = "urm_product_doc";
	public static String TABLE_POLICY = "urm_product_policy";
	public static String TABLE_DELIVERY = "urm_dist_delivery";
	public static String TABLE_BINARYITEM = "urm_dist_binaryitem";
	public static String TABLE_CONFITEM = "urm_dist_confitem";
	public static String TABLE_COMPONENT = "urm_dist_comp";
	public static String TABLE_COMPITEM = "urm_dist_compitem";
	public static String FIELD_META_ID = "meta_id";
	public static String FIELD_META_PRODUCT_ID = "product_fkid";
	public static String FIELD_META_PRODUCT_NAME = "product_fkname";
	public static String FIELD_META_PRODUCT_MATCHED = "matched";
	public static String FIELD_META_LAST_MAJOR1 = "last_major1";
	public static String FIELD_META_LAST_MAJOR2 = "last_major2";
	public static String FIELD_META_LAST_MINOR1 = "last_minor1";
	public static String FIELD_META_LAST_MINOR2 = "last_minor2";
	public static String FIELD_META_NEXT_MAJOR1 = "next_major1";
	public static String FIELD_META_NEXT_MAJOR2 = "next_major2";
	public static String FIELD_META_NEXT_MINOR1 = "next_minor1";
	public static String FIELD_META_NEXT_MINOR2 = "next_minor2";
	public static String FIELD_UNIT_ID = "unit_id";
	public static String FIELD_UNIT_DESC = "xdesc";
	public static String FIELD_SCHEMA_ID = "schema_id";
	public static String FIELD_SCHEMA_DESC = "xdesc";
	public static String FIELD_SCHEMA_DBTYPE = "dbms_type";
	public static String FIELD_SOURCESET_ID = "srcset_id";
	public static String FIELD_SOURCESET_DESC = "xdesc";
	public static String FIELD_SOURCEPROJECT_ID = "project_id";
	public static String FIELD_SOURCEPROJECT_DESC = "xdesc";
	public static String FIELD_SOURCEPROJECT_SET_ID = "srcset_id";
	public static String FIELD_SOURCEPROJECT_POS = "project_pos";
	public static String FIELD_SOURCEPROJECT_TYPE = "project_type";
	public static String FIELD_SOURCEPROJECT_PROD = "codebase_prod";
	public static String FIELD_SOURCEPROJECT_UNIT_ID = "unit_id";
	public static String FIELD_SOURCEPROJECT_BUILDER_ID = "builder_fkid";
	public static String FIELD_SOURCEPROJECT_BUILDER_NAME = "builder_fkname";
	public static String FIELD_SOURCEPROJECT_BUILDOPTIONS = "builder_options";
	public static String FIELD_SOURCEPROJECT_MIRROR_ID = "mirror_fkid";
	public static String FIELD_SOURCEPROJECT_MIRRORRES = "mirror_fkresource";
	public static String FIELD_SOURCEPROJECT_MIRRORREPO = "mirror_fkrepository";
	public static String FIELD_SOURCEPROJECT_MIRRORPATH = "mirror_fkrepopath";
	public static String FIELD_SOURCEPROJECT_MIRRORDATA = "mirror_fkcodepath";
	public static String FIELD_SOURCEITEM_ID = "srcitem_id";
	public static String FIELD_SOURCEITEM_DESC = "xdesc";
	public static String FIELD_SOURCEITEM_PROJECT_ID = "project_id";
	public static String FIELD_SOURCEITEM_TYPE = "sourceitem_type";
	public static String FIELD_SOURCEITEM_EXT = "ext";
	public static String FIELD_SOURCEITEM_STATICEXT = "staticext";
	public static String FIELD_SOURCEITEM_PATH = "artefact_path";
	public static String FIELD_SOURCEITEM_VERSION = "fixed_version";
	public static String FIELD_SOURCEITEM_NODIST = "nodist";
	public static String FIELD_DOC_ID = "doc_id";
	public static String FIELD_DOC_DESC = "xdesc";
	public static String FIELD_DOC_CATEGORY = "doccategory_type";
	public static String FIELD_DOC_EXT = "ext";
	public static String FIELD_POLICY_ID = "meta_id";
	public static String FIELD_POLICY_LCURGENTALL = "lcurgent_any";
	public static String FIELD_DELIVERY_ID = "delivery_id";
	public static String FIELD_DELIVERY_UNIT_ID = "unit_id";
	public static String FIELD_DELIVERY_DESC = "xdesc";
	public static String FIELD_BINARYITEM_ID = "binary_id";
	public static String FIELD_BINARYITEM_DESC = "xdesc";
	public static String FIELD_BINARYITEM_DISTITEMTYPE = "distitem_type";
	public static String FIELD_BINARYITEM_DISTNAME = "basename_dist";
	public static String FIELD_BINARYITEM_DEPLOYNAME = "basename_deploy";
	public static String FIELD_BINARYITEM_EXT = "ext";
	public static String FIELD_BINARYITEM_DEPLOYVERSIONTYPE = "deployversion_type";
	public static String FIELD_BINARYITEM_ITEMORIGIN = "itemorigin_type";
	public static String FIELD_BINARYITEM_SRCITEM_ID = "srcitem_id";
	public static String FIELD_BINARYITEM_SRCDISTITEM_ID = "src_binary_id";
	public static String FIELD_BINARYITEM_SRCITEMPATH = "src_itempath";
	public static String FIELD_BINARYITEM_ARCHIVEFILES = "archive_files";
	public static String FIELD_BINARYITEM_ARCHIVEEXCLUDE = "archive_exclude";
	public static String FIELD_BINARYITEM_WARSTATICEXT = "war_staticext";
	public static String FIELD_BINARYITEM_WARCONTEXT = "war_context";
	public static String FIELD_CONFITEM_ID = "confitem_id";
	public static String FIELD_CONFITEM_DESC = "xdesc";
	public static String FIELD_CONFITEM_TYPE = "confitem_type";
	public static String FIELD_COMPONENT_ID = "comp_id";
	public static String FIELD_COMPONENT_DESC = "xdesc";
	public static String FIELD_COMPITEM_ID = "compitem_id";
	public static String FIELD_COMPITEM_COMPID = "comp_id";
	public static String FIELD_COMPITEM_TYPE = "compitem_type";
	public static String FIELD_COMPITEM_BINARY_ID = "binary_id";
	public static String FIELD_COMPITEM_CONF_ID = "confitem_id";
	public static String FIELD_COMPITEM_SCHEMA_ID = "schema_id";
	public static String FIELD_COMPITEM_DEPLOYNAME = "deploy_name";
	public static String FIELD_COMPITEM_WSDL = "wsdl_request";
	
	public static PropertyEntity upgradeEntityProductSettings( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.UNKNOWN , DBEnumParamEntityType.PRODUCTDEFS , DBEnumObjectVersionType.UNKNOWN );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_REDISTLINUX_PATH , "Linux Staging Area Path" , true , null , DBEnumOSType.LINUX ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_REDISTWIN_PATH , "Windows Staging Area Path" , true , null , DBEnumOSType.WINDOWS ) ,
				EntityVar.metaPathAbsolute( MetaProductCoreSettings.PROPERTY_DISTR_PATH , "Distributives Path" , true , null , null ) ,
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
				EntityVar.metaObjectDatabaseOnly( FIELD_META_PRODUCT_ID , "Application product id" , DBEnumObjectType.APPPRODUCT , false ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_META_PRODUCT_NAME , "Application product name" , false , null ) ,
				EntityVar.metaBooleanDatabaseOnly( FIELD_META_PRODUCT_MATCHED , "Product match status" , false , false ) ,
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

	public static PropertyEntity upgradeEntityMetaPolicy( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_POLICY , DBEnumParamEntityType.PRODUCT_POLICY , DBEnumObjectVersionType.PRODUCT , TABLE_POLICY , FIELD_POLICY_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaBooleanVar( MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY , FIELD_POLICY_LCURGENTALL , MetaProductPolicy.PROPERTY_RELEASELC_URGENTANY , "Any urgent lifecycle enabled" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaPolicy( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_POLICY , DBEnumParamEntityType.PRODUCT_POLICY , DBEnumObjectVersionType.PRODUCT , TABLE_POLICY , FIELD_POLICY_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaUnit( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_UNIT , DBEnumParamEntityType.PRODUCT_UNIT , DBEnumObjectVersionType.PRODUCT , TABLE_UNIT , FIELD_UNIT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaString( MetaProductUnit.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaProductUnit.PROPERTY_DESC , FIELD_UNIT_DESC , MetaProductUnit.PROPERTY_DESC , "Description" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaUnit( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_UNIT , DBEnumParamEntityType.PRODUCT_UNIT , DBEnumObjectVersionType.PRODUCT , TABLE_UNIT , FIELD_UNIT_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaSchema( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SCHEMA , DBEnumParamEntityType.PRODUCT_SCHEMA , DBEnumObjectVersionType.PRODUCT , TABLE_SCHEMA , FIELD_SCHEMA_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaString( MetaDatabaseSchema.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaDatabaseSchema.PROPERTY_DESC , FIELD_SCHEMA_DESC , MetaDatabaseSchema.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaDatabaseSchema.PROPERTY_DBTYPE , FIELD_SCHEMA_DBTYPE , MetaDatabaseSchema.PROPERTY_DBTYPE , "Database software type" , true , DBEnumDbmsType.UNKNOWN ) ,
				EntityVar.metaString( MetaDatabaseSchema.PROPERTY_DBNAME , "Default database name" , false , null ) ,
				EntityVar.metaString( MetaDatabaseSchema.PROPERTY_DBUSER , "Default database user" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaSchema( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SCHEMA , DBEnumParamEntityType.PRODUCT_SCHEMA , DBEnumObjectVersionType.PRODUCT , TABLE_SCHEMA , FIELD_SCHEMA_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaSourceSet( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCESET , DBEnumParamEntityType.PRODUCT_SOURCESET , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCESET , FIELD_SOURCESET_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaString( MetaSourceProjectSet.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectSet.PROPERTY_DESC , FIELD_SOURCESET_DESC , MetaSourceProjectSet.PROPERTY_DESC , "Description" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaSourceSet( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCESET , DBEnumParamEntityType.PRODUCT_SOURCESET , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCESET , FIELD_SOURCESET_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaSourceProject( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCEPROJECT , DBEnumParamEntityType.PRODUCT_SOURCEPROJECT , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCEPROJECT , FIELD_SOURCEPROJECT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SOURCEPROJECT_SET_ID , "Source set" , DBEnumObjectType.META_SOURCESET , true ) ,
				EntityVar.metaString( MetaSourceProject.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_DESC , FIELD_SOURCEPROJECT_DESC , MetaSourceProject.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaIntegerVar( MetaSourceProject.PROPERTY_PROJECTPOS , FIELD_SOURCEPROJECT_POS , MetaSourceProject.PROPERTY_PROJECTPOS , "Source set" , true , null ) ,
				EntityVar.metaEnumVar( MetaSourceProject.PROPERTY_PROJECTTYPE , FIELD_SOURCEPROJECT_TYPE , MetaSourceProject.PROPERTY_PROJECTTYPE , "Source project type" , true , DBEnumProjectType.UNKNOWN ) ,
				EntityVar.metaBooleanVar( MetaSourceProject.PROPERTY_PROD , FIELD_SOURCEPROJECT_PROD , MetaSourceProject.PROPERTY_PROD , "Use in production build" , true , false ) ,
				EntityVar.metaStringXmlOnly( MetaSourceProject.PROPERTY_UNIT , "Source project unit name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SOURCEPROJECT_UNIT_ID , "Source project unit id" , DBEnumObjectType.META_UNIT , false ) ,
				EntityVar.metaString( MetaSourceProject.PROPERTY_TRACKER , "Ticket management project name" , false , null ) ,
				EntityVar.metaString( MetaSourceProject.PROPERTY_BRANCH , "Default production branch" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SOURCEPROJECT_BUILDER_ID , "Source project builder" , DBEnumObjectType.BUILDER , false ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_BUILDER_NAME , FIELD_SOURCEPROJECT_BUILDER_NAME , MetaSourceProject.PROPERTY_BUILDER_NAME , "Source project builder name" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_BUILDER_OPTIONS , FIELD_SOURCEPROJECT_BUILDOPTIONS , MetaSourceProject.PROPERTY_BUILDER_OPTIONS , "Additional build options" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SOURCEPROJECT_MIRROR_ID , "Source project mirror id" , DBEnumObjectType.MIRROR , false ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_MIRRORRES , FIELD_SOURCEPROJECT_MIRRORRES , MetaSourceProject.PROPERTY_MIRRORRES , "Respository resource" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_MIRRORREPO , FIELD_SOURCEPROJECT_MIRRORREPO , MetaSourceProject.PROPERTY_MIRRORREPO , "Respository name" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_MIRRORPATH , FIELD_SOURCEPROJECT_MIRRORPATH , MetaSourceProject.PROPERTY_MIRRORPATH , "Path to respository" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProject.PROPERTY_MIRRORDATA , FIELD_SOURCEPROJECT_MIRRORDATA , MetaSourceProject.PROPERTY_MIRRORDATA , "Respository path to data" , false , null ) ,
				EntityVar.metaBoolean( MetaSourceProject.PROPERTY_CUSTOM_BUILD , "Build using custom plugin" , false , false ) ,
				EntityVar.metaBoolean( MetaSourceProject.PROPERTY_CUSTOM_GET , "Get artefacts using custom plugin" , false , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaSourceProject( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCEPROJECT , DBEnumParamEntityType.PRODUCT_SOURCEPROJECT , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCEPROJECT , FIELD_SOURCEPROJECT_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaSourceItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCEITEM , DBEnumParamEntityType.PRODUCT_SOURCEITEM , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCEITEM , FIELD_SOURCEITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_SOURCEITEM_PROJECT_ID , "Source project" , DBEnumObjectType.META_SOURCEPROJECT , true ) ,
				EntityVar.metaString( MetaSourceProjectItem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectItem.PROPERTY_DESC , FIELD_SOURCEITEM_DESC , MetaSourceProjectItem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaSourceProjectItem.PROPERTY_SRCTYPE , FIELD_SOURCEITEM_TYPE , MetaSourceProjectItem.PROPERTY_SRCTYPE , "Source project item type" , true , DBEnumSourceItemType.UNKNOWN ) ,
				EntityVar.metaString( MetaSourceProjectItem.PROPERTY_BASENAME , "Source item basename" , true , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectItem.PROPERTY_EXT , FIELD_SOURCEITEM_EXT , MetaSourceProjectItem.PROPERTY_EXT , "Item extension" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectItem.PROPERTY_STATICEXT , FIELD_SOURCEITEM_STATICEXT , MetaSourceProjectItem.PROPERTY_STATICEXT , "Item static extension" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectItem.PROPERTY_PATH , FIELD_SOURCEITEM_PATH , MetaSourceProjectItem.PROPERTY_PATH , "Item artefact path" , false , null ) ,
				EntityVar.metaStringVar( MetaSourceProjectItem.PROPERTY_VERSION , FIELD_SOURCEITEM_VERSION , MetaSourceProjectItem.PROPERTY_VERSION , "Item artefact fixed version" , false , null ) ,
				EntityVar.metaBooleanVar( MetaSourceProjectItem.PROPERTY_NODIST , FIELD_SOURCEITEM_NODIST , MetaSourceProjectItem.PROPERTY_NODIST , "Internal item flag" , false , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaSourceItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_SOURCEITEM , DBEnumParamEntityType.PRODUCT_SOURCEITEM , DBEnumObjectVersionType.PRODUCT , TABLE_SOURCEITEM , FIELD_SOURCEITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDoc( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DOC , DBEnumParamEntityType.PRODUCT_DOC , DBEnumObjectVersionType.PRODUCT , TABLE_DOC , FIELD_DOC_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaString( MetaProductDoc.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaProductDoc.PROPERTY_DESC , FIELD_DOC_DESC , MetaProductDoc.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaProductDoc.PROPERTY_CATEGORY , FIELD_DOC_CATEGORY , MetaProductDoc.PROPERTY_CATEGORY , "Document category" , true , DBEnumDocCategoryType.UNKNOWN ) ,
				EntityVar.metaStringVar( MetaProductDoc.PROPERTY_EXT , FIELD_DOC_EXT , MetaProductDoc.PROPERTY_EXT , "Document extension" , true , null ) ,
				EntityVar.metaBoolean( MetaProductDoc.PROPERTY_UNITBOUND , "Document type can have separate instance for every unit" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDoc( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DOC , DBEnumParamEntityType.PRODUCT_DOC , DBEnumObjectVersionType.PRODUCT , TABLE_DOC , FIELD_DOC_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDistrDelivery( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_DELIVERY , DBEnumParamEntityType.PRODUCT_DIST_DELIVERY , DBEnumObjectVersionType.PRODUCT , TABLE_DELIVERY , FIELD_DELIVERY_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DELIVERY_UNIT_ID , "delivery unit id" , DBEnumObjectType.META_UNIT , false ) ,
				EntityVar.metaStringXmlOnly( MetaDistrDelivery.PROPERTY_UNIT_NAME , "delivery unit name" , false , null ) ,
				EntityVar.metaString( MetaDistrDelivery.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrDelivery.PROPERTY_DESC , FIELD_DELIVERY_DESC , MetaDistrDelivery.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( MetaDistrDelivery.PROPERTY_FOLDER , "Distributive folder" , true , null ) ,
				EntityVar.metaBoolean( MetaDistrDelivery.PROPERTY_SCHEMA_ANY , "Any database schema allowed" , true , false ) ,
				EntityVar.metaBoolean( MetaDistrDelivery.PROPERTY_DOC_ANY , "Any database document allowed" , true , false )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDistrDelivery( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_DELIVERY , DBEnumParamEntityType.PRODUCT_DIST_DELIVERY , DBEnumObjectVersionType.PRODUCT , TABLE_DELIVERY , FIELD_DELIVERY_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDistrBinaryItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_BINARYITEM , DBEnumParamEntityType.PRODUCT_DIST_BINARYITEM , DBEnumObjectVersionType.PRODUCT , TABLE_BINARYITEM , FIELD_BINARYITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DELIVERY_ID , "delivery id" , DBEnumObjectType.META_DIST_DELIVERY , true ) ,
				EntityVar.metaString( MetaDistrBinaryItem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_DESC , FIELD_BINARYITEM_DESC , MetaDistrBinaryItem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaDistrBinaryItem.PROPERTY_DISTITEMTYPE , FIELD_BINARYITEM_DISTITEMTYPE , MetaDistrBinaryItem.PROPERTY_DISTITEMTYPE , "Distributive item type" , true , DBEnumBinaryItemType.UNKNOWN ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_DISTNAME , FIELD_BINARYITEM_DISTNAME , MetaDistrBinaryItem.PROPERTY_DISTNAME , "Distribute base name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_DEPLOYNAME , FIELD_BINARYITEM_DEPLOYNAME , MetaDistrBinaryItem.PROPERTY_DEPLOYNAME , "Default deployment base name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_EXT , FIELD_BINARYITEM_EXT , MetaDistrBinaryItem.PROPERTY_EXT , "Item file extension" , true , null ) ,
				EntityVar.metaEnumVar( MetaDistrBinaryItem.PROPERTY_DEPLOYVERSIONTYPE , FIELD_BINARYITEM_DEPLOYVERSIONTYPE , MetaDistrBinaryItem.PROPERTY_DEPLOYVERSIONTYPE , "Deployment name version type" , false , DBEnumDeployVersionType.UNKNOWN ) ,
				EntityVar.metaEnumVar( MetaDistrBinaryItem.PROPERTY_ITEMORIGIN , FIELD_BINARYITEM_ITEMORIGIN , MetaDistrBinaryItem.PROPERTY_ITEMORIGIN , "Item origin type" , true , DBEnumItemOriginType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_BINARYITEM_SRCITEM_ID , "source project item id" , DBEnumObjectType.META_SOURCEITEM , false ) ,
				EntityVar.metaStringXmlOnly( MetaDistrBinaryItem.PROPERTY_SRCITEM_NAME , "source project item name" , false , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_BINARYITEM_SRCDISTITEM_ID , "source distributive item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaStringXmlOnly( MetaDistrBinaryItem.PROPERTY_SRCDISTITEM_NAME , "source distributive item name" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_SRCITEMPATH , FIELD_BINARYITEM_SRCITEMPATH , MetaDistrBinaryItem.PROPERTY_SRCITEMPATH , "Source item path" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_ARCHIVEFILES , FIELD_BINARYITEM_ARCHIVEFILES , MetaDistrBinaryItem.PROPERTY_ARCHIVEFILES , "Archive item files" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_ARCHIVEEXCLUDE , FIELD_BINARYITEM_ARCHIVEEXCLUDE , MetaDistrBinaryItem.PROPERTY_ARCHIVEEXCLUDE , "Archive item exclude files" , false , null ) ,
				EntityVar.metaStringDatabaseOnly( FIELD_BINARYITEM_WARSTATICEXT , "Static file extension" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrBinaryItem.PROPERTY_WARCONTEXT , FIELD_BINARYITEM_WARCONTEXT , MetaDistrBinaryItem.PROPERTY_WARCONTEXT , "War file context" , false , null ) ,
				EntityVar.metaBoolean( MetaDistrBinaryItem.PROPERTY_CUSTOMGET , "Use custom method to get" , false , false ) ,
				EntityVar.metaBoolean( MetaDistrBinaryItem.PROPERTY_CUSTOMDEPLOY , "Use custom method to deploy" , false , false ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDistrBinaryItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_BINARYITEM , DBEnumParamEntityType.PRODUCT_DIST_BINARYITEM , DBEnumObjectVersionType.PRODUCT , TABLE_BINARYITEM , FIELD_BINARYITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDistrConfItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_CONFITEM , DBEnumParamEntityType.PRODUCT_DIST_CONFITEM , DBEnumObjectVersionType.PRODUCT , TABLE_CONFITEM , FIELD_CONFITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_DELIVERY_ID , "delivery id" , DBEnumObjectType.META_DIST_DELIVERY , true ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrConfItem.PROPERTY_DESC , FIELD_CONFITEM_DESC , MetaDistrConfItem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaEnumVar( MetaDistrConfItem.PROPERTY_TYPE , FIELD_CONFITEM_TYPE , MetaDistrConfItem.PROPERTY_TYPE , "Configuration item type" , true , DBEnumConfItemType.UNKNOWN ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_FILES , "Any files" , false , null ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_TEMPLATES , "Public templates only" , false , null ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_SECURED , "Secured files only" , false , null ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_EXCLUDE , "Excluded from files" , false , null ) ,
				EntityVar.metaString( MetaDistrConfItem.PROPERTY_EXTCONF , "Template files extensions" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDistrConfItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_CONFITEM , DBEnumParamEntityType.PRODUCT_DIST_CONFITEM , DBEnumObjectVersionType.PRODUCT , TABLE_CONFITEM , FIELD_CONFITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDistrComponent( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_COMPONENT , DBEnumParamEntityType.PRODUCT_DIST_COMPONENT , DBEnumObjectVersionType.PRODUCT , TABLE_COMPONENT , FIELD_COMPONENT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaString( MetaDistrComponent.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( MetaDistrComponent.PROPERTY_DESC , FIELD_COMPONENT_DESC , MetaDistrComponent.PROPERTY_DESC , "Description" , false , null ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDistrComponent( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_COMPONENT , DBEnumParamEntityType.PRODUCT_DIST_COMPONENT , DBEnumObjectVersionType.PRODUCT , TABLE_COMPONENT , FIELD_COMPONENT_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityMetaDistrCompItem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_COMPITEM , DBEnumParamEntityType.PRODUCT_DIST_COMPITEM , DBEnumObjectVersionType.PRODUCT , TABLE_COMPITEM , FIELD_COMPITEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] {
				EntityVar.metaIntegerDatabaseOnly( FIELD_META_ID , "product meta" , true , null ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_COMPITEM_COMPID , "component id" , DBEnumObjectType.META_DIST_COMPITEM , true ) ,
				EntityVar.metaEnumDatabaseOnly( FIELD_COMPITEM_TYPE , "component item type" , true , DBEnumCompItemType.UNKNOWN ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_COMPITEM_BINARY_ID , "binary item id" , DBEnumObjectType.META_DIST_BINARYITEM , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_COMPITEM_CONF_ID , "configuration item id" , DBEnumObjectType.META_DIST_CONFITEM , false ) ,
				EntityVar.metaObjectDatabaseOnly( FIELD_COMPITEM_SCHEMA_ID , "schema item id" , DBEnumObjectType.META_SCHEMA , false ) ,
				EntityVar.metaStringXmlOnly( MetaDistrComponentItem.PROPERTY_NAME , "item name" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrComponentItem.PROPERTY_DEPLOYNAME , FIELD_COMPITEM_DEPLOYNAME , MetaDistrComponentItem.PROPERTY_DEPLOYNAME , "Deployment name" , false , null ) ,
				EntityVar.metaStringVar( MetaDistrComponentItem.PROPERTY_WSDL , FIELD_COMPITEM_WSDL , MetaDistrComponentItem.PROPERTY_WSDL , "Wsdl base address" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityMetaDistrCompItem( DBConnection c ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.META_DIST_COMPITEM , DBEnumParamEntityType.PRODUCT_DIST_COMPITEM , DBEnumObjectVersionType.PRODUCT , TABLE_COMPITEM , FIELD_COMPITEM_ID );
		DBSettings.loaddbAppEntity( c , entity );
		return( entity );
	}
	
	public static void dropProductData( DBConnection c , ProductMeta storage ) throws Exception {
		dropProductDistData( c , storage );
		dropProductCoreData( c , storage );
	}

	public static void dropProductDistData( DBConnection c , ProductMeta storage ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTCOMPITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTBINARYITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTSCHEMAITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTCONFITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTDOCITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTDELIVERY1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DISTCOMP1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
	}
	
	public static void dropProductCoreData( DBConnection c , ProductMeta storage ) throws Exception {
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_SOURCEITEM1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_SOURCEPROJECT1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_UNIT1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_SCHEMA1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_SOURCESET1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_DOC1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_LIFECYCLE1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_POLICY1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
		if( !c.modify( DBQueries.MODIFY_META_DELETEALL_META1 , new String[] { EngineDB.getInteger( storage.ID ) } ) )
			Common.exitUnexpected();
	}

}
