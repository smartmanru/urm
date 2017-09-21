package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.StatusSource;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoringItem;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorTarget extends ActionBase {
	
	public MonitorTargetInfo info;
	public MetaMonitoringTarget target;
	
	public ActionMonitorTarget( ActionBase action , String stream , MonitorTargetInfo info ) {
		super( action , stream , "monitoring, check target" );
		this.info = info;
		this.target = info.target;
	}

	public long executeOnceMajor() throws Exception {
		ActionMonitorCheckEnv actionCheck = new ActionMonitorCheckEnv( this , info.target.NAME , info.storage , info.target );
		MetaEnv env = getEnv( target );
		actionCheck.runSimpleEnv( env , SecurityAction.ACTION_DEPLOY , false );
		info.addCheckEnvData( this , actionCheck.timePassedMillis , actionCheck.isOK() );
		Common.sleep( 1000 );
		return( actionCheck.timePassedMillis );
	}
	
	private MetaEnv getEnv( MetaMonitoringTarget target ) throws Exception {
		Meta meta = target.meta;
		MetaEnv env = meta.getEnv( this , target.ENV );
		return( env );
	}
	
	public void createGraph() throws Exception {
		if( !info.isAvailable() )
			return;
		
		super.trace( "refresh target graph env=" + info.target.ENV + ", sg=" + info.target.SG );
		info.addHistoryGraph( this );
		info.stop( this );

		MetaEnvSegment sg = info.target.getSegment( this );
		if( sg != null ) { 
			EngineStatus status = super.getServerStatus();
			StatusSource source = status.getObjectSource( sg );
			if( source != null )
				source.customEvent( EngineEvents.EVENT_MONITORGRAPHCHANGED , info );
		}
	}

	public long executeOnceMinor() throws Exception {
		// system
		int sgIndex = super.logStartCapture();
		super.info( "Run fast segment checks, sg=" + info.target.SG + " ..." );
		
		long timeStart = System.currentTimeMillis();
		
		boolean ok = true;
		
		MetaEnv env = getEnv( target );
		MetaEnvSegment sg = env.getSG( this , target.SG );
		EngineStatus engineStatus = super.getServerStatus();
		
		for( MetaEnvServer server : sg.getServers() ) {
			if( !super.isServerOffline( server ) ) {
				ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , target , null , server );
				action.runSimpleEnv( server.sg.env , SecurityAction.ACTION_DEPLOY , false );
				if( action.isFailed() )
					ok = false;
				
				engineStatus.setServerItemsStatus( this , action.server , action.serverStatus );
				for( NodeStatus nodeStatus : action.getNodes() )
					engineStatus.setServerNodeItemsStatus( this , nodeStatus.node , nodeStatus );
			}
		}
		
		// direct
		for( MetaMonitoringItem item : target.getUrlsList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , target , item , null );
			action.runSimpleEnv( sg.env , SecurityAction.ACTION_DEPLOY , false );
			if( action.isFailed() )
				ok = false;
		}
		
		for( MetaMonitoringItem item : target.getWSList( this ) ) {
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , target.NAME , target , item , null );
			action.runSimpleEnv( sg.env , SecurityAction.ACTION_DEPLOY , false );
			if( action.isFailed() )
				ok = false;
		}
		
		long timeFinish = System.currentTimeMillis();
		
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + target.NAME , target.NAME );
		
		info( "Fast server checks finished" );
		String[] log = super.logFinishCapture( sgIndex );
		SegmentStatus sgStatus = new SegmentStatus( null , sg );
		sgStatus.setLog( log );
		sgStatus.setTotalStatus( ok );
		engineStatus.setSegmentItemsStatus( this , sgStatus.sg , sgStatus );
		
		long timePassed = timeFinish - timeStart;
		info.addCheckMinorsData( this , timePassed , ok );
		return( timePassed );
	}

}
