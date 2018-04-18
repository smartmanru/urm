package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.properties.ObjectProperties;
import org.urm.meta.loader.EngineObject;

public class AppSystem extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_MATCHED = "matched";
	
	public EngineDirectory directory;
	private Map<String,AppProduct> mapProducts;
	
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
		
		mapProducts = new HashMap<String,AppProduct>();
		ID = -1;
		SV = 0;
	}

	@Override
	public String getName() {
		return( NAME );
	}
	
	public void createSystem( String name , String desc ) throws Exception {
		modifySystem( name , desc );
		this.OFFLINE = true;
		this.MATCHED = true;
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public void modifySystem( String name , String desc ) throws Exception {
		NAME = name;
		DESC = Common.nonull( desc );
		parameters.setStringProperty( PROPERTY_NAME , NAME );
		parameters.setStringProperty( PROPERTY_DESC , DESC );
	}

	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
		parameters.setBooleanProperty( PROPERTY_OFFLINE , OFFLINE );
	}
	
	public void setMatched( boolean matched ) throws Exception {
		this.MATCHED = matched;
		parameters.setBooleanProperty( PROPERTY_MATCHED , MATCHED );
	}
	
	public AppSystem copy( EngineDirectory nd , ObjectProperties rparameters ) {
		AppSystem r = new AppSystem( nd , rparameters );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.OFFLINE = OFFLINE;
		r.MATCHED = MATCHED;
		r.SV = SV;
		
		for( AppProduct product : mapProducts.values() ) {
			AppProduct rp = product.copy( nd , r );
			r.addProduct( rp );
		}
		return( r );
	}
	
	public ObjectProperties getParameters() {
		return( parameters );
	}
	
	public String[] getProductNames() {
		return( Common.getSortedKeys( mapProducts ) );
	}

	public AppProduct[] getProducts() {
		return( mapProducts.values().toArray( new AppProduct[0] ) );
	}

	public AppProduct findProduct( String key ) {
		return( mapProducts.get( key ) );
	}

	public void addProduct( AppProduct product ) {
		mapProducts.put( product.NAME , product );
	}
	
	public void updateProduct( AppProduct product ) throws Exception {
		Common.changeMapKey( mapProducts , product , product.NAME );
	}
	
	public void removeProduct( AppProduct product ) {
		mapProducts.remove( product.NAME );
	}

	public boolean isOffline() {
		return( OFFLINE );
	}

	public boolean isEmpty() {
		if( mapProducts.isEmpty() )
			return( true );
		return( false );
	}
	
}
