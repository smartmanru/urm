package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBEnums.DBEnumOSType;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineSettings {

	public static String ELEMENT_DEFAULTS = "defaults"; 
	public static String ELEMENT_BUILD = "build"; 
	public static String ELEMENT_MODE = "mode";
	public static String MODE_ATTR_NAME = "name";
	
	public static PropertyEntity upgradeEntityProduct( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.PRODUCTDEFS , DBEnumObjectVersionType.CORE );
		DBEnumOSType ostype = DBEnumOSType.getValue( loader.execrc.osType );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_REDISTLINUX_PATH , "Linux Staging Area Path" , true , null , DBEnumOSType.LINUX ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_REDISTWIN_PATH , "Windows Staging Area Path" , true , null , DBEnumOSType.WINDOWS ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_DISTR_PATH , "Distributives Path" , true , null , ostype ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_DISTR_HOSTLOGIN , "Distributives host@login" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_UPGRADE_PATH , "Upgrade Scripts Path" , false , null , ostype ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_BASE_PATH , "Platform Software Path" , false , null , ostype ) ,
				EntityVar.metaPathAbsolute( MetaProductSettings.PROPERTY_MIRRORPATH , "Mirror Repositories" , false , null , ostype ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_ADM_TRACKER , "Codebase Control Tracker" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_COMMIT_TRACKERLIST , "Source Task Trackers" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_SOURCE_CHARSET , "Release Source Charset" , false , null ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_RELEASEROOTDIR , "Release Source Root" , false , null , ostype ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_CFG_ROOTDIR , "Configuration Root" , false , null , ostype ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_CFG_LIVEROOTDIR , "Configuration Live" , false , null , ostype ) ,
				EntityVar.metaPathRelative( MetaProductSettings.PROPERTY_SOURCE_SQL_POSTREFRESH , "Database PostRefresh" , false , null , ostype ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_BUILD , "Custom Builder Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_DEPLOY , "Custom Deployer Plugin" , false , null ) ,
				EntityVar.metaString( MetaProductSettings.PROPERTY_CUSTOM_DATABASE , "Custom Database Plugin" , false , null )
		} ) );
	}

	public static PropertyEntity loaddbEntityProduct( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.PRODUCTDEFS , DBEnumObjectVersionType.CORE );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity upgradeEntityProductBuild( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.PRODUCTBUILD , DBEnumObjectVersionType.CORE );
		DBEnumOSType ostype = DBEnumOSType.getValue( loader.execrc.osType );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMAJOR , "Last Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMAJOR , "Next Major Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_LASTMINOR , "Last Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_NEXTMINOR , "Next Minor Release" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_VERSION , "Release Build Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_APPVERSION , "Artefacts Version" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_BRANCHNAME , "Source Branch Name" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_RELEASE_GROUPFOLDER , "Release Source Group" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_ARTEFACTDIR , "Artefacts Directory" , false , null , ostype ) ,
				EntityVar.metaPathRelative( MetaProductBuildSettings.PROPERTY_LOGPATH , "Build Log Path" , false , null , ostype ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO , "Nexus Repository" , false , null ) ,
				EntityVar.metaString( MetaProductBuildSettings.PROPERTY_NEXUS_REPO_THIRDPARTY , "Nexus Thirdparty Repository" , false , null ) ,
				EntityVar.metaPathAbsolute( MetaProductBuildSettings.PROPERTY_MAVEN_CFGFILE , "Maven Settings" , false , null , ostype )
		} ) );
	}

	public static PropertyEntity loaddbEntityProductBuild( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppPropsEntity( DBEnumObjectType.ROOT , DBEnumParamEntityType.PRODUCTBUILD , DBEnumObjectVersionType.CORE );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineSettings settings , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		settings.version = c.getNextCoreVersion();
		settings.setExecProperties( loader );
		
		importEngineSettings( loader , settings , root );
		importProductDefaults( loader , settings , root );
		
		settings.context = new EngineContext( settings.execrc , settings.engineProperties );
		settings.context.scatterProperties();
	}

	public static void loaddb( EngineLoader loader , EngineSettings settings ) throws Exception {
		DBConnection c = loader.getConnection();
		settings.version = c.getCurrentCoreVersion();
		settings.setExecProperties( loader );
		
		loaddbEngineSettings( loader , settings );
		loaddbProductDefaults( loader , settings );
		
		settings.context = new EngineContext( settings.execrc , settings.engineProperties );
		settings.context.scatterProperties();
	}
	
	private static void importEngineSettings( EngineLoader loader , EngineSettings settings , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		int version = c.getNextCoreVersion();
		
		settings.engineProperties = entities.createEngineProps( settings.execrcProperties );
		DBSettings.importxml( loader , root , settings.engineProperties , DBVersions.CORE_ID , DBVersions.CORE_ID , true , version );
	}
	
	private static void loaddbEngineSettings( EngineLoader loader , EngineSettings settings ) throws Exception {
		EngineEntities entities = loader.getEntities();
		settings.engineProperties = entities.createEngineProps( settings.execrcProperties );
		DBSettings.loaddbValues( loader , DBVersions.CORE_ID , settings.engineProperties , true );
	}
	
	private static void importProductDefaults( EngineLoader loader , EngineSettings settings , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		int version = c.getNextCoreVersion();
		
		// load from xml
		settings.defaultProductProperties = entities.createDefaultProductProps( settings.engineProperties );
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_DEFAULTS );
		DBSettings.importxml( loader , node , settings.defaultProductProperties , DBVersions.CORE_ID , DBVersions.CORE_ID , true , version );
		
		settings.defaultProductBuildProperties = entities.createDefaultBuildCommonProps( settings.defaultProductBuildProperties );
		Node build = ConfReader.xmlGetFirstChild( node , ELEMENT_BUILD );
		DBSettings.importxml( loader , build , settings.defaultProductBuildProperties , DBVersions.CORE_ID , DBVersions.CORE_ID , true , version );
		
		// for build modes
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties properties = entities.createDefaultBuildModeProps( settings.defaultProductBuildProperties , mode );
			settings.addBuildModeDefaults( mode , properties );
		}
			
		Node[] items = ConfReader.xmlGetChildren( build , ELEMENT_MODE );
		if( items != null ) {
			for( Node itemNode : items ) {
				String MODE = ConfReader.getRequiredAttrValue( itemNode , MODE_ATTR_NAME );
				DBEnumBuildModeType mode = DBEnumBuildModeType.valueOf( MODE.toUpperCase() );
				ObjectProperties properties = settings.getDefaultProductBuildObjectProperties( mode );
				DBSettings.importxml( loader , itemNode , properties , DBVersions.CORE_ID , DBVersions.CORE_ID , true , version );
			}
		}
	}
	
	private static void loaddbProductDefaults( EngineLoader loader , EngineSettings settings ) throws Exception {
		EngineEntities entities = loader.getEntities();
		
		settings.defaultProductProperties = entities.createDefaultProductProps( settings.engineProperties );
		DBSettings.loaddbValues( loader , DBVersions.CORE_ID , settings.defaultProductProperties , true );
		
		settings.defaultProductBuildProperties = entities.createDefaultBuildCommonProps( settings.defaultProductBuildProperties );
		DBSettings.loaddbValues( loader , DBVersions.CORE_ID , settings.defaultProductBuildProperties , true );
		
		// for build modes
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties properties = entities.createDefaultBuildModeProps( settings.defaultProductBuildProperties , mode );
			DBSettings.loaddbValues( loader , DBVersions.CORE_ID , properties , true );
			settings.addBuildModeDefaults( mode , properties );
		}
	}
	
	public static void exportxml( EngineLoader loader , EngineSettings settings , Document doc , Element root ) throws Exception {
		// properties
		DBSettings.exportxml( loader , doc , root , settings.engineProperties , true );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , ELEMENT_DEFAULTS );
		DBSettings.exportxml( loader , doc , modeDefaults , settings.defaultProductProperties , true );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , ELEMENT_BUILD );
		DBSettings.exportxml( loader , doc , modeBuild , settings.defaultProductBuildProperties , true );
		
		// product defaults
		DBEnumBuildModeType[] list = new DBEnumBuildModeType[] { DBEnumBuildModeType.DEVTRUNK , DBEnumBuildModeType.DEVBRANCH ,
				DBEnumBuildModeType.TRUNK , DBEnumBuildModeType.BRANCH , DBEnumBuildModeType.MAJORBRANCH };
		for( DBEnumBuildModeType mode : list ) {
			ObjectProperties set = settings.getDefaultProductBuildObjectProperties( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , ELEMENT_MODE );
			Common.xmlSetElementAttr( doc , modeElement , MODE_ATTR_NAME , mode.toString().toLowerCase() );
			DBSettings.exportxml( loader , doc , modeElement , set , true );
		}
	}

}
