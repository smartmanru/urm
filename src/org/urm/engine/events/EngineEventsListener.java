package org.urm.engine.events;

public interface EngineEventsListener {

	public void triggerEvent( EngineSourceEvent event );
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub );
	
}
