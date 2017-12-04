package org.urm.meta.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;
import org.urm.meta.UnmatchedSystem;

public class EngineDirectory extends EngineObject {

	public Engine engine;

	private Map<String,AppSystem> mapSystems;
	private Map<String,AppProduct> mapProducts;
	private Map<Integer,AppSystem> mapSystemsById;
	private Map<Integer,AppProduct> mapProductsById;
	
	private Map<String,UnmatchedSystem> mapSystemUnmatched;
	private Map<String,Integer> mapProductUnmatched;
	private Map<String,Integer> mapEnvUnmatched;
	
	public EngineDirectory( Engine engine ) {
		super( null );
		this.engine = engine;
		mapSystems = new HashMap<String,AppSystem>();
		mapProducts = new HashMap<String,AppProduct>();
		mapSystemsById = new HashMap<Integer,AppSystem>();
		mapProductsById = new HashMap<Integer,AppProduct>();
		
		mapSystemUnmatched = new HashMap<String,UnmatchedSystem>();
		mapProductUnmatched = new HashMap<String,Integer>();
		mapEnvUnmatched = new HashMap<String,Integer>();
	}

	@Override
	public String getName() {
		return( "server-directory" );
	}

	public EngineDirectory copy( TransactionBase transaction ) throws Exception {
		EngineDirectory r = new EngineDirectory( engine );
		
		EngineSettings settings = transaction.getSettings();
		for( AppSystem system : mapSystems.values() ) {
			ObjectProperties props = system.getParameters();
			ObjectProperties rprops = props.copy( settings.getEngineProperties() );
			AppSystem rs = system.copy( r , rprops );
			r.addSystem( rs );
			
			for( AppProduct rp : rs.getProducts() )
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
	
	public AppProduct[] getProducts() {
		return( mapProducts.values().toArray( new AppProduct[0] ) );
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
		return( mapSystemsById.get( id ) );
	}
	
	public AppProduct findProduct( AppProduct product ) {
		if( product == null )
			return( null );
		return( mapProducts.get( product.NAME ) );
	}
	
	public AppProduct findProduct( String name ) {
		return( mapProducts.get( name ) );
	}
	
	public AppProduct findProduct( int id ) {
		return( mapProductsById.get( id ) );
	}
	
	public void addSystem( AppSystem system ) {
		mapSystems.put( system.NAME , system );
		if( system.ID > 0 )
			mapSystemsById.put( system.ID , system );
	}

	public void unloadAll() {
		mapSystems.clear();
		mapSystemsById.clear();
		mapProducts.clear();
		mapProductsById.clear();
		mapProductUnmatched.clear();
		mapEnvUnmatched.clear();
	}
	
	public void unloadSystem( AppSystem system ) {
		mapSystems.remove( system.NAME );
		mapSystemsById.remove( system.ID );
		for( AppProduct product : system.getProducts() )
			unloadProduct( product );
	}

	public void addUnmatchedSystem( AppSystem system ) {
		UnmatchedSystem unmatched = new UnmatchedSystem( system );  
		mapSystemUnmatched.put( system.NAME , unmatched );
	}
	
	public void unloadProduct( AppProduct product ) {
		mapProducts.remove( product.NAME );
		mapProductsById.remove( product.ID );
		product.system.removeProduct( product );
	}
	
	public void addProduct( AppProduct product ) throws Exception {
		mapProducts.put( product.NAME , product );
		mapProductsById.put( product.ID , product );
		product.system.addProduct( product );
	}
	
	public void updateSystem( AppSystem system ) throws Exception {
		Common.changeMapKey( mapSystems , system , system.NAME );
	}
	
	public void updateProduct( AppProduct product ) throws Exception {
		Common.changeMapKey( mapProducts , product , product.NAME );
		product.system.updateProduct( product );
	}

	public void removeSystem( AppSystem system ) {
		unloadSystem( system );
	}
	
	public void removeProduct( AppProduct product ) throws Exception {
		unloadProduct( product );
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

	public AppProduct getProduct( String name ) throws Exception {
		AppProduct product = findProduct( name );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + name , name );
		return( product );
	}

	public AppProduct getProduct( int id ) throws Exception {
		AppProduct product = findProduct( id );
		if( product == null )
			Common.exit1( _Error.UnknownProduct1 , "unknown product=" + id , "" + id );
		return( product );
	}

	public void unloadProducts() {
		mapProductUnmatched.clear();
		mapEnvUnmatched.clear();
	}	
	
	public void checkSystemNameBusy( String name ) throws Exception {
		if( mapSystemUnmatched.containsKey( name ) )
			Common.exit1( _Error.DuplicateSystemNameUnmatched1 , "System with name=" + name + " + already exists, unmatched" , name );
	}
	
	public void checkProductNameBusy( String name ) throws Exception {
		if( mapProductUnmatched.containsKey( name ) )
			Common.exit1( _Error.DuplicateProductNameUnmatched1 , "Product with name=" + name + " + already exists, unmatched" , name );
	}
	
	public void checkEnvNameBusy( String product , String name ) throws Exception {
		if( mapEnvUnmatched.containsKey( product + "::" + name ) )
			Common.exit2( _Error.DuplicateEnvNameUnmatched2 , "Environment with name=" + name + " + already exists in product=" + product + ", unmatched" , product , name );
	}

	public UnmatchedSystem[] getSystemsUnmatched() {
		List<UnmatchedSystem> list = new LinkedList<UnmatchedSystem>();
		for( String name : Common.getSortedKeys( mapSystemUnmatched ) ) {
			UnmatchedSystem system = mapSystemUnmatched.get( name );
			list.add( system );
		}
		
		return( list.toArray( new UnmatchedSystem[0] ) );
	}

}
