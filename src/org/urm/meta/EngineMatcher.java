package org.urm.meta;

import org.urm.common.RunContext;
import org.urm.engine.Engine;

public class EngineMatcher {

	public EngineLoader loader;
	public Engine engine;
	public RunContext execrc;

	public EngineMatcher( EngineLoader loader ) {
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
	}

	public void prepareMatch( int objectId , boolean update , boolean useOldMatch ) throws Exception {
	}
	
}
