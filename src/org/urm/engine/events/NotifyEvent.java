package org.urm.engine.events;

public class NotifyEvent {

	public EngineEventsApp app;
	public EngineEventsListener listener;
	public EngineEventsSubscription sub;
	public SourceEvent eventData;
	
	private NotifyEvent next;
	
	public NotifyEvent( EngineEventsSubscription sub , SourceEvent eventData ) {
		this.sub = sub;
		this.app = sub.app;
		this.listener = sub.listener;
		this.eventData = eventData;
	}

	public NotifyEvent( EngineEventsApp app , EngineEventsListener listener , SourceEvent eventData ) {
		this.app = app;
		this.listener = listener;
		this.eventData = eventData;
	}

	public void setNext( NotifyEvent event ) {
		next = event;
	}
	
	public NotifyEvent getNext() {
		return( next );
	}
	
}
