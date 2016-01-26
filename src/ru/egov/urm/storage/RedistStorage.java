package ru.egov.urm.storage;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarCONTENTTYPE;
import ru.egov.urm.meta.Metadata.VarDEPLOYTYPE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.meta.Metadata.VarITEMVERSION;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.deploy.ServerDeployment;
import ru.egov.urm.shell.Account;
import ru.egov.urm.shell.ShellExecutor;

public class RedistStorage extends ServerStorage {

	public RedistStorage( Artefactory artefactory , String type , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		super( artefactory , type , account , server , node );
	}

	public boolean getSysConfigs( ActionBase action , LocalFolder dstFolder ) throws Exception {
		String F_RUNTIMEDIR;
		String F_FILES;
		
		if( server.TYPE == VarSERVERTYPE.SERVICE ) {
			F_RUNTIMEDIR = "/etc/init.d";
			F_FILES = server.SERVICENAME;
		}
		else {
			F_RUNTIMEDIR = Common.getPath( server.ROOTPATH , server.BINPATH );
			F_FILES = "server.*.sh";
		}

		RemoteFolder tmpDir = getRedistTmpFolder( action );
		tmpDir.recreateThis( action );
		
		String F_CONFIGTARFILE = "config.tgz";
		RemoteFolder runtimeDir = new RemoteFolder( artefactory , action.getAccount( node ) , F_RUNTIMEDIR );
		
		try {
			runtimeDir.createTarGzFromContent( action , tmpDir.getFilePath( action , F_CONFIGTARFILE ) , F_FILES , "" );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}

		// create destination directory and download files
		// upload to destination
		tmpDir.copyFileToLocal( action , dstFolder , F_CONFIGTARFILE , "" );

		if( !dstFolder.checkFileExists( action , F_CONFIGTARFILE ) )
			action.exit( "unable to create " + Common.getPath( dstFolder.folderPath , F_CONFIGTARFILE ) );

		dstFolder.extractTarGz( action , F_CONFIGTARFILE , "" );
		dstFolder.removeFiles( action , F_CONFIGTARFILE );
		tmpDir.removeFiles( action , F_CONFIGTARFILE );
		
		return( true );
	}

	public void tarRuntimeConfigItem( ActionBase action , MetaDistrConfItem confItem , String LOCATION , String filePath ) throws Exception {
		String F_INCLUDE = confItem.getLiveIncludeFiles( action );
		String F_EXCLUDE = confItem.getLiveExcludeFiles( action );
		
		RemoteFolder deployDir = getRuntimeLocationFolder( action , LOCATION );
		deployDir.createTarGzFromContent( action , filePath , F_INCLUDE , F_EXCLUDE );
	}

	public String getConfigItemMD5( ActionBase action , MetaDistrConfItem confItem , String LOCATION ) throws Exception {
		String F_INCLUDE = confItem.getLiveIncludeFiles( action );
		String F_EXCLUDE = confItem.getLiveExcludeFiles( action );
		
		RemoteFolder deployDir = getRuntimeLocationFolder( action , LOCATION );
		return( deployDir.getFilesMD5( action , F_INCLUDE , F_EXCLUDE ) );
	}
	
	public boolean getConfigItem( ActionBase action , LocalFolder dstFolder , MetaDistrConfItem confItem , String LOCATION ) throws Exception {
		RemoteFolder tmpDir = getRedistTmpFolder( action );
		tmpDir.recreateThis( action );
		String tarPath = tmpDir.getFilePath( action , S_CONFIGTARFILE );
		
		try {
			tarRuntimeConfigItem( action , confItem , LOCATION , tarPath );
		}
		catch( Throwable e ) {
			action.log( e );
			return( false );
		}
		
		// create destination directory and download files
		// upload to destination
		tmpDir.copyFileToLocal( action , dstFolder , S_CONFIGTARFILE , "" );

		if( !dstFolder.checkFileExists( action , S_CONFIGTARFILE ) )
			action.exit( "unable to create " + Common.getPath( dstFolder.folderPath , S_CONFIGTARFILE ) );

		dstFolder.extractTarGz( action , S_CONFIGTARFILE , "" );
		dstFolder.removeFiles( action , S_CONFIGTARFILE );
		tmpDir.removeFiles( action , S_CONFIGTARFILE );
		
		return( true );
	}

	public RemoteFolder getRedistReleaseFolder( ActionBase action , String RELEASEDIR ) throws Exception {
		return( getReleaseFolder( action , RELEASEDIR ) );
	}
	
	public void recreateReleaseFolder( ActionBase action , String RELEASEDIR ) throws Exception {
		RemoteFolder folder = getReleaseFolder( action , RELEASEDIR );
		folder.ensureExists( action );
		folder.removeContent( action );
	}
	
