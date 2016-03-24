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
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RuntimeStorage;
import ru.egov.urm.storage.VersionInfoStorage;

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
		log( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.SERVERTYPE + " ..." );
		
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
		if( !isExecute() )
			return;

		RedistStorage redist = artefactory.getRedistStorage( this , server, node );
		RuntimeStorage runtime = artefactory.getRootRuntimeStorage( this , server , node , info.adm );
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , redist.account );

		if( !startUpdate( info , redist , vis ) )
			return;
			
		log( "install base=" + info.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		if( info.isLinuxArchiveLink() )
			executeNodeLinuxArchiveLink( server , node , info , redist , runtime );
		else
		if( info.isLinuxArchiveDirect() )
			executeNodeLinuxArchiveDirect( server , node , info , redist , runtime );
		else
			exitUnexpectedState();
		
		finishUpdate( info , redist );
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = extractArchiveFromRedist( info , redist , redistPath , runtime );
		linkNewBase( info , runtime , runtimePath );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		extractArchiveFromRedist( info , redist , redistPath , runtime );
	}

	private boolean startUpdate( MetaFapBase info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , info.ID );
		if( STATUS.equals( "ok" ) ) {
			if( !context.CTX_FORCE ) {
				log( "skip updating base=" + info.ID + ". Up-to-date" );
				return( false );
			}
		}
				
		vis.setBaseStatus( this , info.ID , "upgrading" );
		return( true );
	}

	private void finishUpdate( MetaFapBase info , RedistStorage redist ) throws Exception {
	}

	private String copySourceToLocal( MetaFapBase info ) throws Exception {
		return( null );
	}

	private String copyLocalToRedist( MetaFapBase info , String localPath , RedistStorage redist ) throws Exception {
		return( null );
	}
	
	private String extractArchiveFromRedist( MetaFapBase info , RedistStorage redist , String redistPath , RuntimeStorage runtime ) throws Exception {
		return( null );
	}

	private void linkNewBase( MetaFapBase info , RuntimeStorage runtime , String runtimePath ) throws Exception {
	}
	
}
