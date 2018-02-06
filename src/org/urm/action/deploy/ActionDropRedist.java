package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.shell.Account;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.RedistStorage;
import org.urm.meta.env.MetaEnvServer;

public class ActionDropRedist extends ActionBase {

	String releaseDir;
	
	public ActionDropRedist( ActionBase action , String stream , String releaseDir ) {
		super( action , stream , "Drop redist data, dir=" + releaseDir );
		this.releaseDir = releaseDir;
	}

	@Override protected SCOPESTATE executeAccount( ScopeState state , ActionScopeSet set , Account account ) throws Exception {
		// drop all redist information for all servers
		RedistStorage redist = artefactory.getRedistStorage( this , account );
		redist.dropAll( this );
		return( SCOPESTATE.RunSuccess );
	}

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName() + " ..." );
		
		VersionInfo version = null;
		if( !releaseDir.equals( "all") )
			version = VersionInfo.getReleaseDirInfo( releaseDir ); 
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			RedistStorage redist = artefactory.getRedistStorage( this , target.envServer , item.envServerNode );
			if( version == null ) {
				if( context.CTX_ALL )
					redist.dropAll( this );
				else
					redist.dropReleaseAll( this );
			}
			else
				redist.dropReleaseData( this , version );
		}
		return( SCOPESTATE.RunSuccess );
	}
	
}
