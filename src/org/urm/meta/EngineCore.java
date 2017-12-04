package org.urm.meta;

import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.EngineEntities;
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
		
		entities = new EngineEntities( this );
		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( engine , this ); 
		lifecycles = new EngineLifecycles( this ); 
	}

	public void upgradeData( EngineLoader loader ) throws Exception {
		entities.upgradeData( loader );
	}
	
	public void useData( EngineLoader loader ) throws Exception {
		entities.useData( loader );
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
		infra = new EngineInfrastructure( engine , this ); 
		lifecycles = new EngineLifecycles( this ); 
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
	
	public void setSettings( TransactionBase transaction , EngineSettings settingsNew ) {
		this.settings = settingsNew;
	}
	
}
