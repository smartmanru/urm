package org.urm.engine.events;

import java.util.HashMap;
import java.util.Map;

abstract public class EngineEventsSource {

	EngineEvents events;
	String sourceId;

	private Map<String,EngineEventsApp> appMap;
	private int stateId = 0;
	
	abstract public EngineEventsState getState();
	
	public EngineEventsSource( EngineEvents events , String sourceId ) {
		this.events = events;
		this.sourceId = sourceId;
		appMap = new HashMap<String,EngineEventsApp>(); 
	}

	void subscribe( EngineEventsApp app ) {
		synchronized( events ) {
			appMap.put( app.appId , app );
		}			
	}

	void unsubscribe( EngineEventsApp app ) {
		synchronized( events ) {
			appMap.remove( app.appId );
		}
	}

	public void unsubscribeAll() {
		synchronized( events ) {
			for( EngineEventsApp app : appMap.values() )
				app.triggerSourceRemoved( this );
			appMap.clear();
		}
	}

	protected void trigger( int eventType , Object data ) {
		EngineSourceEvent event = null;
		EngineEventsApp[] apps = null;
		
		synchronized( events ) {
			stateId++;
			event = new EngineSourceEvent( this , stateId , eventType , data );
			apps = appMap.values().toArray( new EngineEventsApp[0] );
		}
		
		for( EngineEventsApp app : apps )
			app.triggerEvent( event );
	}

	public int getStateId() {
		return( stateId );
	}
	
}
