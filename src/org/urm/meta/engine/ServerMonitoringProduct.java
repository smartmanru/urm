package org.urm.meta.engine;

import org.urm.action.ActionEventsSource;
import org.urm.action.ScopeState;
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
import org.urm.engine.status.ServerStatusSource;
import org.urm.engine.status.ServerStatusData;
import org.urm.engine.status.ServerStatus;
import org.urm.engine.status.ServerStatusData.OBJECT_STATE;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoringTarget;

public class ServerMonitoringProduct implements EngineEventsListener {
	
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

	ServerMonitoring monitoring;
	String productName;
	ServerStatusSource source;
	Engine engine;
	
	ActionMonitorTop ca;
	EngineEventsApp eventsApp;

	public ServerMonitoringProduct( ServerMonitoring monitoring , String productName , ServerStatusSource source , EngineEventsApp eventsApp ) {
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
			ServerStatusSource serverSource = monitoring.getObjectSource( sg );
			if( serverSource == null )
				return;
			
			processSegmentEvent( source , serverSource , sg , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SGITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			SegmentStatus status = ( SegmentStatus )event.data;
			MetaEnvSegment sg = status.sg;
			ServerStatusSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource == null )
				return;
			
			processSegmentItemsEvent( source , sgSource , sg , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SERVER ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ScopeState state = ( ScopeState )event.data;
			MetaEnvServer server = state.target.envServer;
			ServerStatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			ServerStatus status = ( ServerStatus )state;
			processServerEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_SERVERITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ServerStatus status = ( ServerStatus )event.data;
			MetaEnvServer server = status.server;
			ServerStatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			processServerItemsEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_NODE ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ScopeState state = ( ScopeState )event.data;
			MetaEnvServerNode node = state.item.envServerNode;
			ServerStatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			NodeStatus status = ( NodeStatus )state;
			processNodeEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORING_NODEITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			NodeStatus status = ( NodeStatus )event.data;
			MetaEnvServerNode node = status.node;
			ServerStatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			processNodeItemsEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == EngineEvents.EVENT_MONITORGRAPHCHANGED ) {
			MetaMonitoringTarget target = ( MetaMonitoringTarget )event.data;
			ServerStatusSource sgSource = monitoring.findTargetSource( target );
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
		source.setState( OBJECT_STATE.STATE_NOMONITORING );
		ServerProduct product = ( ServerProduct )source.object;
		recalculateSystem( product.system );
	}

	private void processSegmentEvent( ActionEventsSource source , ServerStatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !task.isRunning() )
			return;

		sgSource.setPrimaryLog( status.getLog() );
	}
	
	private void processSegmentItemsEvent( ActionEventsSource source , ServerStatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !task.isRunning() )
			return;

		sgSource.setExtraLog( ServerMonitoring.EXTRA_SEGMENT_ITEMS , status.getLog() );
		if( sgSource.setExtraState( ServerMonitoring.EXTRA_SEGMENT_ITEMS , status.itemState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void processServerEvent( ActionEventsSource source , ServerStatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !task.isRunning() )
			return;

		serverSource.setPrimaryLog( status.getLog() );
		if( serverSource.setState( status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processServerItemsEvent( ActionEventsSource source , ServerStatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !task.isRunning() )
			return;

		serverSource.setExtraLog( ServerMonitoring.EXTRA_SERVER_ITEMS , status.getLog() );
		if( serverSource.setExtraState( ServerMonitoring.EXTRA_SERVER_ITEMS , status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processNodeEvent( ActionEventsSource source , ServerStatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !task.isRunning() )
			return;

		nodeSource.setPrimaryLog( status.getLog() );
		if( nodeSource.setState( status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void processNodeItemsEvent( ActionEventsSource source , ServerStatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !task.isRunning() )
			return;

		nodeSource.setExtraLog( ServerMonitoring.EXTRA_NODE_ITEMS , status.getLog() );
		if( nodeSource.setExtraState( ServerMonitoring.EXTRA_NODE_ITEMS , status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void recalculateServer( MetaEnvServer server ) {
		ServerStatusSource serverSource = monitoring.getObjectSource( server );
		if( serverSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( MetaEnvServerNode node : server.getNodes() ) {
			ServerStatusSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource != null )
				finalState = ServerStatusData.addState( finalState , nodeSource.state.state );
		}
		
		if( serverSource.setState( finalState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void recalculateSegment( MetaEnvSegment sg ) {
		ServerStatusSource sgSource = monitoring.getObjectSource( sg );
		if( sgSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( MetaEnvServer server : sg.getServers() ) {
			ServerStatusSource serverSource = monitoring.getObjectSource( server );
			if( serverSource != null )
				finalState = ServerStatusData.addState( finalState , serverSource.state.state );
		}
		
		if( sgSource.setState( finalState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void recalculateEnv( MetaEnv env ) {
		ServerStatusSource envSource = monitoring.getObjectSource( env );
		if( envSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( MetaEnvSegment sg : env.getSegments() ) {
			ServerStatusSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource != null )
				finalState = ServerStatusData.addState( finalState , sgSource.state.state );
		}
		
		if( envSource.setState( finalState ) ) {
			Meta meta = env.meta;
			recalculateProduct( meta );
		}
	}

	private void recalculateProduct( Meta meta ) {
		ServerProduct product = monitoring.findProduct( meta.name );
		if( product == null )
			return;
		
		ServerStatusSource productSource = monitoring.getObjectSource( product );
		if( productSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( String envName : meta.getEnvNames() ) {
			MetaEnv env = meta.findEnv( envName );
			ServerStatusSource envSource = monitoring.getObjectSource( env );
			if( envSource != null )
				finalState = ServerStatusData.addState( finalState , envSource.state.state );
		}
		
		if( productSource.setState( finalState ) ) {
			ServerSystem system = product.system;
			recalculateSystem( system );
		}
	}

	private void recalculateSystem( ServerSystem system ) {
		ServerStatusSource systemSource = monitoring.getObjectSource( system );
		if( systemSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( String productName : system.getProductNames() ) {
			ServerProduct product = system.findProduct( productName );
			ServerStatusSource productSource = monitoring.getObjectSource( product );
			if( productSource != null )
				finalState = ServerStatusData.addState( finalState , productSource.state.state );
		}
		
		if( systemSource.setState( finalState ) )
			recalculateApp( system.directory );
	}

	private void recalculateApp( ServerDirectory directory ) {
		ServerStatusSource appSource = monitoring.getAppSource();
		if( appSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NOMONITORING;
		for( String systemName : directory.getSystems() ) {
			ServerSystem system = directory.findSystem( systemName );
			ServerStatusSource systemSource = monitoring.getObjectSource( system );
			if( systemSource != null )
				finalState = ServerStatusData.addState( finalState , systemSource.state.state );
		}
		
		appSource.setState( finalState );
	}
	
}
