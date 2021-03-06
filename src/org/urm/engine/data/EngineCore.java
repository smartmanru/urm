package org.urm.engine.data;

import org.urm.engine.Engine;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.loader.EngineLoader;

public class EngineCore {

	public Engine engine;
	
	private EngineEntities entities;
	
	private EngineSettings settings;
	private EngineBase base;
	private EngineInfrastructure infra;
	private EngineLifecycles lifecycles;
	private EngineResources resources;
	private EngineMirrors mirrors;
	private EngineBuilders builders;

	public EngineCore( Engine engine ) {
		this.engine = engine;
		
		entities = new EngineEntities( engine );
	}

	public void upgradeMeta( EngineLoader loader ) throws Exception {
		entities.upgradeMeta( loader );
	}
	
	public void useMeta( EngineLoader loader ) throws Exception {
		entities.useMeta( loader );
	}
	
	public void unloadAll() {
		if( settings != null ) {
			settings.deleteObject();
			settings = null;
		}
		
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
		
		if( base != null ) {
			base.deleteObject();
			base = null;
		}
		
		if( infra != null ) {
			infra.deleteObject();
			infra = null;
		}
		
		if( lifecycles != null ) {
			lifecycles.deleteObject();
			lifecycles = null;
		}
	}

	public EngineEntities getEntities() {
		return( entities );
	}
	
	public EngineSettings getSettings() {
		return( settings );
	}

	public EngineResources getResources() {
		return( resources );
	}

	public EngineMirrors getMirrors() {
		return( mirrors );
	}

	public EngineBuilders getBuilders() {
		return( builders );
	}

	public EngineInfrastructure getInfrastructure() {
		return( infra );
	}

	public EngineLifecycles getLifecycles() {
		return( lifecycles );
	}

	public EngineBase getBase() {
		return( base );
	}
	
	public void setSettings( EngineSettings settingsNew ) {
		this.settings = settingsNew;
	}
	
	public void setBase( EngineBase baseNew ) {
		this.base = baseNew;
	}
	
	public void setInfrastructure( EngineInfrastructure infraNew ) {
		this.infra = infraNew;
	}
	
	public void setLifecycles( EngineLifecycles lifecyclesNew ) {
		this.lifecycles = lifecyclesNew;
	}

	public void updateEntity( PropertyEntity entity ) {
		entities.updateEntity( entity );
	}

	public void setResources( EngineResources resourcesNew ) {
		this.resources = resourcesNew;
	}

	public void setBuilders( EngineBuilders buildersNew ) {
		this.builders = buildersNew;
	}

	public void setMirrors( EngineMirrors mirrorsNew ) {
		this.mirrors = mirrorsNew;
	}

}
