package ru.egov.urm.action.deploy;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.shell.Account;
import ru.egov.urm.storage.VersionInfoStorage;

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
