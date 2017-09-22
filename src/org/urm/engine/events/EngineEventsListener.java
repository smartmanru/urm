package org.urm.engine.events;

public interface EngineEventsListener {

	public void triggerEvent( EngineEventsSubscription sub , EngineSourceEvent event );
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub );
	
}
