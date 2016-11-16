package org.urm.action.monitor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.urm.action.ActionBase;
import org.urm.action.ActionSet;
import org.urm.action.ActionSetItem;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringItem;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorTop extends ActionBase implements ServerEventsListener {

	boolean continueRunning;
	String productName;
	ServerEventsApp eventsApp;
	
	public ActionMonitorTop( ActionBase action , String stream , String productName , ServerEventsApp eventsApp ) {
		super( action , stream );
		this.productName = productName;
		this.eventsApp = eventsApp;
	}

	@Override 
	protected SCOPESTATE executeSimple() throws Exception {
		continueRunning = true;

		long lastStartMajor = 0;
		long lastStartMinor = 0;
		long lastStopMajor = 0;
		long lastStopMinor = 0;
		
		boolean runMajor = true;
		int majorCount = 0;
		int minorCount = 0;
		
		MonitorInfo info = null;
		while( continueRunning ) {
			ServerLoader loader = super.engine.getLoader( super.actionInit );
			ServerProductMeta productStorage = loader.findProductStorage( productName );
			if( productStorage == null ) {
				info( "product=" + productName + ": not found, stop monitoring ..." );
				break;
			}

			Meta meta = productStorage.meta;
			MetaMonitoring mon = meta.getMonitoring( this );
			MonitoringStorage storage = artefactory.getMonitoringStorage( this , mon );
			
			boolean enabledMajor = ( mon.MAJORINTERVAL > 0 )? true : false;
			boolean enabledMinor = ( mon.MINORINTERVAL > 0 )? true : false;
			if( enabledMajor == false && enabledMinor == false ) {
				runSleep( 60000 );
				continue;
			}
			
			if( enabledMajor == false )
				runMajor = false;
			else
			if( enabledMinor == false )
				runMajor = true;
			
			long current = System.currentTimeMillis();
			try {
				if( runMajor ) {
					runMajor = false;
					lastStartMajor = current;
					majorCount++;
					minorCount = 0;
					info( "product=" + mon.meta.name + ": start major checks #" + majorCount + ": " );
					info = new MonitorInfo( this , storage );
					executeOnceMajor( mon , info );
					current = System.currentTimeMillis();
					lastStopMajor = current;
					info( "product=" + mon.meta.name + ": major checks #" + majorCount + " done in : " + ( lastStopMajor - lastStartMajor ) + "ms" );
				}
				else {
					lastStartMinor = current;
					minorCount++;
					info( "product=" + mon.meta.name + ": start minor checks #" + minorCount + ": " );
					if( info == null )
						info = new MonitorInfo( this , storage );
					executeOnceMinor( mon , info );
					current = System.currentTimeMillis();
					lastStopMinor = current; 
					info( "product=" + mon.meta.name + ": minor checks #" + minorCount + " done in : " + ( lastStopMinor - lastStartMinor ) + "ms" );
				}
			}
			catch( Throwable e ) {
				handle( e );
			}

			if( runMajor && enabledMinor && lastStartMinor == 0 ) {
				runMajor = false;
				continue;
			}
			
			// create graphs
			for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
				info.addHistoryGraph( target );
				super.eventSource.customEvent( ServerMonitoring.EVENT_MONITORGRAPHCHANGED , target );
			}
			
			// calculate sleep and next action
			long nextMinor = 0;
			nextMinor = lastStartMinor + mon.MINORINTERVAL * 1000;
			long nextMajor = 0;
			nextMajor = lastStartMajor + mon.MAJORINTERVAL * 1000;

			// finish with data
			try {
				info.stop();
			}
			catch( Throwable e ) {
				handle( e );
			}

			// wait for next
			if( enabledMajor && ( nextMajor < nextMinor || enabledMinor == false ) ) {
				runMajor = true;
				if( nextMajor < ( current + mon.MINSILENT * 1000 ) )
					nextMajor = current + mon.MINSILENT * 1000;
				if( !runSleep( nextMajor - current ) )
					stopRunning();
			}
			else {
				runMajor = false;
				if( nextMinor < ( current + mon.MINSILENT * 1000 ) )
					nextMinor = current + mon.MINSILENT * 1000;
				if( !runSleep( nextMinor - current ) )
					stopRunning();
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerMonitoring.EVENT_MONITORING_SERVER ||
			event.eventType == ServerMonitoring.EVENT_MONITORING_NODE )
			super.eventSource.forwardScopeItem( event.eventType , ( ScopeState )event.data );
	}
	
	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}

	public void updateCheckItemState( ActionMonitorCheckItem checkAction , boolean res ) {
	}
	
	public void stopRunning() {
		continueRunning = false;
		synchronized( this ) {
			notifyAll();
		}
	}
	
	private synchronized boolean runSleep( long millis ) {
		long startTime = System.currentTimeMillis();
		long endTime = startTime + millis;
		
		long waitTime = millis;
		while( continueRunning ) {
			try {
				wait( waitTime );
			}
			catch( Throwable e ) {
				handle( e );
				return( false );
			}
			
			long currentTime = System.currentTimeMillis();
			if( currentTime >= endTime )
				return( true );
			
			waitTime = endTime - currentTime;
		}
		
		return( false );
	}

	private void executeOnceMajor( MetaMonitoring mon , MonitorInfo info ) throws Exception {
		List<ActionMonitorCheckEnv> checkenvActions = new LinkedList<ActionMonitorCheckEnv>();
		
		// run checkenv for all targets
		ActionSet set = new ActionSet( this , "major" );
		for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
			ActionMonitorCheckEnv action = getCheckEnvAction( info , set , target );
			checkenvActions.add( action );
		}
		
		set.waitDone();
		
		for( ActionMonitorCheckEnv action : checkenvActions )
			info.addCheckEnvData( action.target , action.timePassedMillis , action.isOK() );
	}
	
	private void executeOnceMinor( MetaMonitoring mon , MonitorInfo info ) throws Exception {
		// run checkenv for all targets
		for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
			checkTargetItems( mon , info , target );
		}
	}

	private ActionMonitorCheckEnv getCheckEnvAction( MonitorInfo info , ActionSet set , MetaMonitoringTarget target ) throws Exception {
		ActionMonitorCheckEnv action = new ActionMonitorCheckEnv( this , target.NAME , info.storage , target , eventsApp );
		eventsApp.subscribe( action.eventSource , this );
		set.runSimple( action );
		return( action );
	}

	private void checkTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target ) throws Exception {
		ActionSet set = new ActionSet( this , "minor" );
		
		// system
		MetaEnvDC dc = addSystemTargetItems( mon , info , target , set );
		
		// direct
		for( MetaMonitoringItem item : target.getUrlsList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , item , null );
			set.runSimple( action );
		}
		
		for( MetaMonitoringItem item : target.getWSList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , item , null );
			set.runSimple( action );
		}
		
		boolean ok = set.waitDone();
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + target.NAME , target.NAME );
		
		checkSystemTargetItems( mon , info , target , set , dc );
		
		info.addCheckMinorsData( target , ok );
	}

	private MetaEnvDC addSystemTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set ) throws Exception {
		Meta meta = target.meta;
		MetaEnv env = meta.getEnv( this , target.ENV );
		MetaEnvDC dc = env.getDC( this , target.DC );
		for( MetaEnvServer server : dc.getServers() ) {
			if( !server.isOffline() )
				addSystemServerItems( mon , info , target , set , server );
		}
		
		return( dc );
	}

	private void addSystemServerItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set , MetaEnvServer server ) throws Exception {
		ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , null , server );
		set.runSimple( action );
	}

	private void checkSystemTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set , MetaEnvDC dc ) throws Exception {
		boolean totalStatus = true;
		Map<MetaEnvServer,ServerStatus> serverData = new HashMap<MetaEnvServer,ServerStatus>();
		Map<MetaEnvServerNode,NodeStatus> nodeData = new HashMap<MetaEnvServerNode,NodeStatus>();
		
		for( ActionSetItem item : set.getActions() ) {
			ActionMonitorCheckItem action = ( ActionMonitorCheckItem )item.action;
			if( action.server == null ) {
				if( action.isFailed() )
					totalStatus = false;
			}
			else {
				ServerStatus serverStatus = serverData.get( action.server );
				if( serverStatus == null ) {
					serverStatus = new ServerStatus( this , action.server );
					serverData.put( action.server , serverStatus );
				}
				
				for( MetaMonitoringItem monItem : action.getServerItems() ) {
					boolean ok = monItem.monitorStatus;
					serverStatus.addWholeUrlStatus( monItem.URL , monItem.NAME , ok );
				}
				
				for( MetaEnvServerNode node : action.server.getNodes() ) {
					NodeStatus nodeStatus = nodeData.get( node );
					if( nodeStatus == null ) {
						nodeStatus = new NodeStatus( this , node );
						nodeData.put( node , nodeStatus );
					}
					
					for( MetaMonitoringItem monItem : action.getNodeItems( node ) ) {
						boolean ok = monItem.monitorStatus;
						nodeStatus.addWholeUrlStatus( monItem.URL , monItem.NAME , ok );
					}
					
				}
			}
		}
		
		for( Entry<MetaEnvServerNode,NodeStatus> entry : nodeData.entrySet() )
			super.eventSource.customEvent( ServerMonitoring.EVENT_MONITORING_NODEITEMS , entry.getValue() );
		for( Entry<MetaEnvServer,ServerStatus> entry : serverData.entrySet() )
			super.eventSource.customEvent( ServerMonitoring.EVENT_MONITORING_SERVERITEMS , entry.getValue() );

		DatacenterStatus dcStatus = new DatacenterStatus( this , dc );
		dcStatus.setActionStatus( totalStatus );
		super.eventSource.customEvent( ServerMonitoring.EVENT_MONITORING_DCITEMS , dcStatus );
	}
	
}
