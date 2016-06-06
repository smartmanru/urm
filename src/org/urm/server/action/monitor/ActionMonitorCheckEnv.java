package org.urm.server.action.monitor;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScope;
import org.urm.server.action.deploy.ActionCheckEnv;
import org.urm.server.meta.MetaMonitoringTarget;
import org.urm.server.storage.MonitoringStorage;

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
			super.setFailed();
		
		timePassedMillis = System.currentTimeMillis() - timerStarted;  
		action.stopRedirect();
		
		String logSave = storage.getCheckEnvFile( target );
		action.session.move( this , logRunning , logSave );
		
		return( true );
	}
	
}
