package org.urm.engine.status;

import org.urm.engine.ServerEngine;
import org.urm.meta.ServerObject;

public class EngineStatus extends ServerObject {

	ServerEngine engine;
	
	public EngineStatus( ServerEngine engine ) {
		super( null );
		this.engine = engine;
	}

	@Override
	public String getName() {
		return( "server-status" );
	}

	public void init() throws Exception {
	}
	
}
