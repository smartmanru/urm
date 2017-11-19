package org.urm.meta;

import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.engine.Engine;

public class EngineMatcher {

	public EngineLoader loader;
	public DBConnection connection;
	public Engine engine;
	public RunContext execrc;

	public EngineMatcher( EngineLoader loader , DBConnection connection ) {
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
	}

	public void prepareMatch( int objectId , boolean update , boolean useOldMatch ) throws Exception {
	}
	
}
