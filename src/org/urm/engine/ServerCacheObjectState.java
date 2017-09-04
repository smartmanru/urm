package org.urm.engine;

import org.urm.engine.events.ServerEventsState;

public class ServerCacheObjectState extends ServerEventsState {
	
	public Object data;
	
	public ServerCacheObjectState( ServerCacheObject object , int stateId , Object data ) {
		super( object , stateId );
		this.data = data;
	}
	
}
