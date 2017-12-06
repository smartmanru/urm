package org.urm.meta;

import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineMirrors;

public class EngineMatcher {

	public EngineLoader loader;
	public Engine engine;
	public RunContext execrc;

	public EngineMatcher( EngineLoader loader ) {
		this.loader = loader;
		this.engine = loader.engine;
		this.execrc = engine.execrc;
	}

	public void prepareMatchDirectory() throws Exception {
		EngineMirrors mirrors = loader.getMirrors();
		mirrors.clearProductReferences();
	}
	
	public void prepareMatchSystem( AppSystem system , boolean update , boolean useOldMatch ) throws Exception {
	}
	
	public void doneSystem( AppSystem system ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		if( !system.MATCHED )
			directory.addUnmatchedSystem( system );
	}
	
}
