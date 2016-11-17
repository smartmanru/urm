package org.urm.meta.engine;

import org.urm.action.ActionEventsSource;
import org.urm.action.ScopeState;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.action.monitor.DatacenterStatus;
import org.urm.action.monitor.NodeStatus;
import org.urm.action.monitor.ServerStatus;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoringTarget;

public class ServerMonitoringProduct implements Runnable , ServerEventsListener {

	ServerMonitoring monitoring;
	String productName;
	ServerMonitoringSource source;
	ServerEngine engine;
	
	private Thread thread;
	private boolean started = false;
	private boolean stopped = false;
	private boolean stopping = false;
	
	ActionMonitorTop ca;
	ServerEventsApp eventsApp;

	public ServerMonitoringProduct( ServerMonitoring monitoring , String productName , ServerMonitoringSource source , ServerEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.productName = productName;
		this.source = source;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
	}
	
	@Override
	public void run() {
		started = true;
		try {
			ca = new ActionMonitorTop( engine.serverAction , null , productName , eventsApp );
			eventsApp.subscribe( ca.eventSource , this );
			ca.runSimple();
		}
		catch( Throwable e ) {
			engine.serverAction.handle( "thread pool house keeping error" , e );
		}
		
		synchronized( this ) {
			eventsApp.unsubscribe( this );
			thread = null;
			ca = null;
			stopped = true;
			notifyAll();
		}
	}

	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_DATACENTER ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			DatacenterStatus status = ( DatacenterStatus )event.data;
			MetaEnvDC dc = status.dc;
			ServerMonitoringSource serverSource = monitoring.getObjectSource( dc );
			if( serverSource == null )
				return;
			
			processDatacenterEvent( source , serverSource , dc , status );
			return;
		}
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_DCITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			DatacenterStatus status = ( DatacenterStatus )event.data;
			MetaEnvDC dc = status.dc;
			ServerMonitoringSource dcSource = monitoring.getObjectSource( dc );
			if( dcSource == null )
				return;
			
			processDatacenterItemsEvent( source , dcSource , dc , status );
			return;
		}
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_SERVER ) {
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
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_SERVERITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			ServerStatus status = ( ServerStatus )event.data;
			MetaEnvServer server = status.server;
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource == null )
				return;
			
			processServerItemsEvent( source , serverSource , server , status );
			return;
		}
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_NODE ) {
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
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_NODEITEMS ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			NodeStatus status = ( NodeStatus )event.data;
			MetaEnvServerNode node = status.node;
			ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource == null )
				return;
			
			processNodeItemsEvent( source , nodeSource , node , status );
			return;
		}
		
		if( event.eventType == ServerMonitoring.EVENT_MONITORGRAPHCHANGED ) {
			MetaMonitoringTarget target = ( MetaMonitoringTarget )event.data;
			ServerMonitoringSource dcSource = monitoring.findTargetSource( target );
			if( dcSource == null )
				return;
			
			dcSource.customEvent( ServerMonitoring.EVENT_MONITORGRAPHCHANGED , target );
			return;
		}
	}

	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}
	
	public synchronized void start() {
		if( started )
			return;
		
		stopping = false;
        thread = new Thread( null , this , "monitoring:" + productName );
        thread.start();
	}
	
	public synchronized void stop() {
		if( started == false || stopped )
			return;
		
		stopping = true;
		ca.stopRunning();
		try {
			wait();
		}
		catch( Throwable e ) {
			engine.serverAction.log( "ServerMonitoringProduct stop" , e );
		}
		
		// cleanup product data
		source.setState( MONITORING_STATE.STATE_NOMONITORING );
		ServerProduct product = ( ServerProduct )source.object;
		recalculateSystem( product.system );
	}

	private void processDatacenterEvent( ActionEventsSource source , ServerMonitoringSource dcSource , MetaEnvDC dc , DatacenterStatus status ) {
		if( stopping )
			return;

		dcSource.setPrimaryLog( status.getLog() );
	}
	
	private void processDatacenterItemsEvent( ActionEventsSource source , ServerMonitoringSource dcSource , MetaEnvDC dc , DatacenterStatus status ) {
		if( stopping )
			return;

		dcSource.setExtraLog( ServerMonitoring.EXTRA_DATACENTER_ITEMS , status.getLog() );
		if( dcSource.setExtraState( ServerMonitoring.EXTRA_DATACENTER_ITEMS , status.itemState ) ) {
			MetaEnv env = dc.env;
			recalculateEnv( env );
		}
	}
	
	private void processServerEvent( ActionEventsSource source , ServerMonitoringSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( stopping )
			return;

		serverSource.setPrimaryLog( status.getLog() );
		if( serverSource.setState( status.itemState ) ) {
			MetaEnvDC dc = server.dc;
			recalculateDatacenter( dc );
		}
	}
	
	private void processServerItemsEvent( ActionEventsSource source , ServerMonitoringSource serverSource , MetaEnvServer server , ServerStatus status ) {
		if( stopping )
			return;

		serverSource.setExtraLog( ServerMonitoring.EXTRA_SERVER_ITEMS , status.getLog() );
		if( serverSource.setExtraState( ServerMonitoring.EXTRA_SERVER_ITEMS , status.itemState ) ) {
			MetaEnvDC dc = server.dc;
			recalculateDatacenter( dc );
		}
	}
	
	private void processNodeEvent( ActionEventsSource source , ServerMonitoringSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( stopping )
			return;

		nodeSource.setPrimaryLog( status.getLog() );
		if( nodeSource.setState( status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void processNodeItemsEvent( ActionEventsSource source , ServerMonitoringSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		if( stopping )
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
			MetaEnvDC dc = server.dc;
			recalculateDatacenter( dc );
		}
	}
	
	private void recalculateDatacenter( MetaEnvDC dc ) {
		ServerMonitoringSource dcSource = monitoring.getObjectSource( dc );
		if( dcSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( MetaEnvServer server : dc.getServers() ) {
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource != null )
				finalState = ServerMonitoringState.addState( finalState , serverSource.state.state );
		}
		
		if( dcSource.setState( finalState ) ) {
			MetaEnv env = dc.env;
			recalculateEnv( env );
		}
	}
	
	private void recalculateEnv( MetaEnv env ) {
		ServerMonitoringSource envSource = monitoring.getObjectSource( env );
		if( envSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.STATE_NOMONITORING;
		for( MetaEnvDC dc : env.getDatacenters() ) {
			ServerMonitoringSource dcSource = monitoring.getObjectSource( dc );
			if( dcSource != null )
				finalState = ServerMonitoringState.addState( finalState , dcSource.state.state );
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
		for( String envName : meta.getEnvList() ) {
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
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
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
