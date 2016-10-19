package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

public class ServerEventsApp {

	ServerEvents events;
	String appId;

	private List<ServerEventsSubscription> subs;
	
	ServerEventsApp( ServerEvents events , String appId ) {
		this.events = events;
		this.appId = appId;
		
		subs = new LinkedList<ServerEventsSubscription>();
	}

	public void deleteSubscriptions() {
		for( ServerEventsSubscription sub : subs ) {
			ServerEventsSource source = sub.source;
			if( isPrimarySubscription( sub ) )
				source.unsubscribe( this );
		}
		
		subs.clear();
	}

	public void subscribe( ServerEventsSource source , ServerEventsListener listener ) {
		synchronized( events ) {
			ServerEventsSubscription sub = new ServerEventsSubscription( this , source , listener );
			subs.add( sub );
		}
	}

	public void unsubscribe( ServerEventsListener listener ) {
		synchronized( events ) {
			List<ServerEventsSubscription> removed = new LinkedList<ServerEventsSubscription>();
			for( ServerEventsSubscription sub : subs ) {
				if( sub.listener == listener )
					removed.add( sub );
			}
					
			subs.removeAll( removed );
		}
	}

	public void unsubscribe( ServerEventsSubscription sub ) {
		synchronized( events ) {
			subs.remove( sub );
			for( ServerEventsSubscription subCheck : subs ) {
				if( subCheck.source == sub.source )
					return;
			}
			
			sub.source.unsubscribe( this );
		}
	}
	
	private boolean isPrimarySubscription( ServerEventsSubscription subCheck ) {
		ServerEventsSource source = subCheck.source;
		boolean first = true;
		for( ServerEventsSubscription sub : subs ) {
			if( sub.source != source )
				continue;
			if( sub == subCheck ) {
				if( first )
					return( true );
				return( false );
			}
			first = false;
		}
		return( false );
	}

	public void trigger( ServerSourceEvent event ) {
		synchronized( events ) {
			for( ServerEventsSubscription sub : subs )
				sub.trigger( event );
		}
	}
	
}
