package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RuntimeStorage;

public class ActionRollout extends ActionBase {

	Dist dist;

	public ActionRollout( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		// ignore database and unreachable
		MetaEnvServer server = target.envServer;
		if( !server.isDeployPossible( this ) ) {
			trace( "ignore due to server empty deployment" );
			return( true );
		}

		if( target.getItems( this ).size() == 0 ) {
			trace( "no nodes to rollout. Skipped." );
			return( true );
		}

		executeServer( target );
		return( true );
	}
	
	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		ServerDeployment[] deps = new ServerDeployment[ target.getItems( this ).size() ];
		debug( "get deployment data ..." );
		int k = 0;
		boolean hasDeployments = false;
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			
			ServerDeployment deployment = redist.getDeployment( this , dist.RELEASEDIR );
			deps[ k++ ] = deployment;
			if( !deployment.isEmpty( this ) )
				hasDeployments = true;
		}

		if( !hasDeployments ) {
			log( "specified nodes of server=" + server.NAME + " have no deployments. Skipped." );
			return;
		}
		
		log( "============================================ execute server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );
		log( "rootpath=" + server.ROOTPATH );

		k = 0;
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "rollout server=" + server.NAME + " node=" + node.POS + " ..." );

			// deploy both binaries and configs to each node
			ServerDeployment deployment = deps[ k++ ];
			executeNode( server , node , deployment );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , ServerDeployment deployment ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		
		RuntimeStorage runtime = artefactory.getRuntimeStorage( this , server , node );
		runtime.rollout( this , dist.RELEASEDIR , deployment );
	}
	
}
