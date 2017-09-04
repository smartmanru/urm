package org.urm.engine.events;

public class EngineSourceEvent {
	
	public EngineEventsSource source;
	public int stateId;
	public int eventType;
	public Object data;
	
	EngineSourceEvent( EngineEventsSource source , int stateId , int eventType , Object data ) {
		this.source = source;
		this.stateId = stateId;
		this.eventType = eventType;
		this.data = data;
	}

}
