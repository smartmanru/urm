package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerBase;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaFapBase;
import ru.egov.urm.storage.BaseRepository;

public class ActionBaseInstall extends ActionBase {

	public ActionBaseInstall( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		executeServer( target );
		return( true );
	}

	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerBase base = server.base;
		log( "============================================ execute server=" + server.NAME + ", type=" + server.SERVERTYPE + " ..." );
		
		if( base == null ) {
			log( "server has no base defined. Skipped" );
			return;
		}
			
		log( "rootpath=" + server.ROOTPATH + ", base=" + base.ID );

		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			log( "install server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( server , node , base );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , MetaEnvServerBase base ) throws Exception {
		BaseRepository repo = artefactory.getBaseRepository( this );
		MetaFapBase info = repo.getBaseInfo( this , base.ID , node.properties );
		
		// install dependencies
		for( String depBase : info.dependencies ) {
			MetaFapBase depInfo = repo.getBaseInfo( this , depBase , node.properties );
			executeNodeInstall( server , node , depInfo );
		}

		// install main
		executeNodeInstall( server , node , info );
	}

	private void executeNodeInstall( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info ) throws Exception {
		log( "install base=" + info.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		if( info.isLinuxArchiveLink() )
			executeNodeLinuxArchiveLink( server , node , info );
		else
		if( info.isLinuxArchiveDirect() )
			executeNodeLinuxArchiveDirect( server , node , info );
		else
			exitUnexpectedState();
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info ) throws Exception {
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info ) throws Exception {
	}
	
}
