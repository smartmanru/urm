package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.engine.Engine;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineSourceEvent;
import org.urm.engine.schedule.EngineScheduler;
import org.urm.engine.schedule.EngineScheduler.ScheduleTaskCategory;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.status.StatusSource;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;

public class EngineMonitoringProduct implements EngineEventsListener {

	class ScheduleTaskSegmentMonitoringMajor extends ScheduleTask {
		MetaEnvSegment sg;
		long timeLimit;
	
		ScheduleTaskSegmentMonitoringMajor( String name , ScheduleProperties schedule , MetaEnvSegment sg , long timeLimit ) {
			super( name , schedule );
			this.sg = sg;
			this.timeLimit = timeLimit;
		}
		
		@Override
		public void execute() {
		}
	};
	
	class ScheduleTaskSegmentMonitoringMinor extends ScheduleTask {
		MetaEnvSegment sg;
		long timeLimit;
	
		ScheduleTaskSegmentMonitoringMinor( String name , ScheduleProperties schedule , MetaEnvSegment sg , long timeLimit ) {
			super( name , schedule );
			this.sg = sg;
			this.timeLimit = timeLimit;
		}
		
		@Override
		public void execute() {
		}
	};
	
	EngineMonitoring monitoring;
	MetaMonitoring meta;
	Engine engine;
	
	ActionMonitorTop ca;
	EngineEventsApp eventsApp;

	public EngineMonitoringProduct( EngineMonitoring monitoring , MetaMonitoring meta , EngineEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.meta = meta;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
	}
	
	public void runTop() {
		try {
			ca = new ActionMonitorTop( engine.serverAction , meta.meta.name , meta.meta.name );
			eventsApp.subscribe( ca.eventSource , this );
			ca.runSimpleProduct( meta.meta.name , SecurityAction.ACTION_MONITOR , false );
		}
		catch( Throwable e ) {
			engine.handle( "thread pool house keeping error" , e );
		}
		
		eventsApp.unsubscribe( this );
		ca = null;
	}

	@Override
	public void triggerEvent( EngineSourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_MONITORGRAPHCHANGED ) {
			MetaMonitoringTarget target = ( MetaMonitoringTarget )event.data;
			StatusSource sgSource = monitoring.findTargetSource( target );
			if( sgSource == null )
				return;
			
			sgSource.customEvent( EngineEvents.EVENT_MONITORGRAPHCHANGED , target );
			return;
		}
	}

	@Override
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub ) {
	}
	
	public synchronized void start( ActionBase action ) throws Exception {
		if( !monitoring.isEnabled() )
			return;
		
		if( !meta.ENABLED )
			return;
		
		if( action.isProductOffline( meta.meta ) )
			return;
		
		for( MetaMonitoringTarget target : meta.getTargets() )
			startTarget( action , target );
	}
	
	public synchronized void stop( ActionBase action ) throws Exception {
		for( MetaMonitoringTarget target : meta.getTargets() )
			stopTarget( action , target );
	}

	private void stopTarget( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment( action );
		EngineScheduler scheduler = action.getServerScheduler();
		String sgName = sg.meta.name + "-" + sg.env.ID + sg.NAME;
		
		String codeMajor = sgName + "-major";
		ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
	}
	
	private void startTarget( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment( action );
		if( action.isSegmentOffline( sg ) )
			return;
		
		EngineScheduler scheduler = action.getServerScheduler();
		
		String sgName = sg.meta.name + "-" + sg.env.ID + sg.NAME;
		
		if( target.enabledMajor ) {
			String codeMajor = sgName + "-major";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMajor( codeMajor , target.scheduleMajor , sg , target.maxTimeMajor ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
		
		if( target.enabledMinor ) {
			String codeMinor = sgName + "-minor";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , sgName + "-minor" );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMinor( codeMinor , target.scheduleMinor , sg , target.maxTimeMinor ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
	}
	
}
