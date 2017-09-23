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

	public synchronized void close() {
		closed = true;
		for( EngineEventsSubscription sub : subs ) {
			EngineEventsSource source = sub.source;
			if( isPrimarySubscription( sub ) )
				source.unsubscribe( this );
		}
		
		subs.clear();
	}

	public synchronized EngineEventsSubscription subscribe( EngineEventsSource source , EngineEventsListener listener ) {
		source.subscribe( this );
		EngineEventsSubscription sub = new EngineEventsSubscription( this , source , listener );
		subs.add( sub );
		return( sub );
	}

	public synchronized void unsubscribe( EngineEventsListener listener ) {
		List<EngineEventsSubscription> removed = new LinkedList<EngineEventsSubscription>();
		for( EngineEventsSubscription sub : subs ) {
			if( sub.listener == listener )
				removed.add( sub );
		}
				
		subs.removeAll( removed );
	}

	public synchronized void unsubscribe( EngineEventsSubscription sub ) {
		subs.remove( sub );
		for( EngineEventsSubscription subCheck : subs ) {
			if( subCheck.source == sub.source )
				return;
		}
		
		sub.source.unsubscribe( this );
	}
	
	public synchronized void notifyEvent( SourceEvent event ) {
		for( EngineEventsSubscription sub : subs ) {
			if( sub.source == event.source )
				sub.addEvent( event );
		}
	}

	public synchronized void triggerSourceRemoved( EngineEventsSource source ) {
		for( EngineEventsSubscription sub : subs ) {
			if( sub.source == source )
				sub.triggerSubscriptionRemoved();
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

}
