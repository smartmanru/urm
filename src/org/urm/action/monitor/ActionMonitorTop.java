package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionSet;
import org.urm.action.ActionSetItem;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.EngineCacheObject;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.events.EngineSourceEvent;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringItem;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorTop extends ActionBase implements EngineEventsListener {

	boolean continueRunning;
	String productName;
	EngineEventsApp eventsApp;
	EngineCacheObject co;
	
	public ActionMonitorTop( ActionBase action , String stream , String productName , EngineEventsApp eventsApp ) {
		super( action , stream , "Monitoring, check product=" + productName );
		this.productName = productName;
		this.eventsApp = eventsApp;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		continueRunning = true;

		long lastStartMajor = 0;
		long lastStartMinor = 0;
		long lastStopMajor = 0;
		long lastStopMinor = 0;
		
		boolean runMajor = true;
		int majorCount = 0;
		int minorCount = 0;
		
		co = super.getProductCacheObject( productName );
		eventsApp.subscribe( co , this );
		
		MonitorInfo info = null;
		while( continueRunning ) {
			EngineLoader loader = super.engine.getLoader( super.actionInit );
			ProductMeta productStorage = loader.findProductStorage( productName );
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
					lastStartMajor = current;
					lastStartMinor = 0;
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

			if( runMajor && enabledMinor ) {
				runMajor = false;
				continue;
			}
			
			// create graphs
			for( MetaMonitoringTarget target : mon.getTargets( this ) ) {
				trace( "refresh target graph env=" + target.ENV + ", sg=" + target.SG );
				info.addHistoryGraph( target );
				super.eventSource.customEvent( EngineEvents.EVENT_MONITORGRAPHCHANGED , target );
			}
			
			// calculate sleep and next action
			long nextMinor = 0;
			nextMinor = lastStartMinor + mon.MINORINTERVAL * 1000;
			long nextMajor = 0;
			nextMajor = lastStartMajor + mon.MAJORINTERVAL * 1000;

			// finish with data
			try {
				info.stop( this );
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
	public void triggerEvent( EngineSourceEvent event ) {
		if( event.eventType == EngineEvents.EVENT_CACHE_SEGMENT )
			super.eventSource.forwardScopeItem( EngineEvents.EVENT_MONITORING_SEGMENT , ( ScopeState )event.data );
		else
		if( event.eventType == EngineEvents.EVENT_CACHE_SERVER )
			super.eventSource.forwardScopeItem( EngineEvents.EVENT_MONITORING_SERVER , ( ScopeState )event.data );
		else
		if( event.eventType == EngineEvents.EVENT_CACHE_NODE )
			super.eventSource.forwardScopeItem( EngineEvents.EVENT_MONITORING_NODE , ( ScopeState )event.data );
	}
	
	@Override
	public void triggerSubscriptionRemoved( EngineEventsSubscription sub ) {
	}

	public void updateCheckItemState( ActionMonitorCheckItem checkAction , boolean res ) {
	}
	
	public void stopRunning() {
		synchronized( this ) {
			continueRunning = false;
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
		// run checkenv for all targets
		ActionSet set = new ActionSet( this , "major" );
		MetaMonitoringTarget[] targets = mon.getTargets( this );
		
		if( targets.length == 0 ) {
			super.debug( "no monitoring targets, skipped." );
		}
		else {
			List<ActionMonitorCheckEnv> checkenvActions = new LinkedList<ActionMonitorCheckEnv>();
			for( MetaMonitoringTarget target : targets ) {
				ActionMonitorCheckEnv action = getCheckEnvAction( info , set , target );
				checkenvActions.add( action );
			}
			
			set.waitDone();
			
			for( ActionMonitorCheckEnv action : checkenvActions )
				info.addCheckEnvData( action.target , action.timePassedMillis , action.isOK() );
		}
		
		Common.sleep( 1000 );
	}
	
	private void executeOnceMinor( MetaMonitoring mon , MonitorInfo info ) throws Exception {
		// run checkenv for all targets
		for( MetaMonitoringTarget target : mon.getTargets( this ) ) {
			checkTargetItems( mon , info , target );
		}
	}

	private ActionMonitorCheckEnv getCheckEnvAction( MonitorInfo info , ActionSet set , MetaMonitoringTarget target ) throws Exception {
		ActionMonitorCheckEnv action = new ActionMonitorCheckEnv( this , target.NAME , info.storage , target );
		MetaEnv env = getEnv( target );
		set.runSimpleEnv( action , env , SecurityAction.ACTION_DEPLOY , false );
		return( action );
	}

	private void checkTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target ) throws Exception {
		ActionSet set = new ActionSet( this , "minor" );
		
		// system
		int sgIndex = super.logStartCapture();
		info( "Run fast segment checks, sg=" + target.SG + " ..." );
		MetaEnvSegment sg = addSystemTargetItems( mon , info , target , set );
		SegmentStatus sgStatus = new SegmentStatus( this , sg );
		
		// direct
		for( MetaMonitoringItem item : target.getUrlsList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , item , null );
			set.runSimpleEnv( action , sg.env , SecurityAction.ACTION_DEPLOY , false );
		}
		
		for( MetaMonitoringItem item : target.getWSList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , item , null );
			set.runSimpleEnv( action , sg.env , SecurityAction.ACTION_DEPLOY , false );
		}
		
		boolean ok = set.waitDone();
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + target.NAME , target.NAME );
		
		info( "Fast server checks finished" );
		String[] log = super.logFinishCapture( sgIndex );
		sgStatus.setLog( log );
		checkSystemTargetItems( mon , info , target , set , sgStatus );
		
		info.addCheckMinorsData( target , ok );
	}

	private MetaEnv getEnv( MetaMonitoringTarget target ) throws Exception {
		Meta meta = target.meta;
		MetaEnv env = meta.getEnv( this , target.ENV );
		return( env );
	}
	
	private MetaEnvSegment addSystemTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set ) throws Exception {
		MetaEnv env = getEnv( target );
		MetaEnvSegment sg = env.getSG( this , target.SG );
		for( MetaEnvServer server : sg.getServers() ) {
			if( !server.isOffline() )
				addSystemServerItems( mon , info , target , set , server );
		}
		
		return( sg );
	}

	private void addSystemServerItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set , MetaEnvServer server ) throws Exception {
		ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , mon , target , null , server );
		set.runSimpleEnv( action , server.sg.env , SecurityAction.ACTION_DEPLOY , false );
	}

	private void checkSystemTargetItems( MetaMonitoring mon , MonitorInfo info , MetaMonitoringTarget target , ActionSet set , SegmentStatus sgStatus ) throws Exception {
		boolean totalStatus = true;
		
		for( ActionSetItem item : set.getActions() ) {
			ActionMonitorCheckItem action = ( ActionMonitorCheckItem )item.action;
			if( action.server == null ) {
				if( action.isFailed() )
					totalStatus = false;
			}
			else {
				super.eventSource.customEvent( EngineEvents.EVENT_MONITORING_SERVERITEMS , action.serverStatus );
				for( NodeStatus nodeStatus : action.getNodes() )
					super.eventSource.customEvent( EngineEvents.EVENT_MONITORING_NODEITEMS , nodeStatus );
			}
		}
		
		sgStatus.setActionStatus( totalStatus );
		super.eventSource.customEvent( EngineEvents.EVENT_MONITORING_SGITEMS , sgStatus );
	}
	
}
