package org.urm.action.deploy;

import org.urm.Common;
import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.dist.Dist;
import org.urm.meta.MetaEnvServer;
import org.urm.shell.Account;
import org.urm.storage.RedistStorage;

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
