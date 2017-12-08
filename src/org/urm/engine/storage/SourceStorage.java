package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.action.conf.ConfSourceFolder;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.vcs.GenericVCS;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerDeployment;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaProductBuildSettings;

public class SourceStorage {

	Artefactory artefactory;
	LocalFolder downloadFolder;
	Meta meta;

	public static String CONFIG_FOLDER = "config";
	public static String DATABASE_FOLDER = "db";
	public static String ERRORS_FOLDER = "errors";
	public static String MANUAL_FOLDER = "manual";

	public static String DATA_TEMPLATES = "templates";
	public static String DATA_CHANGES = "changes";
	public static String DATA_LIVE = "live";
	public static String DATA_POSTREFRESH = "postrefresh";
	
	public SourceStorage( Artefactory artefactory , Meta meta , LocalFolder downloadFolder ) {
		this.artefactory = artefactory;
		this.meta = meta;
		this.downloadFolder = downloadFolder;
	}
	
	private GenericVCS getMirrorVCS( ActionBase action , MirrorRepository mirror ) throws Exception {
		return( GenericVCS.getVCS( action , meta , mirror.RESOURCE_ID ) );
	}
	
	public void downloadThirdpartyItemFromVCS( ActionBase action , String ITEMPATH , String FOLDER ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
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
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !mirror.isActive() ) {
			action.error( "release mirror has not been initialized. Skipped." );
			return( false );
		}
		
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATAReleaseManualPath( action , distStorage );

