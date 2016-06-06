package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.action.conf.ConfBuilder;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerBase;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.MetaFapBase;
import org.urm.server.meta.MetaFapBase.VarBASESRCFORMAT;
import org.urm.server.meta.Metadata.VarARCHIVETYPE;
import org.urm.server.meta.Metadata.VarSERVERTYPE;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.BaseRepository;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.urm.server.storage.RuntimeStorage;
import org.urm.server.storage.VersionInfoStorage;

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
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );
		
		if( base == null ) {
			info( "server has no base defined. Skipped" );
			return;
		}
			
		info( "rootpath=" + server.ROOTPATH + ", base=" + base.ID );

		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			info( "install server=" + server.NAME + " node=" + node.POS + " ..." );
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
		if( info.isNoDist() )
			executeNodeNoDist( server , node , info , redist , runtime );
		else
		if( info.isInstaller() )
			executeNodeInstaller( server , node , info , redist , runtime );
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
		String runtimePath = info.INSTALLPATH;
		
		extractArchiveFromRedist( info , redistPath , runtimePath , runtime );
		linkNewBase( info , runtime , runtimePath );
		copySystemFiles( info , redist , runtime );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = info.INSTALLPATH;
		
		extractArchiveFromRedist( info , redistPath , runtimePath , runtime );
		copySystemFiles( info , redist , runtime );
	}

	private void executeNodeNoDist( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		copySystemFiles( info , redist , runtime );
	}
	
	private void executeNodeInstaller( MetaEnvServer server , MetaEnvServerNode node , MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		LocalFolder workBase = getSystemFiles( info , redist.server , redist.node );
		String installerFile = copySourceToLocal( info );
		RemoteFolder redistFolder = redist.getRedistTmpFolder( this );
		
		String redistFile = redistFolder.copyFileFromLocal( this , installerFile );
		
		if( info.isArchive() )
			extractArchiveFromRedist( info , redistFile , redistFolder.folderPath , runtime );
		
		redistFolder.copyDirContentFromLocal( this , workBase , "" );
		
		ShellExecutor shell = super.getShell( node );
		if( server.isLinux( this ) )
			shell.customCheckErrorsDebug( this , redistFolder.folderPath , "chmod 777 server.*.sh" );
		
		// run installer
		int timeout = setTimeoutUnlimited();
		String cmd = ( server.isLinux( this ) )? "./server.prepare.sh" : "call server.prepare.cmd";
		shell.customCheckStatus( this , redistFolder.folderPath , cmd );
		setTimeout( timeout );
	}
	
	private boolean startUpdate( MetaFapBase info , RuntimeStorage runtime , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , info.ID );
		if( STATUS.equals( "ok" ) ) {
			if( !context.CTX_FORCE ) {
				info( "skip updating base=" + info.ID + ". Already installed." );
				return( false );
			}
		}

		String dowhat = ( STATUS.isEmpty() )? "install" : "reinstall";
		info( runtime.account.HOSTLOGIN + ": " + dowhat + " base=" + info.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		vis.setBaseStatus( this , info.ID , "upgrading" );
		runtime.createRootPath( this );
		return( true );
	}

	private void finishUpdate( MetaFapBase info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		vis.setBaseStatus( this , info.ID , "ok" );
	}

	private String copySourceToLocal( MetaFapBase info ) throws Exception {
		int timeout = setTimeoutUnlimited();
		
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
		setTimeout( timeout );
		
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
	
	private void extractArchiveFromRedist( MetaFapBase info , String redistPath , String installPath , RuntimeStorage runtime ) throws Exception {
		int timeout = setTimeoutUnlimited();

		if( info.srcFormat == VarBASESRCFORMAT.TARGZ_SINGLEDIR ) {
			runtime.extractBaseArchiveSingleDir( this , redistPath , info.SRCSTOREDIR , installPath , VarARCHIVETYPE.TARGZ );
			debug( "runtime path: " + info.INSTALLPATH );
		}

		if( info.srcFormat == VarBASESRCFORMAT.ZIP_SINGLEDIR ) {
			runtime.extractBaseArchiveSingleDir( this , redistPath , info.SRCSTOREDIR , installPath , VarARCHIVETYPE.ZIP );
			debug( "runtime path: " + info.INSTALLPATH );
		}

		setTimeout( timeout );
		exitUnexpectedState();
	}

	private void linkNewBase( MetaFapBase info , RuntimeStorage runtime , String runtimePath ) throws Exception {
		runtime.createDirLink( this , info.INSTALLLINK , runtimePath );
		debug( "link path: " + info.INSTALLLINK );
	}

	private void copySystemFiles( MetaFapBase info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		if( info.serverType == null )
			return;
		
		LocalFolder workBase = getSystemFiles( info , redist.server , redist.node );
		
		// deploy
		if( info.serverType != VarSERVERTYPE.SERVICE )
			runtime.createBinPath( this );
		runtime.restoreSysConfigs( this , redist , workBase );
	}

	private LocalFolder getSystemFiles( MetaFapBase info , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		LocalFolder workBase = artefactory.getWorkFolder( this , "sysbase" );
		workBase.recreateThis( this );
		
		// copy system files from base
		RemoteFolder baseMaster = info.getFolder( this );
		if( info.serverType == VarSERVERTYPE.SERVICE ) {
			if( !server.isLinux( this ) )
				exitUnexpectedState();
			
			baseMaster.copyFileToLocalRename( this , workBase , "service" , server.SERVICENAME );
		}
		else {
			if( server.isLinux( this ) )
				baseMaster.copyFilesToLocal( this , workBase , "server.*.sh" );
			else
				baseMaster.copyFilesToLocal( this , workBase , "server.*.cmd" );
		}
		
		// configure
		ConfBuilder builder = new ConfBuilder( this );
		builder.configureFolder( this , workBase , node , info.properties , info.charset );
		return( workBase );
	}
}
