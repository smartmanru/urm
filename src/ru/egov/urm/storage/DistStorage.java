package ru.egov.urm.storage;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvServerLocation;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.MetaReleaseSet;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.MetaSourceProjectSet;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarDISTITEMSOURCE;
import ru.egov.urm.meta.Metadata.VarDISTITEMTYPE;
import ru.egov.urm.run.ActionBase;

public class DistStorage {

	public static String metaFileName = "release.xml";
	public static String confDiffFileName = "diffconf.txt";
	public static String stateFileName = "state.txt";

	Artefactory artefactory;
	private RemoteFolder distFolder;
	Metadata meta;
	
	public String RELEASEDIR;
	public MetaRelease info;
	public boolean prod;
	String infoPath;

	private FileSet files;

	ReleaseState state;
	boolean openedForUse;
	
	public DistStorage( Artefactory artefactory , RemoteFolder distFolder , boolean prod ) {
		this.artefactory = artefactory; 
		this.distFolder = distFolder;
		this.meta = artefactory.meta;
		this.prod = prod;
				
		RELEASEDIR = distFolder.folderName;
		state = new ReleaseState( distFolder );
		files = null;
		openedForUse = false;
	}

	public void open( ActionBase action ) throws Exception {
		state.ctlOpenForUse( action , action.context.env.PROD );
		openedForUse = true;
		gatherFiles( action );
	}
	
	public boolean isRemote( ActionBase action ) throws Exception {
		return( distFolder.isRemote( action ) );
	}
	
	public String getState( ActionBase action ) throws Exception {
		return( state.state.name() );
	}
	
	public void load( ActionBase action ) throws Exception {
		action.debug( "loading release " + RELEASEDIR + " ...");
		
		state.ctlLoadReleaseState( action );
		
		infoPath = distFolder.copyFileToLocal( action , artefactory.workFolder , metaFileName , "" );
		info = new MetaRelease( meta );
		
		String RELEASEVER = Common.getPartBeforeFirst( RELEASEDIR , "-" );
		info.load( action , RELEASEVER , infoPath );
	}

	public void copyConfToDistr( ActionBase action , LocalFolder sourceFolder , MetaDistrConfItem conf ) throws Exception {
		state.checkDistChangeEnabled( action );
		String parentFolder = getReleaseConfCompParentFolder( action , conf );
		distFolder.copyDirFromLocal( action , sourceFolder , parentFolder );
	}
	
	public void copyVFileToDistr( ActionBase action , MetaDistrBinaryItem distItem , LocalFolder sourceFolder , String FNAME , String BASENAME , String EXT ) throws Exception {
		state.checkDistChangeEnabled( action );
		String folder = getReleaseBinaryFolder( action , distItem );
		distFolder.copyVFileFromLocal( action , sourceFolder , FNAME , folder , BASENAME , EXT );
	}

	public void copyDatabaseFilesToDistr( ActionBase action , MetaDistrDelivery dbDelivery , LocalFolder srcPrepared ) throws Exception {
		state.checkDistChangeEnabled( action );
		String folder = getDeliveryDatabaseFolder( action , dbDelivery );
		distFolder.removeFolder( action , folder );
		
		String parentFolder = Common.getDirName( folder );
		distFolder.ensureFolderExists( action , parentFolder );
		distFolder.copyDirFromLocal( action , srcPrepared , parentFolder );
	}

