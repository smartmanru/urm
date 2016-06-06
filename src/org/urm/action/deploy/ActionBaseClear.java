package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.shell.Account;
import org.urm.storage.VersionInfoStorage;

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
