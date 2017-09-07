package org.urm.engine.events;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.EngineExecutorTask;

public class EngineEventsNotifier extends EngineEventsSource {

	class NotifyEvent {
		public EngineEventsApp app;
		public EngineEventsListener listener;
		public EngineSourceEvent eventData;
		
		public NotifyEvent( EngineEventsApp app , EngineEventsListener listener , EngineSourceEvent eventData ) {
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
	
	public EngineEventsNotifier( EngineEvents events ) {
		super( events , "urm.notifier" );
		queue = new LinkedList<NotifyEvent>();
		task = new ServerExecutorTaskNotify();
	}

	@Override
	public EngineEventsState getState() {
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
		
		event.app.triggerEvent( event.eventData );
	}
	
	public void start() {
		events.engine.executor.executeCycle( task );
	}

	public synchronized void stop() {
		events.engine.executor.stopTask( task );
	}

	public void addEvent( EngineEventsApp app , EngineEventsListener listener , EngineSourceEvent eventData ) {
		synchronized( task ) {
			if( !task.isRunning() )
				return;
			
			NotifyEvent event = new NotifyEvent( app , listener , eventData );
			queue.add( event );
			task.notifyAll();
		}
	}
	
}
