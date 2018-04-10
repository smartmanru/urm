package org.urm.engine.events;

import java.util.HashMap;
import java.util.Map;

import org.urm.engine.EventService;

abstract public class EngineEventsSource {

	EventService events;
	public String sourceId;

	private Map<String,EngineEventsApp> appMap;
	private int stateId = 0;
	
	abstract public EngineEventsState getState();
	
	public EngineEventsSource( EventService events , String sourceId ) {
		this.events = events;
		this.sourceId = sourceId;
		appMap = new HashMap<String,EngineEventsApp>(); 
	}

	synchronized void subscribe( EngineEventsApp app ) {
		appMap.put( app.appId , app );
	}

	synchronized void unsubscribe( EngineEventsApp app ) {
		appMap.remove( app.appId );
	}

	public synchronized void unsubscribeAll() {
		for( EngineEventsApp app : appMap.values() )
			app.triggerSourceRemoved( this );
		appMap.clear();
	}

	protected void notify( int eventOwner , int eventType , Object data ) {
		synchronized( this ) {
			stateId++;
			SourceEvent event = new SourceEvent( this , eventOwner , eventType , data , stateId );
			for( EngineEventsApp app : appMap.values() )
				events.notifyApp( app , event );
		}
	}

	public synchronized int getStateId() {
		return( stateId );
	}

	public void waitDelivered() {
		events.waitDelivered( this );
	}
	
	public void notifyCustomEvent( int eventOwner , int eventType , Object object ) {
		notify( eventOwner , eventType , object );
	}
	
	public SourceEvent createCustomEvent( int eventOwner , int eventType , Object object ) {
		synchronized( this ) {
			stateId++;
			SourceEvent event = new SourceEvent( this , eventOwner , eventType , object , stateId );
			return( event );
		}
	}
	
}
