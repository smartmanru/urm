package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.system.DBProduct;
import org.urm.db.system.DBSystem;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineMatcher;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.AppSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineDirectory {

	public static void importxml( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		loadxml( loader , directory , root );
		resolvexml( loader , directory );
		matchxml( loader , directory );
	}
	
	public static void loaddb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		readdb( loader , directory );
		resolvedb( loader , directory );
		matchdb( loader , directory , false );
	}
	
	public static void loadxml( EngineLoader loader , EngineDirectory directory , Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , "system" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			AppSystem system = DBSystem.importxml( loader , directory , itemNode );
			directory.addSystem( system );
			for( Product product : system.getProducts() )
				directory.addProduct( product );
		}
	}
		
	public static void readdb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		AppSystem[] systems = DBSystem.loaddb( loader , directory );
		for( AppSystem system : systems )
			directory.addSystem( system );
		
		Product[] products = DBProduct.loaddb( loader , directory );
		for( Product product : products ) {
			product.system.addProduct( product );
			directory.addProduct( product );
		}
	}

	public static void resolvexml( EngineLoader loader , EngineDirectory directory ) throws Exception {
		for( AppSystem system : directory.getSystems() )
			DBSystem.resolvexml( loader , directory , system );
	}

	public static void resolvedb( EngineLoader loader , EngineDirectory directory ) throws Exception {
		for( AppSystem system : directory.getSystems() )
			DBSystem.resolvedb( loader , directory , system );
	}
	
	public static void matchxml( EngineLoader loader , EngineDirectory directory ) throws Exception {
		DBConnection c = loader.getConnection();
		EngineMatcher matcher = loader.getMatcher();
		for( AppSystem system : directory.getSystems() ) {
			int systemId = DBSystem.getSystemIdByName( c , system.NAME );
			matcher.prepareMatch( systemId , false , false );
			DBSystem.matchxml( loader , directory , system );
		}
	}
	
	public static void matchdb( EngineLoader loader , EngineDirectory directory , boolean update ) throws Exception {
		EngineMatcher matcher = loader.getMatcher();
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
		}
	}
	
	public static void exportxml( EngineLoader loader , EngineDirectory directory , Document doc , Element root ) throws Exception {
		// directory 
		for( AppSystem system : directory.getSystems() ) {
			Element elementSystem = Common.xmlCreateElement( doc , root , "system" );
			DBSystem.exportxml( loader , directory , system , doc , elementSystem );
		}
	}

}
