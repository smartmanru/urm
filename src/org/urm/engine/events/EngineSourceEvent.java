package org.urm.engine.events;

public class EngineSourceEvent {
	
	public EngineEventsSource source;
	public int stateId;
	public int eventType;
	public Object data;
	
	public EngineSourceEvent( EngineEventsSource source , int eventType , Object data , int stateId ) {
		this.source = source;
		this.stateId = stateId;
		this.eventType = eventType;
		this.data = data;
	}

}
