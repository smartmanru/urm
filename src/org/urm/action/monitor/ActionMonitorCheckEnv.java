package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.deploy.ActionCheckEnv;
import org.urm.engine.storage.MonitoringStorage;
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

	@Override protected boolean executeSimple() throws Exception {
		ActionCheckEnv action = new ActionCheckEnv( this , null );
		ActionScope scope = ActionScope.getEnvScope( this , null );
		
		String logRunning = storage.getCheckEnvRunningFile( target ); 
		action.startRedirect( "checkenv log" , logRunning );

		long timerStarted = System.currentTimeMillis();
		if( !action.runAll( scope ) )
			super.fail0( _Error.MonitorEnvFailed0 , "Checkenv monitoring failed" );
		
		timePassedMillis = System.currentTimeMillis() - timerStarted;  
		action.stopRedirect();
		
		String logSave = storage.getCheckEnvFile( target );
		action.shell.move( this , logRunning , logSave );
		
		return( true );
	}
	
}
