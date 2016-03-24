package ru.egov.urm.action.deploy;

import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.shell.Account;
import ru.egov.urm.storage.VersionInfoStorage;

public class ActionBaseList extends ActionBase {

	public ActionBaseList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , account );
		Map<String,String> items = vis.getBaseList( this );
		if( items.isEmpty() ) {
			log( account.HOSTLOGIN + ": no base items" );
			return( true );
		}
		
		log( account.HOSTLOGIN + ":" );
		for( String key : Common.getSortedKeys( items ) ) {
			String value = items.get( key );
			log( "base=" + key + " value=" + value );
		}
		
		return( true );
	}

	
}
