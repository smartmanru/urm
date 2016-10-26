package org.urm.meta.engine;

import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPETYPE;
import org.urm.action.deploy.NodeStatus;
import org.urm.action.monitor.ActionMonitorTop;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerSourceEvent;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoring;

public class ServerMonitoringProduct implements Runnable , ServerEventsListener {

	ServerMonitoring monitoring;
	MetaMonitoring meta;
	ServerMonitoringSource source;
	ServerEngine engine;
	
	private Thread thread;
	private boolean started = false;
	private boolean stopped = false;
	
	ActionMonitorTop ca;
	ServerEventsApp eventsApp;
	
	public ServerMonitoringProduct( ServerMonitoring monitoring , MetaMonitoring meta , ServerMonitoringSource source , ServerEventsApp eventsApp ) {
		this.monitoring = monitoring;
		this.meta = meta;
		this.source = source;
		this.engine = monitoring.engine;
		this.eventsApp = eventsApp;
	}
	
	@Override
	public void run() {
		started = true;
		try {
			ca = new ActionMonitorTop( engine.serverAction , null , meta , eventsApp );
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
		if( event.eventType == ServerMonitoring.EVENT_FINALSTATE ) {
			ScopeState state = ( ScopeState )event.data;
			if( state.type == SCOPETYPE.TypeItem ) {
				MetaEnvServerNode node = state.item.envServerNode;
				NodeStatus status = ( NodeStatus )state;
				if( node != null )
					processNodeEvent( node , status );
			}
		}
	}
	
	public synchronized void start() {
		if( started )
			return;
		
        thread = new Thread( null , this , "monitoring:" + meta.meta.name );
        thread.start();
	}
	
	public synchronized void stop() {
		if( started == false || stopped )
			return;
		
		ca.stopRunning();
		try {
			wait();
		}
		catch( Throwable e ) {
			engine.serverAction.log( "ServerMonitoringProduct stop" , e );
		}
	}

	private void processNodeEvent( MetaEnvServerNode node , NodeStatus status ) {
		ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
		if( nodeSource != null && nodeSource.setState( status.itemState ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( server );
		}
	}
	
	private void recalculateServer( MetaEnvServer server ) {
		ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
		if( serverSource == null )
			return;

		MONITORING_STATE finalState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		for( MetaEnvServerNode node : server.getNodes() ) {
			ServerMonitoringSource nodeSource = monitoring.getObjectSource( node );
			if( nodeSource != null )
				finalState = ServerMonitoringState.addState( finalState , nodeSource.data.state );
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

		MONITORING_STATE finalState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		for( MetaEnvServer server : dc.getServers() ) {
			ServerMonitoringSource serverSource = monitoring.getObjectSource( server );
			if( serverSource != null )
				finalState = ServerMonitoringState.addState( finalState , serverSource.data.state );
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

		MONITORING_STATE finalState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		for( MetaEnvDC dc : env.getDatacenters() ) {
			ServerMonitoringSource dcSource = monitoring.getObjectSource( dc );
			if( dcSource != null )
				finalState = ServerMonitoringState.addState( finalState , dcSource.data.state );
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

		MONITORING_STATE finalState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		for( String envName : meta.getEnvList() ) {
			MetaEnv env = meta.findEnv( envName );
			ServerMonitoringSource envSource = monitoring.getObjectSource( env );
			if( envSource != null )
				finalState = ServerMonitoringState.addState( finalState , envSource.data.state );
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

		MONITORING_STATE finalState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			ServerMonitoringSource productSource = monitoring.getObjectSource( product );
			if( productSource != null )
				finalState = ServerMonitoringState.addState( finalState , productSource.data.state );
		}
		
		systemSource.setState( finalState );
	}
	
}
