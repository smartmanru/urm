package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaEnvServerNode;
import org.urm.engine.meta.Meta.VarCONTENTTYPE;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.RedistStateInfo;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;

public class ActionGetRedistInfo extends ActionBase {

	Dist dist;

	public ActionGetRedistInfo( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );
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
		return( true );
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
		ServerDeployment deployment = redist.getDeployment( this , dist.RELEASEDIR );
		for( String category : deployment.getCategories( this ) ) {
			boolean first = true;
			
			VarCONTENTTYPE CONTENTTYPE = deployment.getCategoryContent( this , category );
			boolean rollout = deployment.getCategoryRollout( this , category );
			
			for( String LOCATION : deployment.getCategoryLocations( this , category ) ) {
				RemoteFolder rf = redist.getRedistLocationFolder( this , dist.RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
				
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
				RedistStateInfo stateInfo = new RedistStateInfo();
				stateInfo.gather( this , redist.node , CONTENTTYPE , rf.folderPath );
				
				for( String key : stateInfo.getKeys( this ) ) {
					FileInfo info = stateInfo.getVerData( this , key );

					String text = info.itemName;
					text += ", version: " + info.version;
					if( info.binaryItem != null ) {
						if( info.binaryItem.isArchive( this ) )
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
