package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.EventService;
import org.urm.engine.StateService;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.StatusSource;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.system.MetaMonitoringItem;
import org.urm.meta.system.MetaMonitoringTarget;

public class ActionMonitorTarget extends ActionBase {
	
	public MonitorTargetInfo info;
	public String name;
	public MetaMonitoringTarget target;
	
	volatile boolean running;
	volatile ActionBase currentAction;
	
	public ActionMonitorTarget( ActionBase action , String stream , MonitorTargetInfo info , String name ) {
		super( action , stream , "monitoring, check target" );
		this.info = info;
		this.target = info.target;
		this.name = name;
		running = false;
	}

	public long executeOnceMajor( ScopeState state ) throws Exception {
		ActionMonitorCheckEnv actionCheck = new ActionMonitorCheckEnv( this , null , info );
		currentAction = actionCheck;
		MetaEnv env = getEnv( target );
		currentAction.runSimpleEnv( state , env , SecurityAction.ACTION_DEPLOY , false );
		info.addCheckEnvData( this , actionCheck.timePassedMillis , actionCheck.isOK() );
		Common.sleep( 1000 );
		return( actionCheck.timePassedMillis );
	}
	
	private MetaEnv getEnv( MetaMonitoringTarget target ) throws Exception {
		MetaEnv env = target.getEnv();
		return( env );
	}
	
	public void start() {
		running = true;
	}
	
	public void stop() {
		running = false;
		ActionBase action = currentAction;
		if( action != null )
			action.stopExecution();
	}
	
	public void createGraph() throws Exception {
		if( !running )
			return;
		
		if( !info.isAvailable() )
			return;
		
		MetaEnvSegment sg = info.target.getSegment();
		super.trace( "refresh target graph env=" + sg.env.NAME + ", sg=" + sg.NAME );
		info.addHistoryGraph( this );
		info.stop( this );

		if( sg != null ) { 
			StateService status = super.getEngineStatus();
			StatusSource source = status.getObjectSource( sg );
			if( source != null )
				source.customEvent( EventService.OWNER_ENGINE , EventService.EVENT_MONITORGRAPHCHANGED , info );
		}
	}

	public long executeOnceMinor( ScopeState state ) throws Exception {
		// system
		int sgIndex = super.logStartCapture();
		MetaEnvSegment sg = info.target.getSegment();
		super.info( "Run fast segment checks, env=" + sg.env.NAME + ", sg=" + sg.NAME + " ..." );
		
		long timeStart = System.currentTimeMillis();
		boolean ok = true;
		
		StateService engineStatus = super.getEngineStatus();
		String name = sg.env.NAME + "::" + sg.NAME;
		
		for( MetaEnvServer server : sg.getServers() ) {
			if( !running )
				break;
			
			if( !super.isServerOffline( server ) ) {
				ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , name , target , null , server );
				currentAction = action;
				action.runSimpleEnv( state , server.sg.env , SecurityAction.ACTION_DEPLOY , false );
				if( action.isFailed() )
					ok = false;
				
				engineStatus.setServerItemsStatus( this , action.server , action.serverStatus );
				for( NodeStatus nodeStatus : action.getNodes() )
					engineStatus.setServerNodeItemsStatus( this , nodeStatus.node , nodeStatus );
			}
		}
		
		// direct
		for( MetaMonitoringItem item : target.getUrlsList() ) {
			if( !running )
				break;
			
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , name , target , item , null );
			currentAction = action;
			action.runSimpleEnv( state , sg.env , SecurityAction.ACTION_DEPLOY , false );
			if( action.isFailed() )
				ok = false;
		}
		
		for( MetaMonitoringItem item : target.getWSList() ) {
			if( !running )
				break;
			
			ActionMonitorCheckItem action = new ActionMonitorCheckItem( this , name , target , item , null );
			currentAction = action;
			action.runSimpleEnv( state , sg.env , SecurityAction.ACTION_DEPLOY , false );
			if( action.isFailed() )
				ok = false;
		}
		
		long timeFinish = System.currentTimeMillis();
		
		if( !ok )
			super.fail1( _Error.MonitorTargetFailed1 , "Monitoring target failed name=" + name , name );
		
		info( "Fast server checks finished" );
		String[] log = super.logFinishCapture( sgIndex );
		SegmentStatus sgStatus = new SegmentStatus( sg );
		sgStatus.setLog( log );
		sgStatus.setTotalStatus( ok );
		engineStatus.setSegmentItemsStatus( this , sgStatus.sg , sgStatus );
		
		long timePassed = timeFinish - timeStart;
		info.addCheckMinorsData( this , timePassed , ok );
		return( timePassed );
	}

}