	public void dropReleaseAll( ActionBase action ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		action.log( folder.account.HOSTLOGIN + ": drop all release data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropAll( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistHostRootFolder( action );
		action.log( folder.account.HOSTLOGIN + ": drop redist completely at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropStateData( ActionBase action ) throws Exception {
		RemoteFolder folder = getStateFolder( action );
		action.log( folder.account.HOSTLOGIN + ": drop state data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropReleaseData( ActionBase action , String RELEASEDIR ) throws Exception {
		RemoteFolder folder = getReleaseFolder( action , RELEASEDIR );
		action.log( folder.account.HOSTLOGIN + ": drop release=" + RELEASEDIR + " at " + folder.folderPath + " ..." );
		folder.recreateThis( action );
	}

	public void createLocation( ActionBase action , String RELEASEDIR , MetaEnvServerLocation location , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String LOCATION = location.DEPLOYPATH;
		String F_DSTDIR_STATE = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		String F_DSTDIR_DEPLOY = getPathRedistLocation( action , RELEASEDIR , LOCATION , CONTENTTYPE , true );

		action.debug( node.HOSTLOGIN + ": create redist location=" + LOCATION + " contenttype=" + Common.getEnumLower( CONTENTTYPE ) + " ..." );
		ShellExecutor shell = action.getShell( node );
		
		String cmd = "mkdir -p " + F_DSTDIR_STATE + " " + F_DSTDIR_DEPLOY;
		shell.customCheckErrorsDebug( action , cmd );
		
		if( action.context.BACKUP )
			createLocationBackup( action , RELEASEDIR , location , CONTENTTYPE );
	}

	public void createLocationBackup( ActionBase action , String RELEASEDIR , MetaEnvServerLocation location , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String LOCATION = location.DEPLOYPATH;
		String F_DSTDIR_BACKUP = getPathRedistLocation( action , RELEASEDIR , LOCATION , CONTENTTYPE , false );

		// create empty initial script
		ShellExecutor shell = action.getShell( node );
		
		String cmd = "mkdir -p " + F_DSTDIR_BACKUP;
		shell.customCheckErrorsDebug( action , cmd );
	}
	
	public RedistStateInfo getStateInfo( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String F_DSTDIR_STATE = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		RedistStateInfo state = new RedistStateInfo();
		state.gather( action , node , F_DSTDIR_STATE );
		return( state );
	}

	public void copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , DistStorage dist , MetaEnvServerLocation location , String fileName , String deployBaseName ) throws Exception {
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , true );
		createLocation( action , dist.RELEASEDIR , location , CONTENTTYPE );
		RemoteFolder locationDir = getRedistLocationFolder( action , dist.RELEASEDIR , LOCATION , CONTENTTYPE , true );

		String redistFileName = getDeployVersionedName( action , location , item , deployBaseName , dist.info.RELEASEVER );  
		dist.copyDistItemToTarget( action , item , fileName , locationDir , redistFileName );

		// create state file
		String runtimeName = getRedistBinaryFileDeployName( action , redistFileName );
		String data = RedistStateInfo.getValue( action , locationDir , redistFileName , deployBaseName , dist.info.RELEASEVER , runtimeName );
		String stateBaseName = super.getStateBaseName( action , CONTENTTYPE , redistFileName );
		String verName = super.getStateInfoName( action , stateBaseName );
		locationDir.createFileFromString( action , verName , data );
	}

	public void copyReleaseFile( ActionBase action , MetaDistrConfItem item , DistStorage dist , MetaEnvServerLocation location , LocalFolder srcFolder , String fileName , String deployBaseName ) throws Exception {
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , false );
		createLocation( action , dist.RELEASEDIR , location , CONTENTTYPE );
		RemoteFolder locationDir = getRedistLocationFolder( action , dist.RELEASEDIR , LOCATION , CONTENTTYPE , true );
		
		String path = srcFolder.getFilePath( action , fileName );
		String redistFileName = deployBaseName;
		locationDir.copyFileFromLocalRename( action , path , deployBaseName );

		// create state file
		String data = RedistStateInfo.getValue( action , locationDir , redistFileName , deployBaseName , dist.info.RELEASEVER , "ignore" );
		String stateBaseName = super.getStateBaseName( action , CONTENTTYPE , redistFileName );
		String verName = super.getStateInfoName( action , stateBaseName );
		locationDir.createFileFromString( action , verName , data );
	}

	public void restoreConfigFile( ActionBase action , MetaDistrConfItem confItem , MetaEnvServerLocation location , String redistPath , String version ) throws Exception {
		String fileBaseName = getConfigArchiveName( action , confItem , true );
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , false );
		RemoteFolder locationDir = getStateLocationFolder( action , location.DEPLOYPATH , CONTENTTYPE );
		locationDir.ensureExists( action );
		locationDir.copyFileRename( action , redistPath , fileBaseName );
		
