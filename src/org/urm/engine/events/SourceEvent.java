package org.urm.engine.events;

import org.urm.engine.EventService;

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

	public boolean isEngineEvent( int event ) {
		if( eventOwner == EventService.OWNER_ENGINE && eventType == event )
			return( true );
		return( false );
	}
	
	public boolean isBuildEvent( int event ) {
		if( eventOwner == EventService.OWNER_ENGINEBUILDPLAN && eventType == event )
			return( true );
		return( false );
	}
	
	public boolean isDeployEvent( int event ) {
		if( eventOwner == EventService.OWNER_ENGINEDEPLOYPLAN && eventType == event )
			return( true );
		return( false );
	}
	
}