	public void copyManualFilesToDistr( ActionBase action , LocalFolder src ) throws Exception {
		state.checkDistChangeEnabled( action );
		String folder = getManualFolder( action  );
		distFolder.removeFolder( action , folder );
		
		distFolder.ensureExists( action );
		distFolder.copyDirFromLocal( action , src , "" );
	}

	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String file ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		return( copyDistToFolder( action , workFolder , "" , file ) );
	}

	public String copyEmbeddedItemToFolder( ActionBase action , LocalFolder folder , MetaDistrBinaryItem item , String fileName ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );

		MetaDistrBinaryItem srcItem = item.srcItem;
		
		// extract on remote redist
		RedistStorage redist = artefactory.getRedistStorage( "dist" , distFolder.account );
		RemoteFolder tmp = redist.getRedistTmpFolder( action );
		tmp.ensureExists( action );
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
		action.session.unzipPart( action , workFolder.folderPath , filePath , part , target ); 
	}

	public boolean checkFileExists( ActionBase action , String path ) throws Exception {
		return( distFolder.checkFileExists( action , path ) );
	}
	
	public String getDistFolder( ActionBase action ) throws Exception {
		return( distFolder.folderName );
	}
	
	public String getManualFolder( ActionBase action ) throws Exception {
		return( "manual" );
	}
	
	public String getDeliveryConfFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , "config" ) );
	}
	
	public String getDeliveryDatabaseFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , "db" ) );
	}
	
	public String getDeliveryDatabaseScriptFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery ) , "scripts" ) );
	}
	
	public String getDeliveryDatabaseLoadFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( getDeliveryDatabaseFolder( action , delivery ) , "dataload" ) );
	}
	
	public String getDeliveryBinaryFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDER , "binary" ) );
	}
	
	public void replaceConfDiffFile( ActionBase action , String filePath , MetaReleaseDelivery delivery ) throws Exception {
		state.checkDistChangeEnabled( action );
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyFileFromLocal( action , filePath , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , MetaReleaseDelivery delivery , LocalFolder localFolder ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public boolean copyDistConfToFolder( ActionBase action , MetaReleaseTarget confTarget , LocalFolder parentFolder ) throws Exception {
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
		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			createInternalDeliveryFolder( action , getDeliveryBinaryFolder( action , delivery.distDelivery ) );
			createInternalDeliveryFolder( action , getDeliveryConfFolder( action , delivery.distDelivery ) );
			createInternalDeliveryFolder( action , getDeliveryDatabaseFolder( action , delivery.distDelivery ) );
		}
	}

	private void createInternalDeliveryFolder( ActionBase action , String folder ) throws Exception {
		RemoteFolder subFolder = distFolder.getSubFolder( action , folder );
		subFolder.ensureExists( action );
	}
	
	// top-level control
	public void create( ActionBase action , VarBUILDMODE BUILDMODE ) throws Exception {
		state.ctlCreate( action , BUILDMODE );
		load( action );
	}

	public void createProd( ActionBase action , String RELEASEVER ) throws Exception {
		state.ctlCreateProd( action , RELEASEVER );
		load( action );
	}
	
	public void openForChange( ActionBase action ) throws Exception {
		state.ctlOpenForChange( action );
	}
	
	public void closeChange( ActionBase action ) throws Exception {
		state.ctlCloseChange( action );
	}

	public void forceClose( ActionBase action ) throws Exception {
		state.ctlForceClose( action );
	}

	public void finish( ActionBase action ) throws Exception {
		state.ctlOpenForChange( action );

		// check consistency, drop empty directories
		FileSet fsd = distFolder.getFileSet( action );
		FileSet fsr = createExpectedFileSet( action );  
		if( !finishDist( action , fsd , fsr ) ) {
			action.log( "distributive is not ready to be finished" );
			state.ctlCloseChange( action );
			return;
		}
		
		// finish release
		state.ctlFinish( action );
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
		
		String filePath = artefactory.workFolder.getFilePath( action , metaFileName );
		Document doc = info.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		distFolder.copyFileFromLocal( action , filePath );
	}

	public boolean addAllSource( ActionBase action , MetaSourceProjectSet set ) throws Exception {
		action.debug( "release - add source set=" + set.NAME );
		return( info.addSourceSet( action , set , true ) );
	}
	
	public boolean addAllCategory( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		action.debug( "release - add category=" + Common.getEnumLower( CATEGORY ) );
		return( info.addCategorySet( action , CATEGORY , true ) );
	}
	
	public boolean addProjectAllItems( ActionBase action , MetaSourceProject project ) throws Exception {
		action.debug( "release - add project=" + project.PROJECT );
		
		if( !info.addSourceSet( action , project.set , false ) )
			return( false );
		if( !info.addProject( action , project , true ) )
			return( false );
		return( true );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
		action.debug( "release - add project=" + project.PROJECT + ", item=" + item.ITEMNAME );
		
		// ignore internal items
		if( item.INTERNAL ) {
			action.log( "item=" + item.ITEMNAME + " is internal. Skipped.");
			return( true );
		}
		
		if( !info.addCategorySet( action , project.CATEGORY , false ) )
			return( false );
		if( !info.addProject( action , project , false ) )
			return( false );
		if( !info.addProjectItem( action , project , item ) )
			return( false );
		return( true );
	}

	public boolean addConfItem( ActionBase action , MetaDistrConfItem item ) throws Exception {
		action.debug( "release - add conf item=" + item.KEY );
		
		if( !info.addCategorySet( action , VarCATEGORY.CONFIG , false ) )
			return( false );
		if( !info.addConfItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		action.debug( "release - add manual item=" + item.KEY );
		
		if( !info.addCategorySet( action , VarCATEGORY.MANUAL , false ) )
			return( false );
		if( !info.addManualItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		action.debug( "release - add database delivery=" + item.NAME );
		
		if( !info.addCategorySet( action , VarCATEGORY.DB , false ) )
			return( false );
		if( !info.addDatabaseItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabase( ActionBase action ) throws Exception {
		action.debug( "release - add database" );
		if( !info.addCategorySet( action , VarCATEGORY.DB , true ) )
			return( false );
		return( true );
	}

	public String getReleaseConfCompParentFolder( ActionBase action , MetaDistrConfItem comp ) throws Exception {
		MetaReleaseDelivery delivery = info.getDelivery( action , comp.delivery.NAME );
		String folder = getDeliveryConfFolder( action , delivery.distDelivery );
		return( folder );
	}
	
	public String getReleaseBinaryFolder( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		MetaReleaseDelivery delivery = info.getDelivery( action , item.delivery.NAME );
		String folder = getDeliveryBinaryFolder( action , delivery.distDelivery );
		return( folder );
	}
	
	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrBinaryItem item , boolean getMD5 ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		DistItemInfo info = new DistItemInfo( item );
		info.subPath = getReleaseBinaryFolder( action , item );
		info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
		info.found = ( info.fileName.isEmpty() )? false : true;
		
		if( info.found && getMD5 ) {
			RemoteFolder fileFolder = distFolder.getSubFolder( action , info.subPath );
			if( item.DISTTYPE == VarDISTITEMTYPE.BINARY )
				info.md5value = fileFolder.getFileMD5( action , info.fileName );
			else {
				RedistStorage redist = artefactory.getRedistStorage( "tmp" , fileFolder.account );
				RemoteFolder tmp = redist.getRedistTmpFolder( action );
				RemoteFolder tmpTar = tmp.getSubFolder( action , "tar" );
				tmpTar.recreateThis( action );
				tmpTar.extractTarGz( action , fileFolder.getFilePath( action , info.fileName ) , "" );
				info.md5value = redist.getArchiveMD5( action , item , tmpTar , false );
				tmpTar.removeThis( action );
			}
		}
		
		return( info );
	}

	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrConfItem item ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		DistItemInfo info = new DistItemInfo( item );
		info.subPath = getReleaseConfCompParentFolder( action , item );
		info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
		info.found = ( info.fileName.isEmpty() )? false : true;
		return( info );
	}

	public void descopeSet( ActionBase action , MetaReleaseSet set ) throws Exception {
		state.ctlReloadCheckOpened( action );
		for( MetaReleaseTarget target : set.getTargets( action ).values() )
			dropTarget( action , target );
		
		if( action.meta.isSourceCategory( action , set.CATEGORY ) )
			info.deleteSourceSet( action , set.set );
		else
			info.deleteCategorySet( action , set.CATEGORY );
	}
	
	public void descopeTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		state.ctlReloadCheckOpened( action );
		dropTarget( action , target );
		info.deleteTarget( action , target );
	}
	
	public void descopeTargetItems( ActionBase action , MetaReleaseTargetItem[] items ) throws Exception {
		state.ctlReloadCheckOpened( action );
		for( MetaReleaseTargetItem item : items ) {
			dropTargetItem( action , item );
			info.deleteProjectItem( action , item );
		}
	}
	
	private void dropTarget( ActionBase action , MetaReleaseTarget target ) throws Exception {
		if( target.CATEGORY == VarCATEGORY.CONFIG ) {
			String folder = getDeliveryConfFolder( action , target.distConfItem.delivery );
			distFolder.removeFolder( action , folder );
		}
		else
		if( target.CATEGORY == VarCATEGORY.DB ) {
			String folder = getDeliveryDatabaseFolder( action , target.distDatabaseItem );
			distFolder.removeFolderContent( action , folder );
		}
		else
		if( target.CATEGORY == VarCATEGORY.MANUAL ) {
			String folder = getReleaseBinaryFolder( action , target.distManualItem );
			distFolder.deleteVFile( action , folder , target.distManualItem.DISTBASENAME , target.distManualItem.EXT );
		}
		else {
			for( MetaReleaseTargetItem item : target.getItems( action ).values() )
				dropTargetItem( action , item );
		}
	}

	private void dropTargetItem( ActionBase action , MetaReleaseTargetItem item ) throws Exception {
		String folder = getReleaseBinaryFolder( action , item.distItem );
		distFolder.deleteVFile( action , folder , item.distItem.DISTBASENAME , item.distItem.EXT );
	}

	public boolean checkIfReleaseItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		if( item.DISTSOURCE == VarDISTITEMSOURCE.MANUAL ) {
			MetaReleaseTarget target = info.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( false );
			return( true );
		}
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.DISTITEM )
			return( checkIfReleaseItem( action , item.srcItem ) );
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.BUILD ) {
			MetaReleaseTarget target = info.findBuildProject( action , item.sourceItem.project.PROJECT );
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
			MetaReleaseTarget target = info.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( "" );
			
			if( target.DISTFILE == null || target.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( "binary" , target.DISTFILE ) );
		}
		else if( item.DISTSOURCE == VarDISTITEMSOURCE.BUILD ) {
			MetaReleaseTarget target = info.findBuildProject( action , item.sourceItem.project.PROJECT );
			if( target == null )
				return( "" );
			
			MetaReleaseTargetItem targetItem = target.getItem( action , item.KEY );
			if( targetItem == null )
				return( "" );
			
			if( targetItem.DISTFILE == null || targetItem.DISTFILE.isEmpty() )
				return( "" );
			
			return( Common.getPath( "binary" , targetItem.DISTFILE ) );
		}
		else
			action.exitUnexpectedState();
		
		return( null );
	}

	private void gatherFiles( ActionBase action ) throws Exception {
		action.log( "find distributive files ..." );
		files = distFolder.getFileSet( action );
		
		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			FileSet deliveryFiles = files.getDirByPath( action , delivery.distDelivery.FOLDER );
			
			for( MetaReleaseTargetItem targetItem : delivery.getProjectItems( action ).values() )
				gatherDeliveryBinaryItem( action , delivery , deliveryFiles , targetItem );
				
			for( MetaReleaseTarget targetItem : delivery.getManualItems( action ).values() )
				gatherDeliveryManualItem( action , delivery , deliveryFiles , targetItem );
		}
	}

	private void gatherDeliveryBinaryItem( ActionBase action , MetaReleaseDelivery delivery , FileSet deliveryFiles , MetaReleaseTargetItem targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , "binary" );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distItem.KEY + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}

	private void gatherDeliveryManualItem( ActionBase action , MetaReleaseDelivery delivery , FileSet deliveryFiles , MetaReleaseTarget targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , "binary" );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distManualItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distManualItem.KEY + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}
	
	public MetaDistrConfItem[] getLocationConfItems( ActionBase action , MetaEnvServerLocation[] locations ) throws Exception {
		Map<String,MetaDistrConfItem> confs = new HashMap<String,MetaDistrConfItem>(); 
		for( MetaEnvServerLocation location : locations )
			for( MetaDistrConfItem conf : location.confItems.values() ) {
				if( info.findConfComponent( action , conf.KEY ) == null )
					continue;
				
				if( !confs.containsKey( conf.KEY ) )
					confs.put( conf.KEY , conf );
			}
		return( confs.values().toArray( new MetaDistrConfItem[0] ) );
	}

	public String[] getManualDatabaseFiles( ActionBase action ) throws Exception {
		if( !openedForUse )
			action.exit( "distributive is not opened for use" );
		
		FileSet set = files.getDirByPath( action , "manual/db" );
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
			LocalFolder work = artefactory.getWorkFolder( action , "copy" );
			copyDistFileToFolderRename( action , work , item.delivery.FOLDER , fileName , redistFileName );
			locationDir.copyFileFromLocal( action , work.getFilePath( action , redistFileName ) );
		}
		else {
			String path = Common.getPath( distFolder.folderPath , item.delivery.FOLDER , fileName );
			locationDir.copyFileFromLocalRename( action , path , redistFileName );
		}
	}

	private FileSet createExpectedFileSet( ActionBase action ) throws Exception {
		gatherFiles( action );
		
		FileSet fs = new FileSet( null );
		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaReleaseTarget item : delivery.getConfItems( action ).values() )
				createExpectedConfDeliveryItem( action , fs , delivery , item );
			for( MetaReleaseTargetItem item : delivery.getProjectItems( action ).values() )
				createExpectedProjectDeliveryItem( action , fs , delivery , item );
			for( MetaReleaseTarget item : delivery.getManualItems( action ).values() )
				createExpectedManualDeliveryItem( action , fs , delivery , item );
			MetaReleaseTarget dbitem = delivery.getDatabaseItem( action );
			if( dbitem != null )
				createExpectedDatabaseDeliveryItem( action , fs , delivery , dbitem );
		}
		
		return( fs );
	}
	
	private void createExpectedConfDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTarget item ) throws Exception {
		FileSet dir = fs.createDir( getDeliveryConfFolder( action , delivery.distDelivery ) );
		dir.createDir( item.distConfItem.KEY );
	}
	
	private void createExpectedProjectDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTargetItem item ) throws Exception {
		FileSet dir = fs.createDir( getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		if( !item.DISTFILE.isEmpty() )
			dir.addFile( item.DISTFILE );
		else
			dir.addFile( item.distItem.getBaseFile( action ) );
	}
	
	private void createExpectedManualDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTarget item ) throws Exception {
		FileSet dir = fs.createDir( getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		if( !item.DISTFILE.isEmpty() )
			dir.addFile( item.DISTFILE );
		else
			dir.addFile( item.distManualItem.getBaseFile( action ) );
	}
	
	private void createExpectedDatabaseDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTarget item ) throws Exception {
		fs.createDir( getDeliveryDatabaseFolder( action , delivery.distDelivery ) );
	}
	
	private boolean finishDist( ActionBase action , FileSet fsd , FileSet fsr ) throws Exception {
		// check expected directory set is the same as actual
		// folders = deliveries
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			MetaReleaseDelivery delivery = info.findDeliveryByFolder( action , dir );
			if( delivery == null || delivery.isEmpty() ) {
				if( dirFilesDist.hasFiles() ) {
					if( !action.context.CTX_FORCE )
						action.exit( "distributive delivery " + dir + " has files, while nothing is declared in release" );
				}
				
				action.log( "delete non-release delivery=" + dir + " ..." );
				distFolder.removeFiles( action , dir );
			}
			else {
				FileSet dirFilesRelease = fsr.dirs.get( dir );
				if( !finishDistDelivery( action , delivery , dirFilesDist , dirFilesRelease ) )
					return( false );
			}
		}
		
		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.log( "distributive has missing delivery=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDelivery( ActionBase action , MetaReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		// check by category
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			FileSet dirFilesRelease = fsr.dirs.get( dir );
			if( dirFilesRelease == null ) {
				if( dirFilesDist.hasFiles() ) {
					if( !action.context.CTX_FORCE )
						action.exit( "distributive delivery " + delivery.distDelivery.NAME + 
								" dir= " + dir + " has files, while nothing is declared in release" );
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , dir );
				action.log( "delete non-release delivery folder=" + folder + " ..." );
				distFolder.removeFolder( action , folder );
			}
			else {
				if( dir.equals( "binary" ) ) {
					if( !finishDistDeliveryBinary( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
				else
				if( dir.equals( "config" ) ) {
					if( !finishDistDeliveryConfig( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
			}
		}

		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.log( "distributive has missing delivery=" + delivery.distDelivery.NAME + ", category=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryBinary( ActionBase action , MetaReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String fileDist : fsd.files.keySet() ) {
			String fileRelease = fsr.files.get( fileDist );
			if( fileRelease == null ) {
				if( !action.context.CTX_FORCE )
					action.exit( "distributive delivery " + delivery.distDelivery.NAME + 
						" dir= " + delivery.distDelivery.FOLDER + " has non-release file=" + fileDist );
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , "binary" );
				action.log( "delete non-release delivery item folder=" + folder + " file=" + fileDist + " ..." );
				distFolder.removeFolderFile( action , folder , fileDist );
			}
		}
		
		for( String fileRelease : fsr.files.keySet() ) {
			String fileDist = fsd.files.get( fileRelease );
			if( fileDist == null ) {
				action.log( "distributive has missing delivery=" + delivery.distDelivery.NAME + " file=" + fileRelease );
				return( false );
			}
		}
		
		return( true );
	}
	
	private boolean finishDistDeliveryConfig( ActionBase action , MetaReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesRelease = fsr.dirs.get( dir );
			if( dirFilesRelease == null ) {
				if( !action.context.CTX_FORCE )
					action.exit( "distributive delivery " + delivery.distDelivery.NAME + 
							" has non-release config=" + dir );
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , "config" , dir );
				action.log( "delete non-release configuration item delivery=" + delivery.distDelivery.NAME + " config=" + dir + " ..." );
				distFolder.removeFolder( action , folder );
			}
		}
		
		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.log( "distributive has missing delivery=" + delivery.distDelivery.NAME + ", config=" + dir );
				return( false );
			}
		}
		
		return( true );
	}
	
}
