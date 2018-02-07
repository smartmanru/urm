package org.urm.engine.dist;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.database.DatabaseScriptFile;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.blotter.EngineBlotter;
import org.urm.engine.blotter.EngineBlotterReleaseItem;
import org.urm.engine.blotter.EngineBlotterSet;
import org.urm.engine.blotter.EngineBlotter.BlotterType;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.shell.ShellExecutor;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.RedistStorage;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.MatchItem;
import org.urm.meta.Types;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.env.MetaEnvServerLocation;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;

public class Dist {

	public static String MASTER_LABEL = "master";
	public static String MASTER_DIR = "master";
	
	public static String META_FILENAME = "release.xml";
	public static String CONFDIFF_FILENAME = "diffconf.txt";
	public static String STATE_FILENAME = "state.txt";

	public static String BINARY_FOLDER = "binary";
	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String DBSCRIPTS_FOLDER = "scripts";
	public static String DBDATALOAD_FOLDER = "dataload";
	public static String ROLLBACK_FOLDER = "rollback";
	public static String DBMANUAL_FOLDER = "manual/db";
	
	public Meta meta;
	public DistRepository repo;
	
	private RemoteFolder distFolder;
	
	public String RELEASEDIR;
	public Release release;
	String infoPath;

	private FileSet files;

	DistState state;
	boolean openedForUse;
	boolean openedForChange;
	boolean openedForControl;
	
	public Dist( Meta meta , DistRepository repo ) {
		this.meta = meta;
		this.repo = repo;
	}
	
	public Dist copy( ActionBase action , DistRepository rrepo ) throws Exception {
		Dist rdist = new Dist( rrepo.meta , rrepo );
		rdist.distFolder = distFolder;
		rdist.RELEASEDIR = RELEASEDIR;
		
		rdist.release = release.copy( action , rdist );
		rdist.infoPath = infoPath;
		rdist.files = files;
		rdist.state = state.copy( action , rdist );
		
		rdist.openedForUse = openedForUse;
		rdist.openedForChange = openedForChange;
		rdist.openedForControl = openedForControl;
		return( rdist );
	}
	
