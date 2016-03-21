package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaDistrComponentItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerDeployment;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaSourceFolder;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.vcs.GenericVCS;

public class SourceStorage {

	Artefactory artefactory;
	LocalFolder downloadFolder;
	Metadata meta;
	
	public SourceStorage( Artefactory artefactory , LocalFolder downloadFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		this.downloadFolder = downloadFolder;
	}
	
	public void downloadThirdpartyItemFromVCS( ActionBase action , String ITEMPATH , String FOLDER ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		
		String BASENAME = Common.getBaseName( ITEMPATH );
		
		downloadFolder.ensureFolderExists( action , FOLDER );
		downloadFolder.removeFolderFile( action , FOLDER , BASENAME );
		
		LocalFolder subFolder = downloadFolder;
		if( !FOLDER.isEmpty() )
			subFolder = subFolder.getSubFolder( action , FOLDER );
		
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		if( !vcs.exportRepositoryMasterPath( subFolder , REPOSITORY , ITEMPATH , BASENAME ) )
			action.exit( "unable to export from REPOSITORY=" + REPOSITORY + ", ITEMPATH=" + ITEMPATH );
	}
	
	public boolean downloadReleaseManualFolder( ActionBase action , DistStorage distStorage , LocalFolder dstFolder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );  
		String PATH = getReleaseManualPath( action , distStorage );

		if( downloadManualFolder( action , vcs , PATH , dstFolder ) )
			return( true );
		
