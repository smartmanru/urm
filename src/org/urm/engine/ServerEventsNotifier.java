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
	
	private ServerThread thread;
	private List<NotifyEvent> queue;
	
	public ServerEventsNotifier( ServerEvents events ) {
		super( events , "urm.notifier" );
		queue = new LinkedList<NotifyEvent>();
		thread = new ServerThread( events.engine , this , "events notifier" , true );
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	@Override
	public void run() {
		try {
			cycle();
		}
		catch( Throwable e ) {
			events.engine.handle( "events notifier error" , e );
		}
	}

	private void cycle() {
		NotifyEvent event = null;
		try {
			synchronized( thread ) {
				if( !thread.isRunning() )
					return;
				
				if( queue.isEmpty() )
					thread.wait();

				if( !thread.isRunning() )
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
		thread.start();
	}

	public synchronized void stop() {
		thread.stop();
	}

	public void addEvent( ServerEventsApp app , ServerEventsListener listener , Object eventData ) {
		synchronized( thread ) {
			if( !thread.isRunning() )
				return;
			
			NotifyEvent event = new NotifyEvent( app , listener , eventData );
			queue.add( event );
			thread.notifyAll();
		}
	}
	
}
