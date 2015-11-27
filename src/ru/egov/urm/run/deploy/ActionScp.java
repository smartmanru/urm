package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;

public class ActionScp extends ActionBase {

	String srcInfo;
	String dstPath;
	
	public ActionScp( ActionBase action , String stream , String srcInfo , String dstPath ) {
		super( action , stream );
		this.srcInfo = srcInfo;
		this.dstPath = dstPath;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , String hostLogin ) throws Exception {
		String F_CMD = "scp";

		if( !session.checkFileExists( this , srcInfo ) ) 
			F_CMD += " -r";

		if( !meta.env.KEYNAME.isEmpty() )
			F_CMD += " -i " + meta.env.KEYNAME;
		
		String F_SRC = Common.replace( srcInfo ,  "\\" , "" );
		F_CMD += " " + F_SRC + " " + hostLogin + ":" + dstPath;
		
		super.executeLogLive( hostLogin , "scp from " + Common.getQuoted( srcInfo ) + " to " + Common.getQuoted( dstPath ) );
		if( context.SHOWONLY )
			return( true );
		
		session.setTimeoutUnlimited( this );
		session.customCheckStatus( this , F_CMD );
		return( true );
	}
	
}
