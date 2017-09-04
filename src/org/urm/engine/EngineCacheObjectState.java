package org.urm.engine;

import org.urm.engine.events.EngineEventsState;

public class EngineCacheObjectState extends EngineEventsState {
	
	public Object data;
	
	public EngineCacheObjectState( EngineCacheObject object , int stateId , Object data ) {
		super( object , stateId );
		this.data = data;
	}
	
}
