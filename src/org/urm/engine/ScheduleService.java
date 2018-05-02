package org.urm.engine;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.schedule.ScheduleDispatcherTask;
import org.urm.engine.schedule.ScheduleExecutorTask;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.schedule.ScheduleTaskSet;
import org.urm.meta.loader.EngineObject;

public class ScheduleService extends EngineObject {

	public enum ScheduleTaskCategory {
		MONITORING
	};
	
	public enum ScheduleTaskType {
		NOW ,
		SPECIFIC ,
		WEEKLY ,
		DAILY ,
		HOURLY ,
		INTERVAL
	};
	
	public Engine engine;
	Map<ScheduleTaskCategory,ScheduleTaskSet> sets;
	List<ScheduleTask> tasks;
	List<ScheduleExecutorTask> executors;
	ScheduleDispatcherTask dispatcher;
	
	static int minExecutors = 2;
	static int maxExecutors = 10;
	
	volatile boolean running;
	int executorsLastId;
	int executorsAvailable;
	
	public ScheduleService( Engine engine ) {
		super( null );
		this.engine = engine;
		
		executors = new LinkedList<ScheduleExecutorTask>();
		running = false;
		executorsLastId = 0;
		executorsAvailable = 0;
		
		sets = new HashMap<ScheduleTaskCategory,ScheduleTaskSet>();
		tasks = new LinkedList<ScheduleTask>();
		dispatcher = new ScheduleDispatcherTask( this ); 
	}
	
	@Override
	public String getName() {
		return( "engine-scheduler" );
	}
	
	public void init() {
		sets.put( ScheduleTaskCategory.MONITORING , new ScheduleTaskSet( this ) );
	}
	
	public synchronized void start( ActionBase action ) {
		running = true;
		
		engine.tasks.executeCycle( dispatcher );
		for( int k = 0; k < minExecutors; k++ )
			createExecutor();
	}
	
	public void stop() {
		running = false;
		
		synchronized( tasks ) {
			for( ScheduleTaskSet set : sets.values() )
				set.clear();
			tasks.clear();
			tasks.notifyAll();
		}
		
		synchronized( dispatcher ) {
			dispatcher.notifyAll();
		}
		
		for( ScheduleExecutorTask executor : executors )
			engine.tasks.stopTask( executor );
		executors.clear();
		engine.tasks.stopTask( dispatcher );
	}

	public long getTimeInterval( int hours , int minutes , int seconds ) {
		return( ( ( hours * 60 + minutes ) * 60 + seconds ) * 1000 );
	}

	public ScheduleTask findTask( ScheduleTaskCategory category , String name ) {
		ScheduleTaskSet set = sets.get( category );
		if( set == null )
			return( null );
		
		return( set.findTask( name ) );
	}

	public void addTask( ActionBase action , ScheduleTaskCategory category , ScheduleTask task ) {
		if( !running )
			return;
			
		ScheduleTaskSet set = sets.get( category );
		if( set == null )
			return;

		Date firstRun = task.getFirstStart();
		if( firstRun == null )
			return;
		
		set.addTask( task );
		synchronized( this ) {
			addTaskToQueue( task , firstRun );
		}
	}
	
	public void deleteTask( ActionBase action , ScheduleTaskCategory category , ScheduleTask task ) {
		ScheduleTaskSet set = sets.get( category );
		if( set == null )
			return;

		synchronized( tasks ) {
			task.setStopped();
			set.deleteTask( task );
			tasks.remove( task );
		}
	}
	
	private void addTaskToQueue( ScheduleTask task , Date runTime ) {
		if( !running )
			return;
			
		task.setExpectedTime( runTime );
		
		synchronized( tasks ) {
			int size = tasks.size();
			boolean added = false;
			int index = 0;
			
			for( int k = 0; k < size; k++ ) {
				ScheduleTask queueTask = tasks.get( k );
				if( runTime.before( queueTask.expectedTime ) ) {
					tasks.add( k , task );
					index = k;
					added = true;
					break;
				}
			}
			
			if( !added ) {
				index = size; 
				tasks.add( task );
			}
			
			synchronized( dispatcher ) {
				engine.debug( "SCHEDULE task=" + task.name + ": scheduled at " + Common.getTimeStamp( runTime ) + " [" + index + "]" );
				dispatcher.notify();
			}
		}
	}

	private void ensureExecutorAvailable() {
		if( executorsAvailable > 0 )
			return;
		
		if( executors.size() == maxExecutors )
			return;
		
		createExecutor();
	}
	
	private void createExecutor() {
		synchronized( tasks ) {
			executorsLastId++;
			executorsAvailable++;
		}
		
		ScheduleExecutorTask executor = new ScheduleExecutorTask( this , executorsLastId );
		executors.add( executor );
		engine.tasks.executeCycle( executor );
	}

	public ScheduleTask getNextTask( ScheduleExecutorTask executor ) {
		if( !running )
			return( null );
			
		ScheduleTask task = null;
		while( true ) {
			synchronized( tasks ) {
				if( tasks.isEmpty() ) {
					if( !waitTask() )
						return( null );
				}
			
				if( !tasks.isEmpty() ) {
					task = tasks.get( 0 );
					if( task.dispatched ) {
						tasks.remove( 0 );
						executorsAvailable--;
						break;
					}
					else {
						if( !waitTask() )
							return( null );
					}
				}
			}
		}
		
		return( task );
	}

	private boolean waitTask() {
		try {
			tasks.wait( 30000 );
		}
		catch( Throwable e ) {
			return( false );
		}
		
		if( !running )
			return( false );
		
		return( true );
	}
	
	public void waitDispatch() {
		long waitTime = dispatchAll();
		synchronized( dispatcher ) {
			try {
				if( !running )
					return;
				
				engine.trace( "SCHEDULE dispatcher: wait for " + ( waitTime / 1000 ) + "s" );
				dispatcher.wait( waitTime );
			}
			catch( Throwable e ) {
			}
		}
	}

	private long dispatchAll() {
		Date dateNow = new Date();
		synchronized( tasks ) {
			for( ScheduleTask task : tasks ) {
				if( !running )
					return( -1 );
				
				if( !task.dispatched ) {
					long waitTime = task.expectedTime.getTime() - dateNow.getTime();
					
					if( waitTime > 0 )
						return( waitTime );
					
					task.setDispatched();
					engine.trace( "SCHEDULE dispatcher: dispatched task=" + task.name );
					ensureExecutorAvailable();
					tasks.notify();
				}
			}
		}
		
		return( 30000 );
	}
	
	public synchronized void release( ScheduleExecutorTask executor , ScheduleTask task ) {
		if( !task.stopped ) {
			Date nextRun = task.getNextStart();
			if( nextRun != null )
				addTaskToQueue( task , nextRun );
		}
		
		if( executors.size() > minExecutors && executorsAvailable > 0 ) {
			executors.remove( executor );
			engine.tasks.stopTask( executor );
			return;
		}
		
		synchronized( tasks ) {
			executorsAvailable++;
		}
	}
	
}
