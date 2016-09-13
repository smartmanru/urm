package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.server.action.ActionBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerDirectory {

	public ServerRegistry registry;
	public ServerEngine engine;

	private Map<String,ServerSystem> mapSystems;
	private Map<String,ServerProduct> mapProducts;
	
	public ServerDirectory( ServerRegistry registry ) {
		this.registry = registry;
		this.engine = registry.loader.engine;
		mapSystems = new HashMap<String,ServerSystem>();
		mapProducts = new HashMap<String,ServerProduct>();
	}

	public void load( Node root ) throws Exception {
		if( root == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( root , "system" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			ServerSystem item = new ServerSystem( this );
			item.load( itemNode );
			mapSystems.put( item.NAME , item );
			
			for( ServerProduct product : item.mapProducts.values() )
				mapProducts.put( product.NAME , product );
		}
	}
	
	public ServerDirectory copy() throws Exception {
		ServerDirectory r = new ServerDirectory( registry );
		
		for( ServerSystem system : mapSystems.values() ) {
			ServerSystem rs = system.copy( r );
			r.mapSystems.put( rs.NAME , rs );
			
			for( ServerProduct rp : rs.mapProducts.values() )
				r.mapProducts.put( rp.NAME , rp );
		}

		return( r );
	}
	
	public String[] getSystems() {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public String[] getProducts() {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public ServerSystem findSystem( ServerSystem system ) {
		if( system == null )
			return( null );
		return( mapSystems.get( system.NAME ) );
	}
	
	public ServerSystem findSystem( String name ) {
		return( mapSystems.get( name ) );
	}
	
	public ServerProduct findProduct( ServerProduct product ) {
		if( product == null )
			return( null );
		return( mapProducts.get( product.NAME ) );
	}
	
	public ServerProduct findProduct( String name ) {
		return( mapProducts.get( name ) );
	}
	
	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		// directory 
		for( ServerSystem system : mapSystems.values() ) {
			Element elementSystem = Common.xmlCreateElement( doc , root , "system" );
			Common.xmlSetElementAttr( doc , elementSystem , "name" , system.NAME );
			Common.xmlSetElementAttr( doc , elementSystem , "desc" , system.DESC );
			
			for( String productName : system.getProducts() ) {
				ServerProduct product = system.getProduct( productName );
				Element elementProduct = Common.xmlCreateElement( doc , elementSystem , "product" );
				Common.xmlSetElementAttr( doc , elementProduct , "name" , product.NAME );
				Common.xmlSetElementAttr( doc , elementProduct , "desc" , product.DESC );
				Common.xmlSetElementAttr( doc , elementProduct , "path" , product.PATH );
			}
		}
	}

	public void addSystem( ServerTransaction transaction , ServerSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			transaction.exit( _Error.DuplicateSystem1 , "system=" + system.NAME + " is not unique" , new String[] { system.NAME } );
		mapSystems.put( system.NAME , system );
	}

	public void deleteSystem( ServerTransaction transaction , ServerSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			transaction.exit( _Error.TransactionSystemOld1 , "system=" + system.NAME + " is unknown or mismatched" , new String[] { system.NAME } );
		
		for( String productName : system.getProducts() )
			mapProducts.remove( productName );
		
		mapSystems.remove( system.NAME );
	}

	public ServerSystem getSystem( String name ) throws Exception {
		ServerSystem system = findSystem( name );
		if( system == null )
			Common.exit1( _Error.UnknownSystem1 , "unknown system=" + name , name );
		return( system );
	}

	public ServerProduct getProduct( String name ) throws Exception {
		ServerProduct product = findProduct( name );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + name , name );
		return( product );
	}

	public void createProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		if( mapProducts.containsKey( product.NAME ) )
			transaction.exit( _Error.DuplicateProduct1 , "product=" + product.NAME + " is not unique" , new String[] { product.NAME } );
		mapProducts.put( product.NAME , product );
		product.system.addProduct( transaction , product );
	}
	
	public void deleteProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		if( mapProducts.get( product.NAME ) != product )
			transaction.exit( _Error.UnknownProduct1 , "product=" + product.NAME + " is unknown or mismatched" , new String[] { product.NAME } );
		
		mapProducts.remove( product.NAME );
		product.system.removeProduct( transaction , product );
	}

}