		// create state file
		String data = RedistStateInfo.getValue( action , locationDir , fileBaseName , fileBaseName , version , "ignore" );
		String stateBaseName = super.getStateBaseName( action , CONTENTTYPE , fileBaseName );
		String verName = super.getStateInfoName( action , stateBaseName );
		locationDir.createFileFromString( action , verName , data );
	}
	
	public String[] getRedistReleases( ActionBase action ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		if( !folder.checkExists( action ) )
			return( null );
		
		List<String> releases = folder.getTopDirs( action );
		return( releases.toArray( new String[0] ) );
	}

	public String[] getReleaseLocations( ActionBase action , String release ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		if( !folder.checkExists( action ) )
			return( null );
		
		String[] folders = folder.getFolders( action );
		return( folders );
	}

	public ServerDeployment getDeployment( ActionBase action , String RELEASEDIR ) throws Exception {
		RemoteFolder releaseFolder = getRedistReleaseFolder( action , RELEASEDIR );
		ServerDeployment deployment = new ServerDeployment();
		deployment.getFromRedistReleaseFolder( action , this , releaseFolder );
		return( deployment );
	}

	public void backupRedistItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String redistFile ) throws Exception {
		RemoteFolder backupFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , false );
		RedistFileType rft = getRedistFileType( action , redistFile );
		if( rft == RedistFileType.CONFCOMP )
			backupRedistConfItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder );
		else
		if( rft == RedistFileType.BINARY )
			backupRedistBinaryItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder );
		else
		if( rft == RedistFileType.ARCHIVE )
			backupRedistArchiveItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder);
		else
			action.exitUnexpectedState();
		
		// copy version file from state
		String stateBaseName = getStateBaseName( action , CONTENTTYPE , redistFile );
		String stateVerName = getStateInfoName( action , stateBaseName );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		if( stateFolder.checkFileExists( action , stateVerName ) )
			backupFolder.copyFile( action , stateFolder , stateVerName , "" );
	}

	public void backupRedistConfItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String redistFile , RemoteFolder backupFolder ) throws Exception {
		MetaDistrConfItem confItem = getRedistFileConfComp( action , redistFile );
		
		String filePath = backupFolder.getFilePath( action , redistFile );
		tarRuntimeConfigItem( action , confItem , LOCATION , filePath );
		action.log( "backup done, item path=" + backupFolder.folderPath + ", file=" + redistFile );
	}

	public void backupRedistBinaryItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String redistFile , RemoteFolder backupFolder ) throws Exception {
		MetaDistrBinaryItem binaryItem = getRedistFileBinaryItem( action , redistFile );
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		String deployName = getRedistBinaryFileDeployName( action , redistFile );
		
		String runtimeFile = deployFolder.findBinaryDistItemFile( action , binaryItem , deployName );
		
		if( runtimeFile.isEmpty() ) {
			action.debug( "unable to backup, item=" + binaryItem.KEY + ", not found in " + deployFolder.folderPath );
			return;
		}
		
		String redistBackupFile = getRedistBinaryName( action , binaryItem , runtimeFile );  
		backupFolder.copyFile( action , deployFolder , runtimeFile , redistBackupFile );
		action.log( "redist backup done, item file=" + redistBackupFile );
	}
	
	public void backupRedistArchiveItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String redistFile , RemoteFolder backupFolder ) throws Exception {
		MetaDistrBinaryItem archiveItem = getRedistFileArchiveItem( action , redistFile );
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );

		saveArchiveItem( action , archiveItem , deployFolder , redistFile , backupFolder );
		action.log( "redist backup done, item file=" + redistFile );
	}
	
	private boolean saveArchiveItem( ActionBase action , MetaDistrBinaryItem archiveItem , RemoteFolder deployFolder , String redistFile , RemoteFolder backupFolder ) throws Exception {
		if( archiveItem.EXT.equals( ".tar.gz" ) == false && 
			archiveItem.EXT.equals( ".tgz" ) == false )
			action.exitNotImplemented();
		
		String tarFilePath = backupFolder.getFilePath( action , redistFile );
		
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD ) {
			if( !deployFolder.checkFolderExists( action , archiveItem.DEPLOYBASENAME ) ) {
				action.debug( "unable to find runtime child archive item=" + archiveItem.KEY + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			String content = "";
			String exclude = "";
			String prefix = archiveItem.DEPLOYBASENAME + "/";
			
			for( String s : Common.splitSpaced( archiveItem.FILES ) )
				content = Common.addItemToUniqueSpacedList( content , prefix + s );
			for( String s : Common.splitSpaced( archiveItem.EXCLUDE ) )
				exclude = Common.addItemToUniqueSpacedList( exclude , prefix + s );

			deployFolder.createTarGzFromContent( action , tarFilePath , content , exclude );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT ) {
			if( !deployFolder.checkExists( action ) ) {
				action.debug( "unable to find runtime direct archive item=" + archiveItem.KEY + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createTarGzFromContent( action , tarFilePath , archiveItem.FILES , archiveItem.EXCLUDE );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			if( !deployFolder.checkFolderExists( action , archiveItem.DEPLOYBASENAME ) ) {
				action.debug( "unable to find runtime subdir archive item=" + archiveItem.KEY + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createTarGzFromFolderContent( action , tarFilePath , archiveItem.DEPLOYBASENAME , archiveItem.FILES , archiveItem.EXCLUDE );
		}
		else
			action.exitUnexpectedState();
		
		return( true );
	}

	public FileInfo getItemInfo( ActionBase action , MetaDistrBinaryItem binaryItem , String LOCATION , String specificDeployName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			String runtimeFile = deployFolder.findBinaryDistItemFile( action , binaryItem , specificDeployName );
			if( runtimeFile.isEmpty() )
				action.exit( "item=" + binaryItem.KEY + ", is not found in " + deployFolder.folderPath );
			
			String md5value = deployFolder.getFileMD5( action , runtimeFile );
			FileInfo info = binaryItem.getFileInfo( action , runtimeFile , specificDeployName , md5value );
			return( info );
		}
		else 
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD || 
			binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT ||
			binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			String md5value = getArchiveFileMD5( action , binaryItem , deployFolder );
			FileInfo info = new FileInfo( "" , md5value , "" , "" );
			return( info );
		}

		action.exitUnexpectedState();
		return( null );
	}
	
	private String getArchiveFileMD5( ActionBase action , MetaDistrBinaryItem archiveItem , RemoteFolder deployFolder ) throws Exception {
		RedistStorage redist = artefactory.getRedistStorage( action , server , node );
		RemoteFolder tmpFolder = redist.getRedistTmpFolder( action );
		tmpFolder.ensureExists( action );
		
		String fileName = "tmp.tag.gz";
		saveArchiveItem( action , archiveItem , deployFolder , fileName , tmpFolder );
		
		return( tmpFolder.getFileMD5( action , fileName ) );
	}
	
	public void changeStateItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , String redistFile , boolean rollout ) throws Exception {
		RemoteFolder redistFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		String stateBaseName = getStateBaseName( action , CONTENTTYPE , redistFile );
		String stateFileName = getStateFileName( action , stateBaseName );
		String stateInfoName = getStateInfoName( action , stateBaseName );
		
		stateFolder.removeFiles( action , stateFileName + " " + stateInfoName );
		stateFolder.copyFile( action , redistFolder , redistFile , stateFileName );

		// copy version file from source folder to state folder
		if( redistFolder.checkFileExists( action , stateInfoName ) )
			stateFolder.copyFile( action , redistFolder , stateInfoName , "" );
	}
	
	public String getDeployVersionedName( ActionBase action , MetaEnvServerLocation location , MetaDistrBinaryItem item , String deployBaseName , String RELEASEVER ) throws Exception {
		if( item.DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			if( location.DEPLOYTYPE == VarDEPLOYTYPE.LINKS_MULTIDIR ||
				location.DEPLOYTYPE == VarDEPLOYTYPE.LINKS_SINGLEDIR ) {
				String redistName = getRedistBinaryName( action , item , deployBaseName + item.EXT );
				return( redistName );
			}
			
			String name = getVersionItem( action , item , deployBaseName , RELEASEVER );
			String redistName = getRedistBinaryName( action , item , name );
			return( redistName );
		}

		return( getRedistArchiveName( action , item ) ); 
	}

	private String getVersionItem( ActionBase action , MetaDistrBinaryItem item , String deployBaseName , String version ) throws Exception {
		if( item.DEPLOYVERSION == VarITEMVERSION.NONE || item.DEPLOYVERSION == VarITEMVERSION.IGNORE )
			return( deployBaseName + item.EXT );

		if( item.DEPLOYVERSION == VarITEMVERSION.PREFIX )
			return( version + "-" + deployBaseName + item.EXT );

		if( item.DEPLOYVERSION == VarITEMVERSION.MIDDASH )
			return( deployBaseName + "-" + version + item.EXT );

		if( item.DEPLOYVERSION == VarITEMVERSION.MIDPOUND )
			return( deployBaseName + "##" + version + item.EXT );

		action.exit( "getVersionItem: unknown version type=" + Common.getEnumLower( item.DEPLOYVERSION ) );
		return( null );
	}
	
}
