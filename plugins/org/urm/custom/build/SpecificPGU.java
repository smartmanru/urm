package org.urm.custom.build;

import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.Dist;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.storage.Artefactory;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.NexusDownloadInfo;
import org.urm.engine.storage.NexusStorage;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;

public class SpecificPGU {

	Meta meta;
	LocalFolder downloadFolder;
	ActionBase action;
	Artefactory artefactory;
	
	ActionScope scope;

	boolean USE_PROD_DISTR;
	String VERSION;
	Dist srcRelease;
	String SERVICECALL_EXT;
	String STORAGESERVICE_EXT;

	String SERVICECALL_DIR;  
	String STORAGESERVICE_DIR;  

	public static String C_WARDEFAULTPROJECT = "idecs";
	public static String C_ITEMSERVICECALL = "servicecall";
	public static String C_ITEMSTORAGESERVICE = "storageservice";
	public static String C_PGUWARNEXUSGROUPID = "com.nvision.pgu.service";
	public static String C_SERVICECALLGROUPID = "ru.nvg.idecs.servicecall";
	public static String C_STORAGESERVICEGROUPID = "ru.nvg.idecs.storageservice";
	public static String C_PGUFEDGROUPID = "ru.atc.pgu.fed.common.util";

	MetaDistrBinaryItem servicecallItem;
	MetaDistrBinaryItem storageserviceItem;
	
	public SpecificPGU( ActionBase action , LocalFolder downloadFolder ) {
		this.action = action;
		this.downloadFolder = downloadFolder;
		
		this.meta = null;
		this.artefactory = action.artefactory;
	}
	
	public String getPguServiceCallExt() throws Exception {
		MetaProductSettings settings = meta.getProductSettings();
		ObjectProperties ops = settings.getParameters();
		String custom = ops.getStringProperty( "CUSTOM_SERVICECALL_EXT" );
		if( custom != null && !custom.isEmpty() )
			return( custom );
		
		return( "ear" );
	}
	
	public String getPguStorageServiceExt() throws Exception {
		MetaProductSettings settings = meta.getProductSettings();
		ObjectProperties ops = settings.getParameters();
		String custom = ops.getStringProperty( "CUSTOM_STORAGESERVICE_EXT" );
		if( custom != null && !custom.isEmpty() )
			return( custom );
		
		return( "ear" );
	}

	private MetaDistrBinaryItem getWarItem( MetaSourceProject project , int pos ) throws Exception {
		MetaSourceProjectItem item = project.getItems()[ pos ];
		return( item.distItem );
	}
	
