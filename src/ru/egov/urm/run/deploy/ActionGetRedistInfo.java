package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
import ru.egov.urm.storage.ServerStorage.RedistFileType;

public class ActionGetRedistInfo extends ActionBase {

	DistStorage dist;

	public ActionGetRedistInfo( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		comment( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.SERVERTYPE + " ..." );
		comment( "root path: " + server.ROOTPATH );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			comment( "node" + node.POS + " (" + node.HOSTLOGIN + "):" );
			
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
			comment( "\t(nothing)" );
			return;
		}
		
		for( String release : releases ) {
			comment( "\trelease: " + release );
		}
	}
	
	private void showReleaseState( RedistStorage redist ) throws Exception {
		ServerDeployment deployment = redist.getDeployment( this , dist.RELEASEDIR );
		for( String category : deployment.getCategories( this ) ) {
			VarCONTENTTYPE CONTENTTYPE = deployment.getCategoryContent( this , category );
			boolean rollout = deployment.getCategoryRollout( this , category );
			comment( "\tcategory: " + category );
			
			for( String LOCATION : deployment.getCategoryLocations( this , category ) ) {
				RemoteFolder rf = redist.getRedistLocationFolder( this , dist.RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
				
				String[] items = deployment.getLocationItems( this , category , LOCATION );
				if( items.length == 0 ) {
					if( !context.CTX_SHOWALL )
						continue;
				}
				
				comment( "\t\tlocation: " + LOCATION + " (" + rf.folderPath + ")" );
				for( String redistFile : items ) {
					RedistFileType fileType = redist.getRedistFileType( this , redistFile );
					String stateBaseName = redist.getStateBaseName( this , CONTENTTYPE , redistFile );
					String stateInfoName = redist.getStateInfoName( this , stateBaseName );

					String info = Common.getEnumLower( fileType );
					if( rf.checkFileExists( this , stateInfoName ) )
						info += ", version: " + rf.getFileContentAsString( this , stateInfoName );
					else
						info += ", no version info";
					
					comment( "\t\t\tfile: " + redistFile + " (" + info + ")" );
				}
			}
		}
	}
	
}
