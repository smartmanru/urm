package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.deploy.ActionCheckEnv;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.engine.ServerAuth.SecurityAction;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorCheckEnv extends ActionBase implements ServerEventsListener {

	public MonitoringStorage storage;
	public MetaMonitoringTarget target;
	ServerEventsApp eventsApp;
	
	public long timePassedMillis;
	
	public ActionMonitorCheckEnv( ActionBase action , String stream , MonitoringStorage storage , MetaMonitoringTarget target , ServerEventsApp eventsApp ) {
		super( action , stream );
		this.storage = storage;
		this.target = target;
		this.eventsApp = eventsApp;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		ActionCheckEnv action = new ActionCheckEnv( this , null );
		eventsApp.subscribe( action.eventSource , this );
		
		MetaEnv env = target.meta.getEnv( this , target.ENV );
		MetaEnvSegment sg = env.getSG( this , target.SG );
		action.context.update( action , env , sg );
		ActionScope scope = ActionScope.getEnvScope( action , env , sg , null );
		
		LocalFolder logsFolder = storage.getLogsFolder( action , target );
		logsFolder.ensureExists( this );
		String logRunning = logsFolder.getFilePath( action , storage.getCheckEnvRunningFile( target ) ); 
		action.startRedirect( "checkenv log" , logRunning );

		long timerStarted = System.currentTimeMillis();
		if( !action.runAll( scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			super.fail0( _Error.MonitorEnvFailed0 , "Checkenv monitoring failed" );
		
		eventsApp.unsubscribe( this );
		timePassedMillis = System.currentTimeMillis() - timerStarted;  
		action.stopRedirect();
		
		String logSave = logsFolder.getFilePath( action , storage.getCheckEnvFile( target ) );
		action.shell.move( this , logRunning , logSave );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	@Override
	public void triggerEvent( ServerSourceEvent event ) {
		if( event.eventType == ServerEvents.EVENT_MONITORING_SEGMENT ||
			event.eventType == ServerEvents.EVENT_MONITORING_SERVER ||
			event.eventType == ServerEvents.EVENT_MONITORING_NODE ) {
			ScopeState state = ( ScopeState )event.data;
			super.eventSource.forwardScopeItem( event.eventType , state );
		}
	}

	@Override
	public void triggerSubscriptionRemoved( ServerEventsSubscription sub ) {
	}
	
}
