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
import ru.egov.urm.run.ActionBase;

public class DistStorage {

	public static String metaFileName = "release.xml";
	public static String confDiffFileName = "diffconf.txt";
	public static String stateFileName = "state.txt";

	Artefactory artefactory;
	public LocalFolder localFolder;
	private RemoteFolder distFolder;
	Metadata meta;
	
	public String RELEASEDIR;
	public MetaRelease info;
	String infoPath;

	FileSet files;

	ReleaseState state; 
	
	public DistStorage( Artefactory artefactory , LocalFolder localFolder , RemoteFolder distFolder ) {
		this.artefactory = artefactory; 
		this.localFolder = localFolder;
		this.distFolder = distFolder;
		this.meta = localFolder.meta;
				
		RELEASEDIR = distFolder.folderName;
		state = new ReleaseState( distFolder );
		files = null;
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
		
		infoPath = copyDistToFolder( action , artefactory.workFolder , metaFileName );
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
		
		String parentFolder = Common.getDirName( folder );
		distFolder.ensureFolderExists( action , parentFolder );
		distFolder.copyDirFromLocal( action , src , parentFolder );
	}

	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String file ) throws Exception {
		return( copyDistToFolder( action , workFolder , "" , file ) );
	}
	
	public String copyDistToFolder( ActionBase action , LocalFolder workFolder , String srcSubdir , String file ) throws Exception {
		action.debug( "copy from distributive " + file + " to " + localFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocal( action , localFolder , file , "" ) );
	}

	public String copyDistFileToFolderRename( ActionBase action , LocalFolder workFolder , String srcSubdir , String file , String newName ) throws Exception {
		action.debug( "copy from distributive " + file + " to " + localFolder.folderPath + " ..." );
		RemoteFolder srcFolder = distFolder.getSubFolder( action , srcSubdir ); 
		return( srcFolder.copyFileToLocalRename( action , localFolder , file , newName ) );
	}
	
	public void unzipDistFileToFolder( ActionBase action , LocalFolder workFolder , String file , String FOLDER , String target , String part ) throws Exception {
		String filePath = distFolder.copyFileToLocal( action , localFolder , file , FOLDER );
		action.session.unzip( action , localFolder.folderPath , filePath , part , target ); 
	}

	public String getDistPath( ActionBase action ) throws Exception {
		return( distFolder.folderPath );
	}
	
	public boolean checkFileExists( ActionBase action , String path ) throws Exception {
		return( distFolder.checkFileExists( action , path ) );
	}
	
	public String getDistFolder( ActionBase action ) throws Exception {
		return( distFolder.folderName );
	}
	
	public String getManualFolder( ActionBase action ) throws Exception {
		return( Common.getPath( distFolder.folderName , "manual" ) );
	}
	
	public String getDeliveryConfFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDERPATH , "config" ) );
	}
	
	public String getDeliveryDatabaseFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDERPATH , "db" ) );
	}
	
	public String getDeliveryBinaryFolder( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		return( Common.getPath( delivery.FOLDERPATH , "binary" ) );
	}
	
	public void replaceConfDiffFile( ActionBase action , String filePath , MetaReleaseDelivery delivery ) throws Exception {
		state.checkDistChangeEnabled( action );
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyFileFromLocal( action , filePath , confFolder );
	}
	
	public void copyDistConfToFolder( ActionBase action , MetaReleaseDelivery delivery , LocalFolder localFolder ) throws Exception {
		String confFolder = getDeliveryConfFolder( action , delivery.distDelivery );
		distFolder.copyDirContentToLocal( action , localFolder , confFolder );
	}
	
	public boolean copyDistConfToFolder( ActionBase action , MetaReleaseTarget confTarget , LocalFolder parentFolder ) throws Exception {
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
		state.ctlCreate( action , BUILDMODE , RELEASEDIR );
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
		state.ctlReloadCheckOpened( action );

		// drop empty directories
		// calculate hash and finish release
		String hash = getContentHash( action );
		
		state.ctlFinish( action , hash );
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
	
	private String getContentHash( ActionBase action ) throws Exception {
		return( "" );
	}
	
	public void saveReleaseXml( ActionBase action ) throws Exception {
		state.ctlReloadCheckOpened( action );
		
		String filePath = artefactory.workFolder.getFilePath( action , metaFileName );
		Document doc = info.createXml( action );
		Common.xmlSaveDoc( doc , filePath );
		distFolder.copyFileFromLocal( action , filePath );
	}

	public boolean addAllSource( ActionBase action , MetaSourceProjectSet set ) throws Exception {
		return( info.addSourceSet( action , set , true ) );
	}
	
	public boolean addAllCategory( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		return( info.addCategorySet( action , CATEGORY , true ) );
	}
	
	public boolean addProjectAllItems( ActionBase action , MetaSourceProject project ) throws Exception {
		if( !info.addSourceSet( action , project.set , false ) )
			return( false );
		if( !info.addProject( action , project , true ) )
			return( false );
		return( true );
	}

	public boolean addProjectItem( ActionBase action , MetaSourceProject project , MetaSourceProjectItem item ) throws Exception {
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
		if( !info.addCategorySet( action , VarCATEGORY.CONFIG , false ) )
			return( false );
		if( !info.addConfItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addManualItem( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( !info.addCategorySet( action , VarCATEGORY.MANUAL , false ) )
			return( false );
		if( !info.addManualItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabaseItem( ActionBase action , MetaDistrDelivery item ) throws Exception {
		if( !info.addCategorySet( action , VarCATEGORY.DB , false ) )
			return( false );
		if( !info.addDatabaseItem( action , item ) )
			return( false );
		return( true );
	}

	public boolean addDatabase( ActionBase action ) throws Exception {
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
	
	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		DistItemInfo info = new DistItemInfo( item );
		info.subPath = getReleaseBinaryFolder( action , item );
		info.fileName = getFiles( action ).findDistItem( action , item , info.subPath );
		info.found = ( info.fileName.isEmpty() )? false : true;
		return( info );
	}

	public DistItemInfo getDistItemInfo( ActionBase action , MetaDistrConfItem item ) throws Exception {
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
		if( item.MANUAL ) {
			MetaReleaseTarget target = info.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( false );
			return( true );
		}
		
		MetaReleaseTarget target = info.findBuildProject( action , item.sourceItem.project.PROJECT );
		if( target == null )
			return( false );
		
		if( target.getItem( action , item.KEY ) == null )
			return( false );
		
		return( true );
	}
	
	public String getBinaryDistItemFile( ActionBase action , MetaDistrBinaryItem item ) throws Exception {
		if( item.MANUAL ) {
			MetaReleaseTarget target = info.findCategoryTarget( action , VarCATEGORY.MANUAL , item.KEY );
			if( target == null )
				return( "" );
			
			if( target.DISTFILE == null )
				return( "" );
			
			return( target.DISTFILE );
		}
		
		MetaReleaseTarget target = info.findBuildProject( action , item.sourceItem.project.PROJECT );
		if( target == null )
			return( "" );
		
		MetaReleaseTargetItem targetItem = target.getItem( action , item.KEY );
		if( targetItem == null )
			return( "" );
		
		if( targetItem.DISTFILE == null )
			return( "" );
		return( targetItem.DISTFILE );
	}
	
	public void gatherFiles( ActionBase action ) throws Exception {
		action.log( "find distributive files ..." );
		FileSet files = distFolder.getFileSet( action );
		
		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			FileSet deliveryFiles = files.getDirByPath( action , delivery.distDelivery.FOLDERPATH );
			
			for( MetaReleaseTargetItem targetItem : delivery.getProjectItems( action ).values() )
				gatherDeliveryBinaryItem( action , delivery , deliveryFiles , targetItem );
				
			for( MetaReleaseTarget targetItem : delivery.getManualItems( action ).values() )
				gatherDeliveryManualItem( action , delivery , deliveryFiles , targetItem );
		}
	}

	public void gatherDeliveryBinaryItem( ActionBase action , MetaReleaseDelivery delivery , FileSet deliveryFiles , MetaReleaseTargetItem targetItem ) throws Exception {
		FileSet binaryFiles = null;
		if( deliveryFiles != null )
			binaryFiles = deliveryFiles.getDirByPath( action , "binary" );
		String fileName = "";
		
		if( binaryFiles != null )
			fileName = binaryFiles.findDistItem( action , targetItem.distItem );
		targetItem.setDistFile( action , fileName );
		action.trace( "item=" + targetItem.distItem.KEY + ", file=" + ( ( fileName.isEmpty() )? "(missing)" : fileName ) );
	}

	public void gatherDeliveryManualItem( ActionBase action , MetaReleaseDelivery delivery , FileSet deliveryFiles , MetaReleaseTarget targetItem ) throws Exception {
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
	
}
