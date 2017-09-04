package org.urm.engine.events;

public interface ServerEventsListener {

	public void triggerEvent( ServerSourceEvent event );
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub );
	
}
