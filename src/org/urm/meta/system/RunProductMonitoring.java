package org.urm.meta.system;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.monitor.ActionMonitorTarget;
import org.urm.action.monitor.MonitorTargetInfo;
import org.urm.action.monitor.MonitorTop;
import org.urm.engine.ScheduleService;
import org.urm.engine.ScheduleService.ScheduleTaskCategory;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductCoreSettings;

public class RunProductMonitoring {

	class ScheduleTaskSegmentMonitoringMajor extends ScheduleTask {
		ActionMonitorTarget targetAction;
	
		ScheduleTaskSegmentMonitoringMajor( String name , ActionMonitorTarget targetAction ) {
			super( name , targetAction.target.majorSchedule );
			this.targetAction = targetAction;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( targetAction );
			executor.runMajorChecks( null , super.iteration );
		}
	};
	
	class ScheduleTaskSegmentMonitoringMinor extends ScheduleTask {
		ActionMonitorTarget targetAction;
	
		ScheduleTaskSegmentMonitoringMinor( String name , ActionMonitorTarget targetAction ) {
			super( name , targetAction.target.majorSchedule );
			this.targetAction = targetAction;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( targetAction );
			executor.runMinorChecks( null , super.iteration );
		}
	};
	
	EngineMonitoring monitoring;
	EngineProduct ep;
	Map<Integer,ActionMonitorTarget> targets;
	
	public RunProductMonitoring( EngineMonitoring monitoring , EngineProduct ep ) {
		this.monitoring = monitoring;
		this.ep = ep;
		targets = new HashMap<Integer,ActionMonitorTarget>();
	}
	
	public synchronized void start( ActionBase action ) throws Exception {
		if( !monitoring.isEnabled() )
			return;

		AppProduct product = ep.findProduct();
		if( product == null || !product.MONITORING_ENABLED )
			return;
		
		if( product.isOffline() )
			return;
		
		AppProductMonitoring mon = product.getMonitoring();
	
		if( !createFolders( action , mon ) )
			return;
			
		for( AppProductMonitoringTarget target : mon.getTargets() )
			startTarget( action , target );
	}
	
	public synchronized void stop( ActionBase action ) throws Exception {
		for( ActionMonitorTarget target : targets.values() )
			stopTarget( action , target );
		targets.clear();
	}

	private void stopTarget( ActionBase action , ActionMonitorTarget targetAction ) throws Exception {
		targetAction.stop();
		
		ScheduleService scheduler = action.getEngineScheduler();
		
		String codeMajor = targetAction.name + "-major";
		ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
		
		String codeMinor = targetAction.name + "-minor";
		task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
	}
	
	public void addTarget( AppProductMonitoringTarget target , ActionMonitorTarget ta ) {
		targets.put( target.ID , ta );
	}
	
	private void startTarget( ActionBase action , AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		if( sg == null )
			return;
		
		if( action.isSegmentOffline( sg ) )
			return;
	
		ActionMonitorTarget targetAction = targets.get( target.ID );
		if( targetAction == null ) {
			MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , sg.meta );
			MonitorTargetInfo info = new MonitorTargetInfo( target , storage );
			String targetName = sg.meta.name + "-" + sg.env.NAME + sg.NAME;
			targetAction = new ActionMonitorTarget( action , null , info , targetName );
			addTarget( target , targetAction );
		}
		
		targetAction.start();
		
		ScheduleService scheduler = action.getEngineScheduler();
		
		if( target.MAJOR_ENABLED ) {
			String codeMajor = targetAction.name + "-major";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMajor( codeMajor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
		
		if( target.MINOR_ENABLED ) {
			String codeMinor = targetAction.name + "-minor";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMinor( codeMinor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
	}
	
	private boolean createFolders( ActionBase action , AppProductMonitoring mon ) {
		try {
			for( AppProductMonitoringTarget target : mon.getTargets() )
				createFolders( action , target );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "create monitoring folders failed, product=" + ep.productName , e );
			return( false );
		}
	}
	
	public void createFolders( ActionBase action , AppProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.findSegment();
		if( sg == null )
			return;
		
		Meta meta = sg.meta;
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		if( !core.isValidMonitoringSettings() ) {
			action.error( "monitoring is forced off because monitoring folders are not ready, check settings, product=" + meta.name );
			return;
		}
		
		LocalFolder folder = action.getLocalFolder( core.MONITORING_DIR_DATA );
		folder.ensureExists( action );
		folder = action.getLocalFolder( core.MONITORING_DIR_REPORTS );
		folder.ensureExists( action );
		folder = action.getLocalFolder( core.MONITORING_DIR_LOGS );
		folder.ensureExists( action );
		
		MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , meta );
		folder = storage.getDataFolder( action , target );
		folder.ensureExists( action );
		folder = storage.getReportsFolder( action , target );
		folder.ensureExists( action );
		folder = storage.getLogsFolder( action , target );
		folder.ensureExists( action );
	}

}