		if( downloadManualFolder( action , vcs , PATH , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no manual files at " + path + ". Skipped." );
		return( false );
	}
	
	public boolean downloadReleaseConfigItem( ActionBase action , Dist distStorage , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATAReleaseConfigSourcePath( action , distStorage , sourceFolder.releaseComp );
		
		if( downloadConfigItem( action , vcs , PATH , sourceFolder.distrComp , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no configuration at " + path + ". Skipped." );
		return( false );
	}

	public boolean downloadReleaseDatabaseFiles( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATAReleaseDBSourcePath( action , distStorage , dbDelivery );
		
		if( downloadDBFiles( action , vcs , PATH , dbDelivery , dstFolder ) )
			return( true );
		
		String path = vcs.getInfoMasterPath( mirror , PATH );
		action.info( "no database changes at " + path + ". Skipped." );
		return( false );
	}
	
	public boolean downloadProductConfigItem( ActionBase action , ConfSourceFolder sourceFolder , LocalFolder dstFolder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATAProductConfigSourcePath( action , sourceFolder.distrComp );

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
	
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , distrComp.KEY ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH , mirror.NAME , ITEMPATH );
		
		dstFolder.prepareFolderForLinux( action , distrComp.KEY );
		return( true );
	}
	
	private boolean downloadDBFiles( ActionBase action , GenericVCS vcs , String ITEMPATH , MetaDistrDelivery dbDelivery , LocalFolder dstFolder ) throws Exception {
		if( !isValidPath( action , vcs , ITEMPATH ) )
			return( false );
	
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstFolder , ITEMPATH , DATABASE_FOLDER ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", ITEMPATH=" + ITEMPATH , mirror.NAME , ITEMPATH );
		
		if( action.isLocalLinux() )
			dstFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	private boolean downloadManualFolder( ActionBase action , GenericVCS vcs , String PATH , LocalFolder dstManualFolder ) throws Exception {
		if( !isValidPath( action , vcs , PATH ) )
			return( false );
	
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		if( !vcs.exportRepositoryMasterPath( mirror , dstManualFolder.getParentFolder( action ) , PATH , dstManualFolder.folderName ) )
			action.exit2( _Error.UnableExportMirror2 , "unable to export from mirror=" + mirror.NAME + ", PATH=" + PATH , mirror.NAME , PATH );
		
		if( dstManualFolder.checkFolderExists( action , DATABASE_FOLDER ) )
			dstManualFolder.prepareFolderForLinux( action , DATABASE_FOLDER );
		return( true );
	}
	
	public void moveReleaseDatabaseFilesToErrors( ActionBase action , String errorFolder , Dist distStorage , MetaDistrDelivery dbDelivery , String movePath , String message ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = GenericVCS.getVCS( action , meta , mirror.RESOURCE_ID );
		String SRCPATH = getDATAReleaseDBSourcePath( action , distStorage , dbDelivery );
		String ERRORPATH = getDATAReleaseErrorsPath( action , distStorage , dbDelivery , errorFolder );
		
		vcs.createMasterFolder( mirror , ERRORPATH , "create error folder" );
		vcs.moveMasterFiles( mirror , SRCPATH , ERRORPATH , movePath , message );
	}
	
	public boolean isValidPath( ActionBase action , GenericVCS vcs , String PATH ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
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

	public String[] getLiveConfigItems( ActionBase action , MetaEnvServer server ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATALiveConfigServerPath( action , server.sg , server.NAME );
		
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public String[] getLiveConfigServers( ActionBase action , MetaEnvSegment sg ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATALiveConfigSegmentPath( action , sg );
		
		String[] list = vcs.listMasterItems( mirror , PATH );
		return( list );
	}

	public void deleteLiveConfigItem( ActionBase action , MetaEnvServer server , String item , String commitMessage ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATALiveConfigServerPath( action , server.sg , server.NAME );
		PATH = Common.getPath( PATH , item );
		
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void deleteLiveConfigServer( ActionBase action , MetaEnvSegment sg , String server , String commitMessage ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATALiveConfigServerPath( action , sg , server );
		
		vcs.deleteMasterFolder( mirror , PATH , commitMessage );
	}

	public void tagLiveConfigs( ActionBase action , String TAG , String commitMessage ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String PATH = getDATALiveConfigEnvPath( action , action.context.env );
		String setTAG = TAG;
		
		vcs.createMasterTag( mirror , PATH , setTAG , commitMessage );
	}

	public void exportLiveConfigItem( ActionBase action , MetaEnvServer server , String confName , String TAG , LocalFolder folder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String SERVERPATH = getDATALiveConfigServerPath( action , server.sg , server.NAME );
		String PATH = Common.getPath( SERVERPATH , confName );
		String path = vcs.getInfoMasterPath( mirror , PATH );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit2( _Error.UnableExportConfig2 , "unable to export " + confName + " from " + path , confName , path );
		}
		else {
			String useTAG = TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit3( _Error.UnableExportConfigTag3 , "unable to export " + confName + " from " + path + ", TAG=" + useTAG , confName , path , useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void exportTemplateConfigItem( ActionBase action , MetaEnvSegment sg , String confName , String TAG , LocalFolder folder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String CONFPATH = DATA_TEMPLATES;
		String PATH = Common.getPath( CONFPATH , confName );
		String path = vcs.getInfoMasterPath( mirror , PATH );
		if( TAG.isEmpty() ) {
			if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , confName ) )
				action.exit2( _Error.UnableExportConfig2 , "unable to export " + confName + " from " + path , confName , path );
		}
		else {
			String useTAG = TAG;
			if( !vcs.exportRepositoryTagPath( mirror , folder , useTAG , PATH , confName ) )
				action.exit3( _Error.UnableExportConfigTag3 , "unable to export " + confName + " from " + path + ", TAG=" + useTAG , confName , path , useTAG );
		}
		
		// remove windows newlines and add permissions to shell files
		folder.prepareFolderForLinux( action , confName );
	}
	
	public void saveLiveConfigItem( ActionBase action , MetaEnvServer server , MetaEnvServerNode node , String item , LocalFolder folder , String commitMessage ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		String SERVERPATH = getDATALiveConfigServerPath( action , server.sg , server.NAME );
		String PATH = Common.getPath( SERVERPATH , item );

		String path = vcs.getInfoMasterPath( mirror , PATH );
		if( !vcs.isValidRepositoryMasterPath( mirror , PATH ) ) {
			if( !vcs.isValidRepositoryMasterPath( mirror , SERVERPATH ) )
				vcs.ensureMasterFolderExists( mirror , SERVERPATH , commitMessage );
			vcs.importMasterFolder( mirror , folder , PATH , commitMessage );
			action.info( node.HOSTLOGIN + ": live created at " + path );
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

		if( vcs.commitMasterFolder( mirror , coFolder , "/" , commitMessage ) )
			action.info( node.HOSTLOGIN + ": live updated at " + path );
		else
			action.debug( node.HOSTLOGIN + ": live not changed at " + path );
	}

	private void saveLiveConfigItemCopyFolder( ActionBase action , GenericVCS vcs , MirrorRepository mirror , FileSet tobeFiles , FileSet coFiles , LocalFolder folder , LocalFolder coFolder ) throws Exception {
		// add new files
		for( String file : tobeFiles.getAllFiles() ) {
			if( !coFiles.findFileByName( file ) )
				vcs.addFileToCommit( mirror , coFolder , coFiles.dirPath , file );
		}
		
		// delete old files
		for( String file : coFiles.getAllFiles() ) {
			if( !tobeFiles.findFileByName( file ) )
				vcs.deleteFileToCommit( mirror , coFolder , coFiles.dirPath , file );
		}
		
		// add new dirs and check subfolders
		for( FileSet tobeDir : tobeFiles.getAllDirs() ) {
			FileSet coDir = coFiles.findDirByName( tobeDir.dirName );
			if( coDir == null )
				vcs.addDirToCommit( mirror , coFolder , Common.getPath( coFiles.dirPath , tobeDir.dirName ) );
			else
				saveLiveConfigItemCopyFolder( action , vcs , mirror , tobeDir , coDir , folder , coFolder );
		}
		
		// delete old dirs
		for( String coDir : coFiles.getAllDirNames() ) {
			if( coDir.equals( ".svn" ) || coDir.equals( ".git" ) )
				continue;
			if( tobeFiles.findDirByName( coDir ) == null )
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
				exportTemplateConfigItem( action , server.sg , deployment.confItem.KEY , action.context.CTX_TAG , parent );
				continue;
			}
				
			// deployments
			if( deployment.comp == null )
				continue;
			
			for( MetaDistrComponentItem compItem : deployment.comp.getConfItems() ) {
				if( compItem.confItem != null )
					exportTemplateConfigItem( action , server.sg , compItem.confItem.KEY , action.context.CTX_TAG , parent );
			}
		}
	}

	public void exportTemplates( ActionBase action , MetaEnvSegment sg , LocalFolder parent , MetaDistrConfItem[] items ) throws Exception {
		for( MetaDistrConfItem item : items )
			exportTemplateConfigItem( action , sg , item.KEY , "" , parent );
	}
		
	public void exportPostRefresh( ActionBase action , String name , LocalFolder folder ) throws Exception {
		ProductMeta storage = meta.getStorage();
		MirrorRepository mirror = action.getConfigurationMirror( storage );
		GenericVCS vcs = getMirrorVCS( action , mirror );
		
		String CONFPATH = DATA_POSTREFRESH;
		String PATH = Common.getPath( CONFPATH , name );
		String path = vcs.getInfoMasterPath( mirror , PATH );
		if( !vcs.exportRepositoryMasterPath( mirror , folder , PATH , name ) )
			action.exit2( _Error.UnableExportConfig2 , "unable to export " + name + " from " + path , name , path );
		
		// remove windows newlines and add permissions to shell files
		if( action.isLocalLinux() )
			folder.prepareFolderForLinux( action , name );
	}
	
	private String getDATAReleasePath( ActionBase action , Dist distStorage ) throws Exception {
		String PATH = Common.getPath( DATA_CHANGES , 
			getReleaseGroupFolder( action ) ,
			getReleaseFolder( action , distStorage ) );
		return( PATH );
	}
	
	private String getDATAReleaseManualPath( ActionBase action , Dist distStorage ) throws Exception {
		String PATH = Common.getPath( getDATAReleasePath( action , distStorage ) , MANUAL_FOLDER );
		return( PATH );
	}

	private String getDATAReleaseConfigSourcePath( ActionBase action , Dist distStorage , ReleaseTarget releaseComp ) throws Exception {
		String PATH = Common.getPath( getDATAReleasePath( action , distStorage ) , 
			getConfFolderRelPath( action , releaseComp.distConfItem ) );
		return( PATH );
	}

	private String getDATAReleaseDBSourcePath( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery ) throws Exception {
		String PATH = Common.getPath( getDATAReleasePath( action , distStorage ) , 
			getDBFolderRelPath( action , dbDelivery ) );
		return( PATH );
	}

	private String getDATAReleaseErrorsPath( ActionBase action , Dist distStorage , MetaDistrDelivery dbDelivery , String errorFolder ) throws Exception {
		String PATH = Common.getPath( getDATAReleasePath( action , distStorage ) , 
			getErrorFolderRelPath( action , dbDelivery , errorFolder ) );
		return( PATH );
	}

	private String getDATAProductConfigSourcePath( ActionBase action , MetaDistrConfItem distrComp ) throws Exception {
		String PATH = Common.getPath( DATA_TEMPLATES , distrComp.KEY );
		return( PATH );
	}

	private String getDATALiveConfigSegmentPath( ActionBase action , MetaEnvSegment sg ) throws Exception {
		String PATH = Common.getPath( DATA_LIVE , sg.env.NAME , sg.NAME );
		return( PATH );
	}
	
	private String getDATALiveConfigEnvPath( ActionBase action , MetaEnv env ) throws Exception {
		String PATH = Common.getPath( DATA_LIVE , env.NAME );
		return( PATH );
	}
	
	private String getDATALiveConfigServerPath( ActionBase action , MetaEnvSegment sg , String server ) throws Exception {
		String PATH = Common.getPath( getDATALiveConfigSegmentPath( action , sg ) , server );
		return( PATH );
	}
	
}
