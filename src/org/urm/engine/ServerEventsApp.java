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

	public ServerEventsSubscription subscribe( ServerEventsSource source , ServerEventsListener listener ) {
		synchronized( events ) {
			source.subscribe( this );
			ServerEventsSubscription sub = new ServerEventsSubscription( this , source , listener );
			subs.add( sub );
			return( sub );
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

	public void triggerEvent( ServerSourceEvent event ) {
		List<ServerEventsSubscription> subsUse = new LinkedList<ServerEventsSubscription>();
		synchronized( events ) {
			for( ServerEventsSubscription sub : subs ) {
				if( sub.source == event.source )
					subsUse.add( sub );
			}
		}
		
		for( ServerEventsSubscription sub : subsUse )
			sub.triggerEvent( event );
	}

	public void triggerSourceRemoved( ServerEventsSource source ) {
		synchronized( events ) {
			for( ServerEventsSubscription sub : subs ) {
				if( sub.source == source )
					sub.triggerSubscriptionRemoved();
			}
		}
	}
	
}
