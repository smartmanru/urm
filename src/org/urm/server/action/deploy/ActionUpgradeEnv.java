package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.VersionInfoStorage;

public class ActionUpgradeEnv extends ActionBase {

	String S_DATAFILE = "upgrade.data";
	String S_LOGFILE = "upgrade.log";
	String PATCHID;
	
	String PATCHFILE;
	
	public ActionUpgradeEnv( ActionBase action , String stream , String PATCHID ) {
		super( action , stream );
		
		this.PATCHID = PATCHID;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		PATCHFILE = shell.findOneTop( this , context.env.UPGRADE_PATH, PATCHID + "-*" );
		if( PATCHFILE.isEmpty() )
			exit( "unable to find patch file=" + PATCHID + "-* in " + context.env.UPGRADE_PATH );
		
		// execute
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		if( !checkNeed( account , vis ) )
			return( true );
			
		// execute
		int timeout = setTimeoutUnlimited();
		int status = shell.customGetStatus( this , "sh " + Common.getPath( context.env.UPGRADE_PATH , PATCHFILE ) +
				" " + account.HOSTLOGIN + " " + context.CTX_EXTRAARGS );
		setTimeout( timeout );
		if( status < 0 )
			exit( "fatal error" );

		// register result
		registerExecution( account , status , vis );
		
		return( true );
	}

	private boolean checkNeed( Account account , VersionInfoStorage vis ) throws Exception {
		if( context.CTX_FORCE ) {
			if( !isExecute() ) {
				info( account.HOSTLOGIN + ": forced upgrade " + PATCHFILE + " (showonly)" );
				return( false );
			}
			
			info( account.HOSTLOGIN + ": force upgrade " + PATCHFILE + " (execute)" );
			return( true );
		}
		
		String F_ACTION = "initial";

		// check upgrade status
		String F_STATUS = vis.getBaseStatus( this , PATCHID );

		if( !F_STATUS.isEmpty() ) {
			if( F_STATUS.indexOf( PATCHID + ":ok" ) >= 0 ) {
				info( account.HOSTLOGIN + ": upgrade " + PATCHFILE + " is already done. Skipped" );
				return( false );
			}

			F_ACTION = "repair";
		}	

		if( context.CTX_SHOWONLY ) {
			info( account.HOSTLOGIN + ": upgrade " + F_ACTION + " " + PATCHFILE + " (showonly)" );
			return( false );
		}

		// add before record to log
		ShellExecutor remoteSession = getShell( account );
		remoteSession.appendExecuteLog( this , "start " + F_ACTION + " upgrade PATCHID=" + PATCHID );

		// add before record to data
		vis.setBaseStatus( this , PATCHID , "upgrading" );

		info( account.HOSTLOGIN + ": upgrade " + F_ACTION + " " + PATCHFILE + " (execute) ..." );
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
