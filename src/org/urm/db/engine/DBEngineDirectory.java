package org.urm.db.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.system.DBAppProduct;
import org.urm.db.system.DBAppSystem;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.loader.EngineLoader;
import org.urm.meta.loader.EngineMatcher;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineDirectory {

	public static String ELEMENT_SYSTEM = "system";
	public static String ELEMENT_PRODUCT = "product";
	public static String ELEMENT_POLICY = "policy";
	
	public static void importxml( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_SYSTEM );
		if( items != null ) {
			for( Node itemNode : items ) {
				AppSystem system = importxmlSystem( loader , directory , itemNode );
				directory.addUnmatchedSystem( system );
			}
		}
		
		// match systems to engine
		matchSystems( loader , directory , true );
	}
	
	private static AppSystem importxmlSystem( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		AppSystem system = DBAppSystem.importxmlSystem( loader , directory , root );
		
		Node[] items = ConfReader.xmlGetChildren( root , ELEMENT_PRODUCT );
		if( items != null ) {
			for( Node itemNode : items ) {
				AppProduct product = importxmlProduct( loader , directory , system , itemNode );
				directory.addUnmatchedProduct( product );
			}
		}
		return( system );
	}

	private static AppProduct importxmlProduct( EngineLoader loader , EngineDirectory directory , AppSystem system , Node root ) throws Exception {
		DBConnection c = loader.getConnection();
		
		AppProduct product = DBAppProduct.importxmlProduct( loader , directory , system , root );
		
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_POLICY );
		if( node == null )
			DBAppProduct.createdbPolicy( c , directory , product );
		else
			DBAppProduct.importxmlPolicy( loader , directory , product , node );
				
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
		DBAppSystem.exportxml( loader , directory , system , doc , root );
		
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , ELEMENT_PRODUCT );
			exportxmlProduct( loader , directory , product , doc , elementProduct );
		}
	}
	
	private static void exportxmlProduct( EngineLoader loader , EngineDirectory directory , AppProduct product , Document doc , Element root ) throws Exception {
		DBAppProduct.exportxmlProduct( loader , product , doc , root );
		Element elementPolicy = Common.xmlCreateElement( doc , root , ELEMENT_POLICY );
		DBAppProduct.exportxmlPolicy( loader , product , doc , elementPolicy );
	}
	
	public static void loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		loaddbSystems( loader , directory );
		loaddbProducts( loader , directory );
		
		// match systems to engine
		matchSystems( loader , directory , false );
	}
	
	private static void loaddbSystems( EngineLoader loader , EngineDirectory directory ) throws Exception {
		AppSystem[] systems = DBAppSystem.loaddb( loader , directory );
		for( AppSystem system : systems )
			directory.addUnmatchedSystem( system );
	}
	
	private static void loaddbProducts( EngineLoader loader , EngineDirectory directory ) throws Exception {
		AppProduct[] products = DBAppProduct.loaddb( loader , directory );
		
		for( AppProduct product : products )
			DBAppProduct.loaddbPolicy( loader , product );
		
		for( AppProduct product : products )
			directory.addUnmatchedProduct( product );
	}

	public static void matchSystems( EngineLoader loader , EngineDirectory directory , boolean update ) {
		EngineMatcher matcher = loader.getMatcher();
		matcher.prepareMatchDirectory();
		
		for( String name : directory.getAllSystemNames() ) {
			AppSystem system = directory.findSystem( name );
			matcher.matchSystem( loader , directory , system , update );
		}
	}
	
	public static AppSystem createSystem( EngineTransaction transaction , EngineDirectory directory , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		if( directory.findProduct( name ) != null )
			transaction.exit1( _Error.DuplicateSystem1 , "system=" + name + " is not unique" , name );
		
		EngineEntities entities = transaction.getEntities();
		EngineSettings settings = transaction.getSettings(); 
		
		ObjectProperties props = entities.createSystemProps( settings.getEngineProperties() );
		AppSystem system = new AppSystem( directory , props );
		system.createSystem( name , desc );
		DBAppSystem.modifySystem( c , system , true );
		props.setOwnerId( system.ID );
		DBSettings.savedbEntityCustom( c , props , system.SV );
		
		directory.addSystem( system );
		return( system );
	}

	public static void modifySystem( EngineTransaction transaction , EngineDirectory directory , AppSystem system , String name , String desc ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		system.modifySystem( name , desc );
		DBAppSystem.modifySystem( c , system , false );
		directory.updateSystem( system );
	}
	
	public static void setSystemOffline( EngineTransaction transaction , EngineDirectory directory , AppSystem system , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		system.setOffline( offline );
		DBAppSystem.modifySystem( c , system , false );
	}
	
	public static void deleteSystem( EngineTransaction transaction , EngineDirectory directory , AppSystem system ) throws Exception {
		if( !directory.isSystemEmpty( system ) )
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
		
		// create product
		AppProduct product = new AppProduct( directory , system );
		product.createProduct( name , desc , path );
		DBAppProduct.modifyProduct( c , product , true );
		
		// create initial policy
		DBAppProduct.createdbPolicy( c , directory , product );
		
		directory.addMatchedProduct( product );
		return( product );
	}
	
	public static void modifyProduct( EngineTransaction transaction , EngineDirectory directory , AppProduct product , String name , String desc , String path ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.modifyProduct( name , desc , path );
		DBAppProduct.modifyProduct( c , product , false );
		directory.updateProduct( product );
	}
	
	public static void setProductOffline( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean offline ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.setOffline( offline );
		DBAppProduct.modifyProduct( c , product , false );
	}
	
	public static void setMonitoringEnabled( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean enabled ) throws Exception {
		DBConnection c = transaction.getConnection();
		product.setMonitoringEnabled( enabled );
		DBAppProduct.modifyProduct( c , product , false );
	}
	
	public static void deleteProduct( EngineTransaction transaction , EngineDirectory directory , AppProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		DBConnection c = transaction.getConnection();
		if( directory.getProduct( product.ID ) != product )
			transaction.exit( _Error.UnknownProduct1 , "product=" + product.NAME + " is unknown or mismatched" , new String[] { product.NAME } );
		
		DBAppProduct.deleteProduct( c , product );
		directory.removeProduct( product );
		
		if( fsDeleteFlag ) {
			ActionBase action = transaction.getAction();
			UrmStorage storage = action.artefactory.getUrmStorage();
			LocalFolder products = storage.getServerProductsFolder( action );
			LocalFolder productfolder = products.getSubFolder( action , product.PATH );
			productfolder.removeThis( action );
		}
		
		product.deleteObject();
	}

	public static void modifyProductVersion( EngineTransaction transaction , EngineDirectory directory , AppProduct product , int majorLastFirstNumber , int majorLastSecondNumber , int lastProdTag , int lastUrgentTag , int majorNextFirstNumber , int majorNextSecondNumber , int nextProdTag , int nextUrgentTag ) throws Exception {
		DBConnection c = transaction.getConnection();
		
		product.setVersions( majorLastFirstNumber , majorLastSecondNumber , lastProdTag , lastUrgentTag , majorNextFirstNumber , majorNextSecondNumber , nextProdTag , nextUrgentTag );
		DBAppProduct.modifyProduct( c , product , false );
		
		EngineProductRevisions revisions = product.findRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			MetaProductSettings settings = storage.getSettings();
			settings.updateSettings( product );
		}
	}
	
}
