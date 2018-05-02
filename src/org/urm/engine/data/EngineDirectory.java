package org.urm.engine.data;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.loader.EngineObject;

public class EngineDirectory extends EngineObject {

	public Engine engine;
	public DataService data;

	private Map<String,AppSystem> mapSystems;
	private Map<String,AppProduct> mapProducts;
	private Map<Integer,AppSystem> mapSystemsById;
	private Map<Integer,AppProduct> mapProductsById;
	
	private Map<Integer,AppSystem> mapSystemUnmatched;
	private Map<Integer,AppProduct> mapProductUnmatched;
	
	public EngineDirectory( Engine engine , DataService data ) {
		super( null );
		this.engine = engine;
		this.data = data;
		
		mapSystems = new HashMap<String,AppSystem>();
		mapProducts = new HashMap<String,AppProduct>();
		mapSystemsById = new HashMap<Integer,AppSystem>();
		mapProductsById = new HashMap<Integer,AppProduct>();
		
		mapSystemUnmatched = new HashMap<Integer,AppSystem>();
		mapProductUnmatched = new HashMap<Integer,AppProduct>();
	}

	@Override
	public String getName() {
		return( "server-directory" );
	}

	public EngineDirectory copy( TransactionBase transaction ) throws Exception {
		EngineDirectory r = new EngineDirectory( engine , data );
		
		for( AppSystem system : mapSystems.values() ) {
			AppSystem rs = r.copySystem( transaction , system );
			r.addSystem( rs );
		}

		for( AppSystem system : mapSystemUnmatched.values() ) {
			AppSystem rs = r.copySystem( transaction , system );
			r.addUnmatchedSystem( rs );
		}

		for( AppProduct product : mapProductUnmatched.values() ) {
			AppSystem rs = r.findSystem( product.system );
			AppProduct rp = product.copy( r , rs );
			r.addUnmatchedProduct( rp );
		}

		return( r );
	}

	private AppSystem copySystem( TransactionBase transaction , AppSystem system ) throws Exception {
		EngineSettings settings = transaction.getSettings();
		ObjectProperties props = system.getParameters();
		ObjectProperties rprops = props.copy( settings.getEngineProperties() );
		AppSystem rs = system.copy( this , rprops );
		addSystem( rs );
		
		for( AppProduct rp : rs.getProducts() )
			addMatchedProduct( rp );
		
		return( rs );
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
	
	public String[] getSystemProductNames( String systemName ) {
		AppSystem system = findSystem( systemName );
		if( system == null )
			return( new String[0] );
		return( system.getProductNames() );
	}
	
	public AppSystem findSystem( AppSystem system ) {
		if( system == null )
			return( null );
		return( findSystem( system.ID ) );
	}
	
	public AppSystem findSystem( String name ) {
		AppSystem system = mapSystems.get( name );
		if( system != null )
			return( system );
		
		for( AppSystem find : mapSystemUnmatched.values() ) {
			if( name.equals( find.NAME ) )
				return( find );
		}
		return( null );
	}
	
	public AppSystem findSystem( int id ) {
		AppSystem find = mapSystemsById.get( id );
		if( find != null )
			return( find );
		return( mapSystemUnmatched.get( id ) );
	}
	
	public AppProduct findProduct( AppProduct product ) {
		if( product == null )
			return( null );
		return( findProduct( product.ID ) );
	}
	
	public AppProduct findProduct( String name ) {
		AppProduct product = mapProducts.get( name );
		if( product != null )
			return( product );
		
		for( AppProduct find : mapProductUnmatched.values() ) {
			if( name.equals( find.NAME ) )
				return( find );
		}
		return( null );
	}
	
	public AppProduct findProduct( int id ) {
		AppProduct find = mapProductsById.get( id );
		if( find != null )
			return( find );
		return( mapProductUnmatched.get( id ) );
	}
	
	public void addSystem( AppSystem system ) throws Exception {
		if( !system.MATCHED )
			Common.exitUnexpected();
		
		mapSystemUnmatched.remove( system.ID );
		mapSystems.put( system.NAME , system );
		if( system.ID > 0 )
			mapSystemsById.put( system.ID , system );
	}

	public void removeAll() {
		mapSystems.clear();
		mapSystemsById.clear();
		mapSystemUnmatched.clear();
		
		mapProducts.clear();
		mapProductsById.clear();
		mapProductUnmatched.clear();
	}
	
	public void removeSystem( AppSystem system ) {
		mapSystems.remove( system.NAME );
		mapSystemsById.remove( system.ID );
		mapSystemUnmatched.remove( system.ID );
		
		for( AppProduct product : system.getProducts() )
			removeProduct( product );
		
		for( AppProduct product : mapProductUnmatched.values().toArray( new AppProduct[0] ) ) {
			if( product.system.ID == system.ID )
				removeProduct( product );
		}
	}

	public void addUnmatchedSystem( AppSystem system ) throws Exception {
		mapSystemUnmatched.put( system.ID , system );
	}
	
	public void addUnmatchedProduct( AppProduct product ) {
		mapProductUnmatched.put( product.ID , product );
	}
	
	public void removeProduct( AppProduct product ) {
		mapProducts.remove( product.NAME );
		mapProductsById.remove( product.ID );
		mapProductUnmatched.remove( product.ID );
		product.system.removeProduct( product );
	}
	
	public void addMatchedProduct( AppProduct product ) {
		product.setMatched( true );
		mapProductUnmatched.remove( product.ID );
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

	public void checkSystemNameBusy( String name ) throws Exception {
		if( findSystem( name ) != null )
			Common.exit1( _Error.DuplicateSystemNameUnmatched1 , "System with name=" + name + " + already exists, unmatched" , name );
	}
	
	public void checkProductNameBusy( String name ) throws Exception {
		if( findProduct( name ) != null )
			Common.exit1( _Error.DuplicateProductNameUnmatched1 , "Product with name=" + name + " + already exists, unmatched" , name );
	}
	
	public String[] getAllSystemNames() {
		Map<String,AppSystem> map = new HashMap<String,AppSystem>();
		map.putAll( mapSystems );
		
		for( AppSystem system : mapSystemUnmatched.values() )
			map.put( system.NAME , system );
		
		return( Common.getSortedKeys( map ) );
	}

	public String[] getAllProductNames( AppSystem system ) {
		Map<String,AppProduct> map = new HashMap<String,AppProduct>();
		for( AppProduct product : mapProducts.values() ) {
			if( system == null || product.system.ID == system.ID )
				map.put( product.NAME , product );
		}
		
		for( AppProduct product : mapProductUnmatched.values() ) {
			if( system == null || product.system.ID == system.ID )
				map.put( product.NAME , product );
		}
		
		return( Common.getSortedKeys( map ) );
	}

	public boolean isSystemEmpty( AppSystem system ) {
		if( !system.isEmpty() )
			return( false );
		
		for( AppProduct product : mapProductUnmatched.values() ) {
			if( product.system.ID == system.ID )
				return( false );
		}
		return( true );
	}
	
	public EngineProduct findEngineProduct( AppProduct product ) {
		EngineProducts products = data.getProducts();
		return( products.findEngineProduct( product ) );
	}

	public EngineProducts getEngineProducts() {
		return( data.getProducts() );
	}
	
}
