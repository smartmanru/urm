package org.urm.engine.events;

public class EngineEventsSubscription {

	public EngineEventsApp app;
	public EngineEventsSource source;
	public EngineEventsListener listener;
	
	public EngineEventsSubscription( EngineEventsApp app , EngineEventsSource source , EngineEventsListener listener ) {
		this.app = app;
		this.source = source;
		this.listener = listener;
	}

	public void triggerEvent( EngineSourceEvent event ) {
		listener.triggerEvent( event );
	}

	public void triggerSubscriptionRemoved() {
		listener.triggerSubscriptionRemoved( this );
	}
	
	public synchronized EngineEventsState getState() {
		return( source.getState() );
	}
	
}
