package org.urm.engine.events;

import java.util.LinkedList;
import java.util.List;

public class EngineEventsApp {

	EngineEvents events;
	String appId;
	boolean closed;

	private List<EngineEventsSubscription> subs;
	
	EngineEventsApp( EngineEvents events , String appId ) {
		this.events = events;
		this.appId = appId;
		
		closed = false;
		subs = new LinkedList<EngineEventsSubscription>();
	}

	public void close() {
		closed = true;
		for( EngineEventsSubscription sub : subs ) {
			EngineEventsSource source = sub.source;
			if( isPrimarySubscription( sub ) )
				source.unsubscribe( this );
		}
		
		subs.clear();
	}

	public EngineEventsSubscription subscribe( EngineEventsSource source , EngineEventsListener listener ) {
		synchronized( events ) {
			source.subscribe( this );
			EngineEventsSubscription sub = new EngineEventsSubscription( this , source , listener );
			subs.add( sub );
			return( sub );
		}
	}

	public void unsubscribe( EngineEventsListener listener ) {
		synchronized( events ) {
			List<EngineEventsSubscription> removed = new LinkedList<EngineEventsSubscription>();
			for( EngineEventsSubscription sub : subs ) {
				if( sub.listener == listener )
					removed.add( sub );
			}
					
			subs.removeAll( removed );
		}
	}

	public void unsubscribe( EngineEventsSubscription sub ) {
		synchronized( events ) {
			subs.remove( sub );
			for( EngineEventsSubscription subCheck : subs ) {
				if( subCheck.source == sub.source )
					return;
			}
			
			sub.source.unsubscribe( this );
		}
	}
	
	private boolean isPrimarySubscription( EngineEventsSubscription subCheck ) {
		EngineEventsSource source = subCheck.source;
		boolean first = true;
		for( EngineEventsSubscription sub : subs ) {
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

	public void triggerEvent( EngineSourceEvent event ) {
		List<EngineEventsSubscription> subsUse = new LinkedList<EngineEventsSubscription>();
		synchronized( events ) {
			for( EngineEventsSubscription sub : subs ) {
				if( sub.source == event.source )
					subsUse.add( sub );
			}
		}
		
		for( EngineEventsSubscription sub : subsUse )
			sub.triggerEvent( event );
	}

	public void triggerEvent( EngineEventsListener listener , EngineSourceEvent event ) {
		if( !closed )
			listener.triggerEvent( event );
	}
	
	public void triggerSourceRemoved( EngineEventsSource source ) {
		synchronized( events ) {
			for( EngineEventsSubscription sub : subs ) {
				if( sub.source == source )
					sub.triggerSubscriptionRemoved();
			}
		}
	}

}
