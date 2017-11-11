package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.meta.EngineObject;

public class System extends EngineObject {

	public EngineDirectory directory;
	private Map<String,Product> mapProducts;
	
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public int CV;
	
	public System( EngineDirectory directory ) {
		super( directory );
		this.directory = directory;
		mapProducts = new HashMap<String,Product>();
		ID = -1;
		CV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createSystem( String name , String desc ) {
		this.NAME = name;
		this.DESC = desc;
		this.OFFLINE = true;
	}
	
	public void setOffline( boolean OFFLINE ) {
		this.OFFLINE = OFFLINE;
	}
	
	public System copy( EngineDirectory nd ) {
		System r = new System( nd );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.CV = CV;
		
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

	public boolean isBroken( ActionBase action ) {
		for( Product product : mapProducts.values() ) {
			if( product.isBroken( action ) )
				return( true );
		}
		return( false );
	}

}
