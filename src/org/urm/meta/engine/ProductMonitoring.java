package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.monitor.ActionMonitorTarget;
import org.urm.action.monitor.MonitorTargetInfo;
import org.urm.action.monitor.MonitorTop;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.PropertyController;
import org.urm.engine.properties.PropertySet;
import org.urm.engine.schedule.EngineScheduler;
import org.urm.engine.schedule.ScheduleProperties;
import org.urm.engine.schedule.EngineScheduler.ScheduleTaskCategory;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ProductMonitoring extends PropertyController {

	class ScheduleTaskSegmentMonitoringMajor extends ScheduleTask {
		ActionMonitorTarget targetAction;
	
		ScheduleTaskSegmentMonitoringMajor( String name , ActionMonitorTarget targetAction ) {
			super( name , targetAction.target.scheduleMajor );
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
			super( name , targetAction.target.scheduleMajor );
			this.targetAction = targetAction;
		}
		
		@Override
		public void execute() throws Exception {
			MonitorTop executor = new MonitorTop( targetAction );
			executor.runMinorChecks( null , super.iteration );
		}
	};
	
	public EngineMonitoring monitoring;
	public Product product;
	public Engine engine;
	public Meta meta;

	private Map<String,ActionMonitorTarget> targets;
	private Map<String,ProductMonitoringTarget> mapTargets;

	// fixed
	public boolean ENABLED;
	
	// expression
	public String RESOURCE_URL;
	public String DIR_RES;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_LOGS;
	
	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_URL = "resources.url";
	public static String PROPERTY_DIR_RES = "resources.path";
	public static String PROPERTY_DIR_DATA = "data.path";
	public static String PROPERTY_DIR_REPORTS = "reports.path";
	public static String PROPERTY_DIR_LOGS = "logs.path";
	
	public ProductMonitoring( EngineMonitoring monitoring , Product product ) {
		super( monitoring , null , "monitoring" );
		this.monitoring = monitoring;
		this.engine = monitoring.engine;
		targets = new HashMap<String,ActionMonitorTarget>(); 
		mapTargets = new HashMap<String,ProductMonitoringTarget>();
	}
	
	public synchronized void start( ActionBase action ) throws Exception {
		if( !monitoring.isEnabled() )
			return;
		
		meta = action.getProductMetadata( product.NAME );
		if( meta == null || !ENABLED )
			return;
		
		if( action.isProductOffline( meta ) )
			return;
		
		for( ProductMonitoringTarget target : mapTargets.values() )
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
	
	private void startTarget( ActionBase action , ProductMonitoringTarget target ) throws Exception {
		MetaEnvSegment sg = target.getSegment( action );
		if( action.isSegmentOffline( sg ) )
			return;
	
		ActionMonitorTarget targetAction = targets.get( target.NAME );
		if( targetAction == null ) {
			MonitoringStorage storage = action.artefactory.getMonitoringStorage( action , this );
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
	
	@Override
	public String getName() {
		return( "meta-monitoring" );
	}
	
	@Override
	public boolean isValid() {
		if( super.isLoadFailed() )
			return( false );
		return( true );
	}
	
	@Override
	public void scatterProperties( ActionBase action ) throws Exception {
		ENABLED = super.getBooleanProperty( action , PROPERTY_ENABLED );
		RESOURCE_URL = super.getStringProperty( action , PROPERTY_RESOURCE_URL );
		DIR_RES = super.getPathProperty( action , PROPERTY_DIR_RES );
		DIR_DATA = super.getPathProperty( action , PROPERTY_DIR_DATA );
		DIR_REPORTS = super.getPathProperty( action , PROPERTY_DIR_REPORTS );
		DIR_LOGS = super.getPathProperty( action , PROPERTY_DIR_LOGS );
	}
	
	public ProductMonitoring copy( ActionBase action ) throws Exception {
		ProductMonitoring r = new ProductMonitoring( null , product );
		r.initCopyStarted( this , monitoring.properties );
		
		for( ProductMonitoringTarget target : mapTargets.values() ) {
			ProductMonitoringTarget rtarget = target.copy( action , r );
			r.mapTargets.put( target.NAME , rtarget );
		}
		
		r.scatterProperties( action );
		r.initFinished();
		return( r );
	}
	
	public void createMonitoring( TransactionBase transaction ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( transaction.action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;
		
		ActionBase action = transaction.getAction();
		EngineMonitoring sm = action.getServerMonitoring();
		PropertySet src = sm.properties;
		super.setSystemBooleanProperty( PROPERTY_ENABLED , false );
		super.setSystemUrlProperty( PROPERTY_RESOURCE_URL , src.getExpressionByProperty( EngineMonitoring.PROPERTY_RESOURCE_URL ) );
		super.setSystemPathProperty( PROPERTY_DIR_RES , src.getExpressionByProperty( EngineMonitoring.PROPERTY_RESOURCE_PATH ) );
		super.setSystemPathProperty( PROPERTY_DIR_DATA , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_DATA ) );
		super.setSystemPathProperty( PROPERTY_DIR_REPORTS , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_REPORTS ) );
		super.setSystemPathProperty( PROPERTY_DIR_LOGS , src.getExpressionByProperty( EngineMonitoring.PROPERTY_DIR_LOGS ) );
		scatterProperties( action );
		super.initFinished();
	}
	
	public void load( ActionBase action , Node root ) throws Exception {
		MetaProductSettings product = meta.getProductSettings( action );
		if( !super.initCreateStarted( product.getProperties() ) )
			return;

		super.loadFromNodeElements( action , root , false );
		
		scatterProperties( action );
		super.finishProperties( action );
		
		loadTargets( action , ConfReader.xmlGetPathNode( root , "scope" ) );
		
		super.initFinished();
	}

	public ProductMonitoringTarget[] getTargets() { 
		return( mapTargets.values().toArray( new ProductMonitoringTarget[0] ) );
	}
	
	private void loadTargets( ActionBase action , Node node ) throws Exception {
		mapTargets.clear();
		
		Node[] items = null;
		if( node != null )
			items = ConfReader.xmlGetChildren( node , "target" );
		
		if( items == null ) {
			action.info( "no targets defined for monitoring" );
			return;
		}

		for( Node deliveryNode : items ) {
			ProductMonitoringTarget item = new ProductMonitoringTarget( this );
			item.loadTarget( action , deliveryNode );
			addTarget( item );
		}
	}
	
	private void addTarget( ProductMonitoringTarget target ) {
		mapTargets.put( target.NAME , target );
	}

	public void save( ActionBase action , Document doc , Element root ) throws Exception {
		boolean create = createFolders( action );
		if( !create ) {
			action.error( "monitoring is forced off because folders (res=" + DIR_RES + ", data=" + DIR_DATA + ", reports=" + DIR_REPORTS + ", logs=" + DIR_LOGS + ") are not ready, check settings" );
			super.setBooleanProperty( PROPERTY_ENABLED , false );
			ENABLED = false;
		}
		
		super.saveAsElements( doc , root , false );
		
		Element scope = Common.xmlCreateElement( doc , root , "scope" );
		for( ProductMonitoringTarget target : mapTargets.values() ) {
			Element element = Common.xmlCreateElement( doc , scope , "target" );
			if( create )
				target.createFolders( action );
			target.save( action , doc , element );
		}
	}

	private boolean createFolders( ActionBase action ) throws Exception {
		if( DIR_RES.isEmpty() || 
			DIR_DATA.isEmpty() || 
			DIR_REPORTS.isEmpty() || 
			DIR_LOGS.isEmpty() )
			return( false );
		
		LocalFolder folder = action.getLocalFolder( DIR_DATA );
		folder.ensureExists( action );
		folder = action.getLocalFolder( DIR_REPORTS );
		folder.ensureExists( action );
		folder = action.getLocalFolder( DIR_LOGS );
		folder.ensureExists( action );
		return( true );
	}
	
	public ProductMonitoringTarget findMonitoringTarget( MetaEnvSegment sg ) {
		for( ProductMonitoringTarget target : mapTargets.values() ) {
			if( target.ENV.equals( sg.env.ID ) && target.SG.equals( sg.NAME ) )
				return( target );
		}
		return( null );
	}

	public void setMonitoringEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		super.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
	}

	public ProductMonitoringTarget modifyTarget( EngineTransaction transaction , MetaEnvSegment sg , boolean major , boolean enabled , int maxTime , ScheduleProperties schedule ) throws Exception {
		ProductMonitoringTarget target = findMonitoringTarget( sg );
		if( target == null ) {
			target = new ProductMonitoringTarget( this );
			target.createTarget( transaction , sg );
			addTarget( target );
		}
		
		target.modifyTarget( transaction , major , enabled , schedule , maxTime );
		return( target );
	}

	public void setProductProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		super.updateProperties( props );
		setMonitoringEnabled( transaction , false );
		scatterProperties( transaction.getAction() );
	}
	
}
