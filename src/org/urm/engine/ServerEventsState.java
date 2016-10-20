package org.urm.engine;

public class ServerEventsState {

	ServerEventsSource source;
	int stateId;
	
	public ServerEventsState( ServerEventsSource source , int stateId ) {
		this.source = source;
		this.stateId = stateId;
	}
	
}
