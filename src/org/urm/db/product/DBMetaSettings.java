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
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class DBMetaSettings {

	public static void importxml( EngineLoader loader , ProductMeta storage , Node root ) throws Exception {
		ActionBase action = loader.getAction();
		AppProduct product = storage.product;
		EngineEntities entities = loader.getEntities();
		
		MetaProductSettings settings = new MetaProductSettings( storage , storage.meta );
		storage.setSettings( settings );

		// context and custom settings
		ObjectProperties ops = entities.createMetaProps( storage.meta , null );
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder folder = urm.getProductHome( action , storage.name );
		ProductContext productContext = new ProductContext( product , false );
		productContext.create( loader.getSettings() , folder );
		settings.createSettings( ops , productContext );
		
		Node customNode = ConfReader.xmlGetFirstChild( root , "custom" );
		if( customNode == null )
			Common.exitUnexpected();
		DBSettings.importxml( loader , customNode , ops , storage.ID , DBVersions.CORE_ID , false , storage.PV );
		
		// core application settings
		ObjectProperties opsCore = entities.createMetaCoreSettingsProps( ops );
		DBSettings.importxml( loader , root , opsCore , storage.ID , DBVersions.CORE_ID , true , storage.PV );

		settings.createCoreSettings( opsCore , loader.getMonitoring() );
		ops.recalculateProperties();
		opsCore.recalculateProperties();

		// build settings
		ObjectProperties opsBuildCommon = entities.createMetaBuildCommonProps( storage.meta , opsCore );
		Node buildNode = ConfReader.xmlGetFirstChild( root , "build" );
		if( buildNode == null )
			Common.exitUnexpected();
		DBSettings.importxml( loader , buildNode , opsBuildCommon , storage.ID , DBVersions.CORE_ID , true , storage.PV );
		
		settings.createBuildCommonSettings( opsBuildCommon );
		
		Node[] items = ConfReader.xmlGetChildren( buildNode , "mode" );
		if( items != null ) {
			for( Node node : items ) {
				String modeName = ConfReader.getAttrValue( node , "name" );
				DBEnumBuildModeType mode = DBEnumBuildModeType.getValue( modeName , false );
				ObjectProperties opsBuildMode = entities.createMetaBuildCommonProps( storage.meta , opsBuildCommon );
				DBSettings.importxml( loader , buildNode , opsBuildMode , storage.ID , DBVersions.CORE_ID , true , storage.PV );
				
				settings.createBuildModeSettings( mode , opsBuildMode );
			}
		}
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
	}

}
