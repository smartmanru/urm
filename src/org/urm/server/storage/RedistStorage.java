package org.urm.server.storage;

import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.deploy.ServerDeployment;
import org.urm.server.dist.Dist;
import org.urm.server.meta.MetaDistrBinaryItem;
import org.urm.server.meta.MetaDistrConfItem;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerLocation;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.Metadata.VarARCHIVETYPE;
import org.urm.server.meta.Metadata.VarCONTENTTYPE;
import org.urm.server.meta.Metadata.VarDEPLOYTYPE;
import org.urm.server.meta.Metadata.VarDISTITEMTYPE;
import org.urm.server.meta.Metadata.VarITEMVERSION;
import org.urm.server.shell.Account;
import org.urm.server.shell.ShellExecutor;

public class RedistStorage extends ServerStorage {

	public RedistStorage( Artefactory artefactory , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		super( artefactory , account , server , node );
	}

	public void recreateTmpFolder( ActionBase action ) throws Exception {
		RemoteFolder tmp = getRedistTmpFolder( action );
		tmp.recreateThis( action );
	}
	
	public boolean getSysConfigs( ActionBase action , LocalFolder dstFolder ) throws Exception {
		String F_RUNTIMEDIR = server.getSystemPath( action );
		String F_FILES = server.getSystemFiles( action );

		RemoteFolder tmpDir = getRedistTmpFolder( action );
		
		String F_CONFIGTARFILE = "config.tar";
		RemoteFolder runtimeDir = new RemoteFolder( artefactory , action.getNodeAccount( node ) , F_RUNTIMEDIR );
		
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

	public RemoteFolder getRedistStateBaseFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		RemoteFolder folderBase = folder.getSubFolder( action , "base" );
		if( !folderBase.checkExists( action ) ) {
			Account accountRoot = account.getRootAccount( action );
			ShellExecutor remoteSession = action.getShell( accountRoot );
			remoteSession.createPublicDir( action , folderBase.folderPath );
		}
		
		RemoteFolder folderState = folderBase.getSubFolder( action , account.USER );
		folderState.ensureExists( action );
		return( folderState );
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
		action.info( folder.account.HOSTLOGIN + ": drop all release data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropAll( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistHostRootFolder( action );
		action.info( folder.account.HOSTLOGIN + ": drop redist completely at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public Folder getMirrorFolder( ActionBase action , boolean winBuild ) throws Exception {
		if( action.meta.product.CONFIG_GITMIRRORPATH.isEmpty() )
			action.exit( "missing configuraion parameter: CONFIG_GITMIRRORPATH" );
		
		return( artefactory.getAnyFolder( action , action.meta.product.CONFIG_GITMIRRORPATH ) );
	}
	
	public void dropStateData( ActionBase action ) throws Exception {
		RemoteFolder folder = getStateFolder( action );
		action.info( folder.account.HOSTLOGIN + ": drop state data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropReleaseData( ActionBase action , String RELEASEDIR ) throws Exception {
		RemoteFolder folder = getReleaseFolder( action , RELEASEDIR );
		action.info( folder.account.HOSTLOGIN + ": drop release=" + RELEASEDIR + " at " + folder.folderPath + " ..." );
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
		state.gather( action , node , CONTENTTYPE , F_DSTDIR_STATE );
		return( state );
	}

	public boolean copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , Dist dist , MetaEnvServerLocation location , String fileName , String deployBaseName , RedistStateInfo stateInfo ) throws Exception {
		// primary file
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , true );
		RemoteFolder locationDir = getRedistLocationFolder( action , dist.RELEASEDIR , LOCATION , CONTENTTYPE , true );
		String redistFileName = FileInfo.getFileName( action , item );  

		String runtimeName = "";
		if( !item.isArchive( action ) )
			runtimeName = getDeployVersionedName( action , location , item , deployBaseName , dist.release.RELEASEVER );
		
		// check need redist - from distributive
		if( !stateInfo.needUpdate( action , item , dist , fileName , deployBaseName , runtimeName ) )
			return( false );
		
		// copy and save state info
		action.debug( "copy dist file " + fileName + " to " + redistFileName + " ..." );
		dist.copyDistItemToTarget( action , item , fileName , locationDir , redistFileName );

		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , deployBaseName , dist.release.RELEASEVER , runtimeName );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
		return( true );
	}

