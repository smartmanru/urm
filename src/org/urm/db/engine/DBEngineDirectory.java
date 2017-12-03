package org.urm.db.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.DBEnumObjectType;
import org.urm.db.core.DBEnums.DBEnumObjectVersionType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.db.system.DBProduct;
import org.urm.db.system.DBSystem;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineData;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineDirectory {

	public static String ELEMENT_SYSTEM = "system";
	public static String ELEMENT_PRODUCT = "product";
	
	public static String TABLE_SYSTEM = "urm_system";
	public static String TABLE_PRODUCT = "urm_product";
	public static String FIELD_SYSTEM_ID = "system_id";
	public static String FIELD_SYSTEM_DESC = "xdesc";
	public static String FIELD_PRODUCT_SYSTEM_ID = "system_id";
	public static String FIELD_PRODUCT_ID = "product_id";
	public static String FIELD_PRODUCT_DESC = "xdesc";
	
	public static PropertyEntity upgradeEntityDirectorySystem( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.APPSYSTEM , DBEnumObjectVersionType.SYSTEM , TABLE_SYSTEM , FIELD_SYSTEM_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaString( AppSystem.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AppSystem.PROPERTY_DESC , FIELD_SYSTEM_DESC , AppSystem.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaBoolean( AppSystem.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBoolean( AppSystem.PROPERTY_MATCHED , "State of matched to core" , false , true )
		} ) );
	}

	public static PropertyEntity upgradeEntityDirectoryProduct( EngineLoader loader ) throws Exception {
		DBConnection c = loader.getConnection();
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT , DBEnumParamEntityType.APPPRODUCT , DBEnumObjectVersionType.SYSTEM , TABLE_PRODUCT , FIELD_PRODUCT_ID );
		return( DBSettings.savedbObjectEntity( c , entity , new EntityVar[] { 
				EntityVar.metaObjectDatabaseOnly( FIELD_PRODUCT_SYSTEM_ID , "System" , DBEnumObjectType.APPSYSTEM , true ) ,
				EntityVar.metaString( AppProduct.PROPERTY_NAME , "Name" , true , null ) ,
				EntityVar.metaStringVar( AppProduct.PROPERTY_DESC , FIELD_PRODUCT_DESC , AppProduct.PROPERTY_DESC , "Description" , false , null ) ,
				EntityVar.metaString( AppProduct.PROPERTY_PATH , "Path" , true , null ) ,
				EntityVar.metaBoolean( AppProduct.PROPERTY_OFFLINE , "Offline" , false , true ) ,
				EntityVar.metaBoolean( AppProduct.PROPERTY_MONITORING_ENABLED , "Monitoring enabled" , false , false ) ,
		} ) );
	}

	public static PropertyEntity loaddbEntityDirectorySystem( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPSYSTEM , DBEnumParamEntityType.APPSYSTEM , DBEnumObjectVersionType.SYSTEM , TABLE_SYSTEM , FIELD_SYSTEM_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static PropertyEntity loaddbEntityDirectoryProduct( EngineLoader loader ) throws Exception {
		PropertyEntity entity = PropertyEntity.getAppObjectEntity( DBEnumObjectType.APPPRODUCT , DBEnumParamEntityType.APPPRODUCT , DBEnumObjectVersionType.SYSTEM , TABLE_PRODUCT , FIELD_PRODUCT_ID );
		DBSettings.loaddbEntity( loader , entity , DBVersions.APP_ID );
		return( entity );
	}
	
	public static void importxml( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SYSTEM );
		if( items != null ) {
			for( Node itemNode : items ) {
				AppSystem system = importxmlSystem( loader , directory , itemNode );
				directory.addSystem( system );
			}
		}
		
		// match systems to engine
		matchxml( loader , directory );
	}
	
	private static AppSystem importxmlSystem( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		AppSystem system = DBSystem.importxmlSystem( loader , directory , root );
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PRODUCT );
		if( items != null ) {
			for( Node itemNode : items ) {
				AppProduct product = importxmlProduct( loader , directory , system , itemNode );
				directory.addProduct( product );
			}
		}
		return( system );
	}

	private static AppProduct importxmlProduct( EngineLoader loader , EngineDirectory directory , AppSystem system , Node root ) throws Exception {
		AppProduct product = DBProduct.importxmlProduct( loader , directory , system , root );
		return( product );
	}

	public static void exportxml( EngineLoader loader , EngineDirectory directory , Document doc , Element root ) throws Exception {
		// directory 
		for( String name : directory.getSystemNames() ) {
			AppSystem system = directory.findSystem( name );
			Element elementSystem = Common.xmlCreateElement( doc , root , ELEMENT_SYSTEM );
			exportxmlSystem( loader , directory , system , doc , elementSystem );
		}
	}

	private static void exportxmlSystem( EngineLoader loader , EngineDirectory directory , AppSystem system , Document doc , Element root ) throws Exception {
		DBSystem.exportxml( loader , directory , system , doc , root );
		
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , ELEMENT_PRODUCT );
			exportxmlProduct( loader , directory , product , doc , elementProduct );
		}
	}
	
	private static void exportxmlProduct( EngineLoader loader , EngineDirectory directory , AppProduct product , Document doc , Element root ) throws Exception {
		DBProduct.exportxmlProduct( loader , product , doc , root );
	}
	
	public static void loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		loaddbSystems( loader , directory );
		loaddbProducts( loader , directory );
		
		// match systems to engine
		matchdb( loader , directory , false );
	}
	
	private static void loaddbSystems( EngineLoader loader , EngineDirectory directory ) throws Exception {
		AppSystem[] systems = DBSystem.loaddb( loader , directory );
		for( AppSystem system : systems )
			directory.addSystem( system );
	}
	
	private static void loaddbProducts( EngineLoader loader , EngineDirectory directory ) throws Exception {
		AppProduct[] products = DBProduct.loaddb( loader , directory );
		for( AppProduct product : products )
			directory.addProduct( product );
	}

	private static void matchxml( EngineLoader loader , EngineDirectory directory ) throws Exception {
		EngineMatcher matcher = loader.getMatcher();
		EngineData data = loader.getData();
		
		for( AppSystem system : directory.getSystems() ) {
			matcher.prepareMatch( system.ID , false , false );
			DBSystem.matchxmlSystem( loader , directory , system );
			
			data.matchdoneSystem( loader , system );
		}
	}
	
	public static void matchdb( EngineLoader loader , EngineDirectory directory , boolean update ) throws Exception {
		EngineMatcher matcher = loader.getMatcher();
		EngineData data = loader.getData();
		
		for( AppSystem system : directory.getSystems() ) {
			if( update ) {
				matcher.prepareMatch( system.ID , true , true );
				DBSystem.matchdb( loader , directory , system );
			}
			else
			if( system.MATCHED ) {
				matcher.prepareMatch( system.ID , false , true );
				DBSystem.matchdb( loader , directory , system );
			}
			
			data.matchdoneSystem( loader , system );
		}
	}
	
	public static AppSystem createSystem( EngineTransaction transaction , EngineDirectory directory , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( directory.findProduct( name ) != null )
			transaction.exit1( _Error.DuplicateSystem1 , "system=" + name + " is not unique" , name );
		
		EngineData data = directory.data;
		EngineEntities entities = data.getEntities();
		EngineSettings settings = data.getEngineSettings(); 
		
		ObjectProperties props = entities.createSystemProps( settings.getEngineProperties() );
		AppSystem system = new AppSystem( directory , props );
		system.createSystem( name , desc );
		DBSystem.modifySystem( c , system , true );
		
		directory.addSystem( system );
		return( system );
	}

	public static void modifySystem( EngineTransaction transaction , EngineDirectory directory , AppSystem system , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		system.modifySystem( name , desc );
		DBSystem.modifySystem( c , system , false );
		directory.updateSystem( system );
	}
	
	public static void setSystemOffline( EngineTransaction transaction , EngineDirectory directory , AppSystem system , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		system.setOffline( offline );
		DBSystem.modifySystem( c , system , false );
	}
	
	public static void deleteSystem( EngineTransaction transaction , EngineDirectory directory , AppSystem system ) throws Exception {
		if( !system.isEmpty() )
			transaction.exit0( _Error.SystemNotEmpty0 , "System is not empty, unable to delete" );
		
		DBConnection c = transaction.getConnection();
		EngineEntities entities = c.getEntities();
		DBSettings.dropObjectSettings( c , system.ID );
		DBEngineEntities.deleteAppObject( c , entities.entityAppDirectorySystem , system.ID , c.getNextSystemVersion( system ) );
		directory.removeSystem( system );
		system.deleteObject();
	}
	
	public static AppProduct createProduct( EngineTransaction transaction , EngineDirectory directory , AppSystem system , String name , String desc , String path ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( directory.findProduct( name ) != null )
			transaction.exitUnexpectedState();
		
		AppProduct product = new AppProduct( directory , system );
		product.createProduct( name , desc , path );
		DBProduct.modifyProduct( c , product , true );
		
		directory.addProduct( product );
		return( product );
	}
	
	public static void modifyProduct( EngineTransaction transaction , EngineDirectory directory , AppProduct product , String name , String desc , String path ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.modifyProduct( name , desc , path );
		DBProduct.modifyProduct( c , product , false );
		directory.updateProduct( product );
	}
	
	public static void setProductOffline( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.setOffline( offline );
		DBProduct.modifyProduct( c , product , false );
	}
	
	public static void setMonitoringEnabled( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean enabled ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.setMonitoringEnabled( enabled );
		DBProduct.modifyProduct( c , product , false );
	}
	
	public static void deleteProduct( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		if( directory.getProduct( product.ID ) != product )
			transaction.exit( _Error.UnknownProduct1 , "product=" + product.NAME + " is unknown or mismatched" , new String[] { product.NAME } );
		
		directory.removeProduct( product );
		
		ActionBase action = transaction.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder products = storage.getServerProductsFolder( action );
		LocalFolder productfolder = products.getSubFolder( action , product.PATH );
		productfolder.removeThis( action );
		product.deleteObject();
	}

}
