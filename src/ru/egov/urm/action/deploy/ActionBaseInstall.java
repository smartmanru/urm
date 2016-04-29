package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionScopeTargetItem;
import ru.egov.urm.action.conf.ConfBuilder;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerBase;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaFapBase;
import ru.egov.urm.meta.MetaFapBase.VarBASESRCFORMAT;
import ru.egov.urm.meta.Metadata.VarARCHIVETYPE;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
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
		log( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );
		
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
		MetaFapBase info = repo.getBaseInfo( this , base.ID , node , true );
		if( info.serverType != server.serverType )
			exit( "base server type mismatched: " + Common.getEnumLower( info.serverType ) + " <> " + Common.getEnumLower( server.serverType ) );
		
		// install dependencies
		for( String depBase : info.dependencies ) {
			MetaFapBase depInfo = repo.getBaseInfo( this , depBase , node , false );
			executeNodeInstall( server , node , depInfo );
		}

		// install main
		executeNodeInstall( server , node , info );
	}

	private void executeNodeInstall( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info ) throws Exception {
		if( !isExecute() )
			return;

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		RuntimeStorage runtime = artefactory.getRootRuntimeStorage( this , server , node , info.adm );
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , redist.account );

		if( !startUpdate( info , runtime , vis ) )
			return;
			
		if( info.isLinuxArchiveLink() )
			executeNodeLinuxArchiveLink( server , node , info , redist , runtime );
		else
		if( info.isArchiveDirect() )
			executeNodeLinuxArchiveDirect( server , node , info , redist , runtime );
		else
			exitUnexpectedState();
		
		// prepare
		if( info.serverType != null ) {
			ServerProcess process = new ServerProcess( server , node );
			process.prepare( this );
		}
		
		finishUpdate( info , redist , vis );
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = extractArchiveFromRedist( info , redist , redistPath , runtime );
		linkNewBase( info , runtime , runtimePath );
		copySystemFiles( info , redist , runtime );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		extractArchiveFromRedist( info , redist , redistPath , runtime );
		copySystemFiles( info , redist , runtime );
	}

	private boolean startUpdate( MetaFapBase info , RuntimeStorage runtime , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , info.ID );
		if( STATUS.equals( "ok" ) ) {
			if( !context.CTX_FORCE ) {
				log( "skip updating base=" + info.ID + ". Already installed." );
				return( false );
			}
		}

		log( runtime.account.HOSTLOGIN + ": install base=" + info.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		vis.setBaseStatus( this , info.ID , "upgrading" );
		runtime.createRootPath( this );
		return( true );
	}

	private void finishUpdate( MetaFapBase info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		vis.setBaseStatus( this , info.ID , "ok" );
	}

	private String copySourceToLocal( MetaFapBase info ) throws Exception {
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
				localPath = info.getItemPath( this , info.SRCFILE );
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
			runtime.extractBaseArchiveSingleDir( this , redistPath , info.SRCSTOREDIR , info.INSTALLPATH , VarARCHIVETYPE.TARGZ );
			debug( "runtime path: " + info.INSTALLPATH );
			return( info.INSTALLPATH );
		}

		if( info.srcFormat == VarBASESRCFORMAT.ZIP_SINGLEDIR ) {
			runtime.extractBaseArchiveSingleDir( this , redistPath , info.SRCSTOREDIR , info.INSTALLPATH , VarARCHIVETYPE.ZIP );
			debug( "runtime path: " + info.INSTALLPATH );
			return( info.INSTALLPATH );
		}
		
		exitUnexpectedState();
		return( null );
	}

	private void linkNewBase( MetaFapBase info , RuntimeStorage runtime , String runtimePath ) throws Exception {
		runtime.createDirLink( this , info.INSTALLLINK , runtimePath );
		debug( "link path: " + info.INSTALLLINK );
	}

	private void copySystemFiles( MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		if( info.serverType == null )
			return;
		
		LocalFolder workBase = artefactory.getWorkFolder( this , "sysbase" );
		workBase.recreateThis( this );
		
		// copy system files from base
		RemoteFolder baseMaster = info.getFolder( this );
		if( info.serverType == VarSERVERTYPE.SERVICE ) {
			if( !runtime.server.isLinux( this ) )
				exitUnexpectedState();
			
			baseMaster.copyFileToLocalRename( this , workBase , "service" , runtime.server.SERVICENAME );
		}
		else {
			if( runtime.server.isLinux( this ) )
				baseMaster.copyFilesToLocal( this , workBase , "server.*.sh" );
			else
				baseMaster.copyFilesToLocal( this , workBase , "server.*.cmd" );
		}
		
		// configure
		ConfBuilder builder = new ConfBuilder( this );
		builder.configureFolder( this , workBase , runtime.node , info.properties );
		
		// deploy
		runtime.restoreSysConfigs( this , redist , workBase );
	}
	
}
