package org.urm.server.action.deploy;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;

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
