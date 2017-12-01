package org.urm.meta.engine;

import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.meta.EngineCore;
import org.urm.meta.EngineObject;

public class EngineRegistry extends EngineObject {

	public EngineCore core;
	public Engine engine;
	public RunContext execrc;
	
	public EngineResources resources;
	public EngineBuilders builders;
	public EngineMirrors mirrors;

	public EngineRegistry( EngineCore core ) {
		super( null );
		this.core = core;
		this.engine = core.engine;
		this.execrc = engine.execrc;
		mirrors = new EngineMirrors( this ); 
		resources = new EngineResources( this );
		builders = new EngineBuilders( this ); 
	}
	
	@Override
	public String getName() {
		return( "server-registry" );
	}
	
	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		resources = resourcesNew;
	}
	
	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		mirrors = mirrorsNew;
	}
	
	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		builders = buildersNew;
	}
	
}
