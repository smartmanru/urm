package org.urm.db.product;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumBuildModeType;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductContext;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaSettings {

	public static String ELEMENT_CUSTOM = "custom";
	
	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		ActionBase action = loader.getAction();
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context and custom settings
		AppSystem system = product.system;
		ObjectProperties opsContext = entities.createMetaContextProps( system.getParameters() );
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductHome( action , storage.name );
		ProductContext productContext = new ProductContext( product , false );
		productContext.create( loader.getSettings() , folder );
		
		Node customNode = ConfReader.xmlGetFirstChild( root , ELEMENT_CUSTOM );
		DBSettings.importxml( loader , customNode , opsContext , storage.ID , DBVersions.CORE_ID , false , true , storage.PV );
		opsContext.recalculateProperties();
		settings.createSettings( opsContext , productContext );
		
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
				ObjectProperties opsBuildMode = entities.createMetaBuildCommonProps( opsBuildCommon );
				DBSettings.importxml( loader , buildNode , opsBuildMode , storage.ID , DBVersions.CORE_ID , true , false , storage.PV );
				
				settings.createBuildModeSettings( mode , opsBuildMode );
			}
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}

}
