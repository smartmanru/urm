package org.urm.engine.events;

public class ServerSourceEvent {
	
	public ServerEventsSource source;
	public int stateId;
	public int eventType;
	public Object data;
	
	ServerSourceEvent( ServerEventsSource source , int stateId , int eventType , Object data ) {
		this.source = source;
		this.stateId = stateId;
		this.eventType = eventType;
		this.data = data;
	}

}
