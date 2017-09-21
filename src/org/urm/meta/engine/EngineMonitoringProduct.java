package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.monitor.ActionMonitorTarget;
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
		ActionMonitorTarget targetAction;
	
		ScheduleTaskSegmentMonitoringMajor( String name , ActionMonitorTarget targetAction ) {
			super( name , targetAction.target.scheduleMajor );
			this.targetAction = targetAction;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( targetAction );
			executor.runMajorChecks( super.iteration );
		}
	};
	
	class ScheduleTaskSegmentMonitoringMinor extends ScheduleTask {
		ActionMonitorTarget targetAction;
	
		ScheduleTaskSegmentMonitoringMinor( String name , ActionMonitorTarget targetAction ) {
			super( name , targetAction.target.scheduleMajor );
			this.targetAction = targetAction;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( targetAction );
			executor.runMinorChecks( super.iteration );
		}
	};
	
	EngineMonitoring monitoring;
	MetaMonitoring meta;
	Engine engine;
	Map<String,ActionMonitorTarget> targets;
	
	public EngineMonitoringProduct( EngineMonitoring monitoring , MetaMonitoring meta ) {
		this.monitoring = monitoring;
		this.meta = meta;
		this.engine = monitoring.engine;
		targets = new HashMap<String,ActionMonitorTarget>(); 
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
		for( ActionMonitorTarget target : targets.values() )
			stopTarget( action , target );
		targets.clear();
	}

	private void stopTarget( ActionBase action , ActionMonitorTarget targetAction ) throws Exception {
		targetAction.stop();
		
		MetaEnvSegment sg = targetAction.target.getSegment( action );
		EngineScheduler scheduler = action.getServerScheduler();
		String sgName = sg.meta.name + "-" + sg.env.ID + sg.NAME;
		
		String codeMajor = sgName + "-major";
		ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
		
		String codeMinor = sgName + "-minor";
		task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
	}
	
	private void startTarget( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment( action );
		if( action.isSegmentOffline( sg ) )
			return;
	
		ActionMonitorTarget targetAction = targets.get( target.NAME );
		if( targetAction == null ) {
			MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , meta );
			MonitorTargetInfo info = new MonitorTargetInfo( target , storage );
			targetAction = new ActionMonitorTarget( action , null , info );
			targets.put( target.NAME , targetAction );
		}
		
		targetAction.start();
		
		EngineScheduler scheduler = action.getServerScheduler();
		String sgName = sg.meta.name + "-" + sg.env.ID + sg.NAME;
		
		if( target.enabledMajor ) {
			String codeMajor = sgName + "-major";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMajor( codeMajor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
		
		if( target.enabledMinor ) {
			String codeMinor = sgName + "-minor";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMinor( codeMinor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
	}
	
}
