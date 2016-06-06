package org.urm.dist;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.MetaDistrBinaryItem;
import org.urm.meta.MetaDistrConfItem;
import org.urm.meta.MetaDistrDelivery;
import org.urm.meta.MetaEnvServerLocation;
import org.urm.meta.MetaSourceProject;
import org.urm.meta.MetaSourceProjectItem;
import org.urm.meta.MetaSourceProjectSet;
import org.urm.meta.Metadata;
import org.urm.meta.Metadata.VarCATEGORY;
import org.urm.meta.Metadata.VarDISTITEMSOURCE;
import org.urm.server.action.ActionBase;
import org.urm.server.shell.ShellExecutor;
import org.urm.server.storage.FileSet;
import org.urm.server.storage.LocalFolder;
import org.urm.server.storage.RedistStorage;
import org.urm.server.storage.RemoteFolder;
import org.w3c.dom.Document;

public class Dist {

	public static String META_FILENAME = "release.xml";
	public static String CONFDIFF_FILENAME = "diffconf.txt";
	public static String STATE_FILENAME = "state.txt";
	public static String MD5_FILENAME = "state.md5";

	public static String BINARY_FOLDER = "binary";
	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String DBSCRIPTS_FOLDER = "scripts";
	public static String DBDATALOAD_FOLDER = "dataload";
	public static String ROLLBACK_FOLDER = "rollback";
	public static String DBMANUAL_FOLDER = "manual/db";
	
	public Metadata meta;
	public DistRepository repo;
	
	private RemoteFolder distFolder;
	
	public String RELEASEDIR;
	public Release release;
	public boolean prod;
	String infoPath;

	private FileSet files;

	DistState state;
	boolean openedForUse;
	boolean openedForChange;
	
	public Dist( Metadata meta , DistRepository repo ) {
		this.meta = meta;
		this.repo = repo;
	}
	
	public void setFolder( RemoteFolder distFolder , boolean prod ) {
		this.distFolder = distFolder;
		this.prod = prod;
		
		this.RELEASEDIR = distFolder.folderName;
				
		state = new DistState( this , distFolder );
		files = null;
		
		openedForUse = false;
		openedForChange = false;
	}

	public void open( ActionBase action ) throws Exception {
		boolean prod = ( action.context.env != null )? action.context.env.PROD : false;
		state.ctlOpenForUse( action , prod );
		openedForUse = true;
		gatherFiles( action );
	}
	
	public boolean isFinalized( ActionBase action ) throws Exception {
		return( state.isFinalized( action ) );
	}
	
	public boolean isRemote( ActionBase action ) throws Exception {
		return( distFolder.isRemote( action ) );
	}
	
	public String getState( ActionBase action ) throws Exception {
		return( state.state.name() );
	}
	
	public void load( ActionBase action ) throws Exception {
		action.debug( "loading release specification from " + META_FILENAME + " ..." );
		
		state.ctlLoadReleaseState( action );
		
		infoPath = distFolder.copyFileToLocal( action , action.getWorkFolder() , META_FILENAME , "" );
		release = new Release( meta , this );
		release.load( action , infoPath );
	}

	public boolean checkHash( ActionBase action ) throws Exception {
		String actualHash = state.getHashValue( action );
		if( actualHash.equals( state.stateHash ) )
			return( true );
		return( false );
	}
	
	public void copyConfToDistr( ActionBase action , LocalFolder sourceFolder , MetaDistrConfItem conf ) throws Exception {
		if( !openedForChange )
			action.exit( "distributive is not opened for change" );
		
		state.checkDistChangeEnabled( action );
		String parentFolder = getReleaseConfCompParentFolder( action , conf );
		distFolder.copyDirFromLocal( action , sourceFolder , parentFolder );
	}
	
	public void copyVFileToDistr( ActionBase action , MetaDistrBinaryItem distItem , LocalFolder sourceFolder , String FNAME , String BASENAME , String EXT ) throws Exception {
		if( !openedForChange )
			action.exit( "distributive is not opened for change" );
		
		state.checkDistChangeEnabled( action );
		String folder = getReleaseBinaryFolder( action , distItem );
		distFolder.copyVFileFromLocal( action , sourceFolder , FNAME , folder , BASENAME , EXT );
	}

