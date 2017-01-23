package org.urm.engine;

import java.util.LinkedList;
import java.util.List;

public class ServerEventsNotifier extends ServerEventsSource implements Runnable {

	class NotifyEvent {
		public ServerEventsApp app;
		public ServerEventsListener listener;
		public Object eventData;
		
		public NotifyEvent( ServerEventsApp app , ServerEventsListener listener , Object eventData ) {
			this.app = app;
			this.listener = listener;
			this.eventData = eventData;
		}
	};
	
	private Thread thread;
	private boolean started = false;
	private boolean stopped = false;
	private boolean stopping = false;

	private List<NotifyEvent> queue;
	
	public ServerEventsNotifier( ServerEvents events ) {
		super( events , "urm.notifier" );
		queue = new LinkedList<NotifyEvent>(); 
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void run() {
		started = true;
		try {
			while( !stopping )
				cycle();
		}
		catch( Throwable e ) {
			events.engine.handle( "events notifier error" , e );
		}
		
		synchronized( this ) {
			queue.clear();
			thread = null;
			stopped = true;
			notifyAll();
		}
	}

	private void cycle() {
		NotifyEvent event = null;
		try {
			synchronized( this ) {
				if( stopping )
					return;
				
				if( queue.isEmpty() )
					wait();

				if( stopping )
					return;
				
				if( queue.isEmpty() )
					return;
				
				event = queue.remove( 0 );
			}
		}
		catch( Throwable e ) {
			events.engine.handle( "events notifier error" , e );
			return;
		}
		
		ServerSourceEvent sse = new ServerSourceEvent( this , 0 , ServerEvents.EVENT_NOTIFY , event.eventData );
		event.app.triggerEvent( event.listener , sse );
	}
	
	public void start() {
		if( started )
			return;
		
		events.engine.info( "start events notifier ..." );
		stopping = false;
        thread = new Thread( null , this , "timer" );
        thread.start();
	}

	public synchronized void stop() {
		if( started == false || stopped )
			return;
		
		events.engine.info( "stop events notifier ..." );
		stopping = true;
		try {
			wait();
		}
		catch( Throwable e ) {
			events.engine.log( "events notifier stop" , e );
		}
	}

	public void addEvent( ServerEventsApp app , ServerEventsListener listener , Object eventData ) {
		synchronized( this ) {
			if( stopping )
				return;
			
			NotifyEvent event = new NotifyEvent( app , listener , eventData );
			queue.add( event );
		}
	}
	
}
