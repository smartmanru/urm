package org.urm.db.product;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Node;

public class DBMetaSettings {

	public static String ELEMENT_CUSTOM = "custom";
	
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
		
		// core and monitoring settings
		Node coreNode = ConfReader.xmlGetFirstChild( root , "core" );
		if( coreNode == null )
			Common.exitUnexpected();
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( opsContext );
		DBSettings.importxml( loader , coreNode , opsCore , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
		opsCore.recalculateProperties();

		ObjectProperties opsMon = entities.createMetaMonitoringProps( opsCore );
		settings.createCoreSettings( opsCore , opsMon , loader.getMonitoring() );

		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( opsCore );
		Node buildNode = ConfReader.xmlGetFirstChild( root , "build" );
		if( buildNode == null )
			Common.exitUnexpected();
		DBSettings.importxml( loader , buildNode , opsBuildCommon , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
		
		settings.createBuildCommonSettings( opsBuildCommon );
		
		Node[] items = ConfReader.xmlGetChildren( buildNode , "mode" );
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
		
		// core and monitoring settings
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( opsContext );
		DBSettings.loaddbValues( loader , storage.ID , opsCore , true );
		opsCore.recalculateProperties();

		ObjectProperties opsMon = entities.createMetaMonitoringProps( opsCore );
		settings.createCoreSettings( opsCore , opsMon , loader.getMonitoring() );

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

}
