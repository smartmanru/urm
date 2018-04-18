package org.urm.meta.engine;

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
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.env.MetaMonitoringTarget;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductCoreSettings;
import org.urm.meta.product.ProductMeta;

public class MonitoringProduct {

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
	
	public MonitoringProduct( EngineMonitoring monitoring , EngineProduct ep ) {
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
		
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			MetaMonitoring mon = storage.getMonitoring();
		
			if( !createFolders( action , mon ) )
				return;
				
			for( MetaMonitoringTarget target : mon.getTargets() )
				startTarget( action , target );
		}
	}
	
	public synchronized void stop( ActionBase action ) throws Exception {
		for( ActionMonitorTarget target : targets.values() )
			stopTarget( action , target );
		targets.clear();
	}

	private void stopTarget( ActionBase action , ActionMonitorTarget targetAction ) throws Exception {
		targetAction.stop();
		
		MetaEnvSegment sg = targetAction.target.getSegment();
		ScheduleService scheduler = action.getEngineScheduler();
		String sgName = sg.meta.name + "-" + sg.env.NAME + sg.NAME;
		
		String codeMajor = sgName + "-major";
		ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
		
		String codeMinor = sgName + "-minor";
		task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
		if( task != null )
			scheduler.deleteTask( action , ScheduleTaskCategory.MONITORING , task );
	}
	
	public void addTarget( MetaMonitoringTarget target , ActionMonitorTarget ta ) {
		targets.put( target.ID , ta );
	}
	
	private void startTarget( ActionBase action , MetaMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment();
		if( action.isSegmentOffline( sg ) )
			return;
	
		ActionMonitorTarget targetAction = targets.get( target.ID );
		if( targetAction == null ) {
			MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , target.envs.meta );
			MonitorTargetInfo info = new MonitorTargetInfo( target , storage );
			targetAction = new ActionMonitorTarget( action , null , info );
			addTarget( target , targetAction );
		}
		
		targetAction.start();
		
		ScheduleService scheduler = action.getEngineScheduler();
		String sgName = sg.meta.name + "-" + sg.env.NAME + sg.NAME;
		
		if( target.MAJOR_ENABLED ) {
			String codeMajor = sgName + "-major";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMajor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMajor( codeMajor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
		
		if( target.MINOR_ENABLED ) {
			String codeMinor = sgName + "-minor";
			ScheduleTask task = scheduler.findTask( ScheduleTaskCategory.MONITORING , codeMinor );
			if( task == null ) {
				task = new ScheduleTaskSegmentMonitoringMinor( codeMinor , targetAction ); 
				scheduler.addTask( action , ScheduleTaskCategory.MONITORING , task );
			}
		}
	}
	
	private boolean createFolders( ActionBase action , MetaMonitoring mon ) {
		Meta meta = mon.envs.meta;
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		if( !core.isValidMonitoringSettings() ) {
			action.error( "monitoring is forced off because monitoring folders are not ready, check settings, product=" + meta.name );
			return( false );
		}
		
		try {
			LocalFolder folder = action.getLocalFolder( core.MONITORING_DIR_DATA );
			folder.ensureExists( action );
			folder = action.getLocalFolder( core.MONITORING_DIR_REPORTS );
			folder.ensureExists( action );
			folder = action.getLocalFolder( core.MONITORING_DIR_LOGS );
			folder.ensureExists( action );
			
			for( MetaMonitoringTarget target : mon.getTargets() )
				createFolders( action , meta , target );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "create monitoring folders failed, product=" + meta.name , e );
			return( false );
		}
	}
	
	public void createFolders( ActionBase action , Meta meta , MetaMonitoringTarget target ) throws Exception {
		MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , meta );
		LocalFolder folder = storage.getDataFolder( action , target );
		folder.ensureExists( action );
		folder = storage.getReportsFolder( action , target );
		folder.ensureExists( action );
		folder = storage.getLogsFolder( action , target );
		folder.ensureExists( action );
	}

}
