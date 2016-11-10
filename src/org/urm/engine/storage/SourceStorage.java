package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.action.conf.ConfSourceFolder;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.vcs.GenericVCS;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerMirrorRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaProductSettings;

public class SourceStorage {

	Artefactory artefactory;
	LocalFolder downloadFolder;
	Meta meta;

	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String ERRORS_FOLDER = "errors";
	public static String MANUAL_FOLDER = "manual";
	
	public SourceStorage( Artefactory artefactory , Meta meta , LocalFolder downloadFolder ) {
		this.artefactory = artefactory;
		this.meta = meta;
		this.downloadFolder = downloadFolder;
	}
	
	private GenericVCS getMirrorVCS( ActionBase action , ServerMirrorRepository mirror ) throws Exception {
		return( GenericVCS.getVCS( action , meta , mirror.getResource( action ) ) );
	}
	
	public void downloadThirdpartyItemFromVCS( ActionBase action , String ITEMPATH , String FOLDER ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String BASENAME = Common.getBaseName( ITEMPATH );
		
		downloadFolder.ensureFolderExists( action , FOLDER );
		downloadFolder.removeFolderFile( action , FOLDER , BASENAME );
		
		LocalFolder subFolder = downloadFolder;
		if( !FOLDER.isEmpty() )
			subFolder = subFolder.getSubFolder( action , FOLDER );
		
		if( !vcs.exportRepositoryMasterPath( mirror , subFolder , ITEMPATH , BASENAME ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH , mirror.NAME , ITEMPATH );
	}
	
	public boolean downloadReleaseManualFolder( ActionBase action , Dist distStorage , LocalFolder dstFolder ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getReleaseManualPath( action , distStorage );

		if( downloadManualFolder( action , vcs , PATH , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no manual files at " + path + ". Skipped." );
		return( false );
	}
	
	public boolean downloadReleaseConfigItem( ActionBase action , Dist distStorage , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getReleaseConfigSourcePath( action , distStorage , sourceFolder.releaseComp );
		
		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no configuration at " + path + ". Skipped." );
		return( false );
	}

	public boolean downloadReleaseDatabaseFiles( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		
		if( downloadDBFiles( action , vcs , PATH , dbDelivery , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no database changes at " + path + ". Skipped." );
		return( false );
	}
	
	public boolean downloadProductConfigItem( ActionBase action , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getProductConfigSourcePath( action , sourceFolder.distrComp );

		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.ifexit( _Error.MissingConfItem1 , "unable to find configuration at " + path , new String[] { path } );
		
		action.info( "no configuration at " + path + ". Skipped." );
		return( false );
	}

	private boolean downloadConfigItem( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrConfItem distrComp , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , distrComp.KEY ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH , mirror.NAME , ITEMPATH );
		
		dstFolder.prepareFolderForLinux( action , distrComp.KEY );
		return( true );
	}
	
	private boolean downloadDBFiles( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , DATABASE_FOLDER ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH , mirror.NAME , ITEMPATH );
		
		if( action.isLocalLinux() )
			dstFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	private boolean downloadManualFolder( ActionBase action , GenericVCS vcs , String PATH , LocalFolder dstManualFolder ) throws Exception {
		if( !isValidPath( action , vcs , PATH ) )
			return( false );
	
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstManualFolder.getParentFolder( action ) , PATH , dstManualFolder.folderName ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", PATH=" + PATH , mirror.NAME , PATH );
		
		if( dstManualFolder.checkFolderExists( action , DATABASE_FOLDER ) )
			dstManualFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	public void moveReleaseDatabaseFilesToErrors( ActionBase action , String errorFolder , Dist distStorage , MetaDistrDelivery dbDelivery , String movePath , String message ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = GenericVCS.getVCS( action , meta , mirror.getResource( action ) );
		String SRCPATH = getReleaseDBSourcePath( action , distStorage , dbDelivery );
		String ERRORPATH = getReleaseErrorsPath( action , distStorage , dbDelivery , errorFolder );
		
		vcs.createMasterFolder( mirror , ERRORPATH , "create error folder" );
		vcs.moveMasterFiles( mirror , SRCPATH , ERRORPATH , movePath , message );
	}
	
	public boolean isValidPath( ActionBase action , GenericVCS vcs , String PATH ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		return( vcs.isValidRepositoryMasterPath( mirror , PATH ) );
	}
	
	public String getReleaseGroupFolder( ActionBase action ) throws Exception {
		MetaProductBuildSettings build = action.getBuildSettings( meta );
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
		MetaProductSettings settings = meta.getProductSettings( action );
		String PATH = Common.getPath( settings.CONFIG_SOURCE_RELEASEROOTDIR , 
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
		MetaProductSettings settings = meta.getProductSettings( action );
		String PATH = Common.getPath( settings.CONFIG_SOURCE_CFG_ROOTDIR , distrComp.KEY );
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
		MetaProductSettings settings = meta.getProductSettings( action );
		String PATH = Common.getPath( settings.CONFIG_SOURCE_CFG_LIVEROOTDIR , dc.env.ID , dc.NAME );
		return( PATH );
	}
	
	public String getLiveConfigPath( ActionBase action ) throws Exception {
		MetaProductSettings settings = meta.getProductSettings( action );
		String PATH = Common.getPath( settings.CONFIG_SOURCE_CFG_LIVEROOTDIR , action.context.env.ID );
		return( PATH );
	}
	
	public String getLiveConfigServerPath( ActionBase action , MetaEnvDC dc , String server ) throws Exception {
		String PATH = Common.getPath( getLiveConfigDCPath( action , dc ) , server );
		return( PATH );
	}
	
	public String[] getLiveConfigItems( ActionBase action , MetaEnvServer server ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public String[] getLiveConfigServers( ActionBase action , MetaEnvDC dc ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getLiveConfigDCPath( action , dc );
		
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public void deleteLiveConfigItem( ActionBase action , MetaEnvServer server , String item , String commitMessage ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		PATH = Common.getPath( PATH , item );
		
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void deleteLiveConfigServer( ActionBase action , MetaEnvDC dc , String server , String commitMessage ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getLiveConfigServerPath( action , dc , server );
		
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void tagLiveConfigs( ActionBase action , String TAG , String commitMessage ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getLiveConfigPath( action );
		String setTAG = meta.name + "-" + action.context.env.ID + "-" + TAG;
		
		vcs.createMasterTag( mirror , PATH , setTAG , commitMessage );
	}

	public void exportLiveConfigItem( ActionBase action , MetaEnvServer server , String confName , String TAG , LocalFolder folder ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , confName );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit2( _Error.UnableExportConfig2 , "unable to export " + confName + " from " + PATH , confName , PATH );
		}
		else {
			String useTAG = meta.name + "-" + action.context.env.ID + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit3( _Error.UnableExportConfigTag3 , "unable to export " + confName + " from " + PATH + ", TAG=" + useTAG , confName , PATH , useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void exportTemplateConfigItem( ActionBase action , MetaEnvDC dc , String confName , String TAG , LocalFolder folder ) throws Exception {
		MetaProductSettings settings = meta.getProductSettings( action );
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String CONFPATH = settings.CONFIG_SOURCE_CFG_ROOTDIR;
		String PATH = Common.getPath( CONFPATH , confName );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit2( _Error.UnableExportConfig2 , "unable to export " + confName + " from " + PATH , confName , PATH );
		}
		else {
			String useTAG = meta.name + "-" + action.context.env.ID + "-" + dc.NAME + "-" + TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit3( _Error.UnableExportConfigTag3 , "unable to export " + confName + " from " + PATH + ", TAG=" + useTAG , confName , PATH , useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void saveLiveConfigItem( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , String item , LocalFolder folder , String commitMessage ) throws Exception {
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String SERVERPATH = getLiveConfigServerPath( action , server.dc , server.NAME );
		String PATH = Common.getPath( SERVERPATH , item );
		
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
		for( MetaEnvServerDeployment deployment : server.getDeployments() ) {
			if( deployment.confItem != null ) {
				exportTemplateConfigItem( action , server.dc , deployment.confItem.KEY , action.context.CTX_TAG , parent );
				continue;
			}
				
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems() ) {
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
		MetaProductSettings settings = meta.getProductSettings( action );
		ServerProductMeta storage = meta.getStorage( action );
		ServerMirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String CONFPATH = settings.CONFIG_SOURCE_SQL_POSTREFRESH;
		String PATH = Common.getPath( CONFPATH , name );
		String path = vcs.getInfoMasterPath( mirror , PATH );
		if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , name ) )
			action.exit2( _Error.UnableExportConfig2 , "unable to export " + name + " from " + path , name , path );
		
		// remove windows newlines and add permissions to shell files
		if( action.isLocalLinux() )
			folder.prepareFolderForLinux( action , name );
	}
	
}
