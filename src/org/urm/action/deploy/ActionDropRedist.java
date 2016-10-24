package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.shell.Account;
import org.urm.engine.storage.RedistStorage;
import org.urm.meta.product.MetaEnvServer;

public class ActionDropRedist extends ActionBase {

	Dist dist;
	
	public ActionDropRedist( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeAccount( ActionScopeSet set , Account account ) throws Exception {
		// drop all redist information for all servers
		RedistStorage redist = artefactory.getRedistStorage( this , account );
		redist.dropAll( this );
		return( SCOPESTATE.RunSuccess );
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			RedistStorage redist = artefactory.getRedistStorage( this , target.envServer , item.envServerNode );
			if( dist == null )
				redist.dropReleaseAll( this );
			else
				redist.dropReleaseData( this , dist.RELEASEDIR );
		}
		return( SCOPESTATE.RunSuccess );
	}
	
}
