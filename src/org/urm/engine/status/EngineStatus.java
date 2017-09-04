package org.urm.engine.status;

import org.urm.engine.Engine;
import org.urm.meta.ServerObject;

public class EngineStatus extends ServerObject {

	Engine engine;
	
	public EngineStatus( Engine engine ) {
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
