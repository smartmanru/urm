package org.urm.server.storage;

import org.urm.common.Common;
import org.urm.server.ServerMirrorRepository;
import org.urm.server.action.ActionBase;
import org.urm.server.action.conf.ConfSourceFolder;
import org.urm.server.dist.Dist;
import org.urm.server.dist.ReleaseTarget;
import org.urm.server.meta.MetaDistrComponentItem;
import org.urm.server.meta.MetaDistrConfItem;
import org.urm.server.meta.MetaDistrDelivery;
import org.urm.server.meta.MetaEnvDC;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerDeployment;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaProductBuildSettings;
import org.urm.server.vcs.GenericVCS;

public class SourceStorage {

	Artefactory artefactory;
	LocalFolder downloadFolder;
	Meta meta;

	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String ERRORS_FOLDER = "errors";
	public static String MANUAL_FOLDER = "manual";
	
	public SourceStorage( Artefactory artefactory , LocalFolder downloadFolder ) {
		this.artefactory = artefactory;
		this.meta = artefactory.meta;
		this.downloadFolder = downloadFolder;
	}
	
	public void downloadThirdpartyItemFromVCS( ActionBase action , String ITEMPATH , String FOLDER ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		
		String BASENAME = Common.getBaseName( ITEMPATH );
		
		downloadFolder.ensureFolderExists( action , FOLDER );
		downloadFolder.removeFolderFile( action , FOLDER , BASENAME );
		
		LocalFolder subFolder = downloadFolder;
		if( !FOLDER.isEmpty() )
			subFolder = subFolder.getSubFolder( action , FOLDER );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.exportRepositoryMasterPath( mirror , subFolder , ITEMPATH , BASENAME ) )
			action.exit( "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH );
	}
	
	public boolean downloadReleaseManualFolder( ActionBase action , Dist distStorage , LocalFolder dstFolder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );  
		String PATH = getReleaseManualPath( action , distStorage );

		if( downloadManualFolder( action , vcs , PATH , dstFolder ) )
			return( true );
		
