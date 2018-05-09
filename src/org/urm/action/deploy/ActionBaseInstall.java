package org.urm.action.deploy;

import java.nio.charset.Charset;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.conf.ConfBuilder;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.data.EngineBase;
import org.urm.engine.shell.Shell;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.BaseRepository;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.engine.storage.RuntimeStorage;
import org.urm.engine.storage.VersionInfoStorage;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.Types.*;

public class ActionBaseInstall extends ActionBase {

	BaseRepository repo;
	
	public ActionBaseInstall( ActionBase action , String stream ) {
		super( action , stream , "Install base software" );
	}

	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		repo = artefactory.getBaseRepository( this );
		executeServer( target , state );
		return( SCOPESTATE.RunSuccess );
	}

	private void executeServer( ActionScopeTarget target , ScopeState state ) throws Exception {
		MetaEnvServer server = target.envServer;
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName() + " ..." );
		
		BaseItem baseItem = server.getBaseItem();
		if( baseItem == null ) {
			info( "server has no base defined. Skipped" );
			return;
		}
			
		info( "rootpath=" + server.ROOTPATH + ", base=" + baseItem.NAME );

		for( ActionScopeTargetItem item : target.getItems( this ) ) {
			MetaEnvServerNode node = item.envServerNode;
			ScopeState nodeState = new ScopeState( state , item );
			info( "install server=" + server.NAME + " node=" + node.POS + " ..." );
			executeNode( server , node , nodeState , baseItem );
		}
	}

	private void executeNode( MetaEnvServer server , MetaEnvServerNode node , ScopeState state , BaseItem baseItem ) throws Exception {
		if( baseItem.SERVERACCESS_TYPE != server.getServerAccessType() ) {
			String baseType = Common.getEnumLower( baseItem.SERVERACCESS_TYPE );
			String serverType = Common.getEnumLower( server.getServerAccessType() );
			exit2( _Error.BaseServerTypeMismatched2 , "base server type mismatched: " + baseType + " <> " + serverType , baseType , serverType );
		}
		
		// install dependencies
		EngineBase base = super.getEngineBase();
		for( String name : baseItem.getDepItemNames() ) {
			BaseItem depItem = base.getItem( name );
			executeNodeInstall( server , node , state , repo , depItem );
		}

		// install main
		executeNodeInstall( server , node , state , repo , baseItem );
	}

	private void executeNodeInstall( MetaEnvServer server , MetaEnvServerNode node , ScopeState state , BaseRepository repo , BaseItem baseItem ) throws Exception {
		if( !isExecute() )
			return;

		RedistStorage redist = artefactory.getRedistStorage( this , server , node );
		redist.recreateTmpFolder( this );
		RuntimeStorage runtime = artefactory.getRootRuntimeStorage( this , server , node , false );
		VersionInfoStorage vis = artefactory.getVersionInfoStorage( this , redist.account );

		if( !startUpdate( baseItem , runtime , vis ) )
			return;
			
		if( baseItem.isPackage() )
			executeNodeLinuxPackage( server , node , baseItem , redist , runtime );
		else
		if( baseItem.isArchiveLink() )
			executeNodeLinuxArchiveLink( server , node , baseItem , redist , runtime );
		else
		if( baseItem.isArchiveDirect() )
			executeNodeLinuxArchiveDirect( server , node , baseItem , redist , runtime );
		else
		if( baseItem.isNoDist() )
			executeNodeNoDist( server , node , baseItem , redist , runtime );
		else
		if( baseItem.isInstaller() )
			executeNodeInstaller( server , node , baseItem , redist , runtime );
		else
			exitUnexpectedState();
		
		// prepare
		if( baseItem.SERVERACCESS_TYPE != null ) {
			ServerProcess process = new ServerProcess( server , node , state );
			process.prepare( this );
		}
		
		finishUpdate( baseItem , redist , vis );
	}

	private void executeNodeLinuxPackage( MetaEnvServer server , MetaEnvServerNode node , BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		super.exitNotImplemented();
	}
	
	private void executeNodeLinuxArchiveLink( MetaEnvServer server , MetaEnvServerNode node , BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( server , baseItem );
		String redistPath = copyLocalToRedist( baseItem , localPath , redist );
		String runtimePath = baseItem.INSTALLPATH;
		
		extractArchiveFromRedist( baseItem , redistPath , runtimePath , runtime );
		linkNewBase( baseItem , runtime , runtimePath );
		copySystemFiles( baseItem , redist , runtime );
	}
	
	private void executeNodeLinuxArchiveDirect( MetaEnvServer server , MetaEnvServerNode node , BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		String localPath = copySourceToLocal( server , baseItem );
		String redistPath = copyLocalToRedist( baseItem , localPath , redist );
		String runtimePath = baseItem.INSTALLPATH;
		
		extractArchiveFromRedist( baseItem , redistPath , runtimePath , runtime );
		copySystemFiles( baseItem , redist , runtime );
	}

	private void executeNodeNoDist( MetaEnvServer server , MetaEnvServerNode node , BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		copySystemFiles( baseItem , redist , runtime );
	}
	
	private void executeNodeInstaller( MetaEnvServer server , MetaEnvServerNode node , BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		LocalFolder workBase = getSystemFiles( baseItem , redist.server , redist.node );
		String installerFile = copySourceToLocal( server , baseItem );
		RemoteFolder redistFolder = redist.getRedistTmpFolder( this );
		
		String redistFile = redistFolder.copyFileFromLocal( this , installerFile );
		
		if( baseItem.isArchive() )
			extractArchiveFromRedist( baseItem , redistFile , redistFolder.folderPath , runtime );
		
		redistFolder.copyDirContentFromLocal( this , workBase , "" );
		
		ShellExecutor shell = super.getShell( node );
		if( server.isLinux() )
			shell.customCheckErrorsDebug( this , redistFolder.folderPath , "chmod 777 server.*.sh" , Shell.WAIT_DEFAULT );
		
		// run installer
		String cmd = ( server.isLinux() )? "./server.prepare.sh" : "call server.prepare.cmd";
		shell.customCheckStatus( this , redistFolder.folderPath , cmd , Shell.WAIT_LONG );
	}
	
	private boolean startUpdate( BaseItem baseItem , RuntimeStorage runtime , VersionInfoStorage vis ) throws Exception {
		String STATUS = vis.getBaseStatus( this , baseItem.NAME );
		if( STATUS.equals( "ok" ) ) {
			if( !isForced() ) {
				info( "skip updating base=" + baseItem.NAME + ". Already installed." );
				return( false );
			}
		}

		String dowhat = ( STATUS.isEmpty() )? "install" : "reinstall";
		info( runtime.account.getPrintName() + ": " + dowhat + " base=" + baseItem.NAME + ", type=" + Common.getEnumLower( baseItem.BASESRC_TYPE ) + " ..." );
		vis.setBaseStatus( this , baseItem.NAME , "upgrading" );
		runtime.createRootPath( this );
		return( true );
	}

	private void finishUpdate( BaseItem baseItem , RedistStorage redist , VersionInfoStorage vis ) throws Exception {
		vis.setBaseStatus( this , baseItem.NAME , "ok" );
	}

	private String copySourceToLocal( MetaEnvServer server , BaseItem baseItem ) throws Exception {
		String localPath = null;
		if( baseItem.SRCFILE.startsWith( "http:" ) || baseItem.SRCFILE.startsWith( "https:" ) ) {
			LocalFolder folder = artefactory.getArtefactFolder( this , server.OS_TYPE , server.meta , "base" );
			String baseName = Common.getBaseName( baseItem.SRCFILE );
			String filePath = folder.getFilePath( this , baseName );
			shell.downloadUnix( this , baseItem.SRCFILE , filePath , "" );
			localPath = filePath;
		}
		else {
			// check absolute
			if( baseItem.SRCFILE.startsWith( "/" ) )
				localPath = baseItem.SRCFILE;
			else
				localPath = getItemPath( baseItem , baseItem.SRCFILE );
		}
		
		if( !shell.checkFileExists( this , localPath ) )
			exit1( _Error.UnableFindFile1 , "unable to find file: " + localPath , localPath );
		
		debug( "source local path: " + localPath );
		return( localPath );
	}

	private String copyLocalToRedist( BaseItem baseItem , String localPath , RedistStorage redist ) throws Exception {
		RemoteFolder folder = redist.getRedistTmpFolder( this );
		folder.copyFileFromLocal( this , localPath );
		
		String baseName = Common.getBaseName( localPath );
		String redistPath = folder.getFilePath( this , baseName );
		debug( "redist path: " + redistPath );
		return( redistPath );
	}
	
	private void extractArchiveFromRedist( BaseItem baseItem , String redistPath , String installPath , RuntimeStorage runtime ) throws Exception {
		if( baseItem.BASESRCFORMAT_TYPE == DBEnumBaseSrcFormatType.TARGZ_SINGLEDIR ) {
			runtime.extractBaseArchiveSingleDir( this , redistPath , baseItem.SRCFILEDIR , installPath , EnumArchiveType.TARGZ );
			debug( "runtime path: " + baseItem.INSTALLPATH );
		}

		if( baseItem.BASESRCFORMAT_TYPE == DBEnumBaseSrcFormatType.ZIP_SINGLEDIR ) {
			runtime.extractBaseArchiveSingleDir( this , redistPath , baseItem.SRCFILEDIR , installPath , EnumArchiveType.ZIP );
			debug( "runtime path: " + baseItem.INSTALLPATH );
		}

		exitUnexpectedState();
	}

	private void linkNewBase( BaseItem baseItem , RuntimeStorage runtime , String runtimePath ) throws Exception {
		runtime.createDirLink( this , baseItem.INSTALLLINK , runtimePath );
		debug( "link path: " + baseItem.INSTALLLINK );
	}

	private void copySystemFiles( BaseItem baseItem , RedistStorage redist , RuntimeStorage runtime ) throws Exception {
		if( baseItem.SERVERACCESS_TYPE == null )
			return;
		
		LocalFolder workBase = getSystemFiles( baseItem , redist.server , redist.node );
		
		// deploy
		if( baseItem.SERVERACCESS_TYPE == DBEnumServerAccessType.GENERIC )
			runtime.createBinPath( this );
		
		runtime.restoreSysConfigs( this , redist , workBase );
	}

	private LocalFolder getSystemFiles( BaseItem baseItem , MetaEnvServer server , MetaEnvServerNode node ) throws Exception {
		LocalFolder workBase = artefactory.getWorkFolder( this , "sysbase" );
		workBase.recreateThis( this );
		
		// copy system files from base
		RemoteFolder baseMaster = getFolder( baseItem );
		if( baseItem.SERVERACCESS_TYPE == DBEnumServerAccessType.SERVICE ) {
			if( !server.isLinux() )
				exitUnexpectedState();
			
			baseMaster.copyFileToLocalRename( this , workBase , "service" , server.SYSNAME );
		}
		else
		if( baseItem.SERVERACCESS_TYPE == DBEnumServerAccessType.GENERIC ) {
			if( server.isLinux() )
				baseMaster.copyFilesToLocal( this , workBase , "server.*.sh" );
			else
				baseMaster.copyFilesToLocal( this , workBase , "server.*.cmd" );
		}
		else {
			String value = Common.getEnumLower( baseItem.SERVERACCESS_TYPE );
			exit1( _Error.AccTypeNotForOperation1 , "access type (" + value + ") is not supported for operation" , value );
		}
		
		// configure
		ConfBuilder builder = new ConfBuilder( this , server.meta );
		
		Charset charset = Charset.forName( baseItem.CHARSET );
		if( charset == null )
			super.exit1( _Error.UnknownSystemFilesCharset1 , "unknown system files charset=" + baseItem.CHARSET , baseItem.CHARSET );
		
		builder.configureFolder( this , workBase , node , node.getProperties() , charset );
		return( workBase );
	}

	public RemoteFolder getFolder( BaseItem baseItem ) throws Exception {
		return( repo.getBaseFolder( this , baseItem.NAME ) );
	}

	public String getItemPath( BaseItem baseItem , String SRCFILE ) throws Exception {
		return( repo.getBaseItemPath( this , baseItem.NAME , SRCFILE ) );
	}

}
