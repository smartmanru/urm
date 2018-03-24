package org.urm.engine.dist;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.FileSet;
import org.urm.engine.storage.RemoteFolder;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.release.Release;

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
		FileSet fsr = createExpectedFileSet( action , fsd );  
		if( !finishDist( action , fsd , fsr ) )
			return( false );
		
		// finish release
		return( true );
	}
	
	private FileSet createExpectedFileSet( ActionBase action , FileSet fsd ) throws Exception {
		FileSet fs = new FileSet( null );
		
		if( dist.isMaster() ) {
			MetaDistr distr = dist.meta.getDistr();
			for( MetaDistrDelivery delivery : distr.getDeliveries() ) {
				for( MetaDistrBinaryItem item : delivery.getBinaryItems() )
					createExpectedMasterDeliveryItem( action , fsd , fs , delivery , item );
			}
		}
		else {
			dist.gatherFiles( action );
			for( ReleaseDistScopeDelivery delivery : info.getDeliveries() ) {
				for( ReleaseScopeTarget item : delivery.getConfItems() )
					createExpectedConfDeliveryItem( action , fs , delivery , item );
				for( ReleaseScopeItem item : delivery.getProjectItems() )
					createExpectedProjectDeliveryItem( action , fs , delivery , item );
				for( ReleaseScopeTarget item : delivery.getManualItems() )
					createExpectedManualDeliveryItem( action , fs , delivery , item );
				for( ReleaseScopeItem item : delivery.getDocItems() )
					createExpectedDocDeliveryItem( action , fs , delivery , item );
				createExpectedDatabaseDeliveryItem( action , fs , delivery );
			}
		}
		
		return( fs );
	}
	
	private void createExpectedConfDeliveryItem( ActionBase action , FileSet fs , ReleaseDistScopeDelivery delivery , ReleaseScopeTarget item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryConfFolder( action , delivery.distDelivery ) );
		dir.createDir( item.distConfItem.NAME );
	}
	
	private void createExpectedProjectDeliveryItem( ActionBase action , FileSet fs , ReleaseDistScopeDelivery delivery , ReleaseScopeItem item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		String file = ( item.DISTFILE.isEmpty() )? item.distItem.getBaseFile() : item.DISTFILE; 
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private void createExpectedDocDeliveryItem( ActionBase action , FileSet fs , ReleaseDistScopeDelivery delivery , ReleaseScopeItem item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryDocFolder( action , delivery.distDelivery ) );
		String file = ( item.DISTFILE.isEmpty() )? item.doc.getBaseFile() : item.DISTFILE; 
		action.trace( "FINISH: add doc=" + item.NAME + ", dir=" + dir.dirPath + ", file=" + file );
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private void createExpectedManualDeliveryItem( ActionBase action , FileSet fs , ReleaseDistScopeDelivery delivery , ReleaseScopeTarget item ) throws Exception {
		FileSet dir = fs.createDir( dist.getDeliveryBinaryFolder( action , delivery.distDelivery ) );
		String file = ( item.DISTFILE.isEmpty() )? item.distManualItem.getBaseFile() : item.DISTFILE;  
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private void createExpectedDatabaseDeliveryItem( ActionBase action , FileSet fs , ReleaseDistScopeDelivery delivery ) throws Exception {
		if( delivery.hasDatabaseItems() )
			fs.createDir( dist.getDeliveryDatabaseFolder( action , delivery.distDelivery , dist.release.RELEASEVER ) );
	}
	
	private void createExpectedMasterDeliveryItem( ActionBase action , FileSet fsd , FileSet fs , MetaDistrDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		String folder = dist.getDeliveryBinaryFolder( action , delivery );
		String file = fsd.findDistItem( action , item , folder );
		if( file == null )
			file = item.getBaseFile();
		
		FileSet dir = fs.createDir( folder );
		dir.addFile( file );
		dir.addFile( file + ".md5" );
	}
	
	private boolean finishDist( ActionBase action , FileSet fsd , FileSet fsr ) throws Exception {
		// check expected directory set is the same as actual
		// folders = deliveries
		for( String dir : fsd.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			MetaDistrDelivery delivery = null;
			if( dist.isMaster() ) {
				MetaDistr distr = dist.meta.getDistr();
				delivery = distr.findDeliveryByFolder( dir );
			}
			else {
				ReleaseDistScopeDelivery deliveryRelease = info.findDeliveryByFolder( dir );
				if( deliveryRelease != null )
					delivery = deliveryRelease.distDelivery;
			}
			
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
				FileSet dirFilesRelease = fsr.findDirByName( dir );
				if( !finishDistDelivery( action , delivery , dirFilesDist , dirFilesRelease ) )
					return( false );
			}
		}
		
		for( String dir : fsr.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			if( dirFilesDist == null ) {
				action.error( "distributive has missing delivery=" + dir );
				return( false );
			}
		}
		
		if( dist.isMaster() ) {
			if( !finishDistMaster( action ) )
				return( false );
		}
		
		return( true );
	}

	private boolean finishDistDelivery( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		// check by category
		for( String dir : fsd.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			FileSet dirFilesRelease = fsr.findDirByName( dir );
			if( dirFilesRelease == null ) {
				if( dir.equals( Dist.DATABASE_FOLDER ) ) {
					if( !finishDistDeliveryDatabase( action , delivery , dirFilesDist , null ) )
						return( false );
				}
				
				if( dirFilesDist.hasFiles() ) {
					if( !action.isForced() ) {
						action.error( "distributive delivery=" + delivery.NAME + ", dir=" + dir +  
								" has files, while nothing is declared in release" );
						return( false );
					}
				}
				
				String folder = Common.getPath( delivery.FOLDER , dir );
				action.info( "delete non-release delivery folder=" + folder + " ..." );
				distFolder.removeFolder( action , folder );
			}
			else {
				if( dir.equals( Dist.BINARY_FOLDER ) ) {
					if( !finishDistDeliveryBinary( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
					if( dist.isMaster() ) {
						if( !finishDistDeliveryMaster( action , delivery ) )
							return( false );
					}
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
				if( dir.equals( Dist.DOC_FOLDER ) ) {
					if( !finishDistDeliveryDoc( action , delivery , dirFilesDist , dirFilesRelease ) )
						return( false );
				}
				else
					action.exitUnexpectedState();
			}
		}

		for( String dir : fsr.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			if( dirFilesDist == null ) {
				action.error( "distributive has missing delivery=" + delivery.NAME + ", dir=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryBinary( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String fileDist : fsd.getAllFiles() ) {
			String fileRelease = findBasenameFile( fileDist , fsr );
			if( fileRelease == null ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery=" + delivery.NAME + 
						" has non-release file=" + fileDist );
					return( false );
				}
				
				String folder = Common.getPath( delivery.FOLDER , Dist.BINARY_FOLDER );
				action.info( "delete non-release delivery item folder=" + folder + " file=" + fileDist + " ..." );
				distFolder.removeFolderFile( action , folder , fileDist );
			}
		}
		
		if( fsr == null )
			return( true );
		
		for( String fileRelease : fsr.getAllFiles() ) {
			String fileDist = findBasenameFile( fileRelease , fsd );
			if( fileDist == null ) {
				if( fileRelease.endsWith( ".md5" ) ) {
					String fileMD5 = Common.getPath( fsr.dirPath , fileRelease );
					String file = Common.getPartBeforeLast( fileRelease , ".md5" );
					String filePath = Common.getPath( fsr.dirPath , file );
					if( findBasenameFile( file , fsd ) != null ) {
						action.info( "create missing md5 delivery=" + delivery.NAME + " file=" + fileRelease + " ..." );
						String value = distFolder.getFileMD5( action , filePath );
						distFolder.createFileFromString( action , fileMD5 , value );
					}
				}
				else {
					action.error( "distributive has missing delivery=" + delivery.NAME + " file=" + fileRelease );
					return( false );
				}
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryDoc( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String fileDist : fsd.getAllFiles() ) {
			String fileRelease = findBasenameFile( fileDist , fsr );
			if( fileRelease == null ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery=" + delivery.NAME + 
						" has non-release file=" + fileDist );
					return( false );
				}
				
				String folder = Common.getPath( delivery.FOLDER , Dist.DOC_FOLDER );
				action.info( "delete non-release delivery item folder=" + folder + " file=" + fileDist + " ..." );
				distFolder.removeFolderFile( action , folder , fileDist );
			}
		}
		
		if( fsr == null )
			return( true );
		
		for( String fileRelease : fsr.getAllFiles() ) {
			String fileDist = findBasenameFile( fileRelease , fsd );
			if( fileDist == null ) {
				if( fileRelease.endsWith( ".md5" ) ) {
					String fileMD5 = Common.getPath( fsr.dirPath , fileRelease );
					String file = Common.getPartBeforeLast( fileRelease , ".md5" );
					String filePath = Common.getPath( fsr.dirPath , file );
					if( findBasenameFile( file , fsd ) != null ) {
						action.info( "create missing md5 delivery=" + delivery.NAME + " file=" + fileRelease + " ..." );
						String value = distFolder.getFileMD5( action , filePath );
						distFolder.createFileFromString( action , fileMD5 , value );
					}
				}
				else {
					action.error( "distributive has missing delivery=" + delivery.NAME + " file=" + fileRelease );
					return( false );
				}
			}
		}
		
		return( true );
	}

	private String findBasenameFile( String file , FileSet fs ) {
		return( fs.getFilePath( file ) );
	}
	
	private boolean finishDistDeliveryConfig( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		for( String dir : fsd.getAllDirNames() ) {
			FileSet dirFilesRelease = fsr.findDirByName( dir );
			if( dirFilesRelease == null ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery " + delivery.NAME + 
							" has non-release config=" + dir );
					return( false );
				}
				
				String folder = Common.getPath( delivery.FOLDER , Dist.CONFIG_FOLDER , dir );
				action.info( "delete non-release configuration item delivery=" + delivery.NAME + " config=" + dir + " ..." );
				distFolder.removeFolder( action , folder );
			}
		}
		
		for( String dir : fsr.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			if( dirFilesDist == null ) {
				action.error( "distributive has missing delivery=" + delivery.NAME + ", config=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryDatabase( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , FileSet fsr ) throws Exception {
		if( fsr == null ) {
			String folder = fsd.dirPath;
			if( fsd.hasFiles() ) {
				if( !action.isForced() ) {
					action.error( "distributive delivery=" + delivery.NAME + 
							" has non-release database folder=" + folder );
					return( false );
				}
			}
			
			action.info( "delete non-release database delivery=" + delivery.NAME + " folder=" + folder + " ..." );
			distFolder.removeFolder( action , folder );
			return( true );
		}
		
		if( fsd.isEmpty() ) {
			action.error( "distributive has missing database delivery=" + delivery.NAME );
			return( false );
		}
		
		String[] versions = dist.release.getApplyVersions( action );
		for( String dir : fsd.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			if( !finishDistDeliveryDatabaseSet( action , delivery , dirFilesDist , versions ) )
				return( false );
		}
		
		for( String dir : fsr.getAllDirNames() ) {
			FileSet dirFilesDist = fsd.findDirByName( dir );
			if( dirFilesDist == null || dirFilesDist.isEmpty() ) {
				action.error( "distributive has missing/empty database delivery=" + delivery.NAME + ", set=" + dir );
				return( false );
			}
		}
		
		return( true );
	}

	private boolean finishDistDeliveryDatabaseSet( ActionBase action , MetaDistrDelivery delivery , FileSet fsd , String[] versions ) throws Exception {
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
	
	private boolean finishDistMaster( ActionBase action ) throws Exception {
		MetaDistr distr = dist.meta.getDistr(); 
		ReleaseMaster master = dist.release.master;
		for( ReleaseMasterItem item : master.getMasterItems() ) {
			if( distr.findBinaryItem( item.KEY ) == null )
				master.removeMasterItem( item.KEY );
		}
		
		return( true );
	}

	private boolean finishDistDeliveryMaster( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		for( MetaDistrBinaryItem item : delivery.getBinaryItems() ) {
			if( !finishDistDeliveryMasterItem( action , delivery , item ) )
				return( false );
		}
		
		return( true );
	}

	private boolean finishDistDeliveryMasterItem( ActionBase action , MetaDistrDelivery delivery , MetaDistrBinaryItem distItem ) throws Exception {
		ReleaseMaster master = dist.release.master;
		ReleaseMasterItem masterItem = dist.release.findMasterItem( distItem );
		DistItemInfo info = dist.getDistItemInfo( action , distItem , true , false );
		if( !info.found ) {
			String folder = Common.getPath( distItem.delivery.FOLDER , Dist.BINARY_FOLDER );
			action.error( distItem.NAME + " - item not found (" + Common.getPath( folder , distItem.getBaseFile() ) + ")" );
			return( false );
		}
		
		if( masterItem == null )
			master.addMasterItem( action , null , distItem , info );
		else
			masterItem.update( action , distItem , info );
		
		return( true );
	}
	
}
