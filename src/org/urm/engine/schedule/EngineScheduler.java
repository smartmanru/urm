package org.urm.engine.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.Engine;
import org.urm.meta.EngineObject;

public class EngineScheduler extends EngineObject {

	public enum ScheduleTaskCategory {
		MONITORING
	};
	
	public enum ScheduleTaskType {
		SPECIFIC ,
		WEEKLY ,
		DAILY ,
		HOURLY ,
		INTERVAL
	};
	
	Engine engine;
	Map<ScheduleTaskCategory,ScheduleTaskSet> sets;
	List<ScheduleTask> tasks;
	List<ScheduleExecutorTask> executors;
	ScheduleDispatcherTask dispatcher;
	
	static int minExecutors = 2;
	static int maxExecutors = 10;
	
	volatile boolean running;
	int executorsLastId;
	int executorsAvailable;
	
	public EngineScheduler( Engine engine ) {
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
		
		engine.executor.executeCycle( dispatcher );
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
			engine.executor.stopTask( executor );
		executors.clear();
		engine.executor.stopTask( dispatcher );
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

		Date currentDate = new Date(); 
		Date firstRun = task.schedule.getFirstStart();
		if( firstRun.before( currentDate ) )
			return;
		
		set.addTask( task );
		
		synchronized( this ) {
			ensureExecutorAvailable();
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
		engine.executor.executeCycle( executor );
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
		boolean waitAny = false;
		long waitTime = 0;
		
		while( true ) {
			if( !running )
				return;
			
			synchronized( tasks ) {
				if( !running )
					return;
				
				waitAny = true;
				for( ScheduleTask task : tasks ) {
					if( !task.dispatched ) {
						waitAny = false;
						Date dateNow = new Date();
						waitTime = task.expectedTime.getTime() - dateNow.getTime();
						
						if( waitTime <= 0 ) {
							task.setDispatched();
							engine.trace( "SCHEDULE dispatcher: dispatched task=" + task.name );
							tasks.notify();
							return;
						}
						break;
					}
				}
			}
		
			synchronized( dispatcher ) {
				try {
					if( !running )
						return;
					
					if( waitAny )
						dispatcher.wait( 30000 );
					else {
						if( waitTime > 0 ) {
							engine.trace( "SCHEDULE dispatcher: wait for " + ( waitTime / 1000 ) + "s" );
							dispatcher.wait( waitTime );
						}
					}
				}
				catch( Throwable e ) {
				}
			}
		}
	}
	
	public synchronized void release( ScheduleExecutorTask executor , ScheduleTask task ) {
		if( !task.stopped ) {
			Date nextRun = task.schedule.getNextStart( task.lastStarted , task.lastFinished );
			if( nextRun != null )
				addTaskToQueue( task , nextRun );
		}
		
		if( executors.size() > minExecutors && executorsAvailable > 0 ) {
			executors.remove( executor );
			engine.executor.stopTask( executor );
			return;
		}
		
		synchronized( tasks ) {
			executorsAvailable++;
		}
	}
	
}
