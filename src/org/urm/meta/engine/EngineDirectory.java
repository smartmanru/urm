package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;

public class EngineDirectory extends EngineObject {

	public EngineData data;
	public Engine engine;

	private Map<String,AppSystem> mapSystems;
	private Map<String,AppProduct> mapProducts;
	private Map<Integer,AppSystem> mapSystemsById;
	private Map<Integer,AppProduct> mapProductsById;
	
	public EngineDirectory( EngineData data ) {
		super( null );
		this.data = data;
		this.engine = data.engine;
		mapSystems = new HashMap<String,AppSystem>();
		mapProducts = new HashMap<String,AppProduct>();
		mapSystemsById = new HashMap<Integer,AppSystem>();
		mapProductsById = new HashMap<Integer,AppProduct>();
	}

	@Override
	public String getName() {
		return( "server-directory" );
	}

	public EngineDirectory copy() throws Exception {
		EngineDirectory r = new EngineDirectory( data );
		
		EngineSettings settings = data.getEngineSettings();
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
	
	public void addSystem( AppSystem system ) throws Exception {
		mapSystems.put( system.NAME , system );
		if( system.ID > 0 )
			mapSystemsById.put( system.ID , system );
	}

	public void unloadAll() {
		mapSystems.clear();
		mapSystemsById.clear();
		mapProducts.clear();
		mapProductsById.clear();
	}
	
	public void unloadSystem( AppSystem system ) {
		mapSystems.remove( system.NAME );
		mapSystemsById.remove( system.ID );
		for( AppProduct product : system.getProducts() )
			unloadProduct( product );
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

}
