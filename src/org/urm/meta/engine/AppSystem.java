package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;

public class AppSystem extends EngineObject {

	public EngineDirectory directory;
	private Map<String,Product> mapProducts;
	
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public boolean MATCHED;
	public int SV;
	
	ObjectProperties parameters;
	
	public AppSystem( EngineDirectory directory ) {
		super( directory );
		this.directory = directory;
		mapProducts = new HashMap<String,Product>();
		ID = -1;
		SV = 0;
		
		parameters = new ObjectProperties( "system" , directory.engine.execrc );
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createSystem( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
		this.OFFLINE = true;
		this.MATCHED = true;
	}
	
	public void setOffline( boolean OFFLINE ) {
		this.OFFLINE = OFFLINE;
	}
	
	public AppSystem copy( EngineDirectory nd ) {
		AppSystem r = new AppSystem( nd );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.MATCHED = MATCHED;
		r.SV = SV;
		
		for( Product product : mapProducts.values() ) {
			Product rp = product.copy( nd , r );
			r.mapProducts.put( rp.NAME , rp );
		}
		return( r );
	}
	
	public String[] getProductNames() {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public Product[] getProducts() {
		return( mapProducts.values().toArray( new Product[0] ) );
	}

	public Product findProduct( String key ) {
		return( mapProducts.get( key ) );
	}

	public void modifySystem( String name , String desc ) throws Exception {
		NAME = name;
		DESC = desc;
	}

	public void addProduct( Product product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void removeProduct( Product product ) throws Exception {
		mapProducts.remove( product.NAME );
	}

	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isBroken() {
		if( MATCHED )
			return( false );
		return( true );
	}

}
