package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBNames;
import org.urm.db.DBVersions;
import org.urm.db.DBEnums.DBEnumObjectType;
import org.urm.db.system.DBSystem;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineObject;

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

	public EngineDirectory copy() throws Exception {
		EngineDirectory r = new EngineDirectory( registry );
		
		for( System system : mapSystems.values() ) {
			System rs = system.copy( r );
			r.addSystem( rs );
			
			for( Product rp : rs.getProducts() )
				r.addProduct( rp );
		}

		return( r );
	}
	
	public String[] getSystemNames() {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public System[] getSystems() {
		return( mapSystems.values().toArray( new System[0] ) );
	}
	
	public String[] getProductNames() {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public Product[] getProducts() {
		return( mapProducts.values().toArray( new Product[0] ) );
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
	
	public void createSystem( EngineTransaction t , System system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			t.exit( _Error.DuplicateSystem1 , "system=" + system.NAME + " is not unique" , new String[] { system.NAME } );
		
		int systemId = DBNames.getNameIndex( t.connection , DBVersions.CORE_ID , system.NAME , DBEnumObjectType.SYSTEM );
		DBSystem.insert( t.connection , systemId , t.getNextSystemVersion( systemId ) , system );
		addSystem( system );
	}

	public void addSystem( System system ) throws Exception {
		mapSystems.put( system.NAME , system );
	}
	
	public void addProduct( Product product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void modifySystem( EngineTransaction t , System system ) throws Exception {
		if( Common.changeMapKey( mapSystems , system , system.NAME ) )
			DBNames.updateName( t.connection , DBVersions.CORE_ID , system.NAME , system.ID , DBEnumObjectType.SYSTEM );
		DBSystem.update( t.connection , t.getNextSystemVersion( system.ID ) , system );
	}
	
	public void deleteSystem( EngineTransaction t , System system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			t.exit( _Error.TransactionSystemOld1 , "system=" + system.NAME + " is unknown or mismatched" , new String[] { system.NAME } );
		
		DBSystem.delete( t.connection , t.CV , system );
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
		
		addProduct( product );
		product.system.addProduct( product );
	}
	
	public void deleteProduct( EngineTransaction transaction , Product product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		if( mapProducts.get( product.NAME ) != product )
			transaction.exit( _Error.UnknownProduct1 , "product=" + product.NAME + " is unknown or mismatched" , new String[] { product.NAME } );
		
		mapProducts.remove( product.NAME );
		product.system.removeProduct( product );
		
		ActionBase action = transaction.getAction();
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder products = storage.getServerProductsFolder( action );
		LocalFolder productfolder = products.getSubFolder( action , product.PATH );
		productfolder.removeThis( action );
	}

}
