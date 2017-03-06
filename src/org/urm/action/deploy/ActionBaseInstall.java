package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.storage.RuntimeStorage;
import org.urm.engine.storage.VersionInfoStorage;
import org.urm.meta.engine.ServerBaseItemData;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerBase;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.Types.*;

public class ActionBaseInstall extends ActionBase {

	public ActionBaseInstall( ActionBase action , String stream ) {
		super( action , stream , "Install base software" );
	}

	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		executeServer( target );
		return( SCOPESTATE.RunSuccess );
	}

	private void executeServer( ActionScopeTarget target ) throws Exception {
		MetaEnvServer server = target.envServer;
		MetaEnvServerBase base = server.basesw;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );
		
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
		ServerBaseItemData info = repo.getBaseInfo( this , base.ID , node , true );
		if( info.serverAccessType != server.getServerAccessType() ) {
			String baseType = Common.getEnumLower( info.serverAccessType );
			String serverType = Common.getEnumLower( server.getServerAccessType() );
			exit2( _Error.BaseServerTypeMismatched2 , "base server type mismatched: " + baseType + " <> " + serverType , baseType , serverType );
		}
		
		// install dependencies
		for( String depBase : info.dependencies ) {
			ServerBaseItemData depInfo = repo.getBaseInfo( this , depBase , node , false );
			executeNodeInstall( server , node , depInfo );
		}

		// install main
		executeNodeInstall( server , node , info );
	}

	private void executeNodeInstall( MetaEnvServer server , MetaEnvServerNode node , ServerBaseItemData info ) throws Exception {
		if( !isExecute() )
			return;

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		RuntimeStorage runtime = artefactory.getRootRuntimeStorage( this , server , node , info.adm );
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , redist.account );

		if( !startUpdate( info , runtime , vis ) )
			return;
			
		if( info.isArchiveLink() )
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
		if( info.serverAccessType != null ) {
			ServerProcess process = new ServerProcess( server , node );
			process.prepare( this );
		}
		
		finishUpdate( info , redist , vis );
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , ServerBaseItemData info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( server , info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = info.INSTALLPATH;
		
		extractArchiveFromRedist( info , redistPath , runtimePath , runtime );
		linkNewBase( info , runtime , runtimePath );
		copySystemFiles( info , redist , runtime );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , ServerBaseItemData info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( server , info );
		String redistPath = copyLocalToRedist( info , localPath , redist );
		String runtimePath = info.INSTALLPATH;
		
		extractArchiveFromRedist( info , redistPath , runtimePath , runtime );
		copySystemFiles( info , redist , runtime );
	}

	private void executeNodeNoDist( MetaEnvServer server , MetaEnvServerNode node , ServerBaseItemData info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		copySystemFiles( info , redist , runtime );
	}
	
	private void executeNodeInstaller( MetaEnvServer server , MetaEnvServerNode node , ServerBaseItemData info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		LocalFolder workBase = getSystemFiles( info , redist.server , redist.node );
		String installerFile = copySourceToLocal( server , info );
		RemoteFolder redistFolder = redist.getRedistTmpFolder( this );
		
		String redistFile = redistFolder.copyFileFromLocal( this , installerFile );
		
		if( info.isArchive() )
			extractArchiveFromRedist( info , redistFile , redistFolder.folderPath , runtime );
		
		redistFolder.copyDirContentFromLocal( this , workBase , "" );
		
		ShellExecutor shell = super.getShell( node );
		if( server.isLinux() )
			shell.customCheckErrorsDebug( this , redistFolder.folderPath , "chmod 777 server.*.sh" );
		
		// run installer
		int timeout = setTimeoutUnlimited();
		String cmd = ( server.isLinux() )? "./server.prepare.sh" : "call server.prepare.cmd";
		shell.customCheckStatus( this , redistFolder.folderPath , cmd );
		setTimeout( timeout );
	}
	
	private boolean startUpdate( ServerBaseItemData info , RuntimeStorage runtime , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , info.item.ID );
		if( STATUS.equals( "ok" ) ) {
			if( !isForced() ) {
				info( "skip updating base=" + info.item.ID + ". Already installed." );
				return( false );
			}
		}

		String dowhat = ( STATUS.isEmpty() )? "install" : "reinstall";
		info( runtime.account.getPrintName() + ": " + dowhat + " base=" + info.item.ID + ", type=" + Common.getEnumLower( info.type ) + " ..." );
		vis.setBaseStatus( this , info.item.ID , "upgrading" );
		runtime.createRootPath( this );
		return( true );
	}

	private void finishUpdate( ServerBaseItemData info , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		vis.setBaseStatus( this , info.item.ID , "ok" );
	}

	private String copySourceToLocal( MetaEnvServer server , ServerBaseItemData info ) throws Exception {
		int timeout = setTimeoutUnlimited();
		
		String localPath = null;
		if( info.SRCFILE.startsWith( "http:" ) || info.SRCFILE.startsWith( "https:" ) ) {
			LocalFolder folder = artefactory.getArtefactFolder( this , server.osType , server.meta , "base" );
			String baseName = Common.getBaseName( info.SRCFILE );
			String filePath = folder.getFilePath( this , baseName );
			shell.downloadUnix( this , info.SRCFILE , filePath , "" );
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
		
		if( !shell.checkFileExists( this , localPath ) )
			exit1( _Error.UnableFindFile1 , "unable to find file: " + localPath , localPath );
		
		debug( "source local path: " + localPath );
		return( localPath );
	}

	private String copyLocalToRedist( ServerBaseItemData info , String localPath , RedistStorage redist ) throws Exception {
		RemoteFolder folder = redist.getRedistTmpFolder( this );
		folder.copyFileFromLocal( this , localPath );
		
		String baseName = Common.getBaseName( localPath );
		String redistPath = folder.getFilePath( this , baseName );
		debug( "redist path: " + redistPath );
		return( redistPath );
	}
	
	private void extractArchiveFromRedist( ServerBaseItemData info , String redistPath , String installPath , RuntimeStorage runtime ) throws Exception {
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

	private void linkNewBase( ServerBaseItemData info , RuntimeStorage runtime , String runtimePath ) throws Exception {
		runtime.createDirLink( this , info.INSTALLLINK , runtimePath );
		debug( "link path: " + info.INSTALLLINK );
	}

	private void copySystemFiles( ServerBaseItemData info , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		if( info.serverAccessType == null )
			return;
		
		LocalFolder workBase = getSystemFiles( info , redist.server , redist.node );
		
		// deploy
		if( info.serverAccessType == VarSERVERACCESSTYPE.GENERIC )
			runtime.createBinPath( this );
		
		runtime.restoreSysConfigs( this , redist , workBase );
	}

	private LocalFolder getSystemFiles( ServerBaseItemData info , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		LocalFolder workBase = artefactory.getWorkFolder( this , "sysbase" );
		workBase.recreateThis( this );
		
		// copy system files from base
		RemoteFolder baseMaster = info.getFolder( this );
		if( info.serverAccessType == VarSERVERACCESSTYPE.SERVICE ) {
			if( !server.isLinux() )
				exitUnexpectedState();
			
			baseMaster.copyFileToLocalRename( this , workBase , "service" , server.SYSNAME );
		}
		else
		if( info.serverAccessType == VarSERVERACCESSTYPE.GENERIC ) {
			if( server.isLinux() )
				baseMaster.copyFilesToLocal( this , workBase , "server.*.sh" );
			else
				baseMaster.copyFilesToLocal( this , workBase , "server.*.cmd" );
		}
		else {
			String value = Common.getEnumLower( info.serverAccessType );
			exit1( _Error.AccTypeNotForOperation1 , "access type (" + value + ") is not supported for operation" , value );
		}
		
		// configure
		ConfBuilder builder = new ConfBuilder( this , server.meta );
		builder.configureFolder( this , workBase , node , info.getProperties() , info.charset );
		return( workBase );
	}
}
