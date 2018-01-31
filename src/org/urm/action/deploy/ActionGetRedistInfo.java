package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.RedistStateInfo;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.Types.*;

public class ActionGetRedistInfo extends ActionBase {

	Dist dist;

	public ActionGetRedistInfo( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Get redist data information" );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );
		info( "root path: " + server.ROOTPATH );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "node" + node.POS + " (" + node.HOSTLOGIN + "):" );
			
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			if( dist == null )
				showReleases( redist );
			else
				showReleaseState( redist );
		}
		return( SCOPESTATE.RunSuccess );
	}

	private void showReleases( RedistStorage redist ) throws Exception {
		String[] releases = redist.getRedistReleases( this );
		if( releases == null || releases.length == 0 ) {
			info( "\t(nothing)" );
			return;
		}
		
		for( String release : releases ) {
			info( "\trelease: " + release );
		}
	}
	
	private void showReleaseState( RedistStorage redist ) throws Exception {
		VersionInfo version = VersionInfo.getDistVersion( dist ); 
		ServerDeployment deployment = redist.getDeployment( this , version );
		for( String category : deployment.getCategories( this ) ) {
			boolean first = true;
			
			VarCONTENTTYPE CONTENTTYPE = deployment.getCategoryContent( this , category );
			boolean rollout = deployment.getCategoryRollout( this , category );
			
			for( String LOCATION : deployment.getCategoryLocations( this , category ) ) {
				RemoteFolder rf = redist.getRedistLocationFolder( this , version , LOCATION , CONTENTTYPE , rollout );
				
				String[] items = deployment.getLocationItems( this , category , LOCATION );
				if( items.length == 0 ) {
					if( !context.CTX_SHOWALL )
						continue;
				}

				if( first ) {
					info( "\tcategory: " + category );
					first = false;
				}
				
				info( "\t\tlocation: " + LOCATION + " (" + rf.folderPath + ")" );
				RedistStateInfo stateInfo = new RedistStateInfo( redist.meta );
				stateInfo.gather( this , redist.node , CONTENTTYPE , rf.folderPath );
				
				for( String key : stateInfo.getKeys( this ) ) {
					FileInfo info = stateInfo.getVerData( this , key );

					String text = info.itemName;
					text += ", version: " + info.version.getFullVersion();
					if( info.binaryItem != null ) {
						if( info.binaryItem.isArchive() )
							text += ", archive";
						else
							text += ", deployname: " + info.deployFinalName;
					}
					info( "\t\t\tfile: " + info.getFileName( this ) + " (" + text + ")" );
				}
			}
		}
	}
	
}
