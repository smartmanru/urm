package org.urm.meta;

import org.urm.engine.Engine;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineSettings;

public class EngineCore {

	public Engine engine;
	public EngineData data;
	
	private EngineSettings settings;
	private EngineRegistry registry;
	private EngineBase base;
	private EngineInfrastructure infra;
	private EngineReleaseLifecycles lifecycles;

	public EngineCore( EngineData data ) {
		this.data = data;
		this.engine = data.engine;
		
		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
	}

	public void recreateAll() {
		settings.deleteObject();
		registry.deleteObject(); 
		base.deleteObject(); 
		infra.deleteObject(); 
		lifecycles.deleteObject();

		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
	}

	public EngineSettings getServerSettings() {
		return( settings );
	}

	public EngineRegistry getRegistry() {
		return( registry );
	}

	public EngineInfrastructure getInfrastructure() {
		return( infra );
	}

	public EngineReleaseLifecycles getLifecycles() {
		return( lifecycles );
	}

	public EngineBase getBase() {
		return( base );
	}
	
}
