package org.urm.engine;

public class ServerSourceEvent {
	
	ServerEventsSource source;
	int stateId;
	int eventType;
	Object data;
	
	ServerSourceEvent( ServerEventsSource source , int stateId , int eventType , Object data ) {
		this.source = source;
		this.stateId = stateId;
		this.eventType = eventType;
		this.data = data;
	}

}