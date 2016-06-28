package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;

public class ActionScp extends ActionBase {

	String srcInfo;
	String dstPath;
	
	public ActionScp( ActionBase action , String stream , String srcInfo , String dstPath ) {
		super( action , stream );
		this.srcInfo = srcInfo;
		this.dstPath = dstPath;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		String F_CMD = "scp";

		if( !shell.checkFileExists( this , srcInfo ) ) 
			F_CMD += " -r";

		if( !context.env.KEYNAME.isEmpty() )
			F_CMD += " -i " + context.env.KEYNAME;
		
		String F_SRC = Common.replace( srcInfo ,  "\\" , "" );
		F_CMD += " " + F_SRC + " " + account.HOSTLOGIN + ":" + dstPath;
		
		super.executeLogLive( account , "scp from " + Common.getQuoted( srcInfo ) + " to " + Common.getQuoted( dstPath ) );
		if( !isExecute() )
			return( true );
		
		int timeout = setTimeoutUnlimited();
		shell.customCheckStatus( this , F_CMD );
		setTimeout( timeout );
		return( true );
	}
	
}
