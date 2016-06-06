package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.dist.Dist;
import org.urm.meta.MetaEnvServer;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.shell.Account;
import org.urm.server.storage.RedistStorage;

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
