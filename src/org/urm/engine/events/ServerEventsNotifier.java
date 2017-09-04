package org.urm.engine.events;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.EngineExecutorTask;

public class ServerEventsNotifier extends ServerEventsSource {

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

	class ServerExecutorTaskNotify extends EngineExecutorTask {
		ServerExecutorTaskNotify() {
			super( "events notifier" );
		}
		
		@Override
		public void execute() {
			try {
				cycle();
			}
			catch( Throwable e ) {
				events.engine.handle( "events notifier error" , e );
			}
		}
	};		

	private ServerExecutorTaskNotify task;
	private List<NotifyEvent> queue;
	
	public ServerEventsNotifier( ServerEvents events ) {
		super( events , "urm.notifier" );
		queue = new LinkedList<NotifyEvent>();
		task = new ServerExecutorTaskNotify();
	}

	@Override
	public ServerEventsState getState() {
		return( null );
	}
	
	private void cycle() {
		NotifyEvent event = null;
		try {
			synchronized( task ) {
				if( queue.isEmpty() )
					task.wait();

				if( !task.isRunning() )
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
		events.engine.executor.executeCycle( task );
	}

	public synchronized void stop() {
		events.engine.executor.stopTask( task );
	}

	public void addEvent( ServerEventsApp app , ServerEventsListener listener , Object eventData ) {
		synchronized( task ) {
			if( !task.isRunning() )
				return;
			
			NotifyEvent event = new NotifyEvent( app , listener , eventData );
			queue.add( event );
			task.notifyAll();
		}
	}
	
}
