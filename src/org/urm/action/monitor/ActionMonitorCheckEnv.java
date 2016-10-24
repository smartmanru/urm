package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.deploy.ActionCheckEnv;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaMonitoringTarget;

public class ActionMonitorCheckEnv extends ActionBase {

	public MonitoringStorage storage;
	public MetaMonitoringTarget target;
	public long timePassedMillis;
	
	public ActionMonitorCheckEnv( ActionBase action , String stream  , MonitoringStorage storage , MetaMonitoringTarget target ) {
		super( action , stream );
		this.storage = storage;
		this.target = target;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		ActionCheckEnv action = new ActionCheckEnv( this , null );
		
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
		
		timePassedMillis = System.currentTimeMillis() - timerStarted;  
		action.stopRedirect();
		
		String logSave = logsFolder.getFilePath( action , storage.getCheckEnvFile( target ) );
		action.shell.move( this , logRunning , logSave );
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
