package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerBase;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaFapBase;
import ru.egov.urm.meta.MetaFapBase.VarBASESRCFORMAT;
import ru.egov.urm.storage.BaseRepository;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.RedistStorage;
import ru.egov.urm.storage.RemoteFolder;
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
			executeNodeInstall( server , node , repo , depInfo );
		}

		// install main
		executeNodeInstall( server , node , repo , info );
	}

	private void executeNodeInstall( MetaEnvServer server , MetaEnvServerNode node , BaseRepository repo , MetaFapBase info ) throws Exception {
		if( !isExecute() )
			return;

		RedistStorage redist = artefactory.getRedistStorage( this , server, node );
		RuntimeStorage runtime = artefactory.getRootRuntimeStorage( this , server , node , info.adm );
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , redist.account );

		if( !startUpdate( info , redist , vis ) )
			return;
			
		log( "install base=" + info.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		if( info.isLinuxArchiveLink() )
			executeNodeLinuxArchiveLink( server , node , repo , info , redist , runtime );
		else
		if( info.isLinuxArchiveDirect() )
			executeNodeLinuxArchiveDirect( server , node , repo , info , redist , runtime );
		else
			exitUnexpectedState();
		
		finishUpdate( info , redist , vis );
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , BaseRepository repo , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( repo , info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = extractArchiveFromRedist( info , redist , redistPath , runtime );
		linkNewBase( info , runtime , runtimePath );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , BaseRepository repo , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( repo , info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		extractArchiveFromRedist( info , redist , redistPath , runtime );
	}

	private boolean startUpdate( MetaFapBase info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , info.ID );
		if( STATUS.equals( "ok" ) ) {
			if( !context.CTX_FORCE ) {
				log( "skip updating base=" + info.ID + ". Already installed." );
				return( false );
			}
		}
				
		vis.setBaseStatus( this , info.ID , "upgrading" );
		return( true );
	}

	private void finishUpdate( MetaFapBase info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		vis.setBaseStatus( this , info.ID , "ok" );
	}

	private String copySourceToLocal( BaseRepository repo , MetaFapBase info ) throws Exception {
		String localPath = null;
		if( info.SRCFILE.startsWith( "http:" ) || info.SRCFILE.startsWith( "https:" ) ) {
			LocalFolder folder = artefactory.getArtefactFolder( this , "base" );
			String baseName = Common.getBaseName( info.SRCFILE );
			String filePath = folder.getFilePath( this , baseName );
			session.downloadUnix( this , info.SRCFILE , filePath , "" );
			localPath = filePath;
		}
		else {
			// check absolute
			if( info.SRCFILE.startsWith( "/" ) )
				localPath = info.SRCFILE;
			else
				localPath = repo.getBaseItemPath( this , info.ID , info.SRCFILE );
		}
		
		if( !session.checkFileExists( this , localPath ) )
			exit( "unable to find file: " + localPath );
		
		debug( "source local path: " + localPath );
		return( localPath );
	}

	private String copyLocalToRedist( MetaFapBase info , String localPath , RedistStorage redist ) throws Exception {
		RemoteFolder folder = redist.getRedistTmpFolder( this );
		folder.copyFileFromLocal( this , localPath );
		String baseName = Common.getBaseName( localPath );
		String redistPath = folder.getFilePath( this , baseName );
		debug( "redist path: " + redistPath );
		return( redistPath );
	}
	
	private String extractArchiveFromRedist( MetaFapBase info , RedistStorage redist , String redistPath , RuntimeStorage runtime ) throws Exception {
		if( info.srcFormat == VarBASESRCFORMAT.TARGZ_SINGLEDIR ) {
			runtime.extractBaseTarGzSingleDir( this , redistPath , info.SRCSTOREDIR , info.INSTALLPATH );
			debug( "runtime path: " + info.INSTALLPATH );
			return( info.INSTALLPATH );
		}

		exitUnexpectedState();
		return( null );
	}

	private void linkNewBase( MetaFapBase info , RuntimeStorage runtime , String runtimePath ) throws Exception {
	}
	
}