	public void copyDatabaseFilesToDistr( ActionBase action , MetaDistrDelivery dbDelivery , LocalFolder srcPrepared ) throws Exception {
		if( !openedForChange )
			action.exit( "distributive is not opened for change" );
		
		state.checkDistChangeEnabled( action );
		String folder = getDeliveryDatabaseFolder( action , dbDelivery , release.RELEASEVER );
		distFolder.removeFolder( action , folder );
		
		String parentFolder = Common.getDirName( folder );
		distFolder.ensureFolderExists( action , parentFolder );
		distFolder.copyDirFromLocal( action , srcPrepared , parentFolder );
	}

	public void copyManualFilesToDistr( ActionBase action , LocalFolder src ) throws Exception {
		if( !openedForChange )
			action.exit( "distributive is not opened for change" );
		
		state.checkDistChangeEnabled( action );
		String folder = getManualFolder( action  );
		distFolder.removeFolder( action , folder );
		
		distFolder.ensureExists( action );
		distFolder.copyDirFromLocal( action , src , "" );
	}

	public void copyMD5StateFromLocal( ActionBase action , String srcPath ) throws Exception {
		distFolder.copyFileFromLocalRename( action , srcPath , MD5_FILENAME );
	}
	
	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String file ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		return( copyDistToFolder( action , workFolder , "" , file ) );
	}

	public String extractEmbeddedItemToFolder( ActionBase action , LocalFolder folder , MetaDistrBinaryItem item , String fileName ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );

		MetaDistrBinaryItem srcItem = item.srcItem;
		
		// extract on remote redist
		RedistStorage redist = action.artefactory.getRedistStorage( action , distFolder.account );
		RemoteFolder tmp = redist.getRedistTmpFolder( action , "dist" );
		
		String srcFilePath = distFolder.getFilePath( action , Common.getPath( srcItem.delivery.FOLDER , fileName ) );
		String distFile = item.DISTBASENAME + item.EXT;
		tmp.unzipSingleFile( action , srcFilePath , item.SRCITEMPATH , distFile );
		
		// move to folder
		tmp.copyFilesToLocal( action , folder , distFile );
		return( distFile );
	}

	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String srcSubdir , String file ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		action.debug( "copy from distributive " + file + " to " + workFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocal( action , workFolder , file , "" ) );
	}

	public String copyDistFileToFolderRename( ActionBase action , LocalFolder workFolder , String srcSubdir , String file , String newName ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		action.debug( "copy from distributive " + file + " to " + workFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocalRename( action , workFolder , file , newName ) );
	}
	
	public void unzipDistFileToFolder( ActionBase action , LocalFolder workFolder , String file , String FOLDER , String target , String part ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		String filePath = distFolder.copyFileToLocal( action , workFolder , file , FOLDER );
		action.session.unzipPart( action , workFolder.folderPath , filePath , target , part ); 
	}

	public boolean checkFileExists( ActionBase action , String path ) throws Exception {
		return( distFolder.checkFileExists( action , path ) );
	}
	
	public String getDistFolder( ActionBase action ) throws Exception {
		return( distFolder.folderName );
	}
	
	public String getDistPath( ActionBase action ) throws Exception {
		return( distFolder.folderPath );
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
		state.checkDistChangeEnabled( action );
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyFileFromLocal( action , filePath , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , ReleaseDelivery delivery , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , MetaDistrConfItem conf , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		ReleaseDelivery delivery = release.findDelivery( action , conf.delivery.NAME );
		if( delivery == null )
			action.exit( "unknown release delivery=" + conf.delivery.NAME );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		confFolder = Common.getPath( confFolder , conf.KEY );
		localFolder.ensureExists( action );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public boolean copyDistConfToFolder( ActionBase action , ReleaseTarget confTarget , LocalFolder parentFolder ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , confTarget.getDelivery( action ) );
		String compFolder = Common.getPath( confFolder , confTarget.distConfItem.KEY );
		RemoteFolder compDist = distFolder.getSubFolder( action , compFolder );
		if( !compDist.checkExists( action ) )
			return( false );
		
		compDist.copyDirToLocal( action , parentFolder );
		return( true );
	}
	
	public void createDeliveryFolders( ActionBase action ) throws Exception {
		state.checkDistChangeEnabled( action );
		for( ReleaseDelivery delivery : release.getDeliveries( action ).values() ) {
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
	public void create( ActionBase action , String RELEASEDIR ) throws Exception {
		this.RELEASEDIR = RELEASEDIR;
		state.ctlCreate( action );
		load( action );
	}

	public void createProd( ActionBase action , String RELEASEDIR ) throws Exception {
		this.RELEASEDIR = RELEASEDIR;
		state.ctlCreateProd( action , RELEASEDIR );
		load( action );
	}
	
	public void openForChange( ActionBase action ) throws Exception {
		state.ctlOpenForChange( action );
		openedForChange = true;
		openedForUse = true;
	}
	
	public void closeChange( ActionBase action ) throws Exception {
		state.ctlCloseChange( action );
		openedForChange = false;
		openedForUse = false;
	}

	public void forceClose( ActionBase action ) throws Exception {
		state.ctlForceClose( action );
	}

	public void finish( ActionBase action ) throws Exception {
		openForChange( action );

		DistFinalizer finalizer = new DistFinalizer( action , this , distFolder , release );
		if( !finalizer.finish() ) {
			action.error( "distributive is not ready to be finished" );
			state.ctlCloseChange( action );
			return;
		}
		
		state.ctlFinish( action );
	}

	public void reopen( ActionBase action ) throws Exception {
		state.ctlReopen( action );
		state.ctlCloseChange( action );
	}

	public void copyRelease( ActionBase action , Dist src ) throws Exception {
		String filePath = action.getWorkFilePath( Dist.META_FILENAME );
		
		String saveReleaseVer = release.RELEASEVER;
		release.copy( action , src.release );
		release.setReleaseVer( action , saveReleaseVer ); 
		Document doc = src.release.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		
		openForChange( action );
		
		distFolder.copyFileFromLocal( action , filePath );
		ShellExecutor shell = action.getShell( distFolder.account );
		for( ReleaseDelivery delivery : src.release.getDeliveries( action ).values() ) {
			String dirFrom = src.distFolder.getFolderPath( action , delivery.distDelivery.FOLDER );
			String dirTo = distFolder.getFolderPath( action , delivery.distDelivery.FOLDER );
			int timeout = action.setTimeoutUnlimited();
			shell.copyDirDirect( action , dirFrom , dirTo );
			action.setTimeout( timeout );
		}
		
		closeChange( action );
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
		state.ctlReloadCheckOpened( action );
		
		String filePath = action.getWorkFilePath( META_FILENAME );
		Document doc = release.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		distFolder.copyFileFromLocal( action , filePath );
	}

	public boolean addAllSource( ActionBase action , MetaSourceProjectSet set ) throws Exception {
		action.debug( "release - add source set=" + set.NAME );
		return( release.addSourceSet( action , set , true ) );
	}
	
	public boolean addAllCategory( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		action.debug( "release - add category=" + Common.getEnumLower( CATEGORY ) );
		return( release.addCategorySet( action , CATEGORY , true ) );
	}
	
	public boolean addProjectAllItems( ActionBase action , MetaSourceProject project ) throws Exception {
		action.debug( "release - add project=" + project.PROJECT );
		
		if( !release.addSourceSet( action , project.set , false ) )
			return( false );
		if( !release.addProject( action , project , true ) )
			return( false );
		return( true );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		action.debug( "release - add project=" + project.PROJECT + ", item=" + item.ITEMNAME );
		
		// ignore internal items
		if( item.INTERNAL ) {
			action.info( "item=" + item.ITEMNAME + " is internal. Skipped.");
			return( true );
		}
		
		if( !release.addCategorySet( action , project.CATEGORY , false ) )
			return( false );
		if( !release.addProject( action , project , false ) )
			return( false );
		if( !release.addProjectItem( action , project , item ) )
			return( false );
		return( true );
	}

	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		action.debug( "release - add conf item=" + item.KEY );
		
		if( !release.addCategorySet( action , VarCATEGORY.CONFIG , false ) )
			return( false );
		if( !release.addConfItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		action.debug( "release - add manual item=" + item.KEY );
		
		if( !release.addCategorySet( action , VarCATEGORY.MANUAL , false ) )
			return( false );
		if( !release.addManualItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		action.debug( "release - add database delivery=" + item.NAME );
		
		if( !release.addCategorySet( action , VarCATEGORY.DB , false ) )
			return( false );
		if( !release.addDatabaseItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabase( ActionBase action ) throws Exception {
		action.debug( "release - add database" );
		if( !release.addCategorySet( action , VarCATEGORY.DB , true ) )
			return( false );
		return( true );
	}

	public String getReleaseConfCompParentFolder( ActionBase action , MetaDistrConfItem comp ) throws Exception {
		ReleaseDelivery delivery = release.getDelivery( action , comp.delivery.NAME );
		String folder = getDeliveryConfFolder( action , delivery.distDelivery );
		return( folder );
	}
	
	public String getReleaseBinaryFolder( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		ReleaseDelivery delivery = release.getDelivery( action , item.delivery.NAME );
		String folder = getDeliveryBinaryFolder( action , delivery.distDelivery );
		return( folder );
	}
	
	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrBinaryItem item , boolean getMD5 ) throws Exception {
		DistItemInfo info = new DistItemInfo( item );
		if( item.isDerived( action ) ) {
			DistItemInfo infosrc = getDistItemInfo( action , item.srcItem , false );
			info.subPath = infosrc.subPath;
			info.fileName = infosrc.fileName;
			info.found = infosrc.found;
		}
		else {
			info.subPath = getReleaseBinaryFolder( action , item );
			info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
			info.found = ( info.fileName.isEmpty() )? false : true;
		}
		
		if( info.found && getMD5 ) {
			RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
			if( item.isDerived( action ) )
				info.md5value = fileFolder.getArchivePartMD5( action , info.fileName , item.SRCITEMPATH , item.srcItem.EXT );
			else
			if( item.isArchive( action ) )
				info.md5value = fileFolder.getArchiveContentMD5( action , info.fileName , item.EXT );
			else
				info.md5value = fileFolder.getFileMD5( action , info.fileName );
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
	
	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrConfItem item ) throws Exception {
		DistItemInfo info = new DistItemInfo( item );
		info.subPath = getReleaseConfCompParentFolder( action , item );
		info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
		info.found = ( info.fileName.isEmpty() )? false : true;
		return( info );
	}

	public void descopeSet( ActionBase action , ReleaseSet set ) throws Exception {
		state.ctlReloadCheckOpened( action );
		for( ReleaseTarget target : set.getTargets( action ).values() )
			dropTarget( action , target );
		
		if( action.meta.isSourceCategory( action , set.CATEGORY ) )
			release.deleteSourceSet( action , set.set );
		else
			release.deleteCategorySet( action , set.CATEGORY );
	}
	
	public void descopeTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		state.ctlReloadCheckOpened( action );
		dropTarget( action , target );
		release.deleteTarget( action , target );
	}
	
	public void descopeTargetItems( ActionBase action , ReleaseTargetItem[] items ) throws Exception {
		state.ctlReloadCheckOpened( action );
		for( ReleaseTargetItem item : items ) {
			dropTargetItem( action , item );
			release.deleteProjectItem( action , item );
		}
	}
	
	private void dropTarget( ActionBase action , ReleaseTarget target ) throws Exception {
		if( target.CATEGORY == VarCATEGORY.CONFIG ) {
			String folder = getDeliveryConfFolder( action , target.distConfItem.delivery );
			distFolder.removeFolder( action , folder );
		}
		else
		if( target.CATEGORY == VarCATEGORY.DB ) {
			String folder = getDeliveryDatabaseFolder( action , target.distDatabaseItem , release.RELEASEVER );
			distFolder.removeFolderContent( action , folder );
		}
		else
		if( target.CATEGORY == VarCATEGORY.MANUAL ) {
			String folder = getReleaseBinaryFolder( action , target.distManualItem );
			distFolder.deleteVFile( action , folder , target.distManualItem.DISTBASENAME , target.distManualItem.EXT );
		}
		else {
			for( ReleaseTargetItem item : target.getItems( action ).values() )
				dropTargetItem( action , item );
		}
	}

	private void dropTargetItem( ActionBase action , ReleaseTargetItem item ) throws Exception {
		String folder = getReleaseBinaryFolder( action , item.distItem );
		distFolder.deleteVFile( action , folder , item.distItem.DISTBASENAME , item.distItem.EXT );
	}

	public boolean checkIfReleaseItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		if( item.DISTSOURCE == VarDISTITEMSOURCE.MANUAL ) {
			ReleaseTarget target = release.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( false );
			return( true );
		}
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.DISTITEM )
			return( checkIfReleaseItem( action , item.srcItem ) );
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.BUILD ) {
			ReleaseTarget target = release.findBuildProject( action , item.sourceItem.project.PROJECT );
			if( target == null )
				return( false );
			
			if( target.getItem( action , item.KEY ) == null )
				return( false );
			
			return( true );
		}
		else
			action.exitUnexpectedState();
		
		return( false );
	}
	
	public String getBinaryDistItemFile( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		if( item.DISTSOURCE == VarDISTITEMSOURCE.MANUAL ) {
			ReleaseTarget target = release.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( "" );
			
			if( target.DISTFILE == null || target.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( BINARY_FOLDER , target.DISTFILE ) );
		}
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.BUILD ) {
			ReleaseTarget target = release.findBuildProject( action , item.sourceItem.project.PROJECT );
			if( target == null )
				return( "" );
			
			ReleaseTargetItem targetItem = target.getItem( action , item.KEY );
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
		
		for( ReleaseDelivery delivery : release.getDeliveries( action ).values() ) {
			FileSet deliveryFiles = files.getDirByPath( action , delivery.distDelivery.FOLDER );
			
			for( ReleaseTargetItem targetItem : delivery.getProjectItems( action ).values() )
				gatherDeliveryBinaryItem( action , delivery , deliveryFiles , targetItem );
				
			for( ReleaseTarget targetItem : delivery.getManualItems( action ).values() )
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
		action.trace( "item=" + targetItem.distItem.KEY + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}

	private void gatherDeliveryManualItem( ActionBase action , ReleaseDelivery delivery , FileSet deliveryFiles , ReleaseTarget targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , BINARY_FOLDER );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distManualItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distManualItem.KEY + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}
	
	public MetaDistrConfItem[] getLocationConfItems( ActionBase action , MetaEnvServerLocation[] locations ) throws Exception {
		Map<String,MetaDistrConfItem> confs = new HashMap<String,MetaDistrConfItem>(); 
		for( MetaEnvServerLocation location : locations ) {
			String[] items = location.getConfItems( action );
			for( String item : items ) {
				MetaDistrConfItem conf = meta.distr.getConfItem( action , item );
				if( release.findConfComponent( action , conf.KEY ) == null )
					continue;
				
				if( !confs.containsKey( conf.KEY ) )
					confs.put( conf.KEY , conf );
			}
		}
		return( confs.values().toArray( new MetaDistrConfItem[0] ) );
	}

	public String[] getManualDatabaseFiles( ActionBase action ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		FileSet set = files.getDirByPath( action , DBMANUAL_FOLDER );
		if( set == null )
			return( new String[0] );
		
		return( set.files.keySet().toArray( new String[0] ) );
	}

	public String findManualDatabaseItemFile( ActionBase action , String index ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		FileSet set = files.getDirByPath( action , "manual/db" );
		if( set == null )
			return( null );
		
		for( String file : set.files.keySet() ) {
			if( file.startsWith( index + "-" ) )
				return( file );
		}
		
		return( null );
	}

	public void copyDistDatabaseManualFileToFolder( ActionBase action , LocalFolder dstFolder , String file ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		copyDistToFolder( action , dstFolder , "manual/db" , file );
	}

	public void copyDistItemToTarget( ActionBase action , MetaDistrBinaryItem item , String fileName , RemoteFolder locationDir , String redistFileName ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
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
			action.exit( "distributive is not opened for change" );
		
		action.info( "remove distributive content ..." );
		for( String dir : distFolder.getTopDirs( action ) )
			distFolder.removeFolder( action , dir );
		
		action.info( "remove all scope ..." );
		release.descopeAll( action );
	}

	public void copyDatabaseDistrToDistr( ActionBase action , ReleaseDelivery delivery , Dist src ) throws Exception {
		ReleaseDelivery reldel = src.release.findDelivery( action , delivery.distDelivery.NAME );
		if( reldel != null ) {
			String folder = src.getDeliveryDatabaseFolder( action , reldel.distDelivery , src.release.RELEASEVER );
			if( src.distFolder.checkFolderExists( action , folder ) )
				distFolder.copyDir( action , src.distFolder.getFilePath( action , folder ) , folder );
		}
	}
	
	public void copyBinaryDistrToDistr( ActionBase action , ReleaseDelivery delivery , Dist src , String file ) throws Exception {
		ReleaseDelivery reldel = src.release.findDelivery( action , delivery.distDelivery.NAME );
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
		ReleaseDelivery reldel = src.release.findDelivery( action , delivery.distDelivery.NAME );
		if( reldel != null ) {
			String folder = src.getDeliveryConfFolder( action , reldel.distDelivery );
			ShellExecutor session = distFolder.getSession( action );
			String folderSrc = src.distFolder.getFilePath( action , Common.getPath( folder , item.KEY ) );
			String folderDst = distFolder.getFilePath( action , Common.getPath( folder , item.KEY ) );
			distFolder.ensureFolderExists( action , folderDst );
			session.copyDirContent( action , folderSrc , folderDst );
		}
	}
	
}
