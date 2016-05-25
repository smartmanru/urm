package ru.egov.urm.dist;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.storage.FileSet;
import ru.egov.urm.storage.RemoteFolder;

public class DistFinalizer {

	ActionBase action;
	Dist dist;
	RemoteFolder distFolder;
	MetaRelease info;
	
	public DistFinalizer( ActionBase action , Dist dist , RemoteFolder distFolder , MetaRelease info ) {
		this.action = action;
		this.dist = dist;
		this.distFolder = distFolder;
		this.info = info;
	}
	
	public boolean finish() throws Exception {
		// check consistency, drop empty directories
		FileSet fsd = distFolder.getFileSet( action );
		FileSet fsr = createExpectedFileSet( action );  
		if( !finishDist( action , fsd , fsr ) )
			return( false );
		
		// finish release
		createMD5( action );
		return( true );
	}
	
	private FileSet createExpectedFileSet( ActionBase action ) throws Exception {
		dist.gatherFiles( action );
		
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
		FileSet dir = fs.createDir( dist.getDeliveryConfFolder( action , delivery.distDelivery ) );
		dir.createDir( item.distConfItem.KEY );
	}
	
	private void createExpectedProjectDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTargetItem item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		if( !item.DISTFILE.isEmpty() )
			dir.addFile( item.DISTFILE );
		else
			dir.addFile( item.distItem.getBaseFile( action ) );
	}
	
	private void createExpectedManualDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTarget item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		if( !item.DISTFILE.isEmpty() )
			dir.addFile( item.DISTFILE );
		else
			dir.addFile( item.distManualItem.getBaseFile( action ) );
	}
	
	private void createExpectedDatabaseDeliveryItem( ActionBase action , FileSet fs , MetaReleaseDelivery delivery , MetaReleaseTarget item ) throws Exception {
		fs.createDir( dist.getDeliveryDatabaseFolder( action , delivery.distDelivery ) );
	}
	
	private boolean finishDist( ActionBase action , FileSet fsd , FileSet fsr ) throws Exception {
		// check expected directory set is the same as actual
		// folders = deliveries
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			MetaReleaseDelivery delivery = info.findDeliveryByFolder( action , dir );
			if( delivery == null || delivery.isEmpty() ) {
				if( dirFilesDist.hasFiles() ) {
					if( !action.context.CTX_FORCE ) {
						action.log( "distributive delivery=" + delivery.distDelivery.NAME + ", dir=" + dir + " has files, while nothing is declared in release" );
						return( false );
					}
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
				if( dir.equals( Dist.DATABASE_FOLDER ) ) {
					if( !finishDistDeliveryBinary( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
				
				if( dirFilesDist.hasFiles() ) {
					if( !action.context.CTX_FORCE ) {
						action.log( "distributive delivery=" + delivery.distDelivery.NAME + ", dir=" + dir +  
								" has files, while nothing is declared in release" );
						return( false );
					}
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , dir );
				action.log( "delete non-release delivery folder=" + folder + " ..." );
				distFolder.removeFolder( action , folder );
			}
			else {
				if( dir.equals( Dist.BINARY_FOLDER ) ) {
					if( !finishDistDeliveryBinary( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
				else
				if( dir.equals( Dist.CONFIG_FOLDER ) ) {
					if( !finishDistDeliveryConfig( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
			}
		}

		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.log( "distributive has missing delivery=" + delivery.distDelivery.NAME + ", dir=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryBinary( ActionBase action , MetaReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String fileDist : fsd.files.keySet() ) {
			String fileRelease = fsr.files.get( fileDist );
			if( fileRelease == null ) {
				if( !action.context.CTX_FORCE ) {
					action.log( "distributive delivery=" + delivery.distDelivery.NAME + 
						" has non-release file=" + fileDist );
					return( false );
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , Dist.BINARY_FOLDER );
				action.log( "delete non-release delivery item folder=" + folder + " file=" + fileDist + " ..." );
				distFolder.removeFolderFile( action , folder , fileDist );
			}
		}
		
		if( fsr == null )
			return( true );
		
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
				if( !action.context.CTX_FORCE ) {
					action.log( "distributive delivery " + delivery.distDelivery.NAME + 
							" has non-release config=" + dir );
					return( false );
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , Dist.CONFIG_FOLDER , dir );
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

	private void createMD5( ActionBase action ) throws Exception {
		String md5file = action.getTmpFilePath( "state.md5" );
		
		List<String> lines = new LinkedList<String>();
		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaReleaseTarget manualItem : delivery.getManualItems( action ).values() )
				lines.add( DistMD5.getManualItemRecord( action , dist , manualItem ) );
			for( MetaReleaseTargetItem projectItem : delivery.getProjectItems( action ).values() )
				lines.add( DistMD5.getProjectItemRecord( action , dist , projectItem ) );
		}
		
		Common.createFileFromStringList( md5file , lines );
		dist.copyMD5StateFromLocal( action , md5file );
	}
	
}
