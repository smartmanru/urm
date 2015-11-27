package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.shell.ShellExecutor;

public class ActionUpgradeEnv extends ActionBase {

	String S_DATAFILE = "upgrade.data";
	String S_LOGFILE = "upgrade.log";
	String PATCHID;
	
	String PATCHFILE;
	
	public ActionUpgradeEnv( ActionBase action , String stream , String PATCHID ) {
		super( action , stream );
		
		this.PATCHID = PATCHID;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , String hostLogin ) throws Exception {
		PATCHFILE = session.findOneTop( this , meta.env.UPGRADE_PATH, PATCHID + "-*" );
		if( PATCHFILE.isEmpty() )
			exit( "unable to find patch file=" + PATCHID + "-* in " + meta.env.UPGRADE_PATH );
		
		// execute
		if( !checkNeed( hostLogin ) )
			return( true );
			
		// execute
		session.setTimeoutUnlimited( this );
		int status = session.customGetStatus( this , "sh " + Common.getPath( meta.env.UPGRADE_PATH , PATCHFILE ) +
				" " + hostLogin + " " + options.OPT_EXTRAARGS );
		if( status < 0 )
			exit( "fatal error" );

		// register result
		registerExecution( hostLogin , status );
		
		return( true );
	}

	private boolean checkNeed( String hostLogin ) throws Exception {
		if( options.OPT_FORCE ) {
			if( context.SHOWONLY ) {
				log( hostLogin + ": forced upgrade " + PATCHFILE + " (showonly)" );
				return( false );
			}
			
			log( hostLogin + ": force upgrade " + PATCHFILE + " (execute)" );
			return( true );
		}
		
		String F_ACTION = "initial";

		// check upgrade status
		ShellExecutor remoteSession = super.getShell( hostLogin );
		String F_STATUS = remoteSession.customGetValue( this , "touch ~/" + S_DATAFILE + "; grep \"id=" + PATCHID + ":\" ~/" + S_DATAFILE );

		if( !F_STATUS.isEmpty() ) {
			if( F_STATUS.indexOf( PATCHID + ":ok" ) >= 0 ) {
				log( hostLogin + ": upgrade " + PATCHFILE + " is already done. Skipped" );
				return( false );
			}

			F_ACTION = "repair";
		}	

		if( context.SHOWONLY ) {
			log( hostLogin + ": upgrade " + F_ACTION + " " + PATCHFILE + " (showonly)" );
			return( false );
		}

		// add before record to log
		remoteSession.customCheckErrorsNormal( this , "echo `date` \"(SSH_CLIENT=$SSH_CLIENT): start " + F_ACTION + 
				" upgrade PATCHID=" + PATCHID + "\" >> ~/" + S_LOGFILE );

		// add before record to data
		remoteSession.customCheckErrorsNormal( this , "cat ~/" + S_DATAFILE + " | grep -v \"id=" + PATCHID + 
				":\" > ~/" + S_DATAFILE + ".copy; mv ~/" + S_DATAFILE + ".copy ~/" + S_DATAFILE + 
				"; echo \"id=" + PATCHID + ":incomlete\" >> ~/" + S_DATAFILE );

		log( hostLogin + ": upgrade " + F_ACTION + " " + PATCHFILE + " (execute) ..." );
		return( true );
	}

	private void registerExecution( String hostLogin , int status ) throws Exception {
		String F_STATUS = ( status != 0 )? "errors" : "ok";

		ShellExecutor remoteSession = super.getShell( hostLogin );
		
		// add after record to log
		remoteSession.customCheckErrorsNormal( this , "echo `date` \"(SSH_CLIENT=$SSH_CLIENT): upgrade done PATCHID=" + 
				PATCHID + "\" >> ~/" + S_LOGFILE );

		// replace record in data
		remoteSession.customCheckErrorsNormal( this , "touch ~/" + S_DATAFILE + 
				"; cat ~/" + S_DATAFILE + " | grep -v \"id=" + PATCHID + 
				":\" > " + S_DATAFILE + ".copy; mv ~/" + S_DATAFILE + ".copy ~/" + S_DATAFILE + 
				"; echo \"id=" + PATCHID + ":" + F_STATUS + "\" >> ~/" + S_DATAFILE );
	}
}