	public void downloadWarCopyDistr( boolean copyDistr , Dist release , String VERSION_TAGNAME , ActionScopeTarget scopeProject ) throws Exception {
		MetaDistrBinaryItem distItem = getWarItem( scopeProject.sourceProject , 0 );
		
		ProjectBuilder builder = scopeProject.sourceProject.getBuilder( action );
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , builder.TARGET_RESOURCE_ID , meta , downloadFolder );
		NexusDownloadInfo WAR = nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , distItem.BASENAME_DIST , VERSION , "war" , "" , distItem );
		NexusDownloadInfo STATIC = nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , distItem.BASENAME_DIST , VERSION , "tar.gz" , "webstatic" , distItem );
		nexusStorage.repackageStatic( action , scopeProject.sourceProject.NAME , VERSION , WAR.DOWNLOAD_FILENAME , STATIC.DOWNLOAD_FILENAME , VERSION_TAGNAME , distItem );

		if( copyDistr ) {
			Dist distStorage = release;
			distStorage.copyVFileToDistr( action , distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME , WAR.EXT );
			distStorage.copyVFileToDistr( action , distItem , downloadFolder , STATIC.DOWNLOAD_FILENAME , STATIC.BASENAME , STATIC.EXT );
		}
	}
	
	public void getAllWarApp( boolean copyDistr , Dist release , boolean USE_PROD_DISTR , String VERSION , Dist srcRelease , ActionScope scope ) throws Exception {
		this.USE_PROD_DISTR = USE_PROD_DISTR;
		this.srcRelease = srcRelease;
		this.VERSION = VERSION;
		this.scope = scope;
		
		SERVICECALL_EXT = getPguServiceCallExt();
		STORAGESERVICE_EXT = getPguStorageServiceExt();
	
		SERVICECALL_DIR = ( SERVICECALL_EXT.equals( "ear" ) )? "APP-INF" : "WEB-INF";  
		STORAGESERVICE_DIR = ( STORAGESERVICE_EXT.equals( "ear" ) )? "APP-INF" : "WEB-INF";  
		
		action.info( "getAllWarApp: create servicecall." + SERVICECALL_EXT + " and storageservice." + STORAGESERVICE_EXT + " ..." );
		
		downloadFolder.removeFolder( action , "pgu-services-lib" );
		downloadFolder.removeFolder( action , "servicecall-prod-libs" );

		MetaDistr distr = meta.getDistr();
		servicecallItem = distr.getBinaryItem( C_ITEMSERVICECALL );
		storageserviceItem = distr.getBinaryItem( C_ITEMSTORAGESERVICE );
		
		getAllWarAppDownloadCore();
	
		// unzip servicecall libs - from current PROD (distr/xxx-prod) - action for branch mode only
		if( USE_PROD_DISTR )
			getAllWarAppCopyProd();
	
		getAllWarAppDownloadLibs( release );
		getAllWarAppUpdateLibs( release );
		getAllWarAppCreateBinaries( copyDistr , release );
		getAllWarAppDownloadDeps( copyDistr , release );
	
		downloadFolder.removeFolder( action , "pgu-services-lib" );
		downloadFolder.removeFolder( action , "servicecall-prod-libs" );
	}

	private void getAllWarAppDownloadCore() throws Exception {
		if( srcRelease == null ) {
			action.info( "downloading core servicecall and storageservice from Nexus - to " + artefactory.workFolder.folderPath + " ..." );
			
			ProjectBuilder builder = servicecallItem.sourceProjectItem.project.getBuilder( action );
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , builder.TARGET_RESOURCE_ID , meta , downloadFolder );
			nexusStorage.downloadNexus( action , C_SERVICECALLGROUPID , "servicecall" , VERSION , SERVICECALL_EXT , "" , servicecallItem );
			nexusStorage.downloadNexus( action , C_STORAGESERVICEGROUPID , "storageservice" , VERSION , STORAGESERVICE_EXT , "" , storageserviceItem );
		}
		else {
			Dist distStorage = srcRelease;
			action.info( "copy servicecall and storageservice from " + distStorage.RELEASEDIR + " - to " + downloadFolder.folderPath + " ..." );
			distStorage.copyDistToFolder( action , downloadFolder , "servicecall-" + VERSION + "." + SERVICECALL_EXT );
			distStorage.copyDistToFolder( action , downloadFolder , "storageservice-" + VERSION + "." + STORAGESERVICE_EXT );
		}

		// unzip servicecall and storageservice
		String file = "servicecall-" + VERSION + "." + SERVICECALL_EXT;
		String dir = "servicecall-" + VERSION;
		downloadFolder.unzipToFolder( action , file , dir );
		downloadFolder.removeFiles( action , file );
		downloadFolder.renameFolder( action , dir , file );

		file = "storageservice-" + VERSION + "." + STORAGESERVICE_EXT;
		dir = "storageservice-" + VERSION;
		downloadFolder.unzipToFolder( action , file , dir );
		downloadFolder.removeFiles( action , file );
		downloadFolder.renameFolder( action , dir , file );
	}
	
	private void getAllWarAppCopyProd() throws Exception {
		Dist distStorage = action.getMasterDist( meta );

		action.debug( "copy libraries from " + distStorage.RELEASEDIR + "/servicecall." + SERVICECALL_EXT + " to servicecall-prod-libs ..." );
		distStorage.unzipDistFileToFolder( action , downloadFolder , "servicecall-*." + SERVICECALL_EXT , servicecallItem.delivery.FOLDER , Common.getQuoted( SERVICECALL_DIR + "/lib/*" ) , "servicecall-prod-libs" );
	}
	
	private void getAllWarAppDownloadLibs( Dist release ) throws Exception {
		// create directory for libs and "cd" to it
		LocalFolder libFolder = downloadFolder.getSubFolder( action , "pgu-services-lib" );
		ProjectBuilder builder = servicecallItem.sourceProjectItem.project.getBuilder( action );
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , builder.TARGET_RESOURCE_ID , meta , libFolder );

		// download latest API libs - pfr, fed-common-util
		if( meta.name.equals( "fedpgu" ) ) {
			action.debug( "download API libs for pfr and fed-common-util from Nexus - to pgu-services-lib ..." );
			nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , "pfr-api" , VERSION , "jar" , "" , servicecallItem );
			nexusStorage.downloadNexus( action , C_PGUFEDGROUPID , "pgu-fed-common-util" , VERSION , "jar" , "" , servicecallItem );
		}

		// download latest built libs for all microportals
		action.debug( "download last built libs for all microportals from Nexus - to pgu-services-lib ..." );
		Map<String,ActionScopeTarget> projects = scope.getCategorySetTargets( action , DBEnumScopeCategoryType.SEARCH_SOURCEBUILDABLE );
		
		for( ActionScopeTarget scopeProject : projects.values() ) {
			MetaDistrBinaryItem distItem = getWarItem( scopeProject.sourceProject , 1 );
			String LIB = distItem.NAME;
			nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , LIB , VERSION , "jar" , "" , servicecallItem );
		}
	}

	private void getAllWarAppUpdateLibs( Dist release ) throws Exception {
		// copy all libs from -
		//   current release - if microportal exists in current release distributive
		//   previous release (prod) - otherwise

		if( meta.name.equals( "fedpgu" ) ) {
			// pgu-fed-common-util - always use last built
			if( srcRelease == null )
				getAllWarAppCopySpecificBuilt( "pgu-fed-common" );
		}

		action.debug( "copy libs to servicecall and storageservice from pgu-services-lib and servicecall-prod-libs ..." );
		MetaSources sources = meta.getSources();
		List<MetaSourceProject> list = sources.getAllProjectList( true );
		Dist releaseStorage = release;
		
		for( MetaSourceProject sourceProject : list ) {
			getAllWarAppGetProjectLib( sourceProject , releaseStorage );
		}
	}
	
	private void getAllWarAppGetProjectLib( MetaSourceProject sourceProject , Dist release ) throws Exception {
		MetaDistrBinaryItem libItem = getWarItem( sourceProject , 1 );
		String lib = libItem.NAME + "-" + VERSION + ".jar";

		boolean RELEASED_TO_PROD = false;
		if( USE_PROD_DISTR ) {
			if( artefactory.workFolder.checkFileExists( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ) )
				RELEASED_TO_PROD = true;
		}

		// check if $lib exists in $P_DISTR_DSTDIR/$P_PROJECT ...
		boolean copyFromProd = false;
		if( USE_PROD_DISTR == true && RELEASED_TO_PROD == true )
			if( !release.checkFileExists( action , libItem.NAME + "*war" ) )
				copyFromProd = true;
		
		if( copyFromProd ) {
			// if microportal is NOT in current distributive & present in current PROD - then copy lib from PROD
			if( downloadFolder.checkFileExists( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ) ) {
				action.debug( "copy library " + lib + " from servicecall-prod-libs to servicecall and storageservice ..." );
				downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ,
						"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
				downloadFolder.copyFile( action , "storageservice-prod-libs/" + STORAGESERVICE_DIR + "/lib/" + lib ,
						"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );

				getAllWarAppCopySpecificProd( sourceProject.NAME );
			}
			else
				action.debug( lib + ": not found in servicecall-prod-libs. Skipped." );
		}
		else {
			// copy from nexus by default, otherwise keep as source
			if( srcRelease == null ) {
				if( downloadFolder.checkFileExists( action , "pgu-services-lib/" + lib ) ) {
					action.debug( "copy new library " + lib + "from pgu-services-lib to servicecall and storageservice ..." );
					
					downloadFolder.copyFile( action , "pgu-services-lib/" + lib , 
							"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
					downloadFolder.copyFile( action , "pgu-services-lib/" + lib , 
							"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );

					getAllWarAppCopySpecificBuilt( sourceProject.NAME );
				}
				else
					action.debug( lib + ": not found in pgu-services-lib. Skipped." );
			}
		}
	}
	
	private void getAllWarAppCreateBinaries( boolean copyDistr , Dist release ) throws Exception {
		// Compress modified servicecall and storageservice
		action.debug( "compressing patched servicecall." + SERVICECALL_EXT + " ..." );
		String jarFile = "servicecall-" + VERSION + ".jar";
		String finalName = "servicecall-" + VERSION + "." + SERVICECALL_EXT;
		downloadFolder.createJarFromFolder( action , jarFile , finalName );
		downloadFolder.removeFolder( action , finalName );
		downloadFolder.renameFile( action , jarFile , finalName );
		downloadFolder.md5file( action , finalName );

		action.debug( "compressing patched storageservice." + STORAGESERVICE_EXT + " ..." );
		jarFile = "storageservice-" + VERSION + ".jar";
		finalName = "storageservice-" + VERSION + "." + STORAGESERVICE_EXT;
		downloadFolder.createJarFromFolder( action , jarFile , finalName );
		downloadFolder.removeFolder( action , finalName );
		downloadFolder.renameFile( action , jarFile , finalName );
		downloadFolder.md5file( action , finalName );
		
		if( copyDistr ) {
			Dist releaseStorage = release;
			releaseStorage.copyVFileToDistr( action , servicecallItem , downloadFolder , "servicecall-" + VERSION + "." + SERVICECALL_EXT , "servicecall" , SERVICECALL_EXT );
			releaseStorage.copyVFileToDistr( action , storageserviceItem , downloadFolder , "storageservice-" + VERSION + "." + STORAGESERVICE_EXT , "storageservice" , STORAGESERVICE_EXT );
		}
	}
	
	private void getAllWarAppDownloadDeps( boolean copyDistr , Dist release ) throws Exception {
		MetaSources sources = meta.getSources();
		MetaSourceProject sourceProject = sources.getProject( "pgu-portal" );
		MetaSourceProjectItem sourceItem = sourceProject.getItem( "pgu-dependencies" );
		MetaDistrBinaryItem distItem = sourceItem.distItem;
		
		String GROUPID = sourceItem.PATH.replace( '/' , '.' );
		String EXT = sourceItem.EXT.substring( 1 );
		
		ProjectBuilder builder = sourceProject.getBuilder( action );
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , builder.TARGET_RESOURCE_ID , meta , downloadFolder );
		nexusStorage.downloadNexus( action , GROUPID , sourceItem.BASENAME , VERSION , EXT , "" , distItem );
		if( copyDistr ) {
			Dist releaseStorage = release;
			releaseStorage.copyVFileToDistr( action , distItem , downloadFolder , sourceItem.BASENAME + "-" + VERSION + "." + EXT , sourceItem.BASENAME , EXT );
		}
	}

	private void getAllWarAppCopySpecificBuilt( String LIB_PROJECT ) throws Exception {
		if( LIB_PROJECT.equals( "pgu-pfr" ) ) {
			downloadFolder.copyFile( action , "pgu-services-lib/pfr-api-" + VERSION + ".jar" , 
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
			downloadFolder.copyFile( action , "pgu-services-lib/pfr-api-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );
		}
		else if( LIB_PROJECT.equals( "pgu-fed-common" ) ) {
			downloadFolder.copyFile( action , "pgu-services-lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
			downloadFolder.copyFile( action , "pgu-services-lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );
		}
	}

	private void getAllWarAppCopySpecificProd( String LIB_PROJECT ) throws Exception {
		if( LIB_PROJECT.equals( "pgu-pfr" ) ) {
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pfr-api-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pfr-api-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );
		}
		else if( LIB_PROJECT.equals( "pgu-fed-common" ) ) {
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDER );
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDER );
		}
	}
	

	public String getWarMRId( ActionBase action , String P_WAR ) throws Exception {
		// get war from distributive info
		String S_WAR_MRID = "";
		if( S_WAR_MRID.isEmpty() )
			S_WAR_MRID = "00";
		
		return( S_WAR_MRID );
	}

	public boolean checkWarMRId( ActionBase action , String P_WAR ) throws Exception {
		// get war from distributive info
		MetaDistr distr = meta.getDistr();
		MetaDistrBinaryItem item = distr.findBinaryItem( P_WAR );
		if( item == null )
			return( false );

		return( true );
	}
	
}
