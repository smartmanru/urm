package org.urm.server.action.deploy;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;
import org.urm.server.storage.VersionInfoStorage;

public class ActionBaseClear extends ActionBase {

	public ActionBaseClear( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		vis.clearAll( this );
		
		return( true );
	}

}
