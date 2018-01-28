package org.urm.db.product;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaSettings {

	public static String ELEMENT_CUSTOM = "custom";
	public static String ELEMENT_CORE = "core";
	public static String ELEMENT_MONITORING = "monitoring";
	public static String ELEMENT_BUILD = "build";
	public static String ELEMENT_MODE = "mode";
	
	public static void createdb( EngineLoader loader , ProductMeta storage , ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context and custom settings
		AppSystem system = product.system;
		ObjectProperties opsContext = entities.createMetaContextProps( system.getParameters() );
		int version = c.getNextProductVersion( storage );
		opsContext.recalculateProperties();
		DBSettings.savedbEntityCustom( c , opsContext , storage.ID , storage.ID , version );
		DBSettings.savedbPropertyValues( c , storage.ID , opsContext , false , true , version );
		settings.createSettings( opsContext , context );
		
		// core settings
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( opsContext );
		opsCore.recalculateProperties();
		DBSettings.savedbPropertyValues( c , storage.ID , opsCore , true , false , version );
		settings.createCoreSettings( opsCore );

		// monitoring settings
		ObjectProperties opsMon = entities.createMetaMonitoringProps( opsCore );
		DBSettings.savedbPropertyValues( c , storage.ID , opsMon , true , false , version );
		opsMon.recalculateProperties();
		settings.createMonitoringSettings( opsMon );
		
		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( opsCore );
		EngineSettings engineSettings = context.settings;
		ObjectProperties opsBuildCommonDefaults = engineSettings.getDefaultProductBuildProperties();
		opsBuildCommon.copyOriginalPropertiesToRaw( opsBuildCommonDefaults.getProperties() );
		DBSettings.savedbPropertyValues( c , storage.ID , opsBuildCommon , true , false , version );
		settings.createBuildCommonSettings( opsBuildCommon );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
			ObjectProperties opsBuildModeDefaults = engineSettings.getDefaultProductBuildObjectProperties( mode );
			opsBuildMode.copyOriginalPropertiesToRaw( opsBuildModeDefaults.getProperties() );
			DBSettings.savedbPropertyValues( c , storage.ID , opsBuildMode , true , false , version );
			settings.createBuildModeSettings( mode , opsBuildMode );
		}
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage , ProductContext context , Node root ) throws Exception {
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context and custom settings
		AppSystem system = product.system;
		ObjectProperties opsContext = entities.createMetaContextProps( system.getParameters() );
		Node customNode = ConfReader.xmlGetFirstChild( root , ELEMENT_CUSTOM );
		DBSettings.importxml( loader , customNode , opsContext , storage.ID , storage.ID , false , true , storage.PV );
		opsContext.recalculateProperties();
		settings.createSettings( opsContext , context );
		
		// core settings
		Node coreNode = ConfReader.xmlGetFirstChild( root , ELEMENT_CORE );
		if( coreNode == null )
			Common.exitUnexpected();
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( opsContext );
		DBSettings.importxml( loader , coreNode , opsCore , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
		opsCore.recalculateProperties();
		settings.createCoreSettings( opsCore );

		// monitoring settings
		ObjectProperties opsMon = entities.createMetaMonitoringProps( opsCore );
		Node monitoringNode = ConfReader.xmlGetFirstChild( root , ELEMENT_MONITORING );
		if( monitoringNode != null )
			DBSettings.importxml( loader , monitoringNode , opsMon , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
		opsMon.recalculateProperties();
		settings.createMonitoringSettings( opsMon );
		
		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( opsCore );
		Node buildNode = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILD );
		if( buildNode == null )
			Common.exitUnexpected();
		DBSettings.importxml( loader , buildNode , opsBuildCommon , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
		settings.createBuildCommonSettings( opsBuildCommon );
		
		Node[] items = ConfReader.xmlGetChildren( buildNode , ELEMENT_MODE );
		if( items != null ) {
			for( Node node : items ) {
				String modeName = ConfReader.getAttrValue( node , "name" );
				DBEnumBuildModeType mode = DBEnumBuildModeType.getValue( modeName , false );
				ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
				DBSettings.importxml( loader , buildNode , opsBuildMode , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
				
				settings.createBuildModeSettings( mode , opsBuildMode );
			}
		}
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context and custom settings
		AppSystem system = storage.product.system;
		ObjectProperties opsContext = entities.createMetaContextProps( system.getParameters() );
		ObjectMeta meta = opsContext.getMeta();
		DBSettings.loaddbEntity( c , meta.getCustomEntity() , storage.ID );
		DBSettings.loaddbValues( loader , storage.ID , opsContext , false );
		opsContext.recalculateProperties();
		settings.createSettings( opsContext , context );
		
		// core settings
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( opsContext );
		DBSettings.loaddbValues( loader , storage.ID , opsCore , true );
		opsCore.recalculateProperties();
		settings.createCoreSettings( opsCore );

		// monitoring settings
		ObjectProperties opsMon = entities.createMetaMonitoringProps( opsCore );
		DBSettings.loaddbValues( loader , storage.ID , opsMon , true );
		opsMon.recalculateProperties();
		settings.createMonitoringSettings( opsMon );

		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( opsCore );
		DBSettings.loaddbValues( loader , storage.ID , opsBuildCommon , true );
		
		settings.createBuildCommonSettings( opsBuildCommon );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
			DBSettings.loaddbValues( loader , storage.ID , opsBuildMode , true );
				
			settings.createBuildModeSettings( mode , opsBuildMode );
		}
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaProductSettings settings = storage.getSettings();

		// custom settings
		DBSettings.exportxmlEntity( loader , doc , root , settings.ctx , false , false );
		
		// core settings
		Element coreNode = Common.xmlCreateElement( doc , root , ELEMENT_CORE );
		DBSettings.exportxmlEntity( loader , doc , coreNode , settings.core.ops , false , false );

		// monitoring settings
		Element monitoringNode = Common.xmlCreateElement( doc , root , ELEMENT_MONITORING );
		DBSettings.exportxmlEntity( loader , doc , monitoringNode , settings.core.mon , false , false );

		// build settings
		Element coreBuild = Common.xmlCreateElement( doc , root , ELEMENT_BUILD );
		DBSettings.exportxmlEntity( loader , doc , coreNode , settings.buildCommon.ops , false , false );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			Element coreMode = Common.xmlCreateElement( doc , coreBuild , ELEMENT_BUILD );
			Common.xmlSetNameAttr( doc , coreMode , Common.getEnumLower( mode ) );
			
			MetaProductBuildSettings buildMode = settings.getBuildModeSettings( mode );
			DBSettings.exportxmlEntity( loader , doc , coreMode , buildMode.ops , false , false );
		}
	}

	public static void updateProductCoreProperties( EngineTransaction transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaProductCoreSettings core = settings.getCoreSettings();
		ObjectProperties opsCore = core.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( c , storage.ID , opsCore , true , false , version );
		core.scatterPrimaryProperties();
	}
	
	public static void updateProductCustomProperties( EngineTransaction transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties opsContext = settings.getContextProperties();
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbEntityCustom( c , opsContext , storage.ID , storage.ID , version );
		DBSettings.savedbPropertyValues( c , storage.ID , opsContext , false , true , version );
		opsContext.recalculateChildProperties();
	}
	
	public static void updateProductBuildCommonProperties( EngineTransaction transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaProductBuildSettings build = settings.getBuildCommonSettings();
		ObjectProperties opsBuild = build.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( c , storage.ID , opsBuild , true , false , version );
		build.scatterProperties();
		opsBuild.recalculateChildProperties();
	}
	
	public static void updateProductBuildModeProperties( EngineTransaction transaction , ProductMeta storage , MetaProductSettings settings , DBEnumBuildModeType mode ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaProductBuildSettings build = settings.getBuildModeSettings( mode );
		ObjectProperties opsBuild = build.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( c , storage.ID , opsBuild , true , false , version );
		build.scatterProperties();
		opsBuild.recalculateChildProperties();
	}
	
}
