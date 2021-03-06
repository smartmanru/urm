package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.Shell;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionScp extends ActionBase {

	String srcInfo;
	String dstPath;
	
	public ActionScp( ActionBase action , String stream , String srcInfo , String dstPath ) {
		super( action , stream , "Copy files to environment" );
		this.srcInfo = srcInfo;
		this.dstPath = dstPath;
	}

	@Override 
	protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception {
		String F_CMD = "scp";

		if( !shell.checkFileExists( this , srcInfo ) ) 
			F_CMD += " -r";

		// not implemented
		super.exitNotImplemented();
		
		//if( !context.env.KEYFILE.isEmpty() )
		//	F_CMD += " -i " + context.env.KEYFILE;
		
		if( account.PORT != 22 )
			F_CMD += " -P " + account.PORT;
		
		String F_SRC = Common.replace( srcInfo ,  "\\" , "" );
		
		F_CMD += " " + F_SRC + " " + account.getHostLogin() + ":" + dstPath;
		
		super.executeLogLive( account , "scp from " + Common.getQuoted( srcInfo ) + " to " + Common.getQuoted( dstPath ) );
		if( !isExecute() )
			return( SCOPESTATE.RunFail );
		
		shell.customCheckStatus( this , F_CMD , Shell.WAIT_LONG );
		return( SCOPESTATE.RunSuccess );
	}
	
}
