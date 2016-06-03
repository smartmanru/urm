package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.shell.Account;
import ru.egov.urm.storage.RedistStorage;

public class ActionDropRedist extends ActionBase {

	Dist dist;
	
	public ActionDropRedist( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		// drop all redist information for all servers
		RedistStorage redist = artefactory.getRedistStorage( this , account );
		redist.dropAll( this );
		return( true );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			RedistStorage redist = artefactory.getRedistStorage( this , target.envServer , item.envServerNode );
			if( dist == null )
				redist.dropReleaseAll( this );
			else
				redist.dropReleaseData( this , dist.RELEASEDIR );
		}
		return( true );
	}
	
}
