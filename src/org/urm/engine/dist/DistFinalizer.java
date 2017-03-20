package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.RemoteFolder;

public class DistFinalizer {

	ActionBase action;
	Dist dist;
	RemoteFolder distFolder;
	Release info;
	
	public DistFinalizer( ActionBase action , Dist dist , RemoteFolder distFolder , Release info ) {
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
		return( true );
	}
	
	private FileSet createExpectedFileSet( ActionBase action ) throws Exception {
		dist.gatherFiles( action );
		
		FileSet fs = new FileSet( null );
		for( ReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( ReleaseTarget item : delivery.getConfItems( action ).values() )
				createExpectedConfDeliveryItem( action , fs , delivery , item );
			for( ReleaseTargetItem item : delivery.getProjectItems( action ).values() )
				createExpectedProjectDeliveryItem( action , fs , delivery , item );
			for( ReleaseTarget item : delivery.getManualItems( action ).values() )
				createExpectedManualDeliveryItem( action , fs , delivery , item );
			ReleaseTarget dbitem = delivery.getDatabaseItem( action );
			if( dbitem != null )
				createExpectedDatabaseDeliveryItem( action , fs , delivery , dbitem );
		}
		
		return( fs );
	}
	
	private void createExpectedConfDeliveryItem( ActionBase action , FileSet fs , ReleaseDelivery delivery , ReleaseTarget item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryConfFolder( action , delivery.distDelivery ) );
		dir.createDir( item.distConfItem.KEY );
	}
	
	private void createExpectedProjectDeliveryItem( ActionBase action , FileSet fs , ReleaseDelivery delivery , ReleaseTargetItem item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		String file = ( item.DISTFILE.isEmpty() )? item.distItem.getBaseFile( action ) : item.DISTFILE; 
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private void createExpectedManualDeliveryItem( ActionBase action , FileSet fs , ReleaseDelivery delivery , ReleaseTarget item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		String file = ( item.DISTFILE.isEmpty() )? item.distManualItem.getBaseFile( action ) : item.DISTFILE;  
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private void createExpectedDatabaseDeliveryItem( ActionBase action , FileSet fs , ReleaseDelivery delivery , ReleaseTarget item ) throws Exception {
		fs.createDir( dist.getDeliveryDatabaseFolder( action , delivery.distDelivery , dist.release.RELEASEVER ) );
	}
	
	private boolean finishDist( ActionBase action , FileSet fsd , FileSet fsr ) throws Exception {
		// check expected directory set is the same as actual
		// folders = deliveries
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			ReleaseDelivery delivery = info.findDeliveryByFolder( action , dir );
			if( delivery == null || delivery.isEmpty() ) {
				if( dirFilesDist.hasFiles() ) {
					if( !action.isForced() ) {
						action.error( "distributive delivery folder=" + dir + " has files, while nothing is declared in release" );
						return( false );
					}
				}
				
				action.info( "delete non-release delivery=" + dir + " ..." );
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
				action.error( "distributive has missing delivery=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDelivery( ActionBase action , ReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		// check by category
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			FileSet dirFilesRelease = fsr.dirs.get( dir );
			if( dirFilesRelease == null ) {
				if( dir.equals( Dist.DATABASE_FOLDER ) ) {
					if( !finishDistDeliveryDatabase( action , delivery , dirFilesDist , null ) )
						return( false );
				}
				
				if( dirFilesDist.hasFiles() ) {
					if( !action.isForced() ) {
						action.error( "distributive delivery=" + delivery.distDelivery.NAME + ", dir=" + dir +  
								" has files, while nothing is declared in release" );
						return( false );
					}
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , dir );
				action.info( "delete non-release delivery folder=" + folder + " ..." );
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
				else
				if( dir.equals( Dist.DATABASE_FOLDER ) ) {
					if( !finishDistDeliveryDatabase( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
				else
					action.exitUnexpectedState();
			}
		}

		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.error( "distributive has missing delivery=" + delivery.distDelivery.NAME + ", dir=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryBinary( ActionBase action , ReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String fileDist : fsd.files.keySet() ) {
			String fileRelease = fsr.files.get( fileDist );
			if( fileRelease == null ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery=" + delivery.distDelivery.NAME + 
						" has non-release file=" + fileDist );
					return( false );
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , Dist.BINARY_FOLDER );
				action.info( "delete non-release delivery item folder=" + folder + " file=" + fileDist + " ..." );
				distFolder.removeFolderFile( action , folder , fileDist );
			}
		}
		
		if( fsr == null )
			return( true );
		
		for( String fileRelease : fsr.files.keySet() ) {
			String fileDist = fsd.files.get( fileRelease );
			if( fileDist == null ) {
				if( fileRelease.endsWith( ".md5" ) ) {
					String fileMD5 = Common.getPath( fsr.dirPath , fileRelease );
					action.info( "create missing md5 delivery=" + delivery.distDelivery.NAME + " file=" + fileRelease + " ..." );
					String file = Common.getPartBeforeLast( fileMD5 , ".md5" );
					String value = distFolder.getFileMD5( action , file );
					distFolder.createFileFromString( action , fileMD5 , value );
				}
				else {
					action.error( "distributive has missing delivery=" + delivery.distDelivery.NAME + " file=" + fileRelease );
					return( false );
				}
			}
		}
		
		return( true );
	}
	
	private boolean finishDistDeliveryConfig( ActionBase action , ReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesRelease = fsr.dirs.get( dir );
			if( dirFilesRelease == null ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery " + delivery.distDelivery.NAME + 
							" has non-release config=" + dir );
					return( false );
				}
				
				String folder = Common.getPath( delivery.distDelivery.FOLDER , Dist.CONFIG_FOLDER , dir );
				action.info( "delete non-release configuration item delivery=" + delivery.distDelivery.NAME + " config=" + dir + " ..." );
				distFolder.removeFolder( action , folder );
			}
		}
		
		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null ) {
				action.error( "distributive has missing delivery=" + delivery.distDelivery.NAME + ", config=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryDatabase( ActionBase action , ReleaseDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		if( fsr == null ) {
			String folder = fsd.dirPath;
			if( fsd.hasFiles() ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery=" + delivery.distDelivery.NAME + 
							" has non-release database folder=" + folder );
					return( false );
				}
			}
			
			action.info( "delete non-release database delivery=" + delivery.distDelivery.NAME + " folder=" + folder + " ..." );
			distFolder.removeFolder( action , folder );
			return( true );
		}
		
		if( fsd.isEmpty() ) {
			action.error( "distributive has missing database delivery=" + delivery.distDelivery.NAME );
			return( false );
		}
		
		String[] versions = dist.release.getApplyVersions( action );
		for( String dir : fsd.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( !finishDistDeliveryDatabaseSet( action , delivery , dirFilesDist , versions ) )
				return( false );
		}
		
		for( String dir : fsr.dirs.keySet() ) {
			FileSet dirFilesDist = fsd.dirs.get( dir );
			if( dirFilesDist == null || dirFilesDist.isEmpty() ) {
				action.error( "distributive has missing/empty database delivery=" + delivery.distDelivery.NAME + ", set=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryDatabaseSet( ActionBase action , ReleaseDelivery delivery , FileSet fsd , String[] versions ) throws Exception {
		if( Common.checkListItem( versions , fsd.dirName ) )
			return( true );
		
		if( !action.isForced() ) {
			action.error( "distributive has unexpected database delivery folder=" + fsd.dirPath );
			return( false );
		}
	
		String folder = fsd.dirPath;
		action.info( "delete non-release database delivery folder=" + folder + " ..." );
		distFolder.removeFolder( action , folder );
		return( true );
	}
	
}
