package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionSet;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.action.ActionInit;
import org.urm.engine.action.CommandContext;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.product.Meta;
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
			MonitorInfo info = new MonitorInfo( this , storage );
			
			long current = System.currentTimeMillis();
			try {
				if( runMajor ) {
					runMajor = false;
					lastStartMajor = current;
					majorCount++;
					minorCount = 0;
					info( "product=" + mon.meta.name + ": start major checks #" + majorCount + ": " );
					executeOnceMajor( mon , info );
					current = System.currentTimeMillis();
					lastStopMajor = current;
					info( "product=" + mon.meta.name + ": major checks #" + majorCount + " done in : " + ( lastStopMajor - lastStartMajor ) + "ms" );
					continue;
				}
				else {
					lastStartMinor = current;
					minorCount++;
					info( "product=" + mon.meta.name + ": start minor checks #" + minorCount + ": " );
					executeOnceMinor( mon , info );
					current = System.currentTimeMillis();
					lastStopMinor = current; 
					info( "product=" + mon.meta.name + ": minor checks #" + minorCount + " done in : " + ( lastStopMinor - lastStartMinor ) + "ms" );
				}
			}
			catch( Throwable e ) {
				handle( e );
			}

			if( runMajor && lastStartMinor == 0 ) {
				runMajor = false;
				continue;
			}
			
			// calculate sleep and next action
			long nextMinor = 0;
			nextMinor = lastStartMinor + mon.MINORINTERVAL * 1000;
			if( nextMinor < ( current + mon.MINSILENT * 1000 ) )
				nextMinor = current + mon.MINSILENT * 1000;

			long nextMajor = 0;
			nextMajor = lastStartMajor + mon.MAJORINTERVAL * 1000;
			if( nextMajor < ( current + mon.MINSILENT * 1000 ) )
				nextMajor = current + mon.MINSILENT * 1000;
			
			if( nextMajor < nextMinor ) {
				runMajor = true;
				if( !runSleep( nextMajor - current ) )
					stopRunning();
			}
			else {
				runMajor = false;
				if( !runSleep( nextMinor - current ) )
					stopRunning();
			}
			
			try {
				info.stop();
			}
			catch( Throwable e ) {
				handle( e );
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerMonitoring.EVENT_FINALSTATE )
			super.eventSource.forwardScopeItem( ServerMonitoring.EVENT_FINALSTATE , ( ScopeState )event.data );
	}
	
	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
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
			ActionInit init = getStreamAction( target );
			ActionMonitorCheckEnv action = getCheckEnvAction( info , set , init , target );
			checkenvActions.add( action );
		}
		
		set.waitDone();
		
		for( ActionMonitorCheckEnv action : checkenvActions )
			info.addCheckEnvData( action.target , action.timePassedMillis , action.isOK() );
	}
	
	private void executeOnceMinor( MetaMonitoring mon , MonitorInfo info ) throws Exception {
		// run checkenv for all targets
		for( MetaMonitoringTarget target : mon.getTargets( this ).values() ) {
			ActionInit init = getStreamAction( target ); 
			checkTargetItems( mon , info , init , target );
		}
	}

	private ActionMonitorCheckEnv getCheckEnvAction( MonitorInfo info , ActionSet set , ActionInit init , MetaMonitoringTarget target ) throws Exception {
		ActionMonitorCheckEnv action = new ActionMonitorCheckEnv( init , target.NAME , info.storage , target , eventsApp );
		eventsApp.subscribe( action.eventSource , this );
		set.runSimple( action );
		return( action );
	}

	private ActionInit getStreamAction( MetaMonitoringTarget target ) throws Exception {
		CommandContext initContext = context.getStreamContext( target.NAME );
		ActionInit action = engine.createAction( initContext , this );
		return( action );
	}

	private void checkTargetItems( MetaMonitoring mon , MonitorInfo info , ActionInit init , MetaMonitoringTarget target ) throws Exception {
		ActionSet set = new ActionSet( this , "minor" );
		
		for( MetaMonitoringItem item : target.getUrlsList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( init , target.NAME , mon , item );
			set.runSimple( action );
		}
		
		for( MetaMonitoringItem item : target.getWSList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( init , target.NAME , mon , item );
			set.runSimple( action );
		}
		
		boolean ok = set.waitDone();  
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + target.NAME , target.NAME );
		
		info.addCheckMinorsData( target , ok );
	}
	
}