	public void load( ActionBase action ) throws Exception {
		action.debug( "loading release specification from " + META_FILENAME + " ..." );
		
		state.ctlLoadReleaseState( action );
		
		infoPath = distFolder.copyFileToLocal( action , action.getWorkFolder() , META_FILENAME , "" );
		release = new Release( meta , this );
		release.load( action , infoPath );
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
		String folder = getDeliveryDatabaseFolder( action , dbDelivery , release.RELEASEVER );
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
	
	public String getDeliveryDatabaseScriptFolder( ActionBase action , MetaDistrDelivery delivery , String RELEASEVER ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery , RELEASEVER ) , DBSCRIPTS_FOLDER ) );
	}
	
	public String getDeliveryDatabaseLoadFolder( ActionBase action , MetaDistrDelivery delivery , String RELEASEVER ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery , RELEASEVER ) , DBDATALOAD_FOLDER ) );
	}
	
	public String getDeliveryBinaryFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , BINARY_FOLDER ) );
	}
	
	public void replaceConfDiffFile( ActionBase action , String filePath , ReleaseDelivery delivery ) throws Exception {
		state.checkDistDataChangeEnabled( action );
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyFileFromLocal( action , filePath , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , ReleaseDelivery delivery , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , MetaDistrConfItem conf , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		ReleaseDelivery delivery = release.findDelivery( conf.delivery.NAME );
		if( delivery == null )
			action.exit1( _Error.UnknownReleaseDelivery1 , "unknown release delivery=" + conf.delivery.NAME , conf.delivery.NAME );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		confFolder = Common.getPath( confFolder , conf.NAME );
		localFolder.ensureExists( action );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public boolean copyDistConfToFolder( ActionBase action , ReleaseTarget confTarget , LocalFolder parentFolder ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , confTarget.getDelivery( action ) );
		String compFolder = Common.getPath( confFolder , confTarget.distConfItem.NAME );
		RemoteFolder compDist = distFolder.getSubFolder( action , compFolder );
		if( !compDist.checkExists( action ) )
			return( false );
		
		compDist.copyDirToLocal( action , parentFolder );
		return( true );
	}
	
	public void createDeliveryFolders( ActionBase action ) throws Exception {
		state.checkDistDataChangeEnabled( action );
		for( ReleaseDelivery delivery : release.getDeliveries() ) {
			createInternalDeliveryFolder( action , getDeliveryBinaryFolder( action , delivery.distDelivery ) );
			createInternalDeliveryFolder( action , getDeliveryConfFolder( action , delivery.distDelivery ) );
			createInternalDeliveryFolder( action , getDeliveryDatabaseFolder( action , delivery.distDelivery , release.RELEASEVER ) );
		}
	}

	private void createInternalDeliveryFolder( ActionBase action , String folder ) throws Exception {
		RemoteFolder subFolder = distFolder.getSubFolder( action , folder );
		subFolder.ensureExists( action );
	}
	
	// top-level control
	public void create( ActionBase action , String RELEASEDIR , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		this.RELEASEDIR = RELEASEDIR;
		VersionInfo info = VersionInfo.getReleaseDirInfo( RELEASEDIR );
		lc = getLifecycle( action , meta , lc , info.getLifecycleType() );
		releaseDate = getReleaseDate( action , releaseDate , lc );
		if( releaseDate == null )
			action.exit1( _Error.MissingReleaseDate1 , "unable to create release label=" + RELEASEDIR + " due to missing release date" , RELEASEDIR );
		
		state.ctlCreate( action , releaseDate , lc );
		load( action );
	}

	public void changeReleaseDate( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		DBEnumLifecycleType type = release.getLifecycleType();
		ReleaseLifecycle lcset = getLifecycle( action , meta , lc , type );
		release.setReleaseDate( action , releaseDate , lcset );
	}
	
	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		this.RELEASEDIR = MASTER_DIR;
		state.ctlCreateProd( action , RELEASEVER );
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

	public void finish( ActionBase action ) throws Exception {
		if( isFinalized() ) {
			action.info( "release is already finalized" );
			return;
		}
		
		openForDataChange( action );
		
		if( !release.MASTER ) {
			if( !release.changes.isCompleted() ) {
				action.error( "release changes are not completed" );
				state.ctlCloseDataChange( action );
				return;
			}
		}
		
		DistFinalizer finalizer = new DistFinalizer( action , this , distFolder , release );
		if( !finalizer.finish() ) {
			action.error( "distributive is not ready to be finalyzed" );
			state.ctlCloseDataChange( action );
			return;
		}
		
		release.finish( action );
		saveReleaseXml( action );
		state.ctlFinish( action );
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
		String filePath = action.getWorkFilePath( Dist.META_FILENAME );
		
		release.copyReleaseScope( action , src.release );
		Document doc = release.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		
		openForDataChange( action );
		
		distFolder.copyFileFromLocal( action , filePath );
		ShellExecutor shell = action.getShell( distFolder.account );
		for( ReleaseDelivery delivery : src.release.getDeliveries() ) {
			String dirFrom = src.distFolder.getFolderPath( action , delivery.distDelivery.FOLDER );
			String dirTo = distFolder.getFolderPath( action , delivery.distDelivery.FOLDER );
			int timeout = action.setTimeoutUnlimited();
			shell.copyDirDirect( action , dirFrom , dirTo );
			action.setTimeout( timeout );
		}
		
		closeDataChange( action );
		action.info( "release " + RELEASEDIR + " has beed copied from " + src.RELEASEDIR );
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
		state.ctlReloadCheckOpenedForMetaChange( action );
		
		String filePath = action.getWorkFilePath( META_FILENAME );
		Document doc = release.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		distFolder.copyFileFromLocal( action , filePath );
		state.updateMetaHashValue( action );
	}

	public boolean addAllSource( ActionBase action , MetaSourceProjectSet set ) throws Exception {
		action.debug( "release - add source set=" + set.NAME );
		return( release.addSourceSet( action , set , true ) );
	}
	
	public boolean addAllCategory( ActionBase action , EnumScopeCategory CATEGORY ) throws Exception {
		action.debug( "release - add category=" + Common.getEnumLower( CATEGORY ) );
		return( release.addCategorySet( action , CATEGORY , true ) );
	}
	
	public boolean addProjectAllItems( ActionBase action , MetaSourceProject project ) throws Exception {
		action.debug( "release - add project=" + project.NAME );
		
		if( !release.addSourceSet( action , project.set , false ) )
			return( false );
		if( !release.addProject( action , project , true ) )
			return( false );
		return( true );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		action.debug( "release - add project=" + project.NAME + ", item=" + item.NAME );
		
		// ignore internal items
		if( item.isInternal() ) {
			action.info( "item=" + item.NAME + " is internal. Skipped.");
			return( true );
		}
		
		if( !release.addSourceSet( action , project.set , false ) )
			return( false );
		if( !release.addProject( action , project , false ) )
			return( false );
		if( !release.addProjectItem( action , project , item ) )
			return( false );
		return( true );
	}

	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		action.debug( "release - add conf item=" + item.NAME );
		
		if( !release.addCategorySet( action , EnumScopeCategory.CONFIG , false ) )
			return( false );
		if( !release.addConfItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		action.debug( "release - add manual item=" + item.NAME );
		
		if( !release.addCategorySet( action , EnumScopeCategory.MANUAL , false ) )
			return( false );
		if( !release.addManualItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDerivedItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		action.debug( "release - add derived item=" + item.NAME );
		
		if( !release.addCategorySet( action , EnumScopeCategory.DERIVED , false ) )
			return( false );
		if( !release.addDerivedItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addBinaryItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.isProjectItem() )
			return( addProjectItem( action , item.sourceProjectItem.project , item.sourceProjectItem ) );
		if( item.isManualItem() )
			return( addManualItem( action , item ) );
		if( item.isDerivedItem() )
			return( addDerivedItem( action , item ) );
		return( false );
	}
	
	public boolean addDatabaseDeliveryAllSchemes( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		action.debug( "release - add database delivery=" + delivery.NAME );
		if( !release.addCategorySet( action , EnumScopeCategory.DB , false ) )
			return( false );
		if( !release.addDatabaseDelivery( action , delivery , true ) )
			return( false );
		return( true );
	}

	public boolean addDatabaseDeliverySchema( ActionBase action , MetaDistrDelivery delivery , MetaDatabaseSchema schema ) throws Exception {
		action.debug( "release - add database delivery=" + delivery.NAME + ", schema=" + schema );
		if( !release.addCategorySet( action , EnumScopeCategory.DB , false ) )
			return( false );
		if( !release.addDatabaseDelivery( action , delivery , false ) )
			return( false );
		if( !release.addDatabaseSchema( action , delivery , schema ) )
			return( false );
		return( true );
	}
	
	public boolean addDatabaseAll( ActionBase action ) throws Exception {
		action.debug( "release - add database" );
		if( !release.addCategorySet( action , EnumScopeCategory.DB , true ) )
			return( false );
		return( true );
	}

	public String getReleaseConfCompParentFolder( ActionBase action , MetaDistrConfItem comp ) throws Exception {
		String folder = getDeliveryConfFolder( action , comp.delivery );
		return( folder );
	}
	
	public String getReleaseBinaryFolder( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		String folder = getDeliveryBinaryFolder( action , item.delivery );
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

	public void reloadCheckOpenedForDataChange( ActionBase action ) throws Exception {
		state.ctlReloadCheckOpenedForDataChange( action );
	}
	
	public void descopeSet( ActionBase action , ReleaseDistSet set ) throws Exception {
		for( ReleaseTarget target : set.getTargets() )
			dropTarget( action , target );
		
		if( Types.isSourceCategory( set.CATEGORY ) )
			release.deleteSourceSet( action , set.set );
		else
			release.deleteCategorySet( action , set.CATEGORY );
	}

	public void descopeAllProjects( ActionBase action ) throws Exception {
		for( ReleaseDistSet set : release.getSourceSets() )
			descopeSet( action , set );
	}
	
	public void descopeTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		dropTarget( action , target );
		release.deleteTarget( action , target );
	}
	
	public void descopeTargetItems( ActionBase action , ReleaseTargetItem[] items ) throws Exception {
		for( ReleaseTargetItem item : items ) {
			dropTargetItem( action , item );
			if( item.isBinary() )
				release.deleteProjectItem( action , item );
			else
			if( item.isDatabase() )
				release.deleteDatabaseSchema( action , item );
		}
	}
	
	private void dropTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( target.CATEGORY == EnumScopeCategory.CONFIG ) {
			String folder = getDeliveryConfFolder( action , target.distConfItem.delivery );
			distFolder.removeFolder( action , folder );
		}
		else
		if( target.CATEGORY == EnumScopeCategory.DB ) {
			String folder = getDeliveryDatabaseFolder( action , target.distDatabaseDelivery , release.RELEASEVER );
			distFolder.removeFolderContent( action , folder );
		}
		else
		if( target.CATEGORY == EnumScopeCategory.MANUAL ) {
			String folder = getReleaseBinaryFolder( action , target.distManualItem );
			distFolder.deleteVFile( action , folder , target.distManualItem.BASENAME_DIST , target.distManualItem.EXT );
		}
		else {
			for( ReleaseTargetItem item : target.getItems() )
				dropTargetItem( action , item );
		}
	}

	private void dropTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		if( item.isBinary() ) {
			String folder = getReleaseBinaryFolder( action , item.distItem );
			distFolder.deleteVFile( action , folder , item.distItem.BASENAME_DIST , item.distItem.EXT );
		}
		else
		if( item.isDatabase() ) {
			String folderName = getReleaseBinaryFolder( action , item.distItem );
			RemoteFolder folder = distFolder.getSubFolder( action , folderName );
			if( folder.checkExists( action ) ) {
				FileSet fs = folder.getFileSet( action );
				String[] files = DatabaseScriptFile.getDistSchemaFiles( fs , item.schema );
				folder.removeFiles( action , Common.getList( files , " " ) );
			}
		}
	}

	public boolean checkIfReleaseItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL ) {
			ReleaseTarget target = release.findCategoryTarget( action , EnumScopeCategory.MANUAL , item.NAME );
			if( target == null )
				return( false );
			return( true );
		}
		else
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED ) {
			ReleaseTarget target = release.findCategoryTarget( action , EnumScopeCategory.DERIVED , item.NAME );
			if( target == null )
				return( false );
			return( checkIfReleaseItem( action , item.srcDistItem ) );
		}
		else 
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD ) {
			ReleaseTarget target = release.findBuildProject( action , item.sourceProjectItem.project.NAME );
			if( target == null )
				return( false );
			
			if( target.findDistItem( item ) == null )
				return( false );
			
			return( true );
		}
		else
			action.exitUnexpectedState();
		
		return( false );
	}
	
	public String getBinaryDistItemFile( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !openedForUse )
			action.exit0( _Error.DistributiveNotUse0 , "distributive is not opened for use" );
		
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL ) {
			ReleaseTarget target = release.findCategoryTarget( action , EnumScopeCategory.MANUAL , item.NAME );
			if( target == null )
				return( "" );
			
			if( target.DISTFILE == null || target.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( BINARY_FOLDER , target.DISTFILE ) );
		}
		else
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED ) {
			ReleaseTarget target = release.findCategoryTarget( action , EnumScopeCategory.DERIVED , item.NAME );
			if( target == null )
				return( "" );
			
			if( target.DISTFILE == null || target.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( BINARY_FOLDER , target.DISTFILE ) );
		}
		else
		if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD ) {
			ReleaseTarget target = release.findBuildProject( action , item.sourceProjectItem.project.NAME );
			if( target == null )
				return( "" );
			
			ReleaseTargetItem targetItem = target.findDistItem( item );
			if( targetItem == null )
				return( "" );
			
			if( targetItem.DISTFILE == null || targetItem.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( BINARY_FOLDER , targetItem.DISTFILE ) );
		}
		else
			action.exitUnexpectedState();
		
		return( null );
	}

	public void gatherFiles( ActionBase action ) throws Exception {
		action.info( "find distributive files ..." );
		files = distFolder.getFileSet( action );
		
		for( ReleaseDelivery delivery : release.getDeliveries() ) {
			FileSet deliveryFiles = files.getDirByPath( action , delivery.distDelivery.FOLDER );
			
			for( ReleaseTargetItem targetItem : delivery.getProjectItems() )
				gatherDeliveryBinaryItem( action , delivery , deliveryFiles , targetItem );
				
			for( ReleaseTarget targetItem : delivery.getManualItems() )
				gatherDeliveryManualItem( action , delivery , deliveryFiles , targetItem );
		}
	}

	private void gatherDeliveryBinaryItem( ActionBase action , ReleaseDelivery delivery , FileSet deliveryFiles , ReleaseTargetItem targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , BINARY_FOLDER );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distItem.NAME + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}

	private void gatherDeliveryManualItem( ActionBase action , ReleaseDelivery delivery , FileSet deliveryFiles , ReleaseTarget targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , BINARY_FOLDER );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distManualItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distManualItem.NAME + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}
	
	public MetaDistrConfItem[] getLocationConfItems( ActionBase action , MetaEnvServerLocation[] locations ) throws Exception {
		Map<String,MetaDistrConfItem> confs = new HashMap<String,MetaDistrConfItem>(); 
		MetaDistr distr = meta.getDistr(); 
		for( MetaEnvServerLocation location : locations ) {
			String[] items = location.getConfItems( action );
			for( String item : items ) {
				MetaDistrConfItem conf = distr.getConfItem( item );
				if( release.findConfComponent( action , conf.NAME ) == null )
					continue;
				
				if( !confs.containsKey( conf.NAME ) )
					confs.put( conf.NAME , conf );
			}
		}
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

	public void descopeAll( ActionBase action ) throws Exception {
		if( !openedForChange )
			action.exit0( _Error.DistributiveNotOpened0 , "distributive is not opened for change" );
		
		action.info( "remove distributive content ..." );
		for( String dir : distFolder.getTopDirs( action ) )
			distFolder.removeFolder( action , dir );
		
		action.info( "remove all scope ..." );
		release.descopeAll( action );
	}

	public void copyDatabaseDistrToDistr( ActionBase action , ReleaseDelivery delivery , Dist src ) throws Exception {
		ReleaseDelivery reldel = src.release.findDelivery( delivery.distDelivery.NAME );
		if( reldel != null ) {
			String folder = src.getDeliveryDatabaseFolder( action , reldel.distDelivery , src.release.RELEASEVER );
			if( src.distFolder.checkFolderExists( action , folder ) )
				distFolder.copyExtDir( action , src.distFolder.getFilePath( action , folder ) , folder );
		}
	}
	
	public void copyBinaryDistrToDistr( ActionBase action , ReleaseDelivery delivery , Dist src , String file ) throws Exception {
		ReleaseDelivery reldel = src.release.findDelivery( delivery.distDelivery.NAME );
		if( reldel != null ) {
			String folder = reldel.distDelivery.FOLDER;
			String fileSrc = src.distFolder.getFilePath( action , Common.getPath( folder , file ) );
			String fileDst = Common.getPath( folder , file );
			action.debug( "copy " + fileSrc + " to " + fileDst + " ..." );
			
			distFolder.ensureFolderExists( action , Common.getDirName( fileDst ) );
			distFolder.copyFile( action , fileSrc , fileDst );
		}
	}
	
	public void appendConfDistrToDistr( ActionBase action , ReleaseDelivery delivery , Dist src , MetaDistrConfItem item ) throws Exception {
		ReleaseDelivery reldel = src.release.findDelivery( delivery.distDelivery.NAME );
		if( reldel != null ) {
			String folder = src.getDeliveryConfFolder( action , reldel.distDelivery );
			ShellExecutor session = distFolder.getSession( action );
			String folderSrc = src.distFolder.getFilePath( action , Common.getPath( folder , item.NAME ) );
			String folderDst = distFolder.getFilePath( action , Common.getPath( folder , item.NAME ) );
			distFolder.ensureFolderExists( action , folderDst );
			session.copyDirContent( action , folderSrc , folderDst );
		}
	}
	
	public static ReleaseLifecycle getLifecycle( ActionBase action , Meta meta , ReleaseLifecycle lc , DBEnumLifecycleType type ) throws Exception {
		MetaProductPolicy policy = meta.getPolicy();
		
		if( type == DBEnumLifecycleType.MAJOR ) {
			Integer expected = policy.LC_MAJOR.FKID;
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.MINOR ) {
			Integer expected = policy.LC_MAJOR.FKID;
			if( expected == null ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					if( expected != lc.ID )
						action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.NAME );
					return( lc );
				}
				
				EngineLifecycles lifecycles = action.getServerReleaseLifecycles();
				return( lifecycles.getLifecycle( expected ) );
			}
		}
		else
		if( type == DBEnumLifecycleType.URGENT ) {
			MatchItem[] expected = policy.LC_URGENT_LIST;
			if( expected.length == 0 ) {
				if( lc != null )
					return( lc );
			}
			else {
				if( lc != null ) {
					for( int k = 0; k < expected.length; k++ ) {
						if( expected[ k ].FKID == lc.ID )
							return( lc );
					}
					action.exit1( _Error.NotExpectedReleasecycleType1 , "Unexpected release cycle type=" , lc.NAME );
				}
				
				action.exit0( _Error.MissingReleasecycleType0 , "Missing release cycle type" );
			}
		}
		
		return( null );
	}
	
	private Date getReleaseDate( ActionBase action , Date releaseDate , ReleaseLifecycle lc ) throws Exception {
		if( releaseDate != null )
			return( releaseDate );
		
		if( lc != null ) {
			if( lc.isRegular() ) {
				VersionInfo info = VersionInfo.getReleaseDirInfo( RELEASEDIR );
				String prevReleaseVer = info.getPreviousVersion();
				
				if( !prevReleaseVer.isEmpty() ) {
					EngineBlotterSet blotter = action.getBlotter( BlotterType.BLOTTER_RELEASE );
					EngineBlotterReleaseItem item = blotter.findReleaseItem( meta.name , prevReleaseVer );
					
					if( item != null ) {
						releaseDate = Common.addDays( item.repoItem.dist.release.schedule.releaseDate , lc.SHIFT_DAYS );
						return( releaseDate );
					}
				}
			}
			
		}
		
		action.exit0( _Error.MissingReleaseDate0 , "Missing release date" );
		return( null );
	}

	public void finishStatus( ActionBase action ) throws Exception {
		EngineBlotter blotter = action.getServerBlotter();
		blotter.runDistStatus( action , meta , this );
	}
	
	public void createMasterFiles( ActionBase action , Dist src ) throws Exception {
		release.createMaster( action , src.release.RELEASEVER , true );
		src.gatherFiles( action );
		
		for( ReleaseDelivery delivery : src.release.getDeliveries() ) {
			for( ReleaseTargetItem item : delivery.getProjectItems() )
				copyMasterItem( action , src , delivery , item.distItem , true );
			for( ReleaseTarget item : delivery.getManualItems() )
				copyMasterItem( action , src , delivery , item.distManualItem , true );
		}
		
		release.master.addMasterHistory( action , src.release.RELEASEVER );
	}
	
	public void appendMasterFiles( ActionBase action , Dist src ) throws Exception {
		src.gatherFiles( action );
		for( ReleaseDelivery delivery : src.release.getDeliveries() ) {
			for( ReleaseTargetItem item : delivery.getProjectItems() )
				copyMasterItem( action , src , delivery , item.distItem , false );
			for( ReleaseTarget item : delivery.getManualItems() )
				copyMasterItem( action , src , delivery , item.distManualItem , false );
		}
		
		release.master.addMasterHistory( action , src.release.RELEASEVER );
		release.setReleaseVer( action , src.release.RELEASEVER );
	}
	
	private void copyMasterItem( ActionBase action , Dist src , ReleaseDelivery delivery , MetaDistrBinaryItem distItem , boolean create ) throws Exception {
		DistItemInfo info = src.getDistItemInfo( action , distItem , true , false );
		if( !info.found ) {
			action.error( "missing item=" + distItem.NAME );
			action.exitUnexpectedState();
		}

		RemoteFolder folder;
		if( !create ) {
			ReleaseMasterItem item = release.findMasterItem( distItem );
			if( item != null ) {
				folder = distFolder.getSubFolder( action , Common.getPath( item.FOLDER , BINARY_FOLDER ) );
				folder.removeFiles( action , item.FILE + " " + item.FILE + ".md5" );
			}
		}
		
		release.addMasterItem( action , src.release , distItem , info );
		copyBinaryDistrToDistr( action , delivery , src , Common.getPath( BINARY_FOLDER , info.fileName ) );
		folder = distFolder.getSubFolder( action , Common.getPath( delivery.distDelivery.FOLDER , BINARY_FOLDER ) );
		folder.createFileFromString( action , info.fileName + ".md5" , info.md5value );
	}

	public Dist copyDist( ActionBase action , String newName ) throws Exception {
		RemoteFolder parent = distFolder.getParentFolder( action );
		if( !parent.checkFolderExists( action , RELEASEDIR ) )
			action.exitUnexpectedState();
		if( parent.checkFolderExists( action , newName ) )
			parent.removeFolder( action , newName );
		
		parent.copyDir( action , RELEASEDIR , newName );
		RemoteFolder folderNew = parent.getSubFolder( action , newName );
		Dist distNew = DistRepositoryItem.read( action , repo , folderNew );
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
