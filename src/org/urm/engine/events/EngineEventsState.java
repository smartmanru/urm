package org.urm.engine.events;

public class EngineEventsState {

	EngineEventsSource source;
	int stateId;
	
	public EngineEventsState( EngineEventsSource source , int stateId ) {
		this.source = source;
		this.stateId = stateId;
	}
	
}
