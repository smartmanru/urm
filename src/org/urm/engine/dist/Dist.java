package org.urm.engine.dist;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.BlotterService;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.env.MetaEnvServerLocation;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseDist;

public class Dist {

	public static String MASTER_DIR = "master";
	
	public static String META_FILENAME = "release.xml";
	public static String CONFDIFF_FILENAME = "diffconf.txt";
	public static String STATE_FILENAME = "state.txt";

	public static String BINARY_FOLDER = "binary";
	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String DOC_FOLDER = "doc";
	public static String DBSCRIPTS_FOLDER = "scripts";
	public static String DBDATALOAD_FOLDER = "dataload";
	public static String ROLLBACK_FOLDER = "rollback";
	public static String DBMANUAL_FOLDER = "manual/db";
	
	public Meta meta;
	public DistRepository repo;
	
	private RemoteFolder distFolder;
	
	public String RELEASEDIR;
	public ReleaseDist releaseDist;
	public Release release;
	String infoPath;

	private FileSet files;

	DistState state;
	boolean openedForUse;
	boolean openedForChange;
	boolean openedForControl;
	
	public Dist( Meta meta , DistRepository repo , ReleaseDist releaseDist ) {
		this.meta = meta;
		this.repo = repo;
		this.releaseDist = releaseDist;
		this.release = releaseDist.release;
	}
	
	public void load( ActionBase action ) throws Exception {
		state.ctlLoadReleaseState( action );
	}

	public void setFolder( RemoteFolder distFolder ) {
		this.distFolder = distFolder;
		this.RELEASEDIR = distFolder.folderName;
				
		state = new DistState( this , distFolder );
		files = null;
		
		openedForUse = false;
		openedForChange = false;
		openedForControl = false;
	}

	public boolean isMaster() {
		return( release.MASTER );
	}
	
	public boolean isFinalized() {
		return( state.isFinalized() );
	}
	
	public boolean isCompleted() {
		return( state.isCompleted() );
	}
	
	public boolean isBroken() {
		return( state.isBroken() );
	}
	
	public boolean isRemote( ActionBase action ) throws Exception {
		return( distFolder.isRemote( action ) );
	}
	
	public DISTSTATE getState() {
		return( state.state );
	}
	
	public boolean checkHash( ActionBase action ) throws Exception {
		if( !state.isFinalized() )
			return( true );
		
		String actualDataHash = state.getDataHashValue( action );
		if( !actualDataHash.equals( state.dataHash ) )
			return( false );
		String actualMetaHash = state.getMetaHashValue( action );
		if( !actualMetaHash.equals( state.metaHash ) )
			return( false );
		return( true );
	}
	
	public void copyConfToDistr( ActionBase action , LocalFolder sourceFolder , MetaDistrConfItem conf ) throws Exception {
		if( !openedForChange )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
		
		state.checkDistDataChangeEnabled( action );
		String parentFolder = getReleaseConfCompParentFolder( action , conf );
		distFolder.copyDirFromLocal( action , sourceFolder , parentFolder );
	}
	
	public void copyVFileToDistr( ActionBase action , MetaDistrBinaryItem distItem , LocalFolder sourceFolder , String SNAME , String DBASENAME , String DEXT ) throws Exception {
		if( !openedForChange )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
		
		state.checkDistDataChangeEnabled( action );
		String dstfolder = getReleaseBinaryFolder( action , distItem );
		String dstname = DBASENAME + DEXT;
		distFolder.copyVFileFromLocal( action , sourceFolder , SNAME , dstfolder , dstname , DBASENAME , DEXT );
	}

	public void copyDatabaseFilesToDistr( ActionBase action , MetaDistrDelivery dbDelivery , LocalFolder srcPrepared ) throws Exception {
		if( !openedForChange )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
		
		state.checkDistDataChangeEnabled( action );
		String folder = getDeliveryDocFolder( action , dbDelivery );
		distFolder.removeFolder( action , folder );
		
		String parentFolder = Common.getDirName( folder );
		distFolder.ensureFolderExists( action , parentFolder );
		distFolder.copyDirFromLocal( action , srcPrepared , parentFolder );
	}

