package org.urm.engine.events;

public class SourceEvent {
	
	public EngineEventsSource source;
	public int stateId;
	public int eventOwner;
	public int eventType;
	public Object data;
	
	public SourceEvent( EngineEventsSource source , int eventOwner , int eventType , Object data , int stateId ) {
		this.source = source;
		this.stateId = stateId;
		this.eventOwner = eventOwner;
		this.eventType = eventType;
		this.data = data;
	}

}
