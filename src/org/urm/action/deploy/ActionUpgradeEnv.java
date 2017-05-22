package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.VersionInfoStorage;

public class ActionUpgradeEnv extends ActionBase {

	String S_DATAFILE = "upgrade.data";
	String S_LOGFILE = "upgrade.log";
	String PATCHID;
	
	String PATCHFILE;
	
	public ActionUpgradeEnv( ActionBase action , String stream , String PATCHID ) {
		super( action , stream , "Upgrade environment, patch=" + PATCHID );
		
		this.PATCHID = PATCHID;
	}

	@Override protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception {
		PATCHFILE = shell.findOneTop( this , context.env.UPGRADE_PATH, PATCHID + "-*" );
		if( PATCHFILE.isEmpty() )
			exit2( _Error.UnableFindPatch2 , "unable to find patch file=" + PATCHID + "-* in " + context.env.UPGRADE_PATH , PATCHID , context.env.UPGRADE_PATH );
		
		// execute
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		if( !checkNeed( account , vis ) )
			return( SCOPESTATE.NotRun );
			
		// execute
		int timeout = setTimeoutUnlimited();
		int status = shell.customGetStatus( this , "sh " + Common.getPath( context.env.UPGRADE_PATH , PATCHFILE ) +
				" " + account.getFullName() + " " + context.CTX_EXTRAARGS );
		setTimeout( timeout );
		if( status < 0 )
			exit0( _Error.UpgradeError0 , "error on upgrade" );

		// register result
		registerExecution( account , status , vis );
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean checkNeed( Account account , VersionInfoStorage vis ) throws Exception {
		if( isForced() ) {
			if( !isExecute() ) {
				info( account.getPrintName() + ": forced upgrade " + PATCHFILE + " (showonly)" );
				return( false );
			}
			
			info( account.getPrintName() + ": force upgrade " + PATCHFILE + " (execute)" );
			return( true );
		}
		
		String F_ACTION = "initial";

		// check upgrade status
		String F_STATUS = vis.getBaseStatus( this , PATCHID );

		if( !F_STATUS.isEmpty() ) {
			if( F_STATUS.indexOf( PATCHID + ":ok" ) >= 0 ) {
				info( account.getPrintName() + ": upgrade " + PATCHFILE + " is already done. Skipped" );
				return( false );
			}

			F_ACTION = "repair";
		}	

		if( context.CTX_SHOWONLY ) {
			info( account.getPrintName() + ": upgrade " + F_ACTION + " " + PATCHFILE + " (showonly)" );
			return( false );
		}

		// add before record to log
		ShellExecutor remoteSession = getShell( account );
		remoteSession.appendExecuteLog( this , "start " + F_ACTION + " upgrade PATCHID=" + PATCHID );

		// add before record to data
		vis.setBaseStatus( this , PATCHID , "upgrading" );

		info( account.getPrintName() + ": upgrade " + F_ACTION + " " + PATCHFILE + " (execute) ..." );
		return( true );
	}

	private void registerExecution( Account account , int status , VersionInfoStorage vis ) throws Exception {
		String F_STATUS = ( status != 0 )? "errors" : "ok";

		ShellExecutor remoteSession = super.getShell( account );
		
		// add after record to log
		remoteSession.appendExecuteLog( this , "upgrade done PATCHID=" + PATCHID );

		// replace record in data
		vis.setBaseStatus( this , PATCHID , F_STATUS );
	}
}
