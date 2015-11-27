package ru.egov.urm.run.deploy;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;

public class ActionRunCmd extends ActionBase {

	String cmd;
	
	public ActionRunCmd( ActionBase action , String stream , String cmd ) {
		super( action , stream );
		this.cmd = cmd;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , String hostLogin ) throws Exception {
		super.executeCmdLive( hostLogin , cmd );
		return( true );
	}
	
}
