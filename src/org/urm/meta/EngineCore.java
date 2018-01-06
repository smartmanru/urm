package org.urm.meta;

import org.urm.engine.Engine;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineSettings;

public class EngineCore {

	public Engine engine;
	
	private EngineEntities entities;
	private EngineSettings settings;
	private EngineRegistry registry;
	private EngineBase base;
	private EngineInfrastructure infra;
	private EngineLifecycles lifecycles;

	public EngineCore( Engine engine ) {
		this.engine = engine;
		
		entities = new EngineEntities( engine );
		registry = new EngineRegistry( engine ); 
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
		
		registry.unloadAll();
		
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

	public EngineRegistry getRegistry() {
		return( registry );
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

}
