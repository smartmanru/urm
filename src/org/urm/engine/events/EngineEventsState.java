package org.urm.engine.events;

public class EngineEventsState {

	public EngineEventsSource eventsSource;
	public int stateId;
	
	public EngineEventsState( EngineEventsSource eventsSource , int stateId ) {
		this.eventsSource = eventsSource;
		this.stateId = stateId;
	}
	
}
