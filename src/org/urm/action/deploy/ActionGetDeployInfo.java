package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.database.DatabaseClient;
import org.urm.action.database.DatabaseRegistry;
import org.urm.action.database.DatabaseRegistryRelease;
import org.urm.common.Common;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.FileInfo;
import org.urm.engine.storage.RedistStateInfo;
import org.urm.engine.storage.RedistStorage;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerLocation;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.Types.*;

public class ActionGetDeployInfo extends ActionBase {

	public ActionGetDeployInfo( ActionBase action , String stream ) {
		super( action , stream , "Get information about deployed items" );
	}

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName() + " ..." );

		if( server.isRunDatabase() )
			executeTargetDatabase( server );
		else
			executeTargetApp( target , server );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void executeTargetDatabase( MetaEnvServer server ) throws Exception {
		DatabaseClient client = new DatabaseClient();
		if( !client.checkConnect( this , server ) )
			exit1( _Error.UnableConnectDatbase1 , "unable to connect to server=" + server.NAME , server.NAME );

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
			HostAccount hostAccount = node.getHostAccount();
			info( "node" + node.POS + " (" + hostAccount.getFinalAccount() + "):" );
			
			RedistStorage redist = artefactory.getRedistStorage( this , server , node );
			showDeployInfoApp( server , redist );
		}
	}

	private void showDeployInfoApp( MetaEnvServer server , RedistStorage redist ) throws Exception {
		boolean binary = context.CTX_DEPLOYBINARY;
		boolean conf = context.CTX_CONFDEPLOY;

		for( MetaEnvServerLocation location : server.getLocations( this , binary , conf ) ) {
			info( "\tlocation: " + location.DEPLOYPATH );
			if( binary && location.hasBinaryItems() )
				showDeployInfoContent( server , redist , location , true );
			if( conf && location.hasConfItems() )
				showDeployInfoContent( server , redist , location , false );
		}
	}

	private void showDeployInfoContent( MetaEnvServer server , RedistStorage redist , MetaEnvServerLocation location , boolean binary ) throws Exception {
		EnumContentType contentType = location.getContentType( binary );
		RedistStateInfo info = redist.getStateInfo( this , location.DEPLOYPATH , contentType );
		if( !info.exists ) {
			String type = ( binary )? "binary" : "conf";
			info( "\t\t(" + type + " state information is missing)" );
			return;
		}
			
		for( String key : info.getKeys( this ) ) {
			FileInfo data = info.getVerData( this , key );
			if( binary ) {
				if( data.binaryItem.isArchive() )
					info( "\t\tdistitem=" + data.itemName + ": archive (" + Common.getEnumLower( data.binaryItem.DISTITEM_TYPE ) + "), version=" + data.version.getFullVersion() );
				else
					info( "\t\tdistitem=" + data.itemName + ": file=" + data.deployFinalName + ", version=" + data.version.getFullVersion() );
			}
			else
				info( "\t\tconfitem=" + data.itemName + ": version=" + data.version.getFullVersion() );
		}
	}
	
}
