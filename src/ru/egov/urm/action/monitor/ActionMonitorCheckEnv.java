package ru.egov.urm.action.monitor;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.deploy.ActionCheckEnv;
import ru.egov.urm.meta.MetaMonitoringTarget;
import ru.egov.urm.storage.MonitoringStorage;

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
