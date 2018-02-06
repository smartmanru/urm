package org.urm.engine.storage;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.deploy.ServerDeployment;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.DBEnumDeployModeType;
import org.urm.db.core.DBEnums.DBEnumDeployVersionType;
import org.urm.db.core.DBEnums.DBEnumDistItemType;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellExecutor;
import org.urm.meta.Types;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerLocation;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.Types.*;

public class RedistStorage extends ServerStorage {

	public RedistStorage( Artefactory artefactory , Account account ) {
		super( artefactory , account );
		this.meta = null;
	}

	public RedistStorage( Artefactory artefactory , Account account , MetaEnvServer server , MetaEnvServerNode node ) {
		super( artefactory , account , server , node );
		this.meta = server.meta;
	}

	public void recreateTmpFolder( ActionBase action ) throws Exception {
		RemoteFolder tmp = getRedistTmpFolder( action );
		tmp.ensureExists( action );
	}
	
	public boolean getSysConfigs( ActionBase action , LocalFolder dstFolder ) throws Exception {
		ShellExecutor shell = action.getShell( node );
		String F_RUNTIMEDIR = shell.getSystemPath( action , server );
		String F_FILES = shell.getSystemFiles( action , server );

		RemoteFolder tmpDir = getRedistTmpFolder( action );
		
		String F_CONFIGTARFILE = "config.tar";
		RemoteFolder runtimeDir = new RemoteFolder( action.getNodeAccount( node ) , F_RUNTIMEDIR );
		
		try {
			runtimeDir.createTarFromContent( action , tmpDir.getFilePath( action , F_CONFIGTARFILE ) , F_FILES , "" );
		}
		catch( Throwable e ) {
			action.handle( e );
			return( false );
		}

		// create destination directory and download files
		// upload to destination
		tmpDir.copyFileToLocal( action , dstFolder , F_CONFIGTARFILE , "" );

		if( !dstFolder.checkFileExists( action , F_CONFIGTARFILE ) ) {
			String path = Common.getPath( dstFolder.folderPath , F_CONFIGTARFILE );
			action.exit1( _Error.UnableCreateConfigTar1 , "unable to create " + path , path );
		}

		dstFolder.extractTar( action , F_CONFIGTARFILE , "" );
		dstFolder.removeFiles( action , F_CONFIGTARFILE );
		tmpDir.removeFiles( action , F_CONFIGTARFILE );
		
		return( true );
	}

	public void tarRuntimeConfigItem( ActionBase action , MetaDistrConfItem confItem , String LOCATION , String filePath ) throws Exception {
		String F_INCLUDE = confItem.getLiveIncludeFiles();
		String F_EXCLUDE = confItem.getLiveExcludeFiles();
		
		RemoteFolder deployDir = getRuntimeLocationFolder( action , LOCATION );
		deployDir.createTarFromContent( action , filePath , F_INCLUDE , F_EXCLUDE );
	}

	public String getConfigItemMD5( ActionBase action , MetaDistrConfItem confItem , String LOCATION ) throws Exception {
		String F_INCLUDE = confItem.getLiveIncludeFiles();
		String F_EXCLUDE = confItem.getLiveExcludeFiles();
		
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
			action.handle( e );
			return( false );
		}
		
		// create destination directory and download files
		// upload to destination
		tmpDir.copyFileToLocal( action , dstFolder , S_CONFIGTARFILE , "" );

		if( !dstFolder.checkFileExists( action , S_CONFIGTARFILE ) ) {
			String path = Common.getPath( dstFolder.folderPath , S_CONFIGTARFILE );
			action.exit1( _Error.UnableCreateConfigTar1 , "unable to create " + path , path );
		}

		dstFolder.extractTar( action , S_CONFIGTARFILE , "" );
		dstFolder.removeFiles( action , S_CONFIGTARFILE );
		tmpDir.removeFiles( action , S_CONFIGTARFILE );
		