	public boolean copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , MetaEnvServerLocation location , String filePath , String deployBaseName , String RELEASEDIR , String RELEASEVER , RedistStateInfo stateInfo ) throws Exception {
		// primary file
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , true );
		RemoteFolder locationDir = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , true );
		String redistFileName = FileInfo.getFileName( action , item );
		String deployFinalName = getDeployVersionedName( action , location , item , deployBaseName , RELEASEVER );

		// check need redist - extracted file
		if( !stateInfo.needUpdate( action , item , filePath , deployBaseName , RELEASEVER , deployFinalName ) )
			return( false );
		
		// copy and save state info
		action.debug( "copy extracted file " + filePath + " to " + redistFileName + " ..." );
		locationDir.copyFileFromLocalRename( action , filePath , redistFileName );

		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , deployBaseName , RELEASEVER , deployFinalName );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
		return( true );
	}

	public void copyReleaseFile( ActionBase action , MetaDistrConfItem item , Dist dist , MetaEnvServerLocation location , LocalFolder srcFolder , String configTarFile , boolean partial ) throws Exception {
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , false );
		RemoteFolder locationDir = getRedistLocationFolder( action , dist.RELEASEDIR , LOCATION , CONTENTTYPE , true );
		String path = srcFolder.getFilePath( action , configTarFile );
		String redistFileName = FileInfo.getFileName( action , item );
		createLocation( action , dist.RELEASEDIR , location , CONTENTTYPE );
		locationDir.copyFileFromLocalRename( action , path , FileInfo.getFileName( action , item ) );

		// create state file
		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , dist.release.RELEASEVER , partial );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
	}

	public void restoreConfigFile( ActionBase action , MetaDistrConfItem confItem , MetaEnvServerLocation location , String redistPath , String version ) throws Exception {
		String fileBaseName = FileInfo.getFileName( action , confItem );
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( action , false );
		RemoteFolder locationDir = getStateLocationFolder( action , location.DEPLOYPATH , CONTENTTYPE );
		locationDir.ensureExists( action );
		locationDir.copyFileRename( action , redistPath , fileBaseName );
		
		// create state file
		FileInfo data = RedistStateInfo.getFileInfo( action , confItem , locationDir , fileBaseName , version , false );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
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
		if( releaseFolder.checkExists( action ) )
			deployment.getFromRedistReleaseFolder( action , this , releaseFolder );
		return( deployment );
	}

	public void backupRedistItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , FileInfo stateFile ) throws Exception {
		RemoteFolder backupFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , false );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		
		if( redistFile.confItem != null )
			backupRedistConfItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
		else
		if( redistFile.binaryItem != null ) {
			if( redistFile.binaryItem.isArchive( action ) )
				backupRedistArchiveItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
			else
				backupRedistBinaryItem( action , RELEASEDIR , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
		}
		else
			action.exitUnexpectedState();
	}

	public void backupRedistConfItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
		String redistBackupFile = redistFile.getFileName( action );  
		String filePath = backupFolder.getFilePath( action , redistBackupFile );
		tarRuntimeConfigItem( action , redistFile.confItem , LOCATION , filePath );
		action.info( "redist backup done, config item file=" + redistFile.getFileName( action ) );
		
		// copy version file from state
		String md5 = backupFolder.getFileMD5( action , redistBackupFile );
		String version = ( stateFile == null )? "unknown" : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.confItem , version , md5 , false );
		String stateVerName = newInfo.getInfoName( action );
		backupFolder.createFileFromString( action , stateVerName , newInfo.value( action ) );
	}

	public void backupRedistBinaryItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		String runtimeFile = deployFolder.findBinaryDistItemFile( action , redistFile.binaryItem , redistFile.deployBaseName );
		
		if( runtimeFile.isEmpty() ) {
			action.debug( "unable to backup, item=" + redistFile.itemName + ", not found in " + deployFolder.folderPath );
			return;
		}
		
		String redistBackupFile = redistFile.getFileName( action );  
		backupFolder.copyFile( action , deployFolder , runtimeFile , redistBackupFile );
		action.info( "redist backup done, binary item file=" + redistBackupFile );
		
		// create backup state
		String md5 = backupFolder.getFileMD5( action , redistBackupFile );
		String version = ( stateFile == null )? "unknown" : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.binaryItem , version , md5 , redistFile.deployBaseName , runtimeFile );
		String stateVerName = newInfo.getInfoName( action );
		backupFolder.createFileFromString( action , stateVerName , newInfo.value( action ) );
	}
	
	public void backupRedistArchiveItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );

		String redistBackupFile = redistFile.getFileName( action );
		int timeout = action.setTimeoutUnlimited();
		saveArchiveItem( action , redistFile.binaryItem , deployFolder , redistBackupFile , backupFolder );
		action.setTimeout( timeout );
		action.info( "redist backup done, archive item file=" + redistBackupFile );
		
		// copy version file from state
		String md5 = backupFolder.getFileMD5( action , redistBackupFile );
		String version = ( stateFile == null )? "unknown" : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.binaryItem , version , md5 , redistFile.deployBaseName , "" );
		String stateVerName = redistFile.getInfoName( action );
		backupFolder.createFileFromString( action , stateVerName , newInfo.value( action ) );
	}

	public void saveTmpArchiveItem( ActionBase action , String LOCATION , MetaDistrBinaryItem archiveItem , String tmpName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		RemoteFolder tmpFolder = getRedistTmpFolder( action );
		
		int timeout = action.setTimeoutUnlimited();
		tmpFolder.removeFiles( action , tmpName );
		saveArchiveItem( action , archiveItem , deployFolder , tmpName , tmpFolder );
		action.setTimeout( timeout );
	}
	
	public void copyTmpFileToLocal( ActionBase action , String tmpName , LocalFolder localFolder ) throws Exception {
		RemoteFolder tmpFolder = getRedistTmpFolder( action );
		tmpFolder.copyFileToLocal( action , localFolder , tmpName );
		tmpFolder.removeFiles( action , tmpName );
	}
	
	private boolean saveArchiveItem( ActionBase action , MetaDistrBinaryItem archiveItem , RemoteFolder deployFolder , String fileName , RemoteFolder saveFolder ) throws Exception {
		VarARCHIVETYPE atype = archiveItem.getArchiveType( action );
		String archiveFilePath = saveFolder.getFilePath( action , fileName );
		
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

			deployFolder.createArchiveFromContent( action , atype , archiveFilePath , content , exclude );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_DIRECT ) {
			if( !deployFolder.checkExists( action ) ) {
				action.debug( "unable to find runtime direct archive item=" + archiveItem.KEY + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createArchiveFromContent( action , atype , archiveFilePath , archiveItem.FILES , archiveItem.EXCLUDE );
		}
		else
		if( archiveItem.DISTTYPE == VarDISTITEMTYPE.ARCHIVE_SUBDIR ) {
			if( !deployFolder.checkFolderExists( action , archiveItem.DEPLOYBASENAME ) ) {
				action.debug( "unable to find runtime subdir archive item=" + archiveItem.KEY + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createArchiveFromFolderContent( action , atype , archiveFilePath , archiveItem.DEPLOYBASENAME , archiveItem.FILES , archiveItem.EXCLUDE );
		}
		else
			action.exitUnexpectedState();
		
		return( true );
	}

	public FileInfo getRuntimeItemInfo( ActionBase action , MetaDistrBinaryItem binaryItem , String LOCATION , String specificDeployBaseName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );
		
		if( binaryItem.DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			String runtimeFile = deployFolder.findBinaryDistItemFile( action , binaryItem , specificDeployBaseName );
			if( runtimeFile.isEmpty() )
				action.exit( "item=" + binaryItem.KEY + ", is not found in " + deployFolder.folderPath );
			
			String md5value = deployFolder.getFileMD5( action , runtimeFile );
			FileInfo info = binaryItem.getFileInfo( action , runtimeFile , specificDeployBaseName , md5value );
			return( info );
		}
		
		String md5value = getArchiveMD5( action , binaryItem , deployFolder , true );
		FileInfo info = new FileInfo( binaryItem , "" , md5value , "" , "" );
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
	
	public void changeStateItem( ActionBase action , String RELEASEDIR , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , boolean rollout ) throws Exception {
		String stateFileName = redistFile.getFileName( action );
		String stateInfoName = redistFile.getInfoName( action );
		
		RemoteFolder redistFolder = getRedistLocationFolder( action , RELEASEDIR , LOCATION , CONTENTTYPE , rollout );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		
		stateFolder.removeFiles( action , stateFileName + " " + stateInfoName );
		stateFolder.copyFile( action , redistFolder , stateFileName , stateFileName );

		// copy version file from source folder to state folder
		if( redistFolder.checkFileExists( action , stateInfoName ) )
			stateFolder.copyFile( action , redistFolder , stateInfoName , "" );
	}

	public String getDeployVersionedName( ActionBase action , MetaEnvServerLocation location , MetaDistrBinaryItem item , String deployBaseName , String RELEASEVER ) throws Exception {
		if( item.DISTTYPE == VarDISTITEMTYPE.BINARY ) {
			if( location.DEPLOYTYPE == VarDEPLOYTYPE.LINKS_MULTIDIR ||
				location.DEPLOYTYPE == VarDEPLOYTYPE.LINKS_SINGLEDIR ) {
				String deployName = deployBaseName + item.EXT;
				return( deployName );
			}
			
			String deployName = getVersionItem( action , item , deployBaseName , RELEASEVER );
			return( deployName );
		}

		action.exitUnexpectedState();
		return( null );
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
