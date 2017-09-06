package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.engine.shell.Account;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.VersionInfoStorage;

public class ActionBaseClear extends ActionBase {

	public ActionBaseClear( ActionBase action , String stream ) {
		super( action , stream , "Clear base software registry information" );
	}

	@Override protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception {
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		vis.clearAll( this );
		
		return( SCOPESTATE.RunSuccess );
	}

}
