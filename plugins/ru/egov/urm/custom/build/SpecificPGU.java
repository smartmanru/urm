package ru.egov.urm.custom.build;

import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.storage.Artefactory;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.NexusDownloadInfo;
import ru.egov.urm.storage.NexusStorage;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.MetaSourceProjectItem;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;

public class SpecificPGU {

	Metadata meta;
	LocalFolder downloadFolder;
	ActionBase action;
	Artefactory artefactory;
	
	ActionScope scope;

	boolean USE_PROD_DISTR;
	String VERSION;
	DistStorage srcRelease;
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
		
		this.meta = action.meta;
		this.artefactory = action.artefactory;
	}
	
	public String getPguServiceCallExt() throws Exception {
		String custom = meta.product.getPropertyBase( action , "CUSTOM_SERVICECALL_EXT" );
		if( custom != null && !custom.isEmpty() )
			return( custom );
		
		return( "ear" );
	}
	
	public String getPguStorageServiceExt() throws Exception {
		String custom = meta.product.getPropertyBase( action , "CUSTOM_STORAGESERVICE_EXT" );
		if( custom != null && !custom.isEmpty() )
			return( custom );
		
		return( "ear" );
	}

	public void downloadWarCopyDistr( boolean copyDistr , DistStorage release , String VERSION_TAGNAME , ActionScopeTarget scopeProject ) throws Exception {
		MetaDistrBinaryItem distItem = scopeProject.sourceProject.distItem;
		
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , downloadFolder );
		NexusDownloadInfo WAR = nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , distItem.DISTBASENAME , VERSION , "war" , "" , distItem );
		NexusDownloadInfo STATIC = nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , distItem.DISTBASENAME , VERSION , "tar.gz" , "webstatic" , distItem );
		nexusStorage.repackageStatic( action , scopeProject.sourceProject.PROJECT , VERSION , WAR.DOWNLOAD_FILENAME , STATIC.DOWNLOAD_FILENAME , VERSION_TAGNAME , distItem );

		if( copyDistr ) {
			DistStorage distStorage = release;
			distStorage.copyVFileToDistr( action , distItem , downloadFolder , WAR.DOWNLOAD_FILENAME , WAR.BASENAME , WAR.EXT );
			distStorage.copyVFileToDistr( action , distItem , downloadFolder , STATIC.DOWNLOAD_FILENAME , STATIC.BASENAME , STATIC.EXT );
		}
	}
	
	public void getAllWarApp( boolean copyDistr , DistStorage release , boolean USE_PROD_DISTR , String VERSION , DistStorage srcRelease , ActionScope scope ) throws Exception {
		this.USE_PROD_DISTR = USE_PROD_DISTR;
		this.srcRelease = srcRelease;
		this.VERSION = VERSION;
		this.scope = scope;
		
		SERVICECALL_EXT = getPguServiceCallExt();
		STORAGESERVICE_EXT = getPguStorageServiceExt();
	
		SERVICECALL_DIR = ( SERVICECALL_EXT.equals( "ear" ) )? "APP-INF" : "WEB-INF";  
		STORAGESERVICE_DIR = ( STORAGESERVICE_EXT.equals( "ear" ) )? "APP-INF" : "WEB-INF";  
		
		action.log( "getAllWarApp: create servicecall." + SERVICECALL_EXT + " and storageservice." + STORAGESERVICE_EXT + " ..." );
		
		downloadFolder.removeFolder( action , "pgu-services-lib" );
		downloadFolder.removeFolder( action , "servicecall-prod-libs" );

		servicecallItem = meta.distr.getBinaryItem( action , C_ITEMSERVICECALL );
		storageserviceItem = meta.distr.getBinaryItem( action , C_ITEMSTORAGESERVICE );
		
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
			action.log( "downloading core servicecall and storageservice from Nexus - to " + artefactory.workFolder.folderPath + " ..." );
			
			NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , downloadFolder );
			nexusStorage.downloadNexus( action , C_SERVICECALLGROUPID , "servicecall" , VERSION , SERVICECALL_EXT , "" , servicecallItem );
			nexusStorage.downloadNexus( action , C_STORAGESERVICEGROUPID , "storageservice" , VERSION , STORAGESERVICE_EXT , "" , storageserviceItem );
		}
		else {
			DistStorage distStorage = srcRelease;
			action.log( "copy servicecall and storageservice from " + distStorage.getDistPath( action ) + " - to " + downloadFolder.folderPath + " ..." );
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
		DistStorage distStorage = artefactory.getDistProdStorage( action );

		action.debug( "copy libraries from " + distStorage.getDistPath( action ) + "/servicecall." + SERVICECALL_EXT + " to servicecall-prod-libs ..." );
		distStorage.unzipDistFileToFolder( action , downloadFolder , "servicecall-*." + SERVICECALL_EXT , servicecallItem.delivery.FOLDERPATH , Common.getQuoted( SERVICECALL_DIR + "/lib/*" ) , "servicecall-prod-libs" );
	}
	
	private void getAllWarAppDownloadLibs( DistStorage release ) throws Exception {
		// create directory for libs and "cd" to it
		LocalFolder libFolder = downloadFolder.getSubFolder( action , "pgu-services-lib" );
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , libFolder );

		// download latest API libs - pfr, fed-common-util
		if( meta.product.CONFIG_PRODUCT.equals( "fedpgu" ) ) {
			action.debug( "download API libs for pfr and fed-common-util from Nexus - to pgu-services-lib ..." );
			nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , "pfr-api" , VERSION , "jar" , "" , servicecallItem );
			nexusStorage.downloadNexus( action , C_PGUFEDGROUPID , "pgu-fed-common-util" , VERSION , "jar" , "" , servicecallItem );
		}

		// download latest built libs for all microportals
		action.debug( "download last built libs for all microportals from Nexus - to pgu-services-lib ..." );
		Map<String,ActionScopeTarget> projects = scope.getCategorySetTargets( action , VarCATEGORY.BUILD );
		
		for( ActionScopeTarget scopeProject : projects.values() ) {
			String LIB = scopeProject.sourceProject.DISTLIBITEM;
			nexusStorage.downloadNexus( action , C_PGUWARNEXUSGROUPID , LIB , VERSION , "jar" , "" , servicecallItem );
		}
	}

	private void getAllWarAppUpdateLibs( DistStorage release ) throws Exception {
		// copy all libs from -
		//   current release - if microportal exists in current release distributive
		//   previous release (prod) - otherwise

		if( meta.product.CONFIG_PRODUCT.equals( "fedpgu" ) ) {
			// pgu-fed-common-util - always use last built
			if( srcRelease == null )
				getAllWarAppCopySpecificBuilt( "pgu-fed-common" );
		}

		action.debug( "copy libs to servicecall and storageservice from pgu-services-lib and servicecall-prod-libs ..." );
		List<MetaSourceProject> list = meta.sources.getAllProjectList( action , VarCATEGORY.BUILD );
		DistStorage releaseStorage = release;
		
		for( MetaSourceProject sourceProject : list ) {
			getAllWarAppGetProjectLib( sourceProject , releaseStorage );
		}
	}
	
	private void getAllWarAppGetProjectLib( MetaSourceProject sourceProject , DistStorage release ) throws Exception {
		String lib = sourceProject.DISTLIBITEM + "-" + VERSION + ".jar";

		boolean RELEASED_TO_PROD = false;
		if( USE_PROD_DISTR ) {
			if( artefactory.workFolder.checkFileExists( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ) )
				RELEASED_TO_PROD = true;
		}

		// check if $lib exists in $P_DISTR_DSTDIR/$P_PROJECT ...
		boolean copyFromProd = false;
		if( USE_PROD_DISTR == true && RELEASED_TO_PROD == true )
			if( !release.checkFileExists( action , sourceProject.DISTITEM + "*war" ) )
				copyFromProd = true;
		
		if( copyFromProd ) {
			// if microportal is NOT in current distributive & present in current PROD - then copy lib from PROD
			if( downloadFolder.checkFileExists( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ) ) {
				action.debug( "copy library " + lib + " from servicecall-prod-libs to servicecall and storageservice ..." );
				downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/" + lib ,
						"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
				downloadFolder.copyFile( action , "storageservice-prod-libs/" + STORAGESERVICE_DIR + "/lib/" + lib ,
						"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );

				getAllWarAppCopySpecificProd( sourceProject.PROJECT );
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
							"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
					downloadFolder.copyFile( action , "pgu-services-lib/" + lib , 
							"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );

					getAllWarAppCopySpecificBuilt( sourceProject.PROJECT );
				}
				else
					action.debug( lib + ": not found in pgu-services-lib. Skipped." );
			}
		}
	}
	
	private void getAllWarAppCreateBinaries( boolean copyDistr , DistStorage release ) throws Exception {
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
			DistStorage releaseStorage = release;
			releaseStorage.copyVFileToDistr( action , servicecallItem , downloadFolder , "servicecall-" + VERSION + "." + SERVICECALL_EXT , "servicecall" , SERVICECALL_EXT );
			releaseStorage.copyVFileToDistr( action , storageserviceItem , downloadFolder , "storageservice-" + VERSION + "." + STORAGESERVICE_EXT , "storageservice" , STORAGESERVICE_EXT );
		}
	}
	
	private void getAllWarAppDownloadDeps( boolean copyDistr , DistStorage release ) throws Exception {
		MetaSourceProject sourceProject = meta.sources.getProject( action , "pgu-portal" );
		MetaSourceProjectItem sourceItem = sourceProject.getItem( action , "pgu-dependencies" );
		MetaDistrBinaryItem distItem = meta.distr.getBinaryItem( action , sourceItem.ITEMNAME );
		
		String GROUPID = sourceItem.ITEMPATH.replace( '/' , '.' );
		String EXT = sourceItem.ITEMEXTENSION.substring( 1 );
		
		NexusStorage nexusStorage = artefactory.getDefaultNexusStorage( action , downloadFolder );
		nexusStorage.downloadNexus( action , GROUPID , sourceItem.ITEMBASENAME , VERSION , EXT , "" , distItem );
		if( copyDistr ) {
			DistStorage releaseStorage = release;
			releaseStorage.copyVFileToDistr( action , distItem , downloadFolder , sourceItem.ITEMBASENAME + "-" + VERSION + "." + EXT , sourceItem.ITEMBASENAME , EXT );
		}
	}

	private void getAllWarAppCopySpecificBuilt( String LIB_PROJECT ) throws Exception {
		if( LIB_PROJECT.equals( "pgu-pfr" ) ) {
			downloadFolder.copyFile( action , "pgu-services-lib/pfr-api-" + VERSION + ".jar" , 
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
			downloadFolder.copyFile( action , "pgu-services-lib/pfr-api-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );
		}
		else if( LIB_PROJECT.equals( "pgu-fed-common" ) ) {
			downloadFolder.copyFile( action , "pgu-services-lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
			downloadFolder.copyFile( action , "pgu-services-lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );
		}
	}

	private void getAllWarAppCopySpecificProd( String LIB_PROJECT ) throws Exception {
		if( LIB_PROJECT.equals( "pgu-pfr" ) ) {
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pfr-api-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pfr-api-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );
		}
		else if( LIB_PROJECT.equals( "pgu-fed-common" ) ) {
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"servicecall-" + VERSION + "." + SERVICECALL_EXT + "/" + SERVICECALL_DIR + "/lib" , "" , servicecallItem.delivery.FOLDERPATH );
			downloadFolder.copyFile( action , "servicecall-prod-libs/" + SERVICECALL_DIR + "/lib/pgu-fed-common-util-" + VERSION + ".jar" ,
					"storageservice-" + VERSION + "." + STORAGESERVICE_EXT + "/" + STORAGESERVICE_DIR + "/lib" , "" , storageserviceItem.delivery.FOLDERPATH );
		}
	}
	
}
