package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

abstract public class ServerEventsSource {

	ServerEvents events;
	String sourceId;

	private Map<String,ServerEventsApp> appMap;
	private int stateId = 0;
	
	abstract public ServerEventsState getState();
	
	public ServerEventsSource( ServerEvents events , String sourceId ) {
		this.events = events;
		this.sourceId = sourceId;
		appMap = new HashMap<String,ServerEventsApp>(); 
	}

	void subscribe( ServerEventsApp app ) {
		synchronized( events ) {
			appMap.put( app.appId , app );
		}			
	}

	void unsubscribe( ServerEventsApp app ) {
		synchronized( events ) {
			appMap.remove( app.appId );
		}
	}

	protected void trigger( int eventType , Object data ) {
		synchronized( events ) {
			ServerSourceEvent event = new ServerSourceEvent( this , stateId , eventType , data );
			for( ServerEventsApp app : appMap.values() )
				app.trigger( event );
		}
	}

}
