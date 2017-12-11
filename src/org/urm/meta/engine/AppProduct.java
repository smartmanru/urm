package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.Meta;

public class AppProduct extends EngineObject {

	public static String PROPERTY_NAME = "name";
	public static String PROPERTY_DESC = "desc";
	public static String PROPERTY_PATH = "path";
	public static String PROPERTY_OFFLINE = "offline";
	public static String PROPERTY_MONITORING_ENABLED = "monitoring.enabled";
	
	public EngineDirectory directory;
	public AppSystem system;

	public int ID;
	public String NAME;
	public String DESC;
	public String PATH;
	public boolean OFFLINE;
	public boolean MONITORING_ENABLED;
	public int SV;
	
	private ProductMeta productMeta;

	public AppProduct( EngineDirectory directory , AppSystem system ) {
		super( directory );
		this.directory = directory;
		this.system = system;
		this.ID = -1;
		this.SV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}

	public AppProduct copy( EngineDirectory nr , AppSystem rs ) {
		AppProduct r = new AppProduct( nr , rs );
		r.ID = ID;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PATH = PATH;
		r.OFFLINE = OFFLINE;
		r.MONITORING_ENABLED = MONITORING_ENABLED;
		r.SV = SV;
		r.productMeta = productMeta;
		return( r );
	}
	
	public Meta getMeta( ActionBase action ) throws Exception {
		if( productMeta == null )
			action.exitUnexpectedState();
		
		return( action.getProductMetadata( NAME ) );
	}

	public void setMatched( ProductMeta meta ) {
		this.productMeta = meta;
	}
	
	public boolean isMatched() {
		if( productMeta != null )
			return( true );
		return( false );
	}
	
	public void createProduct( String name , String desc , String path ) throws Exception {
		modifyProduct( name , desc , path );
		OFFLINE = true;
		MONITORING_ENABLED = false;
	}

	public void modifyProduct( String name , String desc , String path ) throws Exception {
		this.NAME = name;
		this.DESC = desc;
		this.PATH = path;
	}
	
	public boolean isOffline() {
		if( OFFLINE )
			return( false );
		return( system.isOffline() );
	}

	public boolean isBroken() {
		return( false );
	}
	
	public void setOffline( boolean offline ) throws Exception {
		this.OFFLINE = offline;
	}

	public void setMonitoringEnabled( boolean enabled ) throws Exception {
		this.MONITORING_ENABLED = enabled;
	}

}
