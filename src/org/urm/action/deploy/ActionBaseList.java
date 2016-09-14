package org.urm.action.deploy;

import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.common.Common;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.VersionInfoStorage;

public class ActionBaseList extends ActionBase {

	public ActionBaseList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		Map<String,String> items = vis.getBaseList( this );
		info( "============================================ account=" + account.getPrintName() );
		
		if( items.isEmpty() ) {
			info( "(no base items)" );
			return( true );
		}
		
		for( String key : Common.getSortedKeys( items ) ) {
			String value = items.get( key );
			info( "base=" + key + " value=" + value );
		}
		
		return( true );
	}
	
}