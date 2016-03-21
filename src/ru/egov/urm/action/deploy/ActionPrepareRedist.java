package ru.egov.urm.action.deploy;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.RedistStorage;

public class ActionPrepareRedist extends ActionBase {

	DistStorage dist;
	boolean recreate;
	
	public ActionPrepareRedist( ActionBase action , String stream , DistStorage dist , boolean recreate ) {
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
			
		recreateFolders( server );
		return( true );
	}

	private void recreateFolders( MetaEnvServer server ) throws Exception {
		log( "prepare server=" + server.NAME + " ..." );
		
		recreateFoldersSingle( server );
		
		if( server.staticServer != null )
			recreateFoldersSingle( server.staticServer );
	}
	
	private void recreateFoldersSingle( MetaEnvServer server ) throws Exception {
		for( MetaEnvServerNode node : server.getNodes( this ) ) {
			RedistStorage storage = artefactory.getRedistStorage( this , server , node );
			recreateFoldersNode( storage );
		}
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
