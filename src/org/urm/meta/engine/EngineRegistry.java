package org.urm.meta.engine;

import org.urm.common.RunContext;
import org.urm.engine.Engine;

public class EngineRegistry {

	public Engine engine;
	public RunContext execrc;
	
	public EngineResources resources;
	public EngineMirrors mirrors;
	public EngineBuilders builders;

	public EngineRegistry( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
	}
	
	public void unloadAll() {
		if( builders != null ) {
			builders.deleteObject();
			builders = null;
		}
		
		if( mirrors != null ) {
			mirrors.deleteObject();
			mirrors = null;
		}

		if( resources != null ) {
			resources.deleteObject();
			resources = null;
		}
	}
	
	public void setResources( EngineResources resourcesNew ) {
		resources = resourcesNew;
	}
	
	public void setMirrors( EngineMirrors mirrorsNew ) {
		mirrors = mirrorsNew;
	}
	
	public void setBuilders( EngineBuilders buildersNew ) {
		builders = buildersNew;
	}
	
}