		action.log( "no manual files in " + PATH + ". Skipped." );
		return( false );
	}
	
	public boolean downloadReleaseConfigItem( ActionBase action , DistStorage distStorage , MetaSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );  
		String PATH = getReleaseConfigSourcePath( action , distStorage , sourceFolder.releaseComp );
		
		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		action.log( "no configuration in " + PATH + ". Skipped." );
		return( false );
	}

	public boolean downloadReleaseDatabaseFiles( ActionBase action , DistStorage distStorage , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );  
		String PATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		
		if( downloadDBFiles( action , vcs , PATH , dbDelivery , dstFolder ) )
			return( true );
		
		action.log( "no database changes in " + PATH + ". Skipped." );
		return( false );
	}
	
	public boolean downloadProductConfigItem( ActionBase action , MetaSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );  
		String PATH = getProductConfigSourcePath( action , sourceFolder.distrComp );

		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		if( !action.context.CTX_FORCE )
			action.exit( "unable to find configuration at " + vcs.getInfoMasterPath( meta.product.CONFIG_SOURCE_REPOSITORY , PATH ) );
		
		action.log( "no configuration in " + PATH + ". Skipped." );
		return( false );
	}

	private boolean downloadConfigItem( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrConfItem distrComp , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		if( !vcs.exportRepositoryMasterPath( dstFolder , REPOSITORY , ITEMPATH , distrComp.KEY ) )
			action.exit( "unable to export from REPOSITORY=" + REPOSITORY + ", ITEMPATH=" + ITEMPATH );
		
		dstFolder.prepareFolderForLinux( action , distrComp.KEY );
		return( true );
	}
	
	private boolean downloadDBFiles( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		if( !vcs.exportRepositoryMasterPath( dstFolder , REPOSITORY , ITEMPATH , "db" ) )
			action.exit( "unable to export from REPOSITORY=" + REPOSITORY + ", ITEMPATH=" + ITEMPATH );
		
		dstFolder.prepareFolderForLinux( action , "db" );
		return( true );
	}
	
	private boolean downloadManualFolder( ActionBase action , GenericVCS vcs , String PATH , LocalFolder dstManualFolder ) throws Exception {
		if( !isValidPath( action , vcs , PATH ) )
			return( false );
	
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		if( !vcs.exportRepositoryMasterPath( dstManualFolder.getParentFolder( action ) , REPOSITORY , PATH , dstManualFolder.folderName ) )
			action.exit( "unable to export from REPOSITORY=" + REPOSITORY + ", PATH=" + PATH );
		
		if( dstManualFolder.checkFolderExists( action , "db" ) )
			dstManualFolder.prepareFolderForLinux( action , "db" );
		return( true );
	}
	
	public void moveReleaseDatabaseFilesToErrors( ActionBase action , String errorFolder , DistStorage distStorage , MetaDistrDelivery dbDelivery , String movePath , String message ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );  
		String SRCPATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		String ERRORPATH = getReleaseErrorsPath( action , distStorage , dbDelivery , errorFolder );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		
		vcs.createMasterFolder( REPOSITORY , ERRORPATH , "create error folder" );
		vcs.moveMasterFiles( REPOSITORY , SRCPATH , ERRORPATH , movePath , message );
	}
	
	public boolean isValidPath( ActionBase action , GenericVCS vcs , String PATH ) throws Exception {
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		return( vcs.isValidRepositoryMasterPath( REPOSITORY , PATH ) );
	}
	
	public String getReleaseGroupFolder( ActionBase action ) throws Exception {
		return( meta.product.CONFIG_RELEASE_GROUPFOLDER );
	}
	
	public String getReleaseFolder( ActionBase action , DistStorage release ) throws Exception {
		String RELEASEVER = Common.getPartBeforeFirst( release.RELEASEDIR , "-" );
		if( release.RELEASEDIR.indexOf( "-demo-" ) > 0 )
			return( Common.getPartAfterFirst( release.RELEASEDIR , "-" ) + "-" + RELEASEVER );
			
		if( RELEASEVER.matches( "^[0-9]+\\.[0-9]+$" ) )
			return( "major-release-" + RELEASEVER );
		
		return( "prod-patch-" + RELEASEVER );
	}

	public String getReleaseManualPath( ActionBase action , DistStorage distStorage ) throws Exception {
		String PATH = meta.product.CONFIG_SOURCE_RELEASEROOTDIR + "/" + 
			getReleaseGroupFolder( action ) + "/" + 
			getReleaseFolder( action , distStorage ) + "/manual";
		
		return( PATH );
	}

	public String getReleaseConfigSourcePath( ActionBase action , DistStorage distStorage , MetaReleaseTarget releaseComp ) throws Exception {
		String PATH = meta.product.CONFIG_SOURCE_RELEASEROOTDIR + "/" + 
			getReleaseGroupFolder( action ) + "/" + 
			getReleaseFolder( action , distStorage ) + "/" +
			getConfFolderRelPath( action , releaseComp.distConfItem );
		
		return( PATH );
	}

	public String getReleaseDBSourcePath( ActionBase action , DistStorage distStorage , MetaDistrDelivery dbDelivery ) throws Exception {
		String PATH = meta.product.CONFIG_SOURCE_RELEASEROOTDIR + "/" + 
			getReleaseGroupFolder( action ) + "/" + 
			getReleaseFolder( action , distStorage ) + "/" +
			getDBFolderRelPath( action , dbDelivery );
		
		return( PATH );
	}

	public String getReleaseErrorsPath( ActionBase action , DistStorage distStorage , MetaDistrDelivery dbDelivery , String errorFolder ) throws Exception {
		String PATH = meta.product.CONFIG_SOURCE_RELEASEROOTDIR + "/" + 
			getReleaseGroupFolder( action ) + "/" + 
			getReleaseFolder( action , distStorage ) + "/" +
			getErrorFolderRelPath( action , dbDelivery , errorFolder );
		
		return( PATH );
	}

	public String getProductConfigSourcePath( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		String PATH = Common.getPath( meta.product.CONFIG_SOURCE_CFG_ROOTDIR , distrComp.KEY );
		return( PATH );
	}

	public String getConfFolderRelPath( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		String PATH = Common.getPath( distrComp.delivery.FOLDER , "config" );
		PATH += "/" + distrComp.KEY;
		return( PATH );
	}
	
	public String getDBFolderRelPath( ActionBase action , MetaDistrDelivery dbDelivery ) throws Exception {
		String PATH = Common.getPath( dbDelivery.FOLDER , "db" );
		return( PATH );
	}
	
	public String getErrorFolderRelPath( ActionBase action , MetaDistrDelivery dbDelivery , String errorFolder ) throws Exception {
		String PATH = Common.getPath( dbDelivery.FOLDER , "errors" );
		PATH = Common.getPath( PATH , errorFolder );
		return( PATH );
	}

	public String getLiveConfigDCPath( ActionBase action , MetaEnvDC dc ) throws Exception {
		String PATH = Common.getPath( meta.product.CONFIG_SOURCE_CFG_LIVEROOTDIR , dc.env.ID , dc.NAME );
		return( PATH );
	}
	
	public String getLiveConfigPath( ActionBase action ) throws Exception {
		String PATH = Common.getPath( meta.product.CONFIG_SOURCE_CFG_LIVEROOTDIR , action.context.env.ID );
		return( PATH );
	}
	
	public String getLiveConfigServerPath( ActionBase action , MetaEnvDC dc , String server ) throws Exception {
		String PATH = getLiveConfigDCPath( action , dc );
		PATH = Common.getPath( PATH , server );
		return( PATH );
	}
	
	public String getLiveConfigItems( ActionBase action , MetaEnvServer server ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String list = vcs.listMasterItems( REPOSITORY , PATH );
		return( list );
	}

	public String getLiveConfigServers( ActionBase action , MetaEnvDC dc ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String PATH = getLiveConfigDCPath( action , dc );
		String list = vcs.listMasterItems( REPOSITORY , PATH );
		return( list );
	}

	public void deleteLiveConfigItem( ActionBase action , MetaEnvServer server , String item , String commitMessage ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		PATH = Common.getPath( PATH , item );
		vcs.deleteMasterFolder( REPOSITORY , PATH , commitMessage );
	}

	public void deleteLiveConfigServer( ActionBase action , MetaEnvDC dc , String server , String commitMessage ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String PATH = getLiveConfigServerPath( action , dc , server );
		vcs.deleteMasterFolder( REPOSITORY , PATH , commitMessage );
	}

	public void tagLiveConfigs( ActionBase action , String TAG , String commitMessage ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String PATH = getLiveConfigPath( action );
		String setTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + TAG;
		vcs.createMasterTag( REPOSITORY , PATH , setTAG , commitMessage );
	}

	public void exportLiveConfigItem( ActionBase action , MetaEnvServer server , String confName , String TAG , LocalFolder folder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , confName );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( folder , REPOSITORY , PATH , confName ) )
				action.exit( "exportLiveConfigItem: unable to export " + confName + " from " + PATH );
		}
		else {
			String useTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( folder , REPOSITORY , useTAG , PATH , confName ) )
				action.exit( "exportLiveConfigItem: unable to export " + confName + " from " + PATH + ", TAG=" + useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void exportTemplateConfigItem( ActionBase action , MetaEnvDC dc , String confName , String TAG , LocalFolder folder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		
		String CONFPATH = meta.product.CONFIG_SOURCE_CFG_ROOTDIR;
		String PATH = Common.getPath( CONFPATH , confName );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( folder , REPOSITORY , PATH , confName ) )
				action.exit( "exportTemplateConfigItem: unable to export " + confName + " from " + PATH );
		}
		else {
			String useTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + dc.NAME + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( folder , REPOSITORY , useTAG , PATH , confName ) )
				action.exit( "exportTemplateConfigItem: unable to export " + confName + " from " + PATH + ", TAG=" + useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void saveLiveConfigItem( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , String item , LocalFolder folder , String commitMessage ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , item );
		
		if( !vcs.isValidRepositoryMasterPath( REPOSITORY , PATH ) ) {
			if( !vcs.isValidRepositoryMasterPath( REPOSITORY , SERVERPATH ) )
				vcs.ensureMasterFolderExists( REPOSITORY , SERVERPATH , commitMessage );
			vcs.importMasterFolder( folder , REPOSITORY , PATH , commitMessage );
			action.log( node.HOSTLOGIN + ": live created at " + PATH );
			return;
		}
		
		// define save path
		LocalFolder coFolder = artefactory.getWorkFolder( action , "config.vcs" );
		coFolder.removeThis( action );
		
		vcs.checkoutMasterFolder( coFolder , REPOSITORY , PATH );
		coFolder.prepareFolderForLinux( action , "" );

		FileSet tobeFiles = folder.getFileSet( action );
		FileSet coFiles = coFolder.getFileSet( action ); 
		
		// copy tobe files over co files and prepare for changes
		coFolder.copyDirContent( action , folder );
		saveLiveConfigItemCopyFolder( action , vcs , tobeFiles , coFiles , folder , coFolder );

		if( vcs.commitMasterFolder( coFolder , REPOSITORY , PATH , commitMessage ) )
			action.log( node.HOSTLOGIN + ": live updated at " + PATH );
		else
			action.debug( node.HOSTLOGIN + ": live not changed at " + PATH );
	}

	private void saveLiveConfigItemCopyFolder( ActionBase action , GenericVCS vcs , FileSet tobeFiles , FileSet coFiles , LocalFolder folder , LocalFolder coFolder ) throws Exception {
		// add new files
		for( String file : tobeFiles.files.keySet() ) {
			if( !coFiles.files.containsKey( file ) )
				vcs.addFileToCommit( coFolder , coFiles.dirPath , file );
		}
		
		// delete old files
		for( String file : coFiles.files.keySet() ) {
			if( !tobeFiles.files.containsKey( file ) )
				vcs.deleteFileToCommit( coFolder , coFiles.dirPath , file );
		}
		
		// add new dirs and check subfolders
		for( FileSet tobeDir : tobeFiles.dirs.values() ) {
			FileSet coDir = coFiles.dirs.get( tobeDir.dirName );
			if( coDir == null )
				vcs.addDirToCommit( coFolder , Common.getPath( coFiles.dirPath , tobeDir.dirName ) );
			else
				saveLiveConfigItemCopyFolder( action , vcs , tobeDir , coDir , folder , coFolder );
		}
		
		// delete old dirs
		for( String coDir : coFiles.dirs.keySet() ) {
			if( coDir.equals( ".svn" ) || coDir.equals( ".git" ) )
				continue;
			if( !tobeFiles.dirs.containsKey( coDir ) )
				vcs.deleteDirToCommit( coFolder , Common.getPath( coFiles.dirPath , coDir ) );
		}
	}

	public String getConfItemLiveName( ActionBase action , MetaEnvServerNode node , MetaDistrConfItem confItem ) throws Exception {
		return( "node" + node.POS + "-" + confItem.KEY );
	}

	public String getSysConfItemLiveName( ActionBase action , MetaEnvServerNode node ) throws Exception {
		return( "node" + node.POS + "-system" );
	}

	public void exportTemplates( ActionBase action , LocalFolder parent , MetaEnvServer server ) throws Exception {
		for( MetaEnvServerDeployment deployment : server.getDeployments( action ) ) {
			if( deployment.confItem != null ) {
				exportTemplateConfigItem( action , server.dc , deployment.confItem.KEY , action.context.CTX_TAG , parent );
				continue;
			}
				
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems( action ).values() ) {
				if( compItem.confItem != null )
					exportTemplateConfigItem( action , server.dc , compItem.confItem.KEY , action.context.CTX_TAG , parent );
			}
		}
	}

	public void exportTemplates( ActionBase action , MetaEnvDC dc , LocalFolder parent , MetaDistrConfItem[] items ) throws Exception {
		for( MetaDistrConfItem item : items )
			exportTemplateConfigItem( action , dc , item.KEY , "" , parent );
	}
		
	public void exportPostRefresh( ActionBase action , String name , LocalFolder folder ) throws Exception {
		GenericVCS vcs = artefactory.getVCS( action , meta.product.CONFIG_SOURCE_VCS , false );
		String REPOSITORY = meta.product.CONFIG_SOURCE_REPOSITORY;
		
		String CONFPATH = meta.product.CONFIG_SOURCE_SQL_POSTREFRESH;
		String PATH = Common.getPath( CONFPATH , name );
		if( !vcs.exportRepositoryMasterPath( folder , REPOSITORY , PATH , name ) )
			action.exit( "exportTemplateConfigItem: unable to export " + name + " from " + PATH );
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , name );
	}
	
}
