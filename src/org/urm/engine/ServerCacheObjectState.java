package org.urm.engine;

public class ServerCacheObjectState extends ServerEventsState {
	
	public Object data;
	
	public ServerCacheObjectState( ServerCacheObject object , int stateId , Object data ) {
		super( object , stateId );
		this.data = data;
	}
	
}
