package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.system.DBProduct;
import org.urm.db.system.DBSystem;
import org.urm.meta.EngineMatcher;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.AppSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineDirectory {

	public static void loadxml( EngineDirectory directory , Node root , DBConnection c ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , "system" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			AppSystem system = DBSystem.loadxml( directory , itemNode );
			directory.addSystem( system );
			for( Product product : system.getProducts() )
				directory.addProduct( product );
		}
	}
		
	public static void loaddb( EngineDirectory directory , DBConnection c ) throws Exception {
		AppSystem[] systems = DBSystem.loaddb( directory , c );
		for( AppSystem system : systems )
			directory.addSystem( system );
		
		Product[] products = DBProduct.loaddb( directory , c );
		for( Product product : products ) {
			product.system.addProduct( product );
			directory.addProduct( product );
		}
	}

	public static void resolvexml( EngineDirectory directory ) throws Exception {
		for( AppSystem system : directory.getSystems() )
			DBSystem.resolvexml( directory , system );
	}

	public static void resolvedb( EngineDirectory directory ) throws Exception {
		for( AppSystem system : directory.getSystems() )
			DBSystem.resolvedb( directory , system );
	}
	
	public static void matchxml( EngineDirectory directory , EngineMatcher matcher ) throws Exception {
		for( AppSystem system : directory.getSystems() ) {
			DBSystem.matchxml( directory , system );
			if( !system.MATCHED )
				directory.unloadSystem( system );
		}
	}
	
	public static void matchdb( EngineDirectory directory , EngineMatcher matcher , boolean update ) throws Exception {
		for( AppSystem system : directory.getSystems() ) {
			if( update ) {
				matcher.prepareMatch( system.ID );
				DBSystem.matchdb( directory , matcher , system , true );
			}
			else
			if( system.MATCHED )
				DBSystem.matchdb( directory , matcher , system , false );
				
			if( !system.MATCHED )
				directory.unloadSystem( system );
		}
	}
	
	public static void savedb( EngineDirectory directory , DBConnection c ) throws Exception {
		for( AppSystem system : directory.getSystems() ) {
			DBSystem.savedb( directory , system , c );
		}
	}
	
	public static void savexml( EngineDirectory directory , Document doc , Element root ) throws Exception {
		// directory 
		for( AppSystem system : directory.getSystems() ) {
			Element elementSystem = Common.xmlCreateElement( doc , root , "system" );
			DBSystem.savexml( directory , system , doc , elementSystem );
		}
	}

}
