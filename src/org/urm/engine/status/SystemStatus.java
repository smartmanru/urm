package org.urm.engine.status;

import org.urm.meta.engine.AppSystem;

public class SystemStatus extends Status {

	public AppSystem system;
	
	public SystemStatus( AppSystem system ) {
		super( STATETYPE.TypeSystem , null , system );
		this.system = system;
	}

}
