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
		
		String F_CONFIGTARFILE = "config.tar";
		RemoteFolder runtimeDir = new RemoteFolder( artefactory , action.getAccount( node ) , F_RUNTIMEDIR );
		
		try {
			runtimeDir.createTarFromContent( action , tmpDir.getFilePath( action , F_CONFIGTARFILE ) , F_FILES , "" );
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

		dstFolder.extractTar( action , F_CONFIGTARFILE , "" );
		dstFolder.removeFiles( action , F_CONFIGTARFILE );
		tmpDir.removeFiles( action , F_CONFIGTARFILE );
		
		return( true );
	}

	public void tarRuntimeConfigItem( ActionBase action , MetaDistrConfItem confItem , String LOCATION , String filePath ) throws Exception {
		String F_INCLUDE = confItem.getLiveIncludeFiles( action );
		String F_EXCLUDE = confItem.getLiveExcludeFiles( action );
		
		RemoteFolder deployDir = getRuntimeLocationFolder( action , LOCATION );
		deployDir.createTarFromContent( action , filePath , F_INCLUDE , F_EXCLUDE );
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

		dstFolder.extractTar( action , S_CONFIGTARFILE , "" );
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

	public Folder getMirrorFolder( ActionBase action , boolean winBuild ) throws Exception {
		if( winBuild ) {
			RemoteFolder folder = getRedistFolder( action );
			if( action.meta.product.CONFIG_GITMIRRORPATHWIN.isEmpty() )
				action.exit( "missing configuraion parameter: CONFIG_GITMIRRORPATHWIN" );
			return( folder.getSubFolder( action , action.meta.product.CONFIG_GITMIRRORPATHWIN ) );
		}
		
		if( action.meta.product.CONFIG_GITMIRRORPATH.isEmpty() )
			action.exit( "missing configuraion parameter: CONFIG_GITMIRRORPATH" );
		
		return( artefactory.getAnyFolder( action , action.meta.product.CONFIG_GITMIRRORPATH ) );
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
		RemoteFolder F_DSTDIR_STATE = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		RemoteFolder F_DSTDIR_DEPLOY = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , true );

		action.debug( node.HOSTLOGIN + ": create redist location=" + LOCATION + " contenttype=" + Common.getEnumLower( CONTENTTYPE ) + " ..." );
		F_DSTDIR_STATE.ensureExists( action );
		F_DSTDIR_DEPLOY.ensureExists( action );
		
		if( action.context.CTX_BACKUP )
			createLocationBackup( action , RELEASEDIR , location , CONTENTTYPE );
	}

	public void createLocationBackup( ActionBase action , String RELEASEDIR , MetaEnvServerLocation location , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String LOCATION = location.DEPLOYPATH;
		RemoteFolder F_DSTDIR_DEPLOY = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , false );

		// create empty initial script
		F_DSTDIR_DEPLOY.ensureExists( action );
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

	public void copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , MetaEnvServerLocation location , String filePath , String deployBaseName , String RELEASEDIR , String RELEASEVER ) throws Exception {
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , true );
		createLocation( action , RELEASEDIR , location , CONTENTTYPE );
		RemoteFolder locationDir = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , true );

		String redistFileName = getDeployVersionedName( action , location , item , deployBaseName , RELEASEVER );
		locationDir.copyFileFromLocalRename( action , filePath , redistFileName );

		// create state file
		String runtimeName = getRedistBinaryFileDeployName( action , redistFileName );
		String data = RedistStateInfo.getValue( action , locationDir , redistFileName , deployBaseName , RELEASEVER , runtimeName );
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

	public void saveTmpArchiveItem( ActionBase action , String LOCATION , MetaDistrBinaryItem archiveItem , String tmpName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		RemoteFolder tmpFolder = getRedistTmpFolder( action );
		tmpFolder.ensureExists( action );
		saveArchiveItem( action , archiveItem , deployFolder , tmpName , tmpFolder );
	}
	
	public void copyTmpFileToLocal( ActionBase action , String tmpName , LocalFolder localFolder ) throws Exception {
		RemoteFolder tmpFolder = this.getRedistTmpFolder( action );
		tmpFolder.copyFileToLocal( action , localFolder , tmpName );
	}
	
	private boolean saveArchiveItem( ActionBase action , MetaDistrBinaryItem archiveItem , RemoteFolder deployFolder , String fileName , RemoteFolder saveFolder ) throws Exception {
		if( archiveItem.EXT.equals( ".tar.gz" ) == false && 
			archiveItem.EXT.equals( ".tgz" ) == false )
			action.exitNotImplemented();
		
		String tarFilePath = saveFolder.getFilePath( action , fileName );
		
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

	public FileInfo getRuntimeItemInfo( ActionBase action , MetaDistrBinaryItem binaryItem , String LOCATION , String specificDeployName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			String runtimeFile = deployFolder.findBinaryDistItemFile( action , binaryItem , specificDeployName );
			if( runtimeFile.isEmpty() )
				action.exit( "item=" + binaryItem.KEY + ", is not found in " + deployFolder.folderPath );
			
			String md5value = deployFolder.getFileMD5( action , runtimeFile );
			FileInfo info = binaryItem.getFileInfo( action , runtimeFile , specificDeployName , md5value );
			return( info );
		}
		
		String md5value = getArchiveMD5( action , binaryItem , deployFolder , true );
		FileInfo info = new FileInfo( "" , md5value , "" , "" );
		return( info );
	}

	public String getArchiveMD5( ActionBase action , MetaDistrBinaryItem binaryItem , RemoteFolder filesFolder , boolean runtimeFolder ) throws Exception {
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_CHILD ) {
			String content = "";
			String exclude = "";
			String prefix = binaryItem.DEPLOYBASENAME + "/";
			
			for( String s : Common.splitSpaced( binaryItem.FILES ) )
				content = Common.addItemToUniqueSpacedList( content , prefix + s );
			for( String s : Common.splitSpaced( binaryItem.EXCLUDE ) )
				exclude = Common.addItemToUniqueSpacedList( exclude , prefix + s );
			
			String md5value = filesFolder.getFilesMD5( action , content , exclude );
			return( md5value );
		}
		else
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT ) {
			String md5value = filesFolder.getFilesMD5( action , binaryItem.FILES , binaryItem.EXCLUDE );
			return( md5value );
		}
		else 
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			RemoteFolder archiveFolder = filesFolder;
			if( runtimeFolder )
				archiveFolder = filesFolder.getSubFolder( action , binaryItem.DEPLOYBASENAME );
			String md5value = archiveFolder.getFilesMD5( action , binaryItem.FILES , binaryItem.EXCLUDE );
			return( md5value );
		}

		action.exitUnexpectedState();
		return( null );
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
		else
		if( item.DISTTYPE == VarDISTITEMTYPE.DOTNETPKG )
			return( getRedistNupkgName( action , item ) );

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
