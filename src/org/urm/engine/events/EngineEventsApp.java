package org.urm.engine.events;

import java.util.LinkedList;
import java.util.List;

// sync order: source -> app -> listener,subscription

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
		List<EngineEventsSource> sources = new LinkedList<EngineEventsSource>(); 
		synchronized( this ) {
			closed = true;
			
			for( EngineEventsSubscription sub : subs ) {
				EngineEventsSource source = sub.source;
				if( sources.indexOf( source ) < 0 )
					sources.add( source );
			}
			
			subs.clear();
		}
		
		for( EngineEventsSource source : sources )
			source.unsubscribe( this );
	}

	public EngineEventsSubscription subscribe( EngineEventsSource source , EngineEventsListener listener ) {
		return( subscribe( source , listener , 0 , null ) );
	}
	
	public EngineEventsSubscription subscribe( EngineEventsSource source , EngineEventsListener listener , int customType , Object customData ) {
		EngineEventsSubscription sub = null;
		synchronized( source ) {
			synchronized( this ) {
				if( closed )
					return( null );
				
				sub = new EngineEventsSubscription( this , source , listener , customType , customData );
				subs.add( sub );
			}
			
			source.subscribe( this );
		}
		return( sub );
	}

	public void unsubscribe( EngineEventsListener listener ) {
		List<EngineEventsSource> sources = new LinkedList<EngineEventsSource>(); 
		synchronized( this ) {
			if( closed )
				return;
			
			for( int k = subs.size() - 1; k >= 0; k-- ) {
				EngineEventsSubscription sub = subs.get( k );
				if( sub.listener == listener ) {
					subs.remove( k );
					
					if( sources.indexOf( sub.source ) < 0 ) {
						if( !checkSubscribed( sub.source ) )
							sources.add( sub.source );
					}
				}
			}
		}
		
		for( EngineEventsSource source : sources ) {
			synchronized( source ) {
				synchronized( this ) {
					if( !checkSubscribed( source ) )
						source.unsubscribe( this );
				}
			}
		}
	}

	public void unsubscribe( EngineEventsSubscription sub ) {
		synchronized( sub.source ) {
			synchronized( this ) {
				subs.remove( sub );
				if( !checkSubscribed( sub.source ) )
					sub.source.unsubscribe( this );
			}
		}
	}
	
	public synchronized void notifyEvent( SourceEvent event ) {
		for( EngineEventsSubscription sub : subs ) {
			if( sub.source == event.source )
				sub.addEvent( event );
		}
	}

	public synchronized void triggerSourceRemoved( EngineEventsSource source ) {
		for( int k = subs.size() - 1; k >= 0; k-- ) { 
			EngineEventsSubscription sub = subs.get( k );
			if( sub.source == source )
				subs.remove( k );
		}
	}

	private boolean checkSubscribed( EngineEventsSource source ) {
		for( EngineEventsSubscription sub : subs ) {
			if( sub.source == sub.source )
				return( true );
		}
		return( false );
	}
	
}