		return( true );
	}

	public RemoteFolder getRedistStateBaseFolder( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistFolder( action );
		RemoteFolder folderBase = folder.getSubFolder( action , "base" );
		if( !folderBase.checkExists( action ) ) {
			Account accountRoot = account.getRootAccount();
			ShellExecutor remoteSession = action.getShell( accountRoot );
			remoteSession.createPublicDir( action , folderBase.folderPath );
		}
		
		RemoteFolder folderState = folderBase.getSubFolder( action , account.USER );
		folderState.ensureExists( action );
		return( folderState );
	}
	
	public RemoteFolder getRedistReleaseFolder( ActionBase action , VersionInfo version ) throws Exception {
		return( getReleaseFolder( action , version ) );
	}
	
	public void recreateReleaseFolder( ActionBase action , VersionInfo version ) throws Exception {
		RemoteFolder folder = getReleaseFolder( action , version );
		folder.ensureExists( action );
		folder.removeContent( action );
	}
	
	public void dropReleaseAll( ActionBase action ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		action.info( folder.account.getPrintName() + ": drop all release data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropAll( ActionBase action ) throws Exception {
		RemoteFolder folder = getRedistHostRootFolder( action );
		action.info( folder.account.getPrintName() + ": drop redist completely at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropStateData( ActionBase action ) throws Exception {
		RemoteFolder folder = getStateFolder( action );
		action.info( folder.account.getPrintName() + ": drop state data at " + folder.folderPath + " ..." );
		folder.ensureExists( action );
		folder.removeContent( action );
	}

	public void dropReleaseData( ActionBase action , VersionInfo version ) throws Exception {
		RemoteFolder folder = getReleaseFolder( action , version );
		action.info( folder.account.getPrintName() + ": drop release=" + version.getReleaseName() + " at " + folder.folderPath + " ..." );
		folder.recreateThis( action );
	}

	public void createLocation( ActionBase action , VersionInfo version , MetaEnvServerLocation location , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String LOCATION = location.DEPLOYPATH;
		RemoteFolder F_DSTDIR_STATE = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		RemoteFolder F_DSTDIR_DEPLOY = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , true );

		HostAccount hostAccount = node.getHostAccount(); 
		action.debug( hostAccount.getFinalAccount() + ": create redist location=" + LOCATION + " contenttype=" + Common.getEnumLower( CONTENTTYPE ) + " ..." );
		F_DSTDIR_STATE.ensureExists( action );
		F_DSTDIR_DEPLOY.ensureExists( action );
		
		if( action.context.CTX_BACKUP )
			createLocationBackup( action , version , location , CONTENTTYPE );
	}

	public void createLocationBackup( ActionBase action , VersionInfo version , MetaEnvServerLocation location , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String LOCATION = location.DEPLOYPATH;
		RemoteFolder F_DSTDIR_DEPLOY = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , false );

		// create empty initial script
		F_DSTDIR_DEPLOY.ensureExists( action );
	}
	
	public RedistStateInfo getStateInfo( ActionBase action , String LOCATION , VarCONTENTTYPE CONTENTTYPE ) throws Exception {
		String F_DSTDIR_STATE = getPathStateLocation( action , LOCATION , CONTENTTYPE );
		RedistStateInfo state = new RedistStateInfo( meta );
		state.gather( action , node , CONTENTTYPE , F_DSTDIR_STATE );
		return( state );
	}

	public boolean copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , Dist dist , MetaEnvServerLocation location , String fileName , String deployBaseName , RedistStateInfo stateInfo ) throws Exception {
		// primary file
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( true );
		VersionInfo version = VersionInfo.getDistVersion( dist );
		RemoteFolder locationDir = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , true );
		String redistFileName = FileInfo.getFileName( action , item );  

		String runtimeName = "";
		if( !item.isArchive() )
			runtimeName = getDeployVersionedName( action , location , item , deployBaseName , version );
		
		// check need redist - from distributive
		if( !stateInfo.needUpdate( action , item , dist , fileName , deployBaseName , runtimeName ) )
			return( false );
		
		// copy and save state info
		action.debug( "copy dist file " + fileName + " to " + redistFileName + " ..." );
		dist.copyDistItemToTarget( action , item , fileName , locationDir , redistFileName );

		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , deployBaseName , version , runtimeName );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
		return( true );
	}

	public boolean copyReleaseFile( ActionBase action , MetaDistrBinaryItem item , MetaEnvServerLocation location , String filePath , String deployBaseName , VersionInfo version , RedistStateInfo stateInfo ) throws Exception {
		// primary file
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( true );
		RemoteFolder locationDir = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , true );
		String redistFileName = FileInfo.getFileName( action , item );
		String deployFinalName = getDeployVersionedName( action , location , item , deployBaseName , version );

		// check need redist - extracted file
		if( !stateInfo.needUpdate( action , item , filePath , deployBaseName , version , deployFinalName ) )
			return( false );
		
		// copy and save state info
		action.debug( "copy extracted file " + filePath + " to " + redistFileName + " ..." );
		locationDir.copyFileFromLocalRename( action , filePath , redistFileName );

		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , deployBaseName , version , deployFinalName );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
		return( true );
	}

	public void copyReleaseFile( ActionBase action , MetaDistrConfItem item , Dist dist , MetaEnvServerLocation location , LocalFolder srcFolder , String configTarFile , boolean partial ) throws Exception {
		String LOCATION = location.DEPLOYPATH; 
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( false );
		VersionInfo version = VersionInfo.getDistVersion( dist );
		RemoteFolder locationDir = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , true );
		String path = srcFolder.getFilePath( action , configTarFile );
		String redistFileName = FileInfo.getFileName( action , item );
		createLocation( action , version , location , CONTENTTYPE );
		locationDir.copyFileFromLocalRename( action , path , FileInfo.getFileName( action , item ) );

		// create state file
		FileInfo data = RedistStateInfo.getFileInfo( action , item , locationDir , redistFileName , version , partial );
		String verName = data.getInfoName( action );
		locationDir.createFileFromString( action , verName , data.value( action ) );
	}

	public void restoreConfigFile( ActionBase action , MetaDistrConfItem confItem , MetaEnvServerLocation location , String redistPath ) throws Exception {
		String fileBaseName = FileInfo.getFileName( action , confItem );
		VarCONTENTTYPE CONTENTTYPE = location.getContentType( false );
		RemoteFolder locationDir = getStateLocationFolder( action , location.DEPLOYPATH , CONTENTTYPE );
		locationDir.ensureExists( action );
		locationDir.copyFileRename( action , redistPath , fileBaseName );
		
		// create state file
		FileInfo data = RedistStateInfo.getFileInfo( action , confItem , locationDir , fileBaseName , null , false );
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

	public String[] getReleaseLocations( ActionBase action ) throws Exception {
		RemoteFolder folder = getReleasesFolder( action );
		if( !folder.checkExists( action ) )
			return( null );
		
		String[] folders = folder.getFolders( action );
		return( folders );
	}

	public ServerDeployment getDeployment( ActionBase action , VersionInfo version ) throws Exception {
		RemoteFolder releaseFolder = getRedistReleaseFolder( action , version );
		ServerDeployment deployment = new ServerDeployment();
		if( releaseFolder.checkExists( action ) )
			deployment.getFromRedistReleaseFolder( action , this , releaseFolder );
		return( deployment );
	}

	public void backupRedistItem( ActionBase action , VersionInfo version , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , FileInfo stateFile ) throws Exception {
		RemoteFolder backupFolder = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , false );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		
		if( redistFile.confItem != null )
			backupRedistConfItem( action , version , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
		else
		if( redistFile.binaryItem != null ) {
			if( redistFile.binaryItem.isArchive() )
				backupRedistArchiveItem( action , version , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
			else
				backupRedistBinaryItem( action , version , CONTENTTYPE , LOCATION , redistFile , backupFolder , stateFolder , stateFile );
		}
		else
			action.exitUnexpectedState();
	}

	public void backupRedistConfItem( ActionBase action , VersionInfo version , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
		String redistBackupFile = redistFile.getFileName( action );  
		String filePath = backupFolder.getFilePath( action , redistBackupFile );
		tarRuntimeConfigItem( action , redistFile.confItem , LOCATION , filePath );
		action.info( "redist backup done, config item file=" + redistFile.getFileName( action ) );
		
		// copy version file from state
		String md5 = backupFolder.getFileMD5( action , redistBackupFile );
		VersionInfo versionFile = ( stateFile == null )? null : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.confItem , versionFile , md5 , false );
		String stateVerName = newInfo.getInfoName( action );
		backupFolder.createFileFromString( action , stateVerName , newInfo.value( action ) );
	}

	public void backupRedistBinaryItem( ActionBase action , VersionInfo version , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
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
		VersionInfo versionFile = ( stateFile == null )? null : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.binaryItem , versionFile , md5 , redistFile.deployBaseName , runtimeFile );
		String stateVerName = newInfo.getInfoName( action );
		backupFolder.createFileFromString( action , stateVerName , newInfo.value( action ) );
	}
	
	public void backupRedistArchiveItem( ActionBase action , VersionInfo version , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , RemoteFolder backupFolder , RemoteFolder stateFolder , FileInfo stateFile ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );

		String redistBackupFile = redistFile.getFileName( action );
		int timeout = action.setTimeoutUnlimited();
		saveArchiveItem( action , redistFile.binaryItem , deployFolder , redistBackupFile , backupFolder );
		action.setTimeout( timeout );
		action.info( "redist backup done, archive item file=" + redistBackupFile );
		
		// copy version file from state
		String md5 = backupFolder.getFileMD5( action , redistBackupFile );
		VersionInfo versionFile = ( stateFile == null )? null : redistFile.version;
		FileInfo newInfo = new FileInfo( redistFile.binaryItem , versionFile , md5 , redistFile.deployBaseName , "" );
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
		
		if( archiveItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_CHILD ) {
			if( !deployFolder.checkFolderExists( action , archiveItem.BASENAME_DEPLOY ) ) {
				action.debug( "unable to find runtime child archive item=" + archiveItem.NAME + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			String content = "";
			String exclude = "";
			String prefix = archiveItem.BASENAME_DEPLOY + "/";
			
			for( String s : Common.splitSpaced( archiveItem.ARCHIVE_FILES ) )
				content = Common.addItemToUniqueSpacedList( content , prefix + s );
			for( String s : Common.splitSpaced( archiveItem.ARCHIVE_EXCLUDE ) )
				exclude = Common.addItemToUniqueSpacedList( exclude , prefix + s );

			deployFolder.createArchiveFromContent( action , atype , archiveFilePath , content , exclude );
		}
		else
		if( archiveItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_DIRECT ) {
			if( !deployFolder.checkExists( action ) ) {
				action.debug( "unable to find runtime direct archive item=" + archiveItem.NAME + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createArchiveFromContent( action , atype , archiveFilePath , archiveItem.ARCHIVE_FILES , archiveItem.ARCHIVE_EXCLUDE );
		}
		else
		if( archiveItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_SUBDIR ) {
			if( !deployFolder.checkFolderExists( action , archiveItem.BASENAME_DEPLOY ) ) {
				action.debug( "unable to find runtime subdir archive item=" + archiveItem.NAME + ", not found in " + deployFolder.folderPath );
				return( false );
			}
				
			deployFolder.createArchiveFromFolderContent( action , atype , archiveFilePath , archiveItem.BASENAME_DEPLOY , archiveItem.ARCHIVE_FILES , archiveItem.ARCHIVE_EXCLUDE );
		}
		else
			action.exitUnexpectedState();
		
		return( true );
	}

	public FileInfo getRuntimeItemInfo( ActionBase action , MetaDistrBinaryItem binaryItem , String LOCATION , String specificDeployBaseName ) throws Exception {
		RemoteFolder deployFolder = getRuntimeLocationFolder( action , LOCATION );

		if( binaryItem.DISTITEM_TYPE == DBEnumDistItemType.BINARY ) {
			String runtimeFile = deployFolder.findBinaryDistItemFile( action , binaryItem , specificDeployBaseName );
			if( runtimeFile.isEmpty() )
				action.exit2( _Error.ItemNotFoundInLive2 , "item=" + binaryItem.NAME + ", is not found in " + deployFolder.folderPath , binaryItem.NAME , deployFolder.folderPath );
			
			String md5value = deployFolder.getFileMD5( action , runtimeFile );
			FileInfo info = binaryItem.getFileInfo( runtimeFile , specificDeployBaseName , md5value );
			return( info );
		}
		
		if( binaryItem.DISTITEM_TYPE == DBEnumDistItemType.PACKAGE ) {
			VarPACKAGEEXTENSION ext = Types.getPackageExtension( binaryItem.EXT , true );
			ShellExecutor shell = action.getShell( deployFolder );
			if( ext == VarPACKAGEEXTENSION.RPM ) {
				String values = shell.customGetValue( action , "rpm -q --qf=\"%{VERSION}:%{SIGMD5}:%{RELEASE}:%{ARCH}\" " + binaryItem.BASENAME_DEPLOY );
				String[] items = Common.split( values , ":" );
				if( items.length != 4 )
					action.exit2( _Error.ItemNotFoundInLive2 , "item=" + binaryItem.NAME + ", is not found in " + deployFolder.folderPath , binaryItem.NAME , deployFolder.folderPath );
				
				String version = items[0].equals( "(none)" )? "" : items[0];
				String md5 = items[1].equals( "(none)" )? "" : items[1];
				String release = items[2].equals( "(none)" )? "" : items[2];
				String arch = items[3].equals( "(none)" )? "" : items[3];
				String name = binaryItem.BASENAME_DEPLOY + "-" + version + "-" + release + "." + arch + ".rpm";
				FileInfo info = new FileInfo( binaryItem , VersionInfo.getFileVersion( version ) , md5 , binaryItem.BASENAME_DEPLOY , name );
				return( info );
			}
			
			action.exitUnexpectedState();
		}
			
		String md5value = getArchiveMD5( action , binaryItem , deployFolder , true );
		FileInfo info = new FileInfo( binaryItem , null , md5value , "" , "" );
		return( info );
	}

	public String getArchiveMD5( ActionBase action , MetaDistrBinaryItem binaryItem , RemoteFolder filesFolder , boolean runtimeFolder ) throws Exception {
		if( binaryItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_CHILD ) {
			String content = "";
			String exclude = "";
			String prefix = binaryItem.BASENAME_DEPLOY + "/";
			
			for( String s : Common.splitSpaced( binaryItem.ARCHIVE_FILES ) )
				content = Common.addItemToUniqueSpacedList( content , prefix + s );
			for( String s : Common.splitSpaced( binaryItem.ARCHIVE_EXCLUDE ) )
				exclude = Common.addItemToUniqueSpacedList( exclude , prefix + s );
			
			String md5value = filesFolder.getFilesMD5( action , content , exclude );
			return( md5value );
		}
		else
		if( binaryItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_DIRECT ) {
			String md5value = filesFolder.getFilesMD5( action , binaryItem.ARCHIVE_FILES , binaryItem.ARCHIVE_EXCLUDE );
			return( md5value );
		}
		else 
		if( binaryItem.DISTITEM_TYPE == DBEnumDistItemType.ARCHIVE_SUBDIR ) {
			RemoteFolder archiveFolder = filesFolder;
			if( runtimeFolder )
				archiveFolder = filesFolder.getSubFolder( action , binaryItem.BASENAME_DEPLOY );
			String md5value = archiveFolder.getFilesMD5( action , binaryItem.ARCHIVE_FILES , binaryItem.ARCHIVE_EXCLUDE );
			return( md5value );
		}

		action.exitUnexpectedState();
		return( null );
	}
	
	public void changeStateItem( ActionBase action , VersionInfo version , VarCONTENTTYPE CONTENTTYPE , String LOCATION , FileInfo redistFile , boolean rollout ) throws Exception {
		String stateFileName = redistFile.getFileName( action );
		String stateInfoName = redistFile.getInfoName( action );
		
		RemoteFolder redistFolder = getRedistLocationFolder( action , version , LOCATION , CONTENTTYPE , rollout );
		RemoteFolder stateFolder = getStateLocationFolder( action , LOCATION , CONTENTTYPE );
		
		stateFolder.removeFiles( action , stateFileName + " " + stateInfoName );
		stateFolder.copyFile( action , redistFolder , stateFileName , stateFileName );

		// copy version file from source folder to state folder
		if( redistFolder.checkFileExists( action , stateInfoName ) )
			stateFolder.copyFile( action , redistFolder , stateInfoName , "" );
	}

	public String getDeployVersionedName( ActionBase action , MetaEnvServerLocation location , MetaDistrBinaryItem item , String deployBaseName , VersionInfo version ) throws Exception {
		if( item.DISTITEM_TYPE == DBEnumDistItemType.BINARY ) {
			if( location.DEPLOYTYPE == DBEnumDeployModeType.LINKS_MULTIDIR ||
				location.DEPLOYTYPE == DBEnumDeployModeType.LINKS_SINGLEDIR ) {
				String deployName = deployBaseName + item.EXT;
				return( deployName );
			}
			
			String deployName = getVersionItem( action , item , deployBaseName , version );
			return( deployName );
		}

		if( item.DISTITEM_TYPE == DBEnumDistItemType.PACKAGE ) {
			String deployName = getVersionItem( action , item , deployBaseName , version );
			return( deployName );
		}
		
		action.exitUnexpectedState();
		return( null );
	}

	private String getVersionItem( ActionBase action , MetaDistrBinaryItem item , String deployBaseName , VersionInfo version ) throws Exception {
		if( item.DEPLOYVERSION_TYPE == DBEnumDeployVersionType.NONE || item.DEPLOYVERSION_TYPE == DBEnumDeployVersionType.IGNORE )
			return( deployBaseName + item.EXT );

		if( item.DEPLOYVERSION_TYPE == DBEnumDeployVersionType.PREFIX )
			return( version.getFileVersion() + "-" + deployBaseName + item.EXT );

		if( item.DEPLOYVERSION_TYPE == DBEnumDeployVersionType.MIDDASH )
			return( deployBaseName + "-" + version.getFileVersion() + item.EXT );

		if( item.DEPLOYVERSION_TYPE == DBEnumDeployVersionType.MIDPOUND )
			return( deployBaseName + "##" + version.getFileVersion() + item.EXT );

		String name = Common.getEnumLower( item.DEPLOYVERSION_TYPE );
		action.exit1( _Error.UnknownVersionType1 , "getVersionItem: unknown version type=" + name , name );
		return( null );
	}
	
}
