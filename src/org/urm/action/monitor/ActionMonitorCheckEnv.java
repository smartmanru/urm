package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ActionEventsSource;
import org.urm.action.ActionScope;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.deploy.ActionCheckEnv;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
import org.urm.engine.ServerSourceEvent;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorCheckEnv extends ActionBase implements ServerEventsListener {

	public MonitoringStorage storage;
	public MetaMonitoringTarget target;
	ServerEventsApp eventsApp;
	
	public long timePassedMillis;
	
	public ActionMonitorCheckEnv( ActionBase action , String stream  , MonitoringStorage storage , MetaMonitoringTarget target , ServerEventsApp eventsApp ) {
		super( action , stream );
		this.storage = storage;
		this.target = target;
		this.eventsApp = eventsApp;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		ActionCheckEnv action = new ActionCheckEnv( this , null );
		eventsApp.subscribe( action.eventSource , this );
		
		MetaEnv env = target.meta.getEnv( this , target.ENV );
		MetaEnvDC dc = env.getDC( this , target.DC );
		action.context.update( action , env , dc );
		ActionScope scope = ActionScope.getEnvScope( action , env , dc , null );
		
		LocalFolder logsFolder = storage.getLogsFolder( action , target );
		String logRunning = logsFolder.getFilePath( action , storage.getCheckEnvRunningFile( target ) ); 
		action.startRedirect( "checkenv log" , logRunning );

		long timerStarted = System.currentTimeMillis();
		if( !action.runAll( scope ) )
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
		if( event.eventType == ActionEventsSource.EVENT_FINISHSTATE ) {
			ActionEventsSource source = ( ActionEventsSource )event.source;
			super.eventSource.setLog( source.getLog() );
			super.eventSource.forwardScopeItem( ServerMonitoring.EVENT_FINALSTATE , ( ScopeState )event.data );
		}
	}
	
}
