package org.urm.db.engine;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.DBNames;
import org.urm.db.DBVersions;
import org.urm.db.DBEnums.DBEnumObjectType;
import org.urm.db.system.DBSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.System;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DBEngineDirectory {

	public static void load( EngineDirectory directory , Node root , DBConnection c , boolean savedb ) throws Exception {
		if( savedb ) {
			if( root == null )
				return;
			
			Node[] items = ConfReader.xmlGetChildren( root , "system" );
			if( items == null )
				return;
			
			for( Node itemNode : items ) {
				System system = loadSystem( directory , itemNode );
				matchSystem( directory , system , c , savedb );
				
				int systemId = DBNames.getNameIndex( c , DBVersions.CORE_ID , system.NAME , DBEnumObjectType.SYSTEM );
				int SV = DBVersions.getCurrentVersion( c , systemId );
				SV = SV + 1;
				DBSystem.insert( c , systemId , SV , system );
			}
		}
		else {
			System[] systems = DBSystem.load( c , directory );
			for( System system : systems )
				matchSystem( directory , system , c , savedb );
		}
		
		for( System system : directory.getSystems() ) {
			for( Product product : system.getProducts() )
				directory.addProduct( product );
		}
	}
	
	public static void save( EngineDirectory directory , Document doc , Element root ) throws Exception {
		// directory 
		for( System system : directory.getSystems() ) {
			Element elementSystem = Common.xmlCreateElement( doc , root , "system" );
			saveSystem( system , doc , elementSystem );
		}
	}

	private static System loadSystem( EngineDirectory directory , Node root ) throws Exception {
		System system = DBSystem.load( directory , root );
		Node[] items = ConfReader.xmlGetChildren( root , "product" );
		if( items == null )
			return( system );
		
		for( Node itemNode : items ) {
			Product item = new Product( directory , system );
			item.load( itemNode );
			system.addProduct( item );
		}
		
		return( system );
	}
	
	private static void saveSystem( System system , Document doc , Element root ) throws Exception {
		DBSystem.save( system , doc , root );
		
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			Element elementProduct = Common.xmlCreateElement( doc , root , "product" );
			product.save( doc , elementProduct );
		}
	}

	public static void matchSystem( EngineDirectory directory , System system , DBConnection c , boolean savedb ) throws Exception {
		system.MATCHED = true;
		directory.addSystem( system );
	}
	
}