		action.info( "no manual files in " + PATH + ". Skipped." );
		return( false );
	}
	
	public boolean downloadReleaseConfigItem( ActionBase action , Dist distStorage , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );  
		String PATH = getReleaseConfigSourcePath( action , distStorage , sourceFolder.releaseComp );
		
		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		action.info( "no configuration in " + PATH + ". Skipped." );
		return( false );
	}

	public boolean downloadReleaseDatabaseFiles( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );  
		String PATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		
		if( downloadDBFiles( action , vcs , PATH , dbDelivery , dstFolder ) )
			return( true );
		
		action.info( "no database changes in " + PATH + ". Skipped." );
		return( false );
	}
	
	public boolean downloadProductConfigItem( ActionBase action , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );  
		String PATH = getProductConfigSourcePath( action , sourceFolder.distrComp );

		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		action.ifexit( "unable to find configuration at " + vcs.getInfoMasterPath( mirror , PATH ) );
		
		action.info( "no configuration in " + PATH + ". Skipped." );
		return( false );
	}

	private boolean downloadConfigItem( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrConfItem distrComp , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		MetaProductBuildSettings build = action.getBuildSettings();
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , distrComp.KEY ) )
			action.exit( "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH );
		
		dstFolder.prepareFolderForLinux( action , distrComp.KEY );
		return( true );
	}
	
	private boolean downloadDBFiles( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		MetaProductBuildSettings build = action.getBuildSettings();
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , DATABASE_FOLDER ) )
			action.exit( "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH );
		
		if( action.isLocalLinux() )
			dstFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	private boolean downloadManualFolder( ActionBase action , GenericVCS vcs , String PATH , LocalFolder dstManualFolder ) throws Exception {
		if( !isValidPath( action , vcs , PATH ) )
			return( false );
	
		MetaProductBuildSettings build = action.getBuildSettings();
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.exportRepositoryMasterPath( mirror , dstManualFolder.getParentFolder( action ) , PATH , dstManualFolder.folderName ) )
			action.exit( "unable to export from mirror=" + mirror.NAME + ", PATH=" + PATH );
		
		if( dstManualFolder.checkFolderExists( action , DATABASE_FOLDER ) )
			dstManualFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	public void moveReleaseDatabaseFilesToErrors( ActionBase action , String errorFolder , Dist distStorage , MetaDistrDelivery dbDelivery , String movePath , String message ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );  
		String SRCPATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		String ERRORPATH = getReleaseErrorsPath( action , distStorage , dbDelivery , errorFolder );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		vcs.createMasterFolder( mirror , ERRORPATH , "create error folder" );
		vcs.moveMasterFiles( mirror , SRCPATH , ERRORPATH , movePath , message );
	}
	
	public boolean isValidPath( ActionBase action , GenericVCS vcs , String PATH ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		ServerMirrorRepository mirror = action.getMirror( build );
		return( vcs.isValidRepositoryMasterPath( mirror , PATH ) );
	}
	
	public String getReleaseGroupFolder( ActionBase action ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		return( build.CONFIG_RELEASE_GROUPFOLDER );
	}
	
	public String getReleaseFolder( ActionBase action , Dist release ) throws Exception {
		String RELEASEVER = Common.getPartBeforeFirst( release.RELEASEDIR , "-" );
		if( release.RELEASEDIR.indexOf( "-demo-" ) > 0 )
			return( Common.getPartAfterFirst( release.RELEASEDIR , "-" ) + "-" + RELEASEVER );
			
		if( RELEASEVER.matches( "^[0-9]+\\.[0-9]+$" ) )
			return( "major-release-" + RELEASEVER );
		
		return( "prod-patch-" + RELEASEVER );
	}

	public String getReleasePath( ActionBase action , Dist distStorage ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		String PATH = Common.getPath( build.CONFIG_SOURCE_RELEASEROOTDIR , 
			getReleaseGroupFolder( action ) ,
			getReleaseFolder( action , distStorage ) );
		return( PATH );
	}
	
	public String getReleaseManualPath( ActionBase action , Dist distStorage ) throws Exception {
		String PATH = Common.getPath( getReleasePath( action , distStorage ) , MANUAL_FOLDER );
		return( PATH );
	}

	public String getReleaseConfigSourcePath( ActionBase action , Dist distStorage , ReleaseTarget releaseComp ) throws Exception {
		String PATH = Common.getPath( getReleasePath( action , distStorage ) , 
			getConfFolderRelPath( action , releaseComp.distConfItem ) );
		return( PATH );
	}

	public String getReleaseDBSourcePath( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery ) throws Exception {
		String PATH = Common.getPath( getReleasePath( action , distStorage ) , 
			getDBFolderRelPath( action , dbDelivery ) );
		return( PATH );
	}

	public String getReleaseErrorsPath( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery , String errorFolder ) throws Exception {
		String PATH = Common.getPath( getReleasePath( action , distStorage ) , 
			getErrorFolderRelPath( action , dbDelivery , errorFolder ) );
		return( PATH );
	}

	public String getProductConfigSourcePath( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		String PATH = Common.getPath( build.CONFIG_SOURCE_CFG_ROOTDIR , distrComp.KEY );
		return( PATH );
	}

	public String getConfFolderRelPath( ActionBase action , MetaDistrDelivery delivery ) throws Exception {
		String PATH = Common.getPath( delivery.FOLDER , SourceStorage.CONFIG_FOLDER );
		return( PATH );
	}
	
	public String getConfFolderRelPath( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		String PATH = Common.getPath( distrComp.delivery.FOLDER , SourceStorage.CONFIG_FOLDER , distrComp.KEY );
		return( PATH );
	}
	
	public String getDBFolderRelPath( ActionBase action , MetaDistrDelivery dbDelivery ) throws Exception {
		String PATH = Common.getPath( dbDelivery.FOLDER , SourceStorage.DATABASE_FOLDER );
		return( PATH );
	}
	
	public String getErrorFolderRelPath( ActionBase action , MetaDistrDelivery dbDelivery , String errorFolder ) throws Exception {
		String PATH = Common.getPath( dbDelivery.FOLDER , SourceStorage.ERRORS_FOLDER , errorFolder );
		return( PATH );
	}

	public String getLiveConfigDCPath( ActionBase action , MetaEnvDC dc ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		String PATH = Common.getPath( build.CONFIG_SOURCE_CFG_LIVEROOTDIR , dc.env.ID , dc.NAME );
		return( PATH );
	}
	
	public String getLiveConfigPath( ActionBase action ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		String PATH = Common.getPath( build.CONFIG_SOURCE_CFG_LIVEROOTDIR , action.context.env.ID );
		return( PATH );
	}
	
	public String getLiveConfigServerPath( ActionBase action , MetaEnvDC dc , String server ) throws Exception {
		String PATH = Common.getPath( getLiveConfigDCPath( action , dc ) , server );
		return( PATH );
	}
	
	public String[] getLiveConfigItems( ActionBase action , MetaEnvServer server ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public String[] getLiveConfigServers( ActionBase action , MetaEnvDC dc ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String PATH = getLiveConfigDCPath( action , dc );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public void deleteLiveConfigItem( ActionBase action , MetaEnvServer server , String item , String commitMessage ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		PATH = Common.getPath( PATH , item );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void deleteLiveConfigServer( ActionBase action , MetaEnvDC dc , String server , String commitMessage ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String PATH = getLiveConfigServerPath( action , dc , server );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void tagLiveConfigs( ActionBase action , String TAG , String commitMessage ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String PATH = getLiveConfigPath( action );
		String setTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + TAG;
		
		ServerMirrorRepository mirror = action.getMirror( build );
		vcs.createMasterTag( mirror , PATH , setTAG , commitMessage );
	}

	public void exportLiveConfigItem( ActionBase action , MetaEnvServer server , String confName , String TAG , LocalFolder folder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , confName );
		ServerMirrorRepository mirror = action.getMirror( build );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit( "exportLiveConfigItem: unable to export " + confName + " from " + PATH );
		}
		else {
			String useTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit( "exportLiveConfigItem: unable to export " + confName + " from " + PATH + ", TAG=" + useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void exportTemplateConfigItem( ActionBase action , MetaEnvDC dc , String confName , String TAG , LocalFolder folder ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		
		String CONFPATH = build.CONFIG_SOURCE_CFG_ROOTDIR;
		String PATH = Common.getPath( CONFPATH , confName );
		ServerMirrorRepository mirror = action.getMirror( build );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit( "exportTemplateConfigItem: unable to export " + confName + " from " + PATH );
		}
		else {
			String useTAG = meta.product.CONFIG_PRODUCT + "-" + action.context.env.ID + "-" + dc.NAME + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit( "exportTemplateConfigItem: unable to export " + confName + " from " + PATH + ", TAG=" + useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void saveLiveConfigItem( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , String item , LocalFolder folder , String commitMessage ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , item );
		
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.isValidRepositoryMasterPath( mirror , PATH ) ) {
			if( !vcs.isValidRepositoryMasterPath( mirror , SERVERPATH ) )
				vcs.ensureMasterFolderExists( mirror , SERVERPATH , commitMessage );
			vcs.importMasterFolder( mirror , folder , PATH , commitMessage );
			action.info( node.HOSTLOGIN + ": live created at " + PATH );
			return;
		}
		
		// define save path
		LocalFolder coFolder = artefactory.getWorkFolder( action , "config.vcs" );
		coFolder.removeThis( action );
		
		vcs.checkoutMasterFolder( mirror , coFolder , PATH );
		coFolder.prepareFolderForLinux( action , "" );

		FileSet tobeFiles = folder.getFileSet( action );
		FileSet coFiles = coFolder.getFileSet( action ); 
		
		// copy tobe files over co files and prepare for changes
		coFolder.copyDirContent( action , folder );
		saveLiveConfigItemCopyFolder( action , vcs , mirror , tobeFiles , coFiles , folder , coFolder );

		if( vcs.commitMasterFolder( mirror , coFolder , PATH , commitMessage ) )
			action.info( node.HOSTLOGIN + ": live updated at " + PATH );
		else
			action.debug( node.HOSTLOGIN + ": live not changed at " + PATH );
	}

	private void saveLiveConfigItemCopyFolder( ActionBase action , GenericVCS vcs , ServerMirrorRepository mirror , FileSet tobeFiles , FileSet coFiles , LocalFolder folder , LocalFolder coFolder ) throws Exception {
		// add new files
		for( String file : tobeFiles.files.keySet() ) {
			if( !coFiles.files.containsKey( file ) )
				vcs.addFileToCommit( mirror , coFolder , coFiles.dirPath , file );
		}
		
		// delete old files
		for( String file : coFiles.files.keySet() ) {
			if( !tobeFiles.files.containsKey( file ) )
				vcs.deleteFileToCommit( mirror , coFolder , coFiles.dirPath , file );
		}
		
		// add new dirs and check subfolders
		for( FileSet tobeDir : tobeFiles.dirs.values() ) {
			FileSet coDir = coFiles.dirs.get( tobeDir.dirName );
			if( coDir == null )
				vcs.addDirToCommit( mirror , coFolder , Common.getPath( coFiles.dirPath , tobeDir.dirName ) );
			else
				saveLiveConfigItemCopyFolder( action , vcs , mirror , tobeDir , coDir , folder , coFolder );
		}
		
		// delete old dirs
		for( String coDir : coFiles.dirs.keySet() ) {
			if( coDir.equals( ".svn" ) || coDir.equals( ".git" ) )
				continue;
			if( !tobeFiles.dirs.containsKey( coDir ) )
				vcs.deleteDirToCommit( mirror , coFolder , Common.getPath( coFiles.dirPath , coDir ) );
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
		MetaProductBuildSettings build = action.getBuildSettings();
		GenericVCS vcs = GenericVCS.getVCS( action , build.CONFIG_SOURCE_RESOURCE , false );
		
		String CONFPATH = build.CONFIG_SOURCE_SQL_POSTREFRESH;
		String PATH = Common.getPath( CONFPATH , name );
		ServerMirrorRepository mirror = action.getMirror( build );
		if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , name ) )
			action.exit( "exportTemplateConfigItem: unable to export " + name + " from " + PATH );
		
		// remove windows newlines and add permissions to shell files
		if( action.isLocalLinux() )
			folder.prepareFolderForLinux( action , name );
	}
	
}
