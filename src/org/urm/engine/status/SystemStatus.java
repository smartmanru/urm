package org.urm.engine.status;

import org.urm.meta.engine.System;

public class SystemStatus extends Status {

	public System system;
	
	public SystemStatus( System system ) {
		super( STATETYPE.TypeSystem , null , system );
		this.system = system;
	}

}
