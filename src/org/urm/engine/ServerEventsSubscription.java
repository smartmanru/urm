package org.urm.engine;

public class ServerEventsSubscription {

	public ServerEventsApp app;
	public ServerEventsSource source;
	public ServerEventsListener listener;
	
	public ServerEventsSubscription( ServerEventsApp app , ServerEventsSource source , ServerEventsListener listener ) {
		this.app = app;
		this.source = source;
		this.listener = listener;
	}

	public void trigger( ServerSourceEvent event ) {
		listener.triggerEvent( event );
	}

	public ServerEventsState getState() {
		return( source.getState() );
	}
	
}
