package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.shell.Account;

public class ActionRunCmd extends ActionBase {

	String cmd;
	
	public ActionRunCmd( ActionBase action , String stream , String cmd ) {
		super( action , stream , "Run generic command" );
		this.cmd = cmd;
	}

	@Override protected SCOPESTATE executeAccount( ActionScopeSet set , Account account ) throws Exception {
		super.executeCmdLive( account , cmd );
		return( SCOPESTATE.RunSuccess );
	}
	
}