	public void copyManualFilesToDistr( ActionBase action , LocalFolder src ) throws Exception {
		if( !openedForChange )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
		
		state.checkDistDataChangeEnabled( action );
		String folder = getManualFolder( action  );
		distFolder.removeFolder( action , folder );
		
		distFolder.ensureExists( action );
		distFolder.copyDirFromLocal( action , src , "" );
	}

	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String file ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		return( copyDistToFolder( action , workFolder , "" , file ) );
	}

	public String extractEmbeddedItemToFolder( ActionBase action , LocalFolder folder , MetaDistrBinaryItem item , String fileName ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );

		MetaDistrBinaryItem srcItem = item.srcDistItem;
		
		// extract on remote redist
		RedistStorage redist = action.artefactory.getRedistStorage( action , distFolder.account );
		RemoteFolder tmp = redist.getRedistTmpFolder( action , "dist" );
		
		String srcFilePath = distFolder.getFilePath( action , Common.getPath( srcItem.delivery.FOLDER , fileName ) );
		String distFile = item.BASENAME_DIST + item.EXT;
		tmp.unzipSingleFile( action , srcFilePath , item.SRC_ITEMPATH , distFile );
		
		// move to folder
		tmp.copyFilesToLocal( action , folder , distFile );
		return( distFile );
	}

	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String srcSubdir , String file ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		action.debug( "copy from distributive " + file + " to " + workFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocal( action , workFolder , file , "" ) );
	}

	public String copyDistFileToFolderRename( ActionBase action , LocalFolder workFolder , String srcSubdir , String file , String newName ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		action.debug( "copy from distributive " + file + " to " + workFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocalRename( action , workFolder , file , newName ) );
	}
	
	public void unzipDistFileToFolder( ActionBase action , LocalFolder workFolder , String file , String FOLDER , String target , String part ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		String filePath = distFolder.copyFileToLocal( action , workFolder , file , FOLDER );
		action.shell.unzipPart( action , workFolder.folderPath , filePath , target , part ); 
	}

	public boolean checkFileExists( ActionBase action , String path ) throws Exception {
		return( distFolder.checkFileExists( action , path ) );
	}
	
	public boolean checkFolderExists( ActionBase action , String path ) throws Exception {
		return( distFolder.checkFolderExists( action , path ) );
	}
	
	public String getDistFolder() {
		return( distFolder.folderName );
	}
	
	public String getDistPath( ActionBase action ) throws Exception {
		return( distFolder.getLocalPath( action ) );
	}
	
	public String getManualFolder( ActionBase action ) throws Exception {
		return( "manual" );
	}
	
	public String getDeliveryConfFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , CONFIG_FOLDER ) );
	}
	
	public String getDeliveryDatabaseFolder( ActionBase action , MetaDistrDelivery delivery , String RELEASEVER ) throws Exception {
		return( Common.getPath( delivery.FOLDER , DATABASE_FOLDER , RELEASEVER ) );
	}
	
	public String getDeliveryDocFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , DOC_FOLDER ) );
	}
	
	public String getDeliveryDatabaseScriptFolder( ActionBase action , MetaDistrDelivery delivery , String RELEASEVER ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery , RELEASEVER ) , DBSCRIPTS_FOLDER ) );
	}
	
	public String getDeliveryDatabaseLoadFolder( ActionBase action , MetaDistrDelivery delivery , String RELEASEVER ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery , RELEASEVER ) , DBDATALOAD_FOLDER ) );
	}
	
	public String getDeliveryBinaryFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , BINARY_FOLDER ) );
	}
	
	public void replaceConfDiffFile( ActionBase action , String filePath , ReleaseDistScopeDelivery delivery ) throws Exception {
		state.checkDistDataChangeEnabled( action );
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyFileFromLocal( action , filePath , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , ReleaseDistScopeDelivery delivery , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , MetaDistrConfItem conf , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , conf.delivery );
		confFolder = Common.getPath( confFolder , conf.NAME );
		localFolder.ensureExists( action );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public void createDeliveryFolders( ActionBase action ) throws Exception {
	}

	// top-level control
	public void create( ActionBase action , String RELEASEDIR , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		this.RELEASEDIR = RELEASEDIR;
		state.ctlCreateNormal( action , null );
		load( action );
	}

	public void changeReleaseDate( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		DBEnumLifecycleType type = release.getLifecycleType();
		ReleaseLifecycle lcset = DBReleaseRepository.getLifecycle( action , meta , lc , type );
		release.setReleaseDate( action , releaseDate , lcset );
	}
	
	public void createMaster( ActionBase action ) throws Exception {
		this.RELEASEDIR = MASTER_DIR;
		state.ctlCreateMaster( action , null );
		MetaDistr distr = meta.getDistr();
		for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
			if( delivery.hasBinaryItems() ) {
				String folder = getDeliveryBinaryFolder( action , delivery );
				distFolder.ensureFolderExists( action , folder );
			}
		}
		
		load( action );
	}
	
	public void openForUse( ActionBase action ) throws Exception {
		boolean prod = ( action.context.env != null )? action.context.env.isProd() : false;
		openForUse( action , prod );
	}

	public void openForUse( ActionBase action , boolean requireReleased ) throws Exception {
		state.ctlOpenForUse( action , requireReleased );
		openedForUse = true;
		gatherFiles( action );
	}
	
	public void openForDataChange( ActionBase action ) throws Exception {
		state.ctlOpenForDataChange( action );
		openedForChange = true;
		openedForUse = true;
	}
	
	public void openForControl( ActionBase action ) throws Exception {
		state.ctlOpenForControl( action );
		openedForControl = true;
	}
	
	public void closeDataChange( ActionBase action ) throws Exception {
		state.ctlCloseDataChange( action );
		openedForChange = false;
		openedForUse = false;
		files = null;
	}

	public void closeControl( ActionBase action , DISTSTATE value ) throws Exception {
		if( !openedForControl )
			action.exitUnexpectedState();
		state.ctlCloseControl( action , value );
		openedForControl = false;
	}

	public void forceClose( ActionBase action ) throws Exception {
		if( openedForControl )
			state.ctlCloseControl( action , state.state );
		else
			state.ctlForceClose( action );
	}

	public boolean finish( ActionBase action ) throws Exception {
		if( isFinalized() ) {
			action.info( "release is already finalized" );
			return( true );
		}
		
		openForDataChange( action );
		
		if( !release.MASTER ) {
			ReleaseChanges changes = release.getChanges();
			if( !changes.isCompleted() ) {
				action.error( "release changes are not completed" );
				state.ctlCloseDataChange( action );
				return( false );
			}
		}
		
		DistFinalizer finalizer = new DistFinalizer( action , this , distFolder , release );
		if( !finalizer.finish() ) {
			action.error( "distributive is not ready to be finalyzed" );
			state.ctlCloseDataChange( action );
			return( false );
		}
		
		release.finish( action );
		saveReleaseXml( action );
		state.ctlFinish( action );
		return( true );
	}

	public void complete( ActionBase action ) throws Exception {
		if( state.isCompleted() ) {
			action.info( "release is already completed" );
			return;
		}
		
		openForControl( action );
		release.complete( action );
		saveReleaseXml( action );
		state.ctlCloseControl( action , DISTSTATE.COMPLETED );
	}

	public void reopen( ActionBase action ) throws Exception {
		state.ctlReopen( action );
		release.reopen( action );
		saveReleaseXml( action );
		state.ctlCloseDataChange( action );
	}

	public void copyScope( ActionBase action , Dist src ) throws Exception {
	}

	public void dropRelease( ActionBase action ) throws Exception {
		state.ctlCheckCanDropRelease( action );
		distFolder.removeThis( action );
		state.ctlClearRelease( action );
	}

	public void forceDrop( ActionBase action ) throws Exception {
		state.ctlCheckCanForceDropRelease( action );
		distFolder.removeThis( action );
		state.ctlClearRelease( action );
	}

	public FileSet getFiles( ActionBase action ) throws Exception {
		if( files == null )
			files = distFolder.getFileSet( action );
		return( files );
	}
	
	public void saveReleaseXml( ActionBase action ) throws Exception {
	}

	public boolean addAllSource( ActionBase action , MetaSourceProjectSet set ) throws Exception {
		return( false );
	}
	
	public boolean addAllCategory( ActionBase action , DBEnumScopeCategoryType CATEGORY ) throws Exception {
		return( false );
	}
	
	public boolean addProjectAllItems( ActionBase action , MetaSourceProject project ) throws Exception {
		return( false );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		return( false );
	}

	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		return( false );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public boolean addDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}

	public boolean addBinaryItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		return( false );
	}
	
	public boolean addDeliveryAllDatabaseSchemes( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public boolean addDeliveryAllDocs( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( false );
	}

	public boolean addDeliveryDatabaseSchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		return( false );
	}
	
	public boolean addDeliveryDoc( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc doc ) throws Exception {
		return( false );
	}
	
	public boolean addDatabaseAll( ActionBase action ) throws Exception {
		return( false );
	}

	public boolean addDocAll( ActionBase action ) throws Exception {
		return( false );
	}

	public String getReleaseConfCompParentFolder( ActionBase action , MetaDistrConfItem comp ) throws Exception {
		String folder = getDeliveryConfFolder( action , comp.delivery );
		return( folder );
	}
	
	public String getReleaseBinaryFolder( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		String folder = getDeliveryBinaryFolder( action , item.delivery );
		return( folder );
	}

	public String getReleaseDocFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		String folder = getDeliveryDocFolder( action , delivery );
		return( folder );
	}

	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrBinaryItem item , boolean getMD5 , boolean getTimestamp ) {
		DistItemInfo info = new DistItemInfo( item );
		
		try {
			if( item.isDerivedItem() ) {
				DistItemInfo infosrc = getDistItemInfo( action , item.srcDistItem , false , true );
				info.subPath = infosrc.subPath;
				info.fileName = infosrc.fileName;
				info.found = infosrc.found;
				info.timestamp = infosrc.timestamp;
			}
			else {
				info.subPath = getReleaseBinaryFolder( action , item );
				info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
				info.found = ( info.fileName.isEmpty() )? false : true;
				
				if( info.found && getTimestamp ) {
					RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
					info.timestamp = fileFolder.getFileChangeTime( action , info.fileName );
				}
			}
			
			if( info.found && getMD5 ) {
				RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
				if( item.isDerivedItem() )
					info.md5value = fileFolder.getArchivePartMD5( action , info.fileName , item.SRC_ITEMPATH , item.srcDistItem.EXT );
				else
				if( item.isArchive() )
					info.md5value = fileFolder.getArchiveContentMD5( action , info.fileName , item.EXT );
				else
					info.md5value = fileFolder.getFileMD5( action , info.fileName );
			}
		}
		catch( Throwable e ) {
			action.log( "get binary distitem info item=" + item.NAME , e );
			info.found = false;
		}
		
		return( info );
	}

	public String getDistItemMD5( ActionBase action , MetaDistrBinaryItem item , String fileName ) throws Exception {
		RemoteFolder fileFolder = distFolder.getSubFolder( action , item.delivery.FOLDER );
		int timeout = action.setTimeoutUnlimited();
		String value = fileFolder.getFileMD5( action , fileName );
		action.setTimeout( timeout );
		return( value );
	}
	
	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrConfItem item ) {
		DistItemInfo info = new DistItemInfo( item );

		try {
			info.subPath = getReleaseConfCompParentFolder( action , item );
			info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
			info.found = ( info.fileName.isEmpty() )? false : true;
		}
		catch( Throwable e ) {
			action.log( "get configuration distitem info item=" + item.NAME , e );
			info.found = false;
		}
		
		return( info );
	}

	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc item , boolean getMD5 , boolean getTimestamp ) {
		DistItemInfo info = new DistItemInfo( item );

		try {
			info.subPath = getReleaseDocFolder( action , delivery );
			info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
			info.found = ( info.fileName.isEmpty() )? false : true;
			
			if( info.found && getTimestamp ) {
				RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
				info.timestamp = fileFolder.getFileChangeTime( action , info.fileName );
			}
			
			if( info.found && getMD5 ) {
				RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
				info.md5value = fileFolder.getFileMD5( action , info.fileName );
			}
		}
		catch( Throwable e ) {
			action.log( "get document distitem info item=" + item.NAME , e );
			info.found = false;
		}
		
		return( info );
	}

	public void reloadCheckOpenedForDataChange( ActionBase action ) throws Exception {
		state.ctlReloadCheckOpenedForDataChange( action );
	}
	
	public void descopeSet( ActionBase action , ReleaseBuildScopeSet set ) throws Exception {
	}

	public void descopeSet( ActionBase action , ReleaseDistScopeSet set ) throws Exception {
	}

	public void descopeAllProjects( ActionBase action ) throws Exception {
	}
	
	public String getBinaryDistItemFile( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		return( null );
	}

	public String getDocDistItemFile( ActionBase action , MetaProductDoc doc ) throws Exception {
		return( null );
	}

	public void gatherFiles( ActionBase action ) throws Exception {
	}

	public MetaDistrConfItem[] getLocationConfItems( ActionBase action , MetaEnvServerLocation[] locations ) throws Exception {
		Map<String,MetaDistrConfItem> confs = new HashMap<String,MetaDistrConfItem>(); 
		return( confs.values().toArray( new MetaDistrConfItem[0] ) );
	}

	public String[] getManualDatabaseFiles( ActionBase action ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		FileSet set = files.getDirByPath( action , DBMANUAL_FOLDER );
		if( set == null )
			return( new String[0] );
		
		return( set.getAllFiles() );
	}

	public String findManualDatabaseItemFile( ActionBase action , String index ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		FileSet set = files.getDirByPath( action , "manual/db" );
		if( set == null )
			return( null );
		
		for( String file : set.getAllFiles() ) {
			if( file.startsWith( index + "-" ) )
				return( file );
		}
		
		return( null );
	}

	public void copyDistDatabaseManualFileToFolder( ActionBase action , LocalFolder dstFolder , String file ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		copyDistToFolder( action , dstFolder , "manual/db" , file );
	}

	public void copyDistItemToTarget( ActionBase action , MetaDistrBinaryItem item , String fileName , RemoteFolder locationDir , String redistFileName ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		if( isRemote( action ) ) {
			// copy via local
			LocalFolder work = action.getWorkFolder( "copy" );
			copyDistFileToFolderRename( action , work , item.delivery.FOLDER , fileName , redistFileName );
			locationDir.copyFileFromLocal( action , work.getFilePath( action , redistFileName ) );
		}
		else {
			String path = Common.getPath( distFolder.folderPath , item.delivery.FOLDER , fileName );
			locationDir.copyFileFromLocalRename( action , path , redistFileName );
		}
	}

	public void copyDocToTarget( ActionBase action , MetaDistrDelivery delivery , MetaProductDoc item , String fileName , RemoteFolder locationDir , String redistFileName ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		if( isRemote( action ) ) {
			// copy via local
			LocalFolder work = action.getWorkFolder( "copy" );
			copyDistFileToFolderRename( action , work , delivery.FOLDER , fileName , redistFileName );
			locationDir.copyFileFromLocal( action , work.getFilePath( action , redistFileName ) );
		}
		else {
			String path = Common.getPath( distFolder.folderPath , delivery.FOLDER , fileName );
			locationDir.copyFileFromLocalRename( action , path , redistFileName );
		}
	}

	public void descopeAll( ActionBase action ) throws Exception {
	}

	public void copyDatabaseDistrToDistr( ActionBase action , MetaDistrDelivery delivery , Dist src ) throws Exception {
		String folder = src.getDeliveryDatabaseFolder( action , delivery , src.release.RELEASEVER );
		if( src.distFolder.checkFolderExists( action , folder ) )
			distFolder.copyExtDir( action , src.distFolder.getFilePath( action , folder ) , folder );
	}
	
	public void copyFileDistrToDistr( ActionBase action , MetaDistrDelivery delivery , Dist src , String file ) throws Exception {
		String folder = delivery.FOLDER;
		String fileSrc = src.distFolder.getFilePath( action , Common.getPath( folder , file ) );
		String fileDst = Common.getPath( folder , file );
		action.debug( "copy " + fileSrc + " to " + fileDst + " ..." );
		
		distFolder.ensureFolderExists( action , Common.getDirName( fileDst ) );
		distFolder.copyFile( action , fileSrc , fileDst );
	}
	
	public void appendConfDistrToDistr( ActionBase action , MetaDistrDelivery delivery , Dist src , MetaDistrConfItem item ) throws Exception {
		String folder = src.getDeliveryConfFolder( action , delivery );
		ShellExecutor session = distFolder.getSession( action );
		String folderSrc = src.distFolder.getFilePath( action , Common.getPath( folder , item.NAME ) );
		String folderDst = distFolder.getFilePath( action , Common.getPath( folder , item.NAME ) );
		distFolder.ensureFolderExists( action , folderDst );
		session.copyDirContent( action , folderSrc , folderDst );
	}
	
	public void finishStatus( ActionBase action ) throws Exception {
		BlotterService blotter = action.getServerBlotter();
		blotter.runDistStatus( action , meta , this );
	}
	
	public void createMasterFiles( ActionBase action , Dist src ) throws Exception {
	}
	
	public void appendMasterFiles( ActionBase action , Dist src ) throws Exception {
	}
	
	public Dist copyDist( ActionBase action , String newName , ReleaseDist newReleaseDist ) throws Exception {
		RemoteFolder parent = distFolder.getParentFolder( action );
		if( !parent.checkFolderExists( action , RELEASEDIR ) )
			action.exitUnexpectedState();
		if( parent.checkFolderExists( action , newName ) )
			parent.removeFolder( action , newName );
		
		parent.copyDir( action , RELEASEDIR , newName );
		RemoteFolder folderNew = parent.getSubFolder( action , newName );
		Dist distNew = DistRepositoryItem.read( action , repo , folderNew , newReleaseDist );
		return( distNew );
	}

	public void moveDist( ActionBase action , String newName ) throws Exception {
		RemoteFolder parent = distFolder.getParentFolder( action );
		if( !parent.checkFolderExists( action , RELEASEDIR ) )
			action.exitUnexpectedState();
		if( parent.checkFolderExists( action , newName ) )
			parent.removeFolder( action , newName );
		
		parent.moveFolderToFolder( action , RELEASEDIR , newName );
		setFolder( parent.getSubFolder( action , newName ) );
	}
	
}
