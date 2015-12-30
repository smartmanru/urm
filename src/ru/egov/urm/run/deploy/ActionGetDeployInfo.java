package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.RedistStateInfo;
import ru.egov.urm.storage.RedistStorage;

public class ActionGetDeployInfo extends ActionBase {

	public ActionGetDeployInfo( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		comment( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.TYPE ) + " ..." );
		comment( "root path: " + server.ROOTPATH );
		
		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			comment( "node" + node.POS + " (" + node.HOSTLOGIN + "):" );
			
			RedistStorage redist = artefactory.getRedistStorage( server , node );
			showDeployInfo( server , redist );
		}
		return( true );
	}

	private void showDeployInfo( MetaEnvServer server , RedistStorage redist ) throws Exception {
		boolean binary = options.OPT_DEPLOYBINARY;
		boolean conf = context.CONF_DEPLOY;

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
			comment( "\t\t" + type + " state information is missing" );
			return;
		}
			
		for( String key : info.getKeys( this ) ) {
			if( binary )
				comment( "\t\tbinary " + key + ": file=" + info.getKeyFileName( this , key ) + ", version=" + info.getKeyVersion( this , key ) );
			else
				comment( "\t\tconf " + key + ": version=" + info.getKeyVersion( this , key ) );
		}
	}
	
}
