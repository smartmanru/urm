package ru.egov.urm.run.deploy;

import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.shell.Account;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.RedistStorage;

public class ActionDropRedist extends ActionBase {

	DistStorage dist;
	
	public ActionDropRedist( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeAccount( ActionScopeSet set , Account account ) throws Exception {
		// drop all redist information for all servers
		RedistStorage redist = artefactory.getRedistStorage( "all" , account );
		redist.dropAll( this );
		return( true );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		log( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.SERVERTYPE + " ..." );
		
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
