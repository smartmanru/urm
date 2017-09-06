package org.urm.meta.engine;

import org.urm.action.ActionEventsSource;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.engine.Engine;
import org.urm.engine.EngineExecutorTask;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineSourceEvent;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.StatusSource;
import org.urm.engine.status.StatusData;
import org.urm.engine.status.ServerStatus;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoringTarget;

public class EngineMonitoringProduct implements EngineEventsListener {
	
	class ServerExecutorTaskMonitorProduct extends EngineExecutorTask {
		ServerExecutorTaskMonitorProduct( String productName ) {
			super( "monitoring::" + productName );
		}
		
		@Override
		public void execute() {
			runTop();
		}
	};		
	
	private ServerExecutorTaskMonitorProduct task;

	EngineMonitoring monitoring;
	String productName;
	StatusSource source;
	Engine engine;
	
	ActionMonitorTop ca;
	EngineEventsApp eventsApp;

	public EngineMonitoringProduct( EngineMonitoring monitoring , String productName , StatusSource source , EngineEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.productName = productName;
		this.source = source;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
		
		task = new ServerExecutorTaskMonitorProduct( productName ); 
	}
	
	public void runTop() {
		try {
			ca = new ActionMonitorTop( engine.serverAction , productName , productName , eventsApp );
			eventsApp.subscribe( ca.eventSource , this );
			ca.runSimpleProduct( productName , SecurityAction.ACTION_MONITOR , false );
		}
		catch( Throwable e ) {
			engine.handle( "thread pool house keeping error" , e );
		}
		
		eventsApp.unsubscribe( this );
		ca = null;
	}

	@Override
	public void triggerEvent( EngineSourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_MONITORING_SEGMENT ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			SegmentStatus status = ( SegmentStatus )event.data;
			MetaEnvSegment sg = status.sg;
			StatusSource serverSource = monitoring.getObjectSource( sg );
			if( serverSource == null )
				return;
			
			processSegmentEvent( source , serverSource , sg , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SGITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			SegmentStatus status = ( SegmentStatus )event.data;
			MetaEnvSegment sg = status.sg;
			StatusSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource == null )
				return;
			
			processSegmentItemsEvent( source , sgSource , sg , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SERVER ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ServerStatus status = ( ServerStatus )event.data;
			MetaEnvServer server = status.server;
			StatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			processServerEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SERVERITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ServerStatus status = ( ServerStatus )event.data;
			MetaEnvServer server = status.server;
			StatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			processServerItemsEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_NODE ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			NodeStatus status = ( NodeStatus )event.data;
			MetaEnvServerNode node = status.node;
			StatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			processNodeEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_NODEITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			NodeStatus status = ( NodeStatus )event.data;
			MetaEnvServerNode node = status.node;
			StatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			processNodeItemsEvent( source , nodeSource , node , status );
			return;
		}
		
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
	
	public synchronized void start() {
		engine.executor.executeOnce( task );
	}
	
	public synchronized void stop() {
		ca.stopRunning();
		engine.executor.stopTask( task );
		
		// cleanup product data
		source.setState( OBJECT_STATE.STATE_NODATA );
		Product product = ( Product )source.object;
		recalculateSystem( product.system );
	}

	private void processSegmentEvent( ActionEventsSource source , StatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !task.isRunning() )
			return;

		sgSource.setPrimaryLog( status.getLog() );
	}
	
	private void processSegmentItemsEvent( ActionEventsSource source , StatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !task.isRunning() )
			return;

		sgSource.setExtraLog( EngineMonitoring.EXTRA_SEGMENT_ITEMS , status.getLog() );
		if( sgSource.setExtraState( EngineMonitoring.EXTRA_SEGMENT_ITEMS , status.itemState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void processServerEvent( ActionEventsSource source , StatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !task.isRunning() )
			return;

		serverSource.setPrimaryLog( status.getLog() );
		if( serverSource.setState( status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processServerItemsEvent( ActionEventsSource source , StatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !task.isRunning() )
			return;

		serverSource.setExtraLog( EngineMonitoring.EXTRA_SERVER_ITEMS , status.getLog() );
		if( serverSource.setExtraState( EngineMonitoring.EXTRA_SERVER_ITEMS , status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processNodeEvent( ActionEventsSource source , StatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !task.isRunning() )
			return;

		nodeSource.setPrimaryLog( status.getLog() );
		if( nodeSource.setState( status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void processNodeItemsEvent( ActionEventsSource source , StatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !task.isRunning() )
			return;

		nodeSource.setExtraLog( EngineMonitoring.EXTRA_NODE_ITEMS , status.getLog() );
		if( nodeSource.setExtraState( EngineMonitoring.EXTRA_NODE_ITEMS , status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void recalculateServer( MetaEnvServer server ) {
		StatusSource serverSource = monitoring.getObjectSource( server );
		if( serverSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvServerNode node : server.getNodes() ) {
			StatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource != null )
				finalState = StatusData.addState( finalState , nodeSource.state.state );
		}
		
		if( serverSource.setState( finalState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void recalculateSegment( MetaEnvSegment sg ) {
		StatusSource sgSource = monitoring.getObjectSource( sg );
		if( sgSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvServer server : sg.getServers() ) {
			StatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource != null )
				finalState = StatusData.addState( finalState , serverSource.state.state );
		}
		
		if( sgSource.setState( finalState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void recalculateEnv( MetaEnv env ) {
		StatusSource envSource = monitoring.getObjectSource( env );
		if( envSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvSegment sg : env.getSegments() ) {
			StatusSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource != null )
				finalState = StatusData.addState( finalState , sgSource.state.state );
		}
		
		if( envSource.setState( finalState ) ) {
			Meta meta = env.meta;
			recalculateProduct( meta );
		}
	}

	private void recalculateProduct( Meta meta ) {
		Product product = monitoring.findProduct( meta.name );
		if( product == null )
			return;
		
		StatusSource productSource = monitoring.getObjectSource( product );
		if( productSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( String envName : meta.getEnvNames() ) {
			MetaEnv env = meta.findEnv( envName );
			StatusSource envSource = monitoring.getObjectSource( env );
			if( envSource != null )
				finalState = StatusData.addState( finalState , envSource.state.state );
		}
		
		if( productSource.setState( finalState ) ) {
			System system = product.system;
			recalculateSystem( system );
		}
	}

	private void recalculateSystem( System system ) {
		StatusSource systemSource = monitoring.getObjectSource( system );
		if( systemSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			StatusSource productSource = monitoring.getObjectSource( product );
			if( productSource != null )
				finalState = StatusData.addState( finalState , productSource.state.state );
		}
		
		if( systemSource.setState( finalState ) )
			recalculateApp( system.directory );
	}

	private void recalculateApp( EngineDirectory directory ) {
		StatusSource appSource = monitoring.getAppSource();
		if( appSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( String systemName : directory.getSystems() ) {
			System system = directory.findSystem( systemName );
			StatusSource systemSource = monitoring.getObjectSource( system );
			if( systemSource != null )
				finalState = StatusData.addState( finalState , systemSource.state.state );
		}
		
		appSource.setState( finalState );
	}
	
}
