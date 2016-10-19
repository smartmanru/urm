package org.urm.engine;

public class ServerEventsSubscription {

	ServerEventsApp app;
	ServerEventsSource source;
	ServerEventsListener listener;
	
	public ServerEventsSubscription( ServerEventsApp app , ServerEventsSource source , ServerEventsListener listener ) {
		this.app = app;
		this.source = source;
		this.listener = listener;
	}

	public void trigger( ServerSourceEvent event ) {
		listener.trigger( event );
	}
	
}
