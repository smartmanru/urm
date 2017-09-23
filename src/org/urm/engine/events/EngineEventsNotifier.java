package org.urm.engine.events;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.EngineExecutorTask;

public class EngineEventsNotifier extends EngineEventsSource {

	class ServerExecutorTaskNotify extends EngineExecutorTask {
		ServerExecutorTaskNotify( int index ) {
			super( "events-notifier-" + index );
		}
		
		@Override
		public void execute() {
			try {
				cycle( this );
			}
			catch( Throwable e ) {
				events.engine.handle( "events notifier error" , e );
			}
		}
	};		

	private List<ServerExecutorTaskNotify> tasks;
	private List<NotifyEvent> queue;
	private volatile boolean running;

	static int NOTIFY_POOL = 10; 
	
	public EngineEventsNotifier( EngineEvents events ) {
		super( events , "urm.notifier" );
		queue = new LinkedList<NotifyEvent>();
		tasks = new LinkedList<ServerExecutorTaskNotify>();
		running = false;
	}

	@Override
	public EngineEventsState getState() {
		return( null );
	}
	
	private void cycle( ServerExecutorTaskNotify task ) {
		NotifyEvent event = null;
		try {
			synchronized( tasks ) {
				if( queue.isEmpty() )
					tasks.wait();

				if( !running )
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
		
		try {
			event.listener.triggerEvent( event.sub , event.eventData );
		}
		catch( Throwable e ) {
			events.engine.handle( "events notifier error" , e );
		}
		
		if( event.sub != null )
			event.sub.finishEvent( event );
	}
	
	public void start() {
		running = true;
		for( int k = 0; k < NOTIFY_POOL; k++ ) {
			ServerExecutorTaskNotify task = new ServerExecutorTaskNotify( k + 1 );
			events.engine.executor.executeCycle( task );
		}
	}

	public void stop() {
		synchronized( tasks ) {
			running = false;
			for( ServerExecutorTaskNotify task : tasks )
				events.engine.executor.stopTask( task );
			tasks.clear();
			tasks.notifyAll();
		}
	}

	public void addEvent( NotifyEvent event ) {
		synchronized( tasks ) {
			if( !running )
				return;
			
			queue.add( event );
			tasks.notify();
		}
	}
	
}
