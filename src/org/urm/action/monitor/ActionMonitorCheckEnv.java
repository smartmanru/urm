package org.urm.action.monitor;

import org.urm.action.ActionBase;
import org.urm.action.ActionEnvScopeMaker;
import org.urm.action.ActionScope;
import org.urm.action.deploy.ActionCheckEnv;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MonitoringStorage;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaMonitoringTarget;

public class ActionMonitorCheckEnv extends ActionBase {

	MonitorTargetInfo info;
	public long timePassedMillis;
	
	public ActionMonitorCheckEnv( ActionBase action , String stream , MonitorTargetInfo info ) {
		super( action , stream , "Monitoring, check environment" );
		this.info = info; 
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		ActionCheckEnv action = new ActionCheckEnv( this , null );

		MonitoringStorage storage = info.storage;
		MetaMonitoringTarget target = info.target;
		
		MetaEnvSegment sg = target.findSegment();
		action.context.update( action , sg.env , sg );
		action.context.CTX_FORCE = true;
		
		ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , sg.env );
		maker.addScopeEnv( sg , null );
		ActionScope scope = maker.getScope();
		
		LocalFolder logsFolder = storage.getLogsFolder( action , target );
		logsFolder.ensureExists( this );
		String logRunning = logsFolder.getFilePath( action , storage.getCheckEnvRunningFile( target ) ); 
		action.startRedirect( "checkenv log" , logRunning );

		long timerStarted = System.currentTimeMillis();
		if( !action.runAll( state , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) )
			super.fail0( _Error.MonitorEnvFailed0 , "Checkenv monitoring failed" );
		
		timePassedMillis = System.currentTimeMillis() - timerStarted;  
		action.stopRedirect();
		
		String logSave = logsFolder.getFilePath( action , storage.getCheckEnvFile( target ) );
		action.shell.move( this , logRunning , logSave );
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
