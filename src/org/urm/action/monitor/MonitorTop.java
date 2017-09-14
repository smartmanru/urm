package org.urm.action.monitor;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionSet;
import org.urm.action.ActionSetItem;
import org.urm.common.Common;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.schedule.ScheduleTask;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.status.StatusSource;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.EngineLoader;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineMonitoringProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringItem;
import org.urm.meta.product.MetaMonitoringTarget;

public class MonitorTop {

	MonitorTargetInfo info;
	Meta meta;
	
	public MonitorTop( MonitorTargetInfo info ) {
		this.info = info;
		this.meta = info.meta;
	}

	public void runMajorChecks( ActionBase action , int iteration ) throws Exception {
		Date start = new Date();
		action.info( "product=" + meta.name + ": start major checks #" + iteration + ": " );
		executeOnceMajor( action );
		Date stop = new Date();
		action.info( "product=" + meta.name + ": major checks #" + iteration + " done in : " + ( stop.getTime() - start.getTime() ) + "ms" );
		createGraph( action );
	}
	
	public void runMinorChecks( ActionBase action , int iteration ) throws Exception {
		Date start = new Date();
		action.info( "product=" + meta.name + ": start minor checks #" + iteration + ": " );
		executeOnceMinor( action );
		Date stop = new Date();
		action.info( "product=" + meta.name + ": minor checks #" + iteration + " done in : " + ( stop.getTime() - start.getTime() ) + "ms" );
		createGraph( action );
	}		
	
	private void createGraph( ActionBase action ) throws Exception {
		action.trace( "refresh target graph env=" + info.target.ENV + ", sg=" + info.target.SG );
		info.addHistoryGraph( action );
		info.stop( action );

		MetaEnvSegment sg = info.target.getSegment( action );
		EngineStatus status = action.getServerStatus();
		StatusSource source = status.getObjectSource( sg );
		source.customEvent( EngineEvents.EVENT_MONITORGRAPHCHANGED , info );
	}

	private void executeOnceMajor( ActionBase action ) throws Exception {
		ActionMonitorCheckEnv actionCheck = new ActionMonitorCheckEnv( action , info.target.NAME , info.storage , info.target );
		MetaEnv env = getEnv( info.target );
		actionCheck.runSimpleEnv( env , SecurityAction.ACTION_DEPLOY , false );
		info.addCheckEnvData( action , actionCheck.timePassedMillis , actionCheck.isOK() );
		Common.sleep( 1000 );
	}
	
	private void executeOnceMinor( ActionBase action ) throws Exception {
		// system
		int sgIndex = action.logStartCapture();
		action.info( "Run fast segment checks, sg=" + info.target.SG + " ..." );
		MetaEnvSegment sg = addSystemTargetItems( set );
		SegmentStatus sgStatus = new SegmentStatus( null , sg );
		
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

	private ActionMonitorCheckEnv getCheckEnvAction( MonitorTargetInfo info , ActionSet set , MetaMonitoringTarget target ) throws Exception {
		ActionMonitorCheckEnv action = new ActionMonitorCheckEnv( this , target.NAME , info.storage , target );
		MetaEnv env = getEnv( target );
		set.runSimpleEnv( action , env , SecurityAction.ACTION_DEPLOY , false );
		return( action );
	}

	private void checkTargetItems( MetaMonitoring mon , MonitorTargetInfo info , MetaMonitoringTarget target ) throws Exception {
		ActionSet set = new ActionSet( this , "minor" );
		
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
			if( !super.isServerOffline( server ) )
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
		
		EngineStatus engineStatus = super.getServerStatus();
		for( ActionSetItem item : set.getActions() ) {
			ActionMonitorCheckItem action = ( ActionMonitorCheckItem )item.action;
			if( action.server == null ) {
				if( action.isFailed() )
					totalStatus = false;
			}
			else {
				engineStatus.setServerItemsStatus( this , action.server , action.serverStatus );
				for( NodeStatus nodeStatus : action.getNodes() )
					engineStatus.setServerNodeItemsStatus( this , nodeStatus.node , nodeStatus );
			}
		}
		
		sgStatus.setTotalStatus( totalStatus );
		engineStatus.setSegmentItemsStatus( this , sgStatus.sg , sgStatus );
	}
	
}
