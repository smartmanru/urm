package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineObject;
import org.urm.meta.product.Meta;

public class Product extends EngineObject {

	public EngineDirectory directory;
	public AppSystem system;

	public int ID;
	public int SYSTEM;
	public String NAME;
	public String DESC;
	public String PATH;
	public boolean OFFLINE;
	public boolean MONITORING_ENABLED;
	public int SV;

	public Product( EngineDirectory directory , AppSystem system ) {
		super( directory );
		this.directory = directory;
		this.system = system;
		this.SYSTEM = system.ID;
		this.ID = -1;
		this.SV = 0;
	}
	
	@Override
	public String getName() {
		return( NAME );
	}

	public void createProduct( EngineTransaction transaction , String newName , String newDesc , String newPath ) throws Exception {
		SYSTEM = system.ID;
		NAME = newName;
		DESC = newDesc;
		PATH = newPath;
		OFFLINE = true;
		MONITORING_ENABLED = true;
	}

	public Product copy( EngineDirectory nr , AppSystem rs ) {
		Product r = new Product( nr , rs );
		r.ID = ID;
		r.SYSTEM = SYSTEM;
		r.NAME = NAME;
		r.DESC = DESC;
		r.PATH = PATH;
		r.OFFLINE = OFFLINE;
		r.MONITORING_ENABLED = MONITORING_ENABLED;
		r.SV = SV;
		return( r );
	}
	
	public Meta getMeta( ActionBase action ) throws Exception {
		return( action.getProductMetadata( NAME ) );
	}
	
	public void modifyProduct( EngineTransaction transaction ) throws Exception {
	}
	
	public boolean isOffline() {
		if( OFFLINE )
			return( false );
		return( system.isOffline() );
	}

	public boolean isBroken( ActionBase action ) {
		return( action.isProductBroken( NAME ) );
	}
	
	public void setMonitoringEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		MONITORING_ENABLED = enabled;
	}

}
