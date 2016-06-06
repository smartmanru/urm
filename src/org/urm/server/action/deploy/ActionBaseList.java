package org.urm.server.action.deploy;

import java.util.Map;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.shell.Account;
import org.urm.server.storage.VersionInfoStorage;

public class ActionBaseList extends ActionBase {

	public ActionBaseList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		Map<String,String> items = vis.getBaseList( this );
		comment( "============================================ account=" + account.HOSTLOGIN );
		
		if( items.isEmpty() ) {
			comment( "(no base items)" );
			return( true );
		}
		
		for( String key : Common.getSortedKeys( items ) ) {
			String value = items.get( key );
			comment( "base=" + key + " value=" + value );
		}
		
		return( true );
	}
	
}
