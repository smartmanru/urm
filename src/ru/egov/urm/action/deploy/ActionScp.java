package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.shell.Account;

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

		if( !session.checkFileExists( this , srcInfo ) ) 
			F_CMD += " -r";

		if( !context.env.KEYNAME.isEmpty() )
			F_CMD += " -i " + context.env.KEYNAME;
		
		String F_SRC = Common.replace( srcInfo ,  "\\" , "" );
		F_CMD += " " + F_SRC + " " + account.HOSTLOGIN + ":" + dstPath;
		
		super.executeLogLive( account , "scp from " + Common.getQuoted( srcInfo ) + " to " + Common.getQuoted( dstPath ) );
		if( context.CTX_SHOWONLY )
			return( true );
		
		int timeout = setTimeoutUnlimited();
		session.customCheckStatus( this , F_CMD );
		setTimeout( timeout );
		return( true );
	}
	
}
