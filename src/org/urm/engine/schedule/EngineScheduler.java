package org.urm.engine.schedule;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
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
	
	boolean started;
	int executorsLastId;
	int executorsAvailable;
	
	public EngineScheduler( Engine engine ) {
		super( null );
		this.engine = engine;
		
		executors = new LinkedList<ScheduleExecutorTask>();
		started = false;
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
		started = true;
		
		engine.executor.executeCycle( dispatcher );
		for( int k = 0; k < minExecutors; k++ )
			createExecutor();
	}
	
	public synchronized void stop() {
		started = false;
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

		set.deleteTask( task );
		task.setStopped();
	}
	
	private void addTaskToQueue( ScheduleTask task , Date runTime ) {
		task.setExpectedTime( runTime );
		
		synchronized( tasks ) {
			boolean empty = tasks.isEmpty();
			boolean added = false;
			
			if( empty ) {
				for( int k = 0; k < tasks.size(); k++ ) {
					ScheduleTask queueTask = tasks.get( k );
					if( runTime.before( queueTask.lastStarted ) ) {
						tasks.add( k , task );
						added = true;
						break;
					}
				}
			}
			
			if( !added )
				tasks.add( task );
			
			synchronized( dispatcher ) {
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
		while( true ) {
			synchronized( tasks ) {
				if( tasks.isEmpty() ) {
					try {
						tasks.wait();
					}
					catch( Throwable e ) {
						return( null );
					}
					
					if( !started )
						return( null );
				}
			
				if( !tasks.isEmpty() ) {
					ScheduleTask task = tasks.remove( 0 );
					executorsAvailable--;
					return( task );
				}
			}
		}
	}

	public void waitDispatch() {
		boolean waitAny = false;
		long waitTime = 0;
		
		synchronized( tasks ) {
			waitAny = true;
			for( ScheduleTask task : tasks ) {
				if( !task.dispatched ) {
					task.setDispatched();
				
					waitAny = false;
					Date dateNow = new Date();
					waitTime = task.expectedTime.getTime() - dateNow.getTime();
					break;
				}
			}
		}
		
		synchronized( dispatcher ) {
			try {
				if( waitAny )
					dispatcher.wait();
				else {
					if( waitTime > 0 )
						dispatcher.wait( waitTime );
				}
			
				tasks.notify();
			}
			catch( Throwable e ) {
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
