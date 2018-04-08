package org.urm.db.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaSettings {

	public static String ELEMENT_CORE = "core";
	public static String ELEMENT_MONITORING = "monitoring";
	public static String ELEMENT_BUILD = "build";
	public static String ELEMENT_MODE = "mode";
	
	public static void createdb( EngineLoader loader , ProductMeta storage , ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		TransactionBase transaction = loader.getTransaction();
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );
		int version = c.getNextProductVersion( storage );

		// context, custom, core settings
		AppSystem system = product.system;
		ObjectProperties ops = entities.createMetaProductProps( storage.ID , system.getParameters() );
		settings.setContextProperties( ops , context );
		
		EngineSettings engineSettings = context.settings;
		ObjectProperties opsDefaults = engineSettings.getDefaultProductSettings();
		ops.copyOriginalPropertiesToRaw( opsDefaults.getProperties() );
		ops.recalculateProperties();
		settings.createCoreSettings( ops );
		
		DBSettings.savedbEntityCustom( c , ops , version );
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		
		// monitoring settings
		EngineMonitoring monitoring = loader.getMonitoring();
		ObjectProperties engineOps = monitoring.getProperties();
		ObjectProperties mon = entities.createMetaMonitoringProps( ops );
		mon.setStringProperty( MetaProductCoreSettings.PROPERTY_MONITORING_RESOURCE_URL , engineOps.getExpressionValue( EngineMonitoring.PROPERTY_RESOURCE_URL ) );
		mon.setPathProperty( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_RES , engineOps.getExpressionValue( EngineMonitoring.PROPERTY_RESOURCE_PATH ) );
		mon.setPathProperty( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_DATA , engineOps.getExpressionValue( EngineMonitoring.PROPERTY_DIR_DATA ) );
		mon.setPathProperty( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_REPORTS , engineOps.getExpressionValue( EngineMonitoring.PROPERTY_DIR_REPORTS ) );
		mon.setPathProperty( MetaProductCoreSettings.PROPERTY_MONITORING_DIR_LOGS , engineOps.getExpressionValue( EngineMonitoring.PROPERTY_DIR_LOGS ) );
		DBSettings.savedbPropertyValues( transaction , mon , true , false , version );
		mon.recalculateProperties();
		settings.createMonitoringSettings( mon );
		
		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( ops );
		ObjectProperties opsBuildCommonDefaults = engineSettings.getDefaultProductBuildSettings();
		opsBuildCommon.copyOriginalPropertiesToRaw( opsBuildCommonDefaults.getProperties() );
		DBSettings.savedbPropertyValues( transaction , opsBuildCommon , true , false , version );
		settings.createBuildCommonSettings( opsBuildCommon );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
			ObjectProperties opsBuildModeDefaults = engineSettings.getDefaultProductBuildModeSettings( mode );
			opsBuildMode.copyOriginalPropertiesToRaw( opsBuildModeDefaults.getProperties() );
			DBSettings.savedbPropertyValues( transaction , opsBuildMode , true , false , version );
			settings.createBuildModeSettings( mode , opsBuildMode );
		}
	}
	
	public static void importxml( EngineLoader loader , ProductMeta storage , ProductContext context , Node root ) throws Exception {
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context, custom settings
		AppSystem system = product.system;
		ObjectProperties ops = entities.createMetaProductProps( storage.ID , system.getParameters() );
		
		EngineSettings engineSettings = context.settings;
		ObjectProperties opsDefaults = engineSettings.getDefaultProductSettings();
		ops.copyOriginalPropertiesToRaw( opsDefaults.getProperties() );
		DBSettings.importxml( loader , root , ops , false , true , storage.PV );
		
		// core settings
		Node coreNode = ConfReader.xmlGetFirstChild( root , ELEMENT_CORE );
		if( coreNode == null )
			Common.exitUnexpected();
		DBSettings.importxmlApp( loader , coreNode , ops , storage.PV , DBEnumParamEntityType.PRODUCTDEFS );
		settings.setContextProperties( ops , context );
		ops.recalculateProperties();
		settings.createCoreSettings( ops );

		// monitoring settings
		ObjectProperties mon = entities.createMetaMonitoringProps( ops );
		Node monitoringNode = ConfReader.xmlGetFirstChild( root , ELEMENT_MONITORING );
		if( monitoringNode != null )
			DBSettings.importxmlApp( loader , monitoringNode , mon , storage.PV , DBEnumParamEntityType.PRODUCT_MONITORING );
		mon.recalculateProperties();
		settings.createMonitoringSettings( mon );
		
		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( ops );
		Node buildNode = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILD );
		if( buildNode == null )
			Common.exitUnexpected();
		DBSettings.importxml( loader , buildNode , opsBuildCommon , true , false , storage.PV );
		settings.createBuildCommonSettings( opsBuildCommon );
		
		Node[] items = ConfReader.xmlGetChildren( buildNode , ELEMENT_MODE );
		if( items != null ) {
			for( Node itemNode : items ) {
				String modeName = ConfReader.getAttrValue( itemNode , "name" );
				DBEnumBuildModeType mode = DBEnumBuildModeType.getValue( modeName , false );
				ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
				DBSettings.importxml( loader , itemNode , opsBuildMode , true , false , storage.PV );
				
				settings.createBuildModeSettings( mode , opsBuildMode );
			}
		}
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			MetaProductBuildSettings build = settings.getBuildModeSettings( mode );
			if( build.ops == null ) {
				loader.trace( "missing build settings, mode=" + mode.name() );
				Common.exitUnexpected();
			}
		}
	}
	
	public static void loaddb( EngineLoader loader , ProductMeta storage , ProductContext context ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		AppSystem system = storage.product.system;
		
		// context, custom, core settings
		ObjectProperties ops = entities.createMetaProductProps( storage.ID , system.getParameters() );
		settings.setContextProperties( ops , context );
		
		DBSettings.loaddbCustomEntity( c , ops , false );
		ops.createCustom();
		DBSettings.loaddbValues( loader , ops );
		ops.recalculateProperties();
		settings.createCoreSettings( ops );

		// monitoring settings
		ObjectProperties mon = entities.createMetaMonitoringProps( ops );
		DBSettings.loaddbValues( loader , mon );
		mon.recalculateProperties();
		settings.createMonitoringSettings( mon );
		
		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( ops );
		DBSettings.loaddbValues( loader , opsBuildCommon );
		
		settings.createBuildCommonSettings( opsBuildCommon );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties opsBuildMode = entities.createMetaBuildModeProps( opsBuildCommon , mode );
			DBSettings.loaddbValues( loader , opsBuildMode );
				
			settings.createBuildModeSettings( mode , opsBuildMode );
		}
	}

	public static void exportxml( EngineLoader loader , ProductMeta storage , Document doc , Element root ) throws Exception {
		MetaProductSettings settings = storage.getSettings();

		// custom settings
		DBSettings.exportxmlCustomEntity( loader , doc , root , settings.ops );
		
		// core settings
		Element coreNode = Common.xmlCreateElement( doc , root , ELEMENT_CORE );
		DBSettings.exportxml( loader , doc , coreNode , settings.ops , true , false , true , DBEnumParamEntityType.PRODUCTDEFS );

		// monitoring settings
		Element monitoringNode = Common.xmlCreateElement( doc , root , ELEMENT_MONITORING );
		DBSettings.exportxml( loader , doc , monitoringNode , settings.mon , true , false , true , DBEnumParamEntityType.PRODUCT_MONITORING );

		// build settings
		Element buildCommonNode = Common.xmlCreateElement( doc , root , ELEMENT_BUILD );
		DBSettings.exportxml( loader , doc , buildCommonNode , settings.buildCommon.ops , true , false , true , null );
		
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			Element buildModeNode = Common.xmlCreateElement( doc , buildCommonNode , ELEMENT_MODE );
			Common.xmlSetNameAttr( doc , buildModeNode , Common.getEnumLower( mode ) );
			
			MetaProductBuildSettings buildMode = settings.getBuildModeSettings( mode );
			DBSettings.exportxml( loader , doc , buildModeNode , buildMode.ops , true , false , true , null );
		}
	}

	public static void updateProductCoreProperties( TransactionBase transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = settings.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( transaction , ops , true , true , version , DBEnumParamEntityType.PRODUCTDEFS );
		ops.recalculateChildProperties();
		settings.updateCoreSettings();
	}
	
	public static void updateProductCustomProperties( TransactionBase transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		ObjectProperties ops = settings.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( transaction , ops , false , true , version );
		ops.recalculateChildProperties();
		settings.updateContextSettings();
	}
	
	public static void updateProductBuildCommonProperties( TransactionBase transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaProductBuildSettings build = settings.getBuildCommonSettings();
		ObjectProperties opsBuild = build.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( transaction , opsBuild , true , false , version );
		opsBuild.recalculateChildProperties();
		build.scatterProperties();
		settings.updateBuildSettings();
	}
	
	public static void updateProductBuildModeProperties( TransactionBase transaction , ProductMeta storage , MetaProductSettings settings , DBEnumBuildModeType mode ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		MetaProductBuildSettings build = settings.getBuildModeSettings( mode );
		ObjectProperties opsBuild = build.ops;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( transaction , opsBuild , true , false , version );
		opsBuild.recalculateChildProperties();
		settings.updateBuildSettings();
	}

	public static void updateMonitoringProperties( TransactionBase transaction , ProductMeta storage , MetaProductSettings settings ) throws Exception {
		DBConnection c = transaction.getConnection();
		AppProduct product = storage.product;
		ActionBase action = transaction.getAction();
		EngineMonitoring mon = transaction.getMonitoring();
		
		mon.stopProduct( action , product );
		
		ObjectProperties ops = settings.mon;
		int version = c.getNextProductVersion( storage );
		DBSettings.savedbPropertyValues( transaction , ops , true , false , version , DBEnumParamEntityType.PRODUCT_MONITORING );
		ops.recalculateChildProperties();
		settings.updateMonitoringSettings();
	}
	
	
}
