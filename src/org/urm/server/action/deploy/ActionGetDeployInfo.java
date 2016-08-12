package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.action.database.DatabaseClient;
import org.urm.server.action.database.DatabaseRegistry;
import org.urm.server.action.database.DatabaseRegistryRelease;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerLocation;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.Meta.VarCONTENTTYPE;
import org.urm.server.storage.FileInfo;
import org.urm.server.storage.RedistStateInfo;
import org.urm.server.storage.RedistStorage;

public class ActionGetDeployInfo extends ActionBase {

	public ActionGetDeployInfo( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );

		if( server.isDatabase( this ) )
			executeTargetDatabase( server );
		else
			executeTargetApp( target , server );
		
		return( true );
	}
	
	private void executeTargetDatabase( MetaEnvServer server ) throws Exception {
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit( "unable to connect to server=" + server.NAME );

		DatabaseRegistry registry = DatabaseRegistry.getRegistry( this , client );
		DatabaseRegistryRelease release = registry.getLastReleaseInfo( this );
		info( "database: " );
		info( "\trelease: " + release.version );
		info( "\tstate: " + Common.getEnumLower( release.state ) );
	}
		
	private void executeTargetApp( ActionScopeTarget target , MetaEnvServer server ) throws Exception {
		info( "root path: " + server.ROOTPATH );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "node" + node.POS + " (" + node.HOSTLOGIN + "):" );
			
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			showDeployInfoApp( server , redist );
		}
	}

	private void showDeployInfoApp( MetaEnvServer server , RedistStorage redist ) throws Exception {
		boolean binary = context.CTX_DEPLOYBINARY;
		boolean conf = context.CTX_CONFDEPLOY;

		for( MetaEnvServerLocation location : server.getLocations( this , binary , conf ) ) {
			info( "\tlocation: " + location.DEPLOYPATH );
			if( binary && location.hasBinaryItems( this ) )
				showDeployInfoContent( server , redist , location , true );
			if( conf && location.hasConfItems( this ) )
				showDeployInfoContent( server , redist , location , false );
		}
	}

	private void showDeployInfoContent( MetaEnvServer server , RedistStorage redist , MetaEnvServerLocation location , boolean binary ) throws Exception {
		VarCONTENTTYPE contentType = location.getContentType( this , binary );
		RedistStateInfo info = redist.getStateInfo( this , location.DEPLOYPATH , contentType );
		if( !info.exists ) {
			String type = ( binary )? "binary" : "conf";
			info( "\t\t(" + type + " state information is missing)" );
			return;
		}
			
		for( String key : info.getKeys( this ) ) {
			FileInfo data = info.getVerData( this , key );
			if( binary ) {
				if( data.binaryItem.isArchive( this ) )
					info( "\t\tdistitem=" + data.itemName + ": archive (" + Common.getEnumLower( data.binaryItem.DISTTYPE ) + "), version=" + data.version );
				else
					info( "\t\tdistitem=" + data.itemName + ": file=" + data.deployFinalName + ", version=" + data.version );
			}
			else
				info( "\t\tconfitem=" + data.itemName + ": version=" + data.version );
		}
	}
	
}
