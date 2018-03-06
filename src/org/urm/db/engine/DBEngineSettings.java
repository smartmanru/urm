package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineLoader;
import org.urm.meta.engine.EngineContext;
import org.urm.meta.engine.EngineSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineSettings {

	public static String ELEMENT_CORE = "core";
	public static String ELEMENT_DEFAULTS = "defaults"; 
	public static String ELEMENT_BUILD = "build"; 
	public static String ELEMENT_MODE = "mode";
	public static String MODE_ATTR_NAME = "name";
	
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
		Node nodeCore = ConfReader.xmlGetFirstChild( root , ELEMENT_CORE );
		DBSettings.importxml( loader , nodeCore , settings.engineProperties , true , true , version );
	}
	
	private static void loaddbEngineSettings( EngineLoader loader , EngineSettings settings ) throws Exception {
		EngineEntities entities = loader.getEntities();
		settings.engineProperties = entities.createEngineProps( settings.execrcProperties );
		DBSettings.loaddbValues( loader , settings.engineProperties );
	}
	
	private static void importProductDefaults( EngineLoader loader , EngineSettings settings , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineEntities entities = loader.getEntities();
		int version = c.getNextCoreVersion();
		
		// load from xml
		settings.defaultProductProperties = entities.createDefaultProductProps( settings.engineProperties );
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_DEFAULTS );
		DBSettings.importxml( loader , node , settings.defaultProductProperties , true , false , version , DBEnumParamEntityType.PRODUCTDEFS);
		
		settings.defaultProductBuildProperties = entities.createDefaultBuildCommonProps( settings.defaultProductProperties );
		Node build = ConfReader.xmlGetFirstChild( node , ELEMENT_BUILD );
		DBSettings.importxml( loader , build , settings.defaultProductBuildProperties , true , false , version );
		
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
				ObjectProperties properties = settings.getDefaultProductBuildModeSettings( mode );
				DBSettings.importxml( loader , itemNode , properties , true , false , version );
			}
		}
	}
	
	private static void loaddbProductDefaults( EngineLoader loader , EngineSettings settings ) throws Exception {
		EngineEntities entities = loader.getEntities();
		
		settings.defaultProductProperties = entities.createDefaultProductProps( settings.engineProperties );
		DBSettings.loaddbValues( loader , settings.defaultProductProperties );
		
		settings.defaultProductBuildProperties = entities.createDefaultBuildCommonProps( settings.defaultProductProperties );
		DBSettings.loaddbValues( loader , settings.defaultProductBuildProperties );
		
		// for build modes
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties properties = entities.createDefaultBuildModeProps( settings.defaultProductBuildProperties , mode );
			DBSettings.loaddbValues( loader , properties );
			settings.addBuildModeDefaults( mode , properties );
		}
	}
	
	public static void exportxml( EngineLoader loader , EngineSettings settings , Document doc , Element root ) throws Exception {
		// properties
		Element core = Common.xmlCreateElement( doc , root , ELEMENT_CORE );
		DBSettings.exportxml( loader , doc , core , settings.engineProperties , true );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , ELEMENT_DEFAULTS );
		DBSettings.exportxml( loader , doc , modeDefaults , settings.defaultProductProperties , true , false , true , DBEnumParamEntityType.PRODUCTDEFS );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , ELEMENT_BUILD );
		DBSettings.exportxml( loader , doc , modeBuild , settings.defaultProductBuildProperties , true );
		
		// product defaults
		DBEnumBuildModeType[] list = new DBEnumBuildModeType[] { DBEnumBuildModeType.DEVTRUNK , DBEnumBuildModeType.DEVBRANCH ,
				DBEnumBuildModeType.TRUNK , DBEnumBuildModeType.BRANCH , DBEnumBuildModeType.MAJORBRANCH };
		for( DBEnumBuildModeType mode : list ) {
			ObjectProperties set = settings.getDefaultProductBuildModeSettings( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , ELEMENT_MODE );
			Common.xmlSetElementAttr( doc , modeElement , MODE_ATTR_NAME , mode.toString().toLowerCase() );
			DBSettings.exportxml( loader , doc , modeElement , set , true );
		}
	}

	public static void updateAppEngineProperties( EngineTransaction transaction , EngineSettings settings ) throws Exception {
		ObjectProperties ops = settings.getEngineProperties();
		DBSettings.modifyAppValues( transaction , ops , null );
	}
	
	public static void updateProductDefaultProperties( EngineTransaction transaction , EngineSettings settings ) throws Exception {
		ObjectProperties ops = settings.getDefaultProductSettigns();
		DBSettings.modifyAppValues( transaction , ops , DBEnumParamEntityType.PRODUCTDEFS );
	}
	
	public static void updateProductDefaultBuildCommonProperties( EngineTransaction transaction , EngineSettings settings ) throws Exception {
		ObjectProperties ops = settings.getDefaultProductBuildSettings();
		DBSettings.modifyAppValues( transaction , ops , null );
	}
	
	public static void updateProductDefaultBuildModeProperties( EngineTransaction transaction , EngineSettings settings , DBEnumBuildModeType mode ) throws Exception {
		ObjectProperties ops = settings.getDefaultProductBuildModeSettings( mode );
		DBSettings.modifyAppValues( transaction , ops , null );
	}
	
}
