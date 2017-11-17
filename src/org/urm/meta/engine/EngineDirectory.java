package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.DBNames;
import org.urm.db.DBVersions;
import org.urm.db.DBEnums.DBEnumObjectType;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.system.DBSystem;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.EngineObject;
import org.w3c.dom.Node;

public class EngineDirectory extends EngineObject {

	public EngineRegistry registry;
	public Engine engine;

	private Map<String,AppSystem> mapSystems;
	private Map<String,Product> mapProducts;
	private Map<Integer,AppSystem> mapSystemsDB;
	private Map<Integer,Product> mapProductsDB;
	
	public EngineDirectory( EngineRegistry registry ) {
		super( registry );
		this.registry = registry;
		this.engine = registry.engine;
		mapSystems = new HashMap<String,AppSystem>();
		mapProducts = new HashMap<String,Product>();
		mapSystemsDB = new HashMap<Integer,AppSystem>();
		mapProductsDB = new HashMap<Integer,Product>();
	}

	@Override
	public String getName() {
		return( "server-directory" );
	}

	public void loadxml( Node root , DBConnection c ) throws Exception {
		DBEngineDirectory.loadxml( this , root , c );
		DBEngineDirectory.resolvexml( this );
		DBEngineDirectory.matchxml( this );
		DBEngineDirectory.savedb( this , c );
	}
	
	public void loaddb( DBConnection c ) throws Exception {
		DBEngineDirectory.loaddb( this , c );
		DBEngineDirectory.resolvedb( this );
		DBEngineDirectory.matchdb( this , false );
	}
	
	public EngineDirectory copy() throws Exception {
		EngineDirectory r = new EngineDirectory( registry );
		
		for( AppSystem system : mapSystems.values() ) {
			AppSystem rs = system.copy( r );
			r.addSystem( rs );
			
			for( Product rp : rs.getProducts() )
				r.addProduct( rp );
		}

		return( r );
	}
	
	public String[] getSystemNames() {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public AppSystem[] getSystems() {
		return( mapSystems.values().toArray( new AppSystem[0] ) );
	}
	
	public String[] getProductNames() {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public Product[] getProducts() {
		return( mapProducts.values().toArray( new Product[0] ) );
	}
	
	public String[] getSystemProducts( String systemName ) {
		AppSystem system = findSystem( systemName );
		if( system == null )
			return( new String[0] );
		return( system.getProductNames() );
	}
	
	public AppSystem findSystem( AppSystem system ) {
		if( system == null )
			return( null );
		return( mapSystems.get( system.NAME ) );
	}
	
	public AppSystem findSystem( String name ) {
		return( mapSystems.get( name ) );
	}
	
	public AppSystem findSystem( int id ) {
		return( mapSystemsDB.get( id ) );
	}
	
	public Product findProduct( Product product ) {
		if( product == null )
			return( null );
		return( mapProducts.get( product.NAME ) );
	}
	
	public Product findProduct( String name ) {
		return( mapProducts.get( name ) );
	}
	
	public Product findProduct( int id ) {
		return( mapProductsDB.get( id ) );
	}
	
	public void createSystem( EngineTransaction t , AppSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			t.exit( _Error.DuplicateSystem1 , "system=" + system.NAME + " is not unique" , new String[] { system.NAME } );
		
		int systemId = DBNames.getNameIndex( t.connection , DBVersions.CORE_ID , system.NAME , DBEnumObjectType.SYSTEM );
		DBSystem.insert( t.connection , systemId , system );
		addSystem( system );
	}

	public void addSystem( AppSystem system ) throws Exception {
		mapSystems.put( system.NAME , system );
		if( system.ID > 0 )
			mapSystemsDB.put( system.ID , system );
	}
	
	public void unloadSystem( AppSystem system ) throws Exception {
		mapSystems.remove( system.NAME );
		mapSystemsDB.remove( system.ID );
		for( Product product : system.getProducts() )
			unloadProduct( product );
	}
	
	public void unloadProduct( Product product ) throws Exception {
		mapProducts.remove( product.NAME );
		mapProductsDB.remove( product.ID );
		product.system.removeProduct( product );
	}
	
	public void addProduct( Product product ) throws Exception {
		mapProducts.put( product.NAME , product );
		if( product.ID > 0 )
			mapProductsDB.put( product.ID , product );
	}
	
	public void modifySystem( EngineTransaction t , AppSystem system ) throws Exception {
		registry.data.checkSystemNameBusy( system.NAME );
		if( Common.changeMapKey( mapSystems , system , system.NAME ) )
			DBNames.updateName( t.connection , DBVersions.CORE_ID , system.NAME , system.ID , DBEnumObjectType.SYSTEM );
		DBSystem.update( t.connection , system );
	}
	
	public void deleteSystem( EngineTransaction t , AppSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			t.exit( _Error.TransactionSystemOld1 , "system=" + system.NAME + " is unknown or mismatched" , new String[] { system.NAME } );
		
		DBSystem.delete( t.connection , system );
		for( String productName : system.getProductNames() )
			mapProducts.remove( productName );
		
		mapSystems.remove( system.NAME );
	}

	public AppSystem getSystem( String name ) throws Exception {
		AppSystem system = findSystem( name );
		if( system == null )
			Common.exit1( _Error.UnknownSystem1 , "unknown system=" + name , name );
		return( system );
	}

	public AppSystem getSystem( int id ) throws Exception {
		AppSystem system = findSystem( id );
		if( system == null )
			Common.exit1( _Error.UnknownSystem1 , "unknown system=" + id , "" + id );
		return( system );
	}

	public Product getProduct( String name ) throws Exception {
		Product product = findProduct( name );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + name , name );
		return( product );
	}

	public Product getProduct( int id ) throws Exception {
		Product product = findProduct( id );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + id , "" + id );
		return( product );
	}

	public void createProduct( EngineTransaction transaction , Product product ) throws Exception {
		if( mapProducts.containsKey( product.NAME ) )
			transaction.exit( _Error.DuplicateProduct1 , "product=" + product.NAME + " is not unique" , new String[] { product.NAME } );
		
		registry.data.checkProductNameBusy( product.NAME );
		
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
