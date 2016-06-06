package org.urm.server.action.deploy;

import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.dist.Dist;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.storage.RedistStorage;

public class ActionPrepareRedist extends ActionBase {

	Dist dist;
	boolean recreate;
	
	public ActionPrepareRedist( ActionBase action , String stream , Dist dist , boolean recreate ) {
		super( action , stream );
		this.dist = dist;
		this.recreate = recreate;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		// ignore database and manual deployments
		MetaEnvServer server = target.envServer;
	
		if( !server.isDeployPossible( this ) ) {
			trace( "ignore due to server properties" );
			return( true );
		}
		
		for( ActionScopeTargetItem item : target.getItems( this ) )
			recreateFolders( server , item.envServerNode );
		
		if( target.itemFull ) {
			if( server.staticServer != null )
				recreateFoldersSingle( server.staticServer );
		}
		
		return( true );
	}

	private void recreateFolders( MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		info( "prepare server=" + server.NAME + ", node=" + node.POS + " ..." );
		
		RedistStorage storage = artefactory.getRedistStorage( this , server , node );
		recreateFoldersNode( storage );
	}
	
	private void recreateFoldersSingle( MetaEnvServer server ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( this ) )
			recreateFolders( server , node );
	}

	private void recreateFoldersNode( RedistStorage storage ) throws Exception {
		if( dist == null ) {
			storage.dropReleaseAll( this );
			if( context.CTX_ALL )
				storage.dropStateData( this );
			return;
		}
		
		if( recreate )
			storage.recreateReleaseFolder( this , dist.RELEASEDIR );
		else
			storage.dropReleaseData( this , dist.RELEASEDIR );
	}
	
}
