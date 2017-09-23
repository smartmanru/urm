package org.urm.engine.events;

public class EngineEventsSubscription {

	public EngineEventsApp app;
	public EngineEventsSource source;
	public EngineEventsListener listener;
	
	private NotifyEvent first; 
	private NotifyEvent last;
	private boolean executing;
	
	public EngineEventsSubscription( EngineEventsApp app , EngineEventsSource source , EngineEventsListener listener ) {
		this.app = app;
		this.source = source;
		this.listener = listener;
	}

	public void triggerEvent( NotifyEvent event ) {
		listener.triggerEvent( this , event.eventData );
	}

	public void triggerSubscriptionRemoved() {
		listener.triggerSubscriptionRemoved( this );
	}
	
	public synchronized EngineEventsState getState() {
		return( source.getState() );
	}

	public synchronized void addEvent( SourceEvent event ) {
		NotifyEvent notify = new NotifyEvent( this , event );
		if( last != null ) {
			last.setNext( notify );
			last = notify;
		}
		else
			first = last = notify;
		
		if( !executing ) {
			source.events.notifyEvent( first );
			executing = true;
		}
	}

	public synchronized void finishEvent( NotifyEvent event ) {
		first = event.getNext();
		if( first == null ) {
			last = null;
			executing = false;
		}
		else {
			source.events.notifyEvent( first );
			executing = true;
		}
	}
	
}
