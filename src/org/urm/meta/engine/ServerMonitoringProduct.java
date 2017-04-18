package org.urm.meta.engine;

import org.urm.action.ActionEventsSource;
import org.urm.action.ScopeState;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.action.monitor.SegmentStatus;
import org.urm.action.monitor.NodeStatus;
import org.urm.action.monitor.ServerStatus;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.ServerExecutorThread;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoringTarget;

public class ServerMonitoringProduct implements Runnable , ServerEventsListener {

	ServerMonitoring monitoring;
	String productName;
	ServerMonitoringSource source;
	ServerEngine engine;
	
	private ServerExecutorThread thread;
	
	ActionMonitorTop ca;
	ServerEventsApp eventsApp;

	public ServerMonitoringProduct( ServerMonitoring monitoring , String productName , ServerMonitoringSource source , ServerEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.productName = productName;
		this.source = source;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
		
		thread = new ServerExecutorThread( engine , this , "monitoring::" + productName , false ); 
	}
	
	@Override
	public void run() {
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
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerEvents.EVENT_MONITORING_SEGMENT ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			SegmentStatus status = ( SegmentStatus )event.data;
			MetaEnvSegment sg = status.sg;
			ServerMonitoringSource serverSource = monitoring.getObjectSource( sg );
			if( serverSource == null )
				return;
			
			processSegmentEvent( source , serverSource , sg , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORING_SGITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			SegmentStatus status = ( SegmentStatus )event.data;
			MetaEnvSegment sg = status.sg;
			ServerMonitoringSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource == null )
				return;
			
			processSegmentItemsEvent( source , sgSource , sg , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORING_SERVER ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ScopeState state = ( ScopeState )event.data;
			MetaEnvServer server = state.target.envServer;
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			ServerStatus status = ( ServerStatus )state;
			processServerEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORING_SERVERITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ServerStatus status = ( ServerStatus )event.data;
			MetaEnvServer server = status.server;
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			processServerItemsEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORING_NODE ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ScopeState state = ( ScopeState )event.data;
			MetaEnvServerNode node = state.item.envServerNode;
			ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			NodeStatus status = ( NodeStatus )state;
			processNodeEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORING_NODEITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			NodeStatus status = ( NodeStatus )event.data;
			MetaEnvServerNode node = status.node;
			ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			processNodeItemsEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == ServerEvents.EVENT_MONITORGRAPHCHANGED ) {
			MetaMonitoringTarget target = ( MetaMonitoringTarget )event.data;
			ServerMonitoringSource sgSource = monitoring.findTargetSource( target );
			if( sgSource == null )
				return;
			
			sgSource.customEvent( ServerEvents.EVENT_MONITORGRAPHCHANGED , target );
			return;
		}
	}

	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}
	
	public synchronized void start() {
		thread.start();
	}
	
	public synchronized void stop() {
		ca.stopRunning();
		thread.stop();
		
		// cleanup product data
		source.setState( MONITORING_STATE.STATE_NOMONITORING );
		ServerProduct product = ( ServerProduct )source.object;
		recalculateSystem( product.system );
	}

	private void processSegmentEvent( ActionEventsSource source , ServerMonitoringSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !thread.isRunning() )
			return;

		sgSource.setPrimaryLog( status.getLog() );
	}
	
	private void processSegmentItemsEvent( ActionEventsSource source , ServerMonitoringSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		if( !thread.isRunning() )
			return;

		sgSource.setExtraLog( ServerMonitoring.EXTRA_SEGMENT_ITEMS , status.getLog() );
		if( sgSource.setExtraState( ServerMonitoring.EXTRA_SEGMENT_ITEMS , status.itemState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void processServerEvent( ActionEventsSource source , ServerMonitoringSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !thread.isRunning() )
			return;

		serverSource.setPrimaryLog( status.getLog() );
		if( serverSource.setState( status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processServerItemsEvent( ActionEventsSource source , ServerMonitoringSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( !thread.isRunning() )
			return;

		serverSource.setExtraLog( ServerMonitoring.EXTRA_SERVER_ITEMS , status.getLog() );
		if( serverSource.setExtraState( ServerMonitoring.EXTRA_SERVER_ITEMS , status.itemState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void processNodeEvent( ActionEventsSource source , ServerMonitoringSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !thread.isRunning() )
			return;

		nodeSource.setPrimaryLog( status.getLog() );
		if( nodeSource.setState( status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void processNodeItemsEvent( ActionEventsSource source , ServerMonitoringSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( !thread.isRunning() )
			return;

		nodeSource.setExtraLog( ServerMonitoring.EXTRA_NODE_ITEMS , status.getLog() );
		if( nodeSource.setExtraState( ServerMonitoring.EXTRA_NODE_ITEMS , status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void recalculateServer( MetaEnvServer server ) {
		ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
		if( serverSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( MetaEnvServerNode node : server.getNodes() ) {
			ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource != null )
				finalState = ServerMonitoringState.addState( finalState , nodeSource.state.state );
		}
		
		if( serverSource.setState( finalState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( sg );
		}
	}
	
	private void recalculateSegment( MetaEnvSegment sg ) {
		ServerMonitoringSource sgSource = monitoring.getObjectSource( sg );
		if( sgSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( MetaEnvServer server : sg.getServers() ) {
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource != null )
				finalState = ServerMonitoringState.addState( finalState , serverSource.state.state );
		}
		
		if( sgSource.setState( finalState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( env );
		}
	}
	
	private void recalculateEnv( MetaEnv env ) {
		ServerMonitoringSource envSource = monitoring.getObjectSource( env );
		if( envSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( MetaEnvSegment sg : env.getSegments() ) {
			ServerMonitoringSource sgSource = monitoring.getObjectSource( sg );
			if( sgSource != null )
				finalState = ServerMonitoringState.addState( finalState , sgSource.state.state );
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
		
		ServerMonitoringSource productSource = monitoring.getObjectSource( product );
		if( productSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( String envName : meta.getEnvNames() ) {
			MetaEnv env = meta.findEnv( envName );
			ServerMonitoringSource envSource = monitoring.getObjectSource( env );
			if( envSource != null )
				finalState = ServerMonitoringState.addState( finalState , envSource.state.state );
		}
		
		if( productSource.setState( finalState ) ) {
			ServerSystem system = product.system;
			recalculateSystem( system );
		}
	}

	private void recalculateSystem( ServerSystem system ) {
		ServerMonitoringSource systemSource = monitoring.getObjectSource( system );
		if( systemSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( String productName : system.getProductNames() ) {
			ServerProduct product = system.findProduct( productName );
			ServerMonitoringSource productSource = monitoring.getObjectSource( product );
			if( productSource != null )
				finalState = ServerMonitoringState.addState( finalState , productSource.state.state );
		}
		
		if( systemSource.setState( finalState ) )
			recalculateApp( system.directory );
	}

	private void recalculateApp( ServerDirectory directory ) {
		ServerMonitoringSource appSource = monitoring.getAppSource();
		if( appSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( String systemName : directory.getSystems() ) {
			ServerSystem system = directory.findSystem( systemName );
			ServerMonitoringSource systemSource = monitoring.getObjectSource( system );
			if( systemSource != null )
				finalState = ServerMonitoringState.addState( finalState , systemSource.state.state );
		}
		
		appSource.setState( finalState );
	}
	
}
