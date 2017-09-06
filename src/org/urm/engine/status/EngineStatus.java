package org.urm.engine.status;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;

public class EngineStatus extends EngineObject {

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
	
	public synchronized void start( ActionBase action , EngineLoader loader ) {
	}
	
	public synchronized void clear() throws Exception {
	}
	
}
