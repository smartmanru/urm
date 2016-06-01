package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.action.database.DatabaseClient;
import ru.egov.urm.action.database.DatabaseRegistry;
import ru.egov.urm.action.database.DatabaseRegistryRelease;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.storage.FileInfo;
import ru.egov.urm.storage.RedistStateInfo;
import ru.egov.urm.storage.RedistStorage;

public class ActionGetDeployInfo extends ActionBase {

	public ActionGetDeployInfo( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		comment( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );

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
		super.comment( "database: " );
		super.comment( "\trelease: " + release.version );
		super.comment( "\tstate: " + Common.getEnumLower( release.state ) );
	}
		
	private void executeTargetApp( ActionScopeTarget target , MetaEnvServer server ) throws Exception {
		comment( "root path: " + server.ROOTPATH );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			comment( "node" + node.POS + " (" + node.HOSTLOGIN + "):" );
			
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			showDeployInfoApp( server , redist );
		}
	}

	private void showDeployInfoApp( MetaEnvServer server , RedistStorage redist ) throws Exception {
		boolean binary = context.CTX_DEPLOYBINARY;
		boolean conf = context.CTX_CONFDEPLOY;

		for( MetaEnvServerLocation location : server.getLocations( this , binary , conf ) ) {
			super.comment( "\tlocation: " + location.DEPLOYPATH );
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
			comment( "\t\t(" + type + " state information is missing)" );
			return;
		}
			
		for( String key : info.getKeys( this ) ) {
			FileInfo data = info.getVerData( this , key );
			if( binary ) {
				if( data.binaryItem.isArchive( this ) )
					comment( "\t\tdistitem=" + data.itemName + ": archive (" + Common.getEnumLower( data.binaryItem.DISTTYPE ) + "), version=" + data.version );
				else
					comment( "\t\tdistitem=" + data.itemName + ": file=" + data.deployFinalName + ", version=" + data.version );
			}
			else
				comment( "\t\tconfitem=" + data.itemName + ": version=" + data.version );
		}
	}
	
}
