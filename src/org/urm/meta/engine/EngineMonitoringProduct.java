package org.urm.meta.engine;

import org.urm.action.ActionBase;
import org.urm.action.monitor.MonitorTargetInfo;
import org.urm.action.monitor.MonitorTop;
import org.urm.engine.Engine;
import org.urm.engine.schedule.EngineScheduler;
import org.urm.engine.schedule.EngineScheduler.ScheduleTaskCategory;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;

public class EngineMonitoringProduct {

	class ScheduleTaskSegmentMonitoringMajor extends ScheduleTask {
		MonitorTargetInfo info;
	
		ScheduleTaskSegmentMonitoringMajor( String name , MonitorTargetInfo info ) {
			super( name , info.target.scheduleMajor );
			this.info = info;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( info );
			executor.runMajorChecks( engine.serverAction , super.iteration );
		}
	};
	
	class ScheduleTaskSegmentMonitoringMinor extends ScheduleTask {
		MonitorTargetInfo info;
	
		ScheduleTaskSegmentMonitoringMinor( String name , MonitorTargetInfo info ) {
			super( name , info.target.scheduleMinor );
			this.info = info;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( info );
			executor.runMinorChecks( engine.serverAction , super.iteration );
		}
	};
	
	EngineMonitoring monitoring;
	MetaMonitoring meta;
	Engine engine;
	
	public EngineMonitoringProduct( EngineMonitoring monitoring , MetaMonitoring meta ) {
		this.monitoring = monitoring;
		this.meta = meta;
		this.engine = monitoring.engine;
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
		
		MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , meta );
		MonitorTargetInfo info = new MonitorTargetInfo( target , storage );
		
		String sgName = sg.meta.name + "-" + sg.env.ID + sg.NAME;
		
		if( target.enabledMajor ) {
			String codeMajor = sgName + "-major";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMajor( codeMajor , info ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
		
		if( target.enabledMinor ) {
			String codeMinor = sgName + "-minor";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , sgName + "-minor" );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMinor( codeMinor , info ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
	}
	
}
