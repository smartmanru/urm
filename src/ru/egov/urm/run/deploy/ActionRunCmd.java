package ru.egov.urm.run.deploy;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.shell.Account;

public class ActionRunCmd extends ActionBase {

	String cmd;
	
	public ActionRunCmd( ActionBase action , String stream , String cmd ) {
		super( action , stream );
		this.cmd = cmd;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		super.executeCmdLive( account , cmd );
		return( true );
	}
	
}
