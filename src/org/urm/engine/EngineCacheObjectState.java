package org.urm.engine;

import org.urm.engine.events.ServerEventsState;

public class EngineCacheObjectState extends ServerEventsState {
	
	public Object data;
	
	public EngineCacheObjectState( EngineCacheObject object , int stateId , Object data ) {
		super( object , stateId );
		this.data = data;
	}
	
}
