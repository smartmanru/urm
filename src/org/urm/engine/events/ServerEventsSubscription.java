package org.urm.engine.events;

public class ServerEventsSubscription {

	public ServerEventsApp app;
	public ServerEventsSource source;
	public ServerEventsListener listener;
	
	public ServerEventsSubscription( ServerEventsApp app , ServerEventsSource source , ServerEventsListener listener ) {
		this.app = app;
		this.source = source;
		this.listener = listener;
	}

	public void triggerEvent( ServerSourceEvent event ) {
		listener.triggerEvent( event );
	}

	public void triggerSubscriptionRemoved() {
		listener.triggerSubscriptionRemoved( this );
	}
	
	public ServerEventsState getState() {
		return( source.getState() );
	}
	
}
