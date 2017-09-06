package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RuntimeStorage;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionRollout extends ActionBase {

	Dist dist;

	public ActionRollout( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Rollout redist to environment, release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		// ignore database and unreachable
		MetaEnvServer server = target.envServer;
		if( !server.isDeployPossible() ) {
			trace( "ignore due to server empty deployment" );
			return( SCOPESTATE.NotRun );
		}

		if( target.getItems( this ).size() == 0 ) {
			trace( "no nodes to rollout. Skipped." );
			return( SCOPESTATE.NotRun );
		}

		executeServer( target );
		return( SCOPESTATE.RunSuccess );
	}
	
	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		ServerDeployment[] deps = new ServerDeployment[ target.getItems( this ).size() ];
		debug( "get deployment data ..." );
		int k = 0;
		boolean hasDeployments = false;
		VersionInfo version = VersionInfo.getDistVersion( this , dist ); 
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			
			ServerDeployment deployment = redist.getDeployment( this , version );
			deps[ k++ ] = deployment;
			if( !deployment.isEmpty( this ) )
				hasDeployments = true;
		}

		if( !hasDeployments ) {
			info( "specified nodes of server=" + server.NAME + " have no deployments. Skipped." );
			return;
		}
		
		info( "============================================ execute server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );
		info( "rootpath=" + server.ROOTPATH );

		k = 0;
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "rollout server=" + server.NAME + " node=" + node.POS + " ..." );

			// deploy both binaries and configs to each node
			ServerDeployment deployment = deps[ k++ ];
			executeNode( server , node , deployment );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , ServerDeployment deployment ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
		VersionInfo version = VersionInfo.getDistVersion( this , dist ); 
		runtime.rollout( this , version , deployment );
	}
	
}
