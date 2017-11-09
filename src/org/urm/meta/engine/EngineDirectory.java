package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.db.DBConnection;
import org.urm.db.meta.DBSystem;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineDirectory extends EngineObject {

	public EngineRegistry registry;
	public Engine engine;

	private Map<String,System> mapSystems;
	private Map<String,Product> mapProducts;
	
	public EngineDirectory( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		this.engine = registry.loader.engine;
		mapSystems = new HashMap<String,System>();
		mapProducts = new HashMap<String,Product>();
	}

	@Override
	public String getName() {
		return( "server-directory" );
	}
	
	public void load( Node root , DBConnection c , boolean savedb ) throws Exception {
		if( savedb ) {
			if( root == null )
				return;
			
			Node[] items = ConfReader.xmlGetChildren( root , "system" );
			if( items == null )
				return;
			
			for( Node itemNode : items ) {
				System item = new System( this );
				item.load( itemNode );
				DBSystem.insert( c , registry.loader.SV , item );
				mapSystems.put( item.NAME , item );
			}
		}
		else {
			System[] systems = DBSystem.load( c , this );
			for( System system : systems )
				mapSystems.put( system.NAME , system );
		}
		
		for( System system : mapSystems.values() ) {
			for( Product product : system.getProducts() )
				mapProducts.put( product.NAME , product );
		}
	}
	
	public EngineDirectory copy() throws Exception {
		EngineDirectory r = new EngineDirectory( registry );
		
		for( System system : mapSystems.values() ) {
			System rs = system.copy( r );
			r.mapSystems.put( rs.NAME , rs );
			
			for( Product rp : rs.mapProducts.values() )
				r.mapProducts.put( rp.NAME , rp );
		}

		return( r );
	}
	
	public String[] getSystemNames() {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public String[] getProducts() {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public String[] getSystemProducts( String systemName ) {
		System system = findSystem( systemName );
		if( system == null )
			return( new String[0] );
		return( system.getProductNames() );
	}
	
	public System findSystem( System system ) {
		if( system == null )
			return( null );
		return( mapSystems.get( system.NAME ) );
	}
	
	public System findSystem( String name ) {
		return( mapSystems.get( name ) );
	}
	
	public Product findProduct( Product product ) {
		if( product == null )
			return( null );
		return( mapProducts.get( product.NAME ) );
	}
	
	public Product findProduct( String name ) {
		return( mapProducts.get( name ) );
	}
	
	public void save( Document doc , Element root ) throws Exception {
		// directory 
		for( System system : mapSystems.values() ) {
			Element elementSystem = Common.xmlCreateElement( doc , root , "system" );
			system.save( doc , elementSystem );
		}
	}

	public void addSystem( EngineTransaction t , System system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			t.exit( _Error.DuplicateSystem1 , "system=" + system.NAME + " is not unique" , new String[] { system.NAME } );
		DBSystem.insert( t.connection , t.CV , system );
		mapSystems.put( system.NAME , system );
	}

	public void deleteSystem( EngineTransaction transaction , System system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			transaction.exit( _Error.TransactionSystemOld1 , "system=" + system.NAME + " is unknown or mismatched" , new String[] { system.NAME } );
		
		for( String productName : system.getProductNames() )
			mapProducts.remove( productName );
		
		mapSystems.remove( system.NAME );
	}

	public System getSystem( String name ) throws Exception {
		System system = findSystem( name );
		if( system == null )
			Common.exit1( _Error.UnknownSystem1 , "unknown system=" + name , name );
		return( system );
	}

	public Product getProduct( String name ) throws Exception {
		Product product = findProduct( name );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + name , name );
		return( product );
	}

	public void createProduct( EngineTransaction transaction , Product product ) throws Exception {
		if( mapProducts.containsKey( product.NAME ) )
			transaction.exit( _Error.DuplicateProduct1 , "product=" + product.NAME + " is not unique" , new String[] { product.NAME } );
		
		mapProducts.put( product.NAME , product );
		product.system.addProduct( transaction , product );
	}
	
	public void deleteProduct( EngineTransaction transaction , Product product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		if( mapProducts.get( product.NAME ) != product )
			transaction.exit( _Error.UnknownProduct1 , "product=" + product.NAME + " is unknown or mismatched" , new String[] { product.NAME } );
		
		mapProducts.remove( product.NAME );
		product.system.removeProduct( transaction , product );
		
		ActionBase action = transaction.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder products = storage.getServerProductsFolder( action );
		LocalFolder productfolder = products.getSubFolder( action , product.PATH );
		productfolder.removeThis( action );
	}

}
