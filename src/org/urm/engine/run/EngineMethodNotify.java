package org.urm.engine.run;

import org.urm.engine.events.EngineEventsSource;

public class EngineMethodNotify {

	public EngineEventsSource source;
	public int eventOwner;
	public int eventType;
	public Object data;
	
	public EngineMethodNotify( EngineEventsSource source , int eventOwner , int eventType , Object data ) {
		this.source = source;
		this.eventOwner = eventOwner;
		this.eventType = eventType;
		this.data = data;
	}
	
}
