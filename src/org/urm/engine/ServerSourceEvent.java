package org.urm.engine;

public class ServerSourceEvent {
	
	ServerEventsSource source;
	int stateId;
	Object data;
	
	ServerSourceEvent( ServerEventsSource source , int stateId , Object data ) {
		this.source = source;
		this.stateId = stateId;
		this.data = data;
	}

}
