package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.EngineObject;

public class AppSystem extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_OFFLINE = "offline";
	
	public EngineDirectory directory;
	private Map<String,Product> mapProducts;
	
	public int ID;
	public String NAME;
	public String DESC;
	public boolean OFFLINE;
	public boolean MATCHED;
	public int SV;
	
	private ObjectProperties parameters;
	
	public AppSystem( EngineDirectory directory , ObjectProperties parameters ) {
		super( directory );
		this.directory = directory;
		this.parameters = parameters;
		
		mapProducts = new HashMap<String,Product>();
		ID = -1;
		SV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createSystem( String name , String desc ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.OFFLINE = true;
		this.MATCHED = true;
		parameters.setStringProperty( PROPERTY_NAME , NAME );
		parameters.setStringProperty( PROPERTY_DESC , DESC );
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public void setOffline( boolean OFFLINE ) throws Exception {
		this.OFFLINE = OFFLINE;
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public AppSystem copy( EngineDirectory nd , ObjectProperties rparameters ) {
		AppSystem r = new AppSystem( nd , rparameters );
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
	
	public ObjectProperties getParameters() {
		return( parameters );
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
		parameters.setStringProperty( PROPERTY_NAME , NAME );
		parameters.setStringProperty( PROPERTY_DESC , DESC );
	}

	public void addProduct( Product product ) throws Exception {
		mapProducts.put( product.NAME , product );
	}
	
	public void removeProduct( Product product ) {
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
