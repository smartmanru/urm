package org.urm.engine.storage;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.SimpleHttp;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaProductBuildSettings;
import org.urm.meta.product.MetaSourceProjectItem;

public class NexusStorage {

	Artefactory artefactory;
	public LocalFolder artefactoryFolder;
	Meta meta;
	Integer NEXUS_RESOURCE;
	String repository;
	
	String authFile = "~/.auth/nexus.http.txt"; 
	
	public NexusStorage( Artefactory artefactory , Meta meta , LocalFolder artefactoryFolder , Integer NEXUS_RESOURCE , String repository ) {
		this.artefactory = artefactory;
		this.artefactoryFolder = artefactoryFolder;
		this.NEXUS_RESOURCE = NEXUS_RESOURCE;
		this.repository = repository;
		this.meta = meta;
	}

	public static boolean verifyRepository( ActionBase action , String repository , AuthResource res ) throws Exception {
		String REPOPATH = res.BASEURL + "/content/repositories/" + repository + "/";
		res.loadAuthData();
		String user = res.ac.getUser( action );
		String password = res.ac.getPassword( action );
		return( SimpleHttp.check( action , REPOPATH , user , password ) );
	}
	
	public NexusDownloadInfo downloadNexus( ActionBase action , String GROUPID , String ARTEFACTID , String VERSION , String PACKAGING , String CLASSIFIER , MetaDistrBinaryItem item ) throws Exception {
		AuthResource res = action.getResource( NEXUS_RESOURCE );
		String REPOPATH = res.BASEURL + "/content/repositories/" + repository;
		String NAME = ARTEFACTID + "-" + VERSION;
		if( !CLASSIFIER.isEmpty() )
			NAME += "-" + CLASSIFIER;
		
		NAME += "." + PACKAGING;

		NexusDownloadInfo info = new NexusDownloadInfo( artefactoryFolder ); 
		String GROUPIDSLASHED = GROUPID.replace( '.' , '/' );
		String nexusAuth = action.readStringFile( authFile );
		
		info.DOWNLOAD_FILENAME = Common.getPath( item.delivery.FOLDER , NAME );
		info.DOWNLOAD_URL = REPOPATH + "/" + GROUPIDSLASHED + "/" + ARTEFACTID + "/" + VERSION + "/" + NAME;
		info.DOWNLOAD_URL_REQUEST = res.BASEURL + "/service/local/artifact/maven/redirect?g=" + GROUPID + "&a=" + ARTEFACTID + "&v=" + VERSION + "&r=" + repository + "&e=" + PACKAGING + "&";

		info.BASENAME = ARTEFACTID;
		info.EXT = "." + PACKAGING;
		if( !CLASSIFIER.isEmpty() )
			info.EXT = "-" + CLASSIFIER + info.EXT;
		
		if( !CLASSIFIER.isEmpty() )
			info.DOWNLOAD_URL_REQUEST += "c=" + CLASSIFIER + "&";

		artefactoryFolder.download( action , info.DOWNLOAD_URL_REQUEST , info.DOWNLOAD_FILENAME , nexusAuth );
		return( info );
	}

	public NexusDownloadInfo downloadNuget( ActionBase action , String ARTEFACTID , String VERSION , MetaDistrBinaryItem item ) throws Exception {
		AuthResource res = action.getResource( NEXUS_RESOURCE );
		String REPOPATH = res.BASEURL + "/content/repositories/" + repository;
		String NAME = ARTEFACTID + "-" + VERSION + ".nupkg";

		NexusDownloadInfo info = new NexusDownloadInfo( artefactoryFolder ); 
		String nexusAuth = action.readStringFile( authFile );
		
		info.DOWNLOAD_FILENAME = Common.getPath( item.delivery.FOLDER , NAME );
		info.DOWNLOAD_URL = REPOPATH + "/" + ARTEFACTID + "/" + VERSION + "/" + NAME;
		info.DOWNLOAD_URL_REQUEST = info.DOWNLOAD_URL;
		info.BASENAME = ARTEFACTID;

		artefactoryFolder.download( action , info.DOWNLOAD_URL_REQUEST , info.DOWNLOAD_FILENAME , nexusAuth );
		return( info );
	}

	public String repackageNugetPlatform( ActionBase action , NexusDownloadInfo src , MetaSourceProjectItem item ) throws Exception {
		LocalFolder tmp = artefactoryFolder.getSubFolder( action , "tmp" );
		tmp.ensureExists( action );
		
		ProjectBuilder builder = action.getBuilder( item.project.getBuilder( action ) );
		action.shell.unzipPart( action , artefactoryFolder.folderPath , src.DOWNLOAD_FILENAME , tmp.folderPath , 
				Common.getPath( "lib" , builder.TARGET_PLATFORM , "*" ) );
		action.shell.unzipPart( action , artefactoryFolder.folderPath , src.DOWNLOAD_FILENAME , tmp.folderPath , 
				Common.getPath( "content" , "*" ) );
		
		// copy to final zip dir
		LocalFolder zip = tmp.getSubFolder( action , "final" );
		zip.ensureExists( action );
		String zipLibPath = zip.getFilePath( action , item.ITEMBASENAME + ".zip" );
		
		tmp.createZipFromFolderContent( action , zipLibPath , Common.getPath( "lib" , builder.TARGET_PLATFORM ) , "*" , "" );
		zip.copyDirContent( action , tmp.getSubFolder( action , "content" ) );
		
		// create final zip file
		String FILENAME = item.ITEMBASENAME + item.ITEMEXTENSION;
		String finalFile = artefactoryFolder.getFilePath( action , FILENAME );
		zip.createZipFromContent( action , finalFile , "*" , "" );
		action.shell.createMD5( action , finalFile );
		
		return( FILENAME );
	}
	
	public void repackageStatic( ActionBase action , String PROJECT , String VERSION , String WARFILE , String STATICFILE , String TAGNAME , MetaDistrBinaryItem distItem ) throws Exception {
		LocalFolder folder = artefactoryFolder.getSubFolder( action , distItem.delivery.FOLDER );
		
		if( !folder.checkFileExists( action , STATICFILE ) )
			action.exit1( _Error.StaticFileNotFound1 , "repackageStatic: " + STATICFILE + " not found" , STATICFILE );

		String rePackageDir = "repackage";
		folder.recreateFolder( action , rePackageDir );
		folder.extractTarGz( action , STATICFILE , rePackageDir );
		folder.removeFiles( action , STATICFILE );
		String STATIC_CONTEXT = folder.getFolderContent( action , rePackageDir );  

		if( STATIC_CONTEXT.isEmpty() )
			action.exit1( _Error.NoContextInStaticFile1 , "repackageStatic: context not found in static file " + STATICFILE , STATICFILE );

		if( !folder.checkFolderExists( action , rePackageDir + "/" + STATIC_CONTEXT + "/htdocs" ) )
			action.exit2( _Error.ContextDataNotFound2 , "repackageStatic: invalid static file, context data not found: " + STATICFILE + "/" + STATIC_CONTEXT + "/htdocs" , STATICFILE , STATIC_CONTEXT );

		// add build info to static
		addBuildInfo( action , rePackageDir + "/" + STATIC_CONTEXT + "/htdocs/buildinfo.txt" , PROJECT , VERSION , WARFILE , STATICFILE , TAGNAME );

		LocalFolder staticDir = folder.getSubFolder( action , rePackageDir + "/" + STATIC_CONTEXT ); 
		staticDir.createTarGzFromContent( action , folder.getFilePath( action , STATICFILE ) , "htdocs" , "" );
		folder.removeFolder( action , rePackageDir );
		folder.md5file( action , STATICFILE );

		action.debug( "download: " + STATICFILE + " statics repackaged, STATIC_CONTEXT=" + STATIC_CONTEXT );
	}

	private void addBuildInfo( ActionBase action , String BUILDINFONAME , String PROJECT , String VERSION , String WARFILE , String STATICFILE , String TAGNAME ) throws Exception {
		String fname = BUILDINFONAME;
		String fileInfo = artefactoryFolder.getFileInfo( action , WARFILE ); 
		
		artefactoryFolder.createFileFromString( action , fname , "BUILDINFO:" );
		artefactoryFolder.appendFileWithString( action , fname , "-------------" );
		artefactoryFolder.appendFileWithString( action , fname , "PROJECT=" + PROJECT );
		artefactoryFolder.appendFileWithString( action , fname , "VERSION=" + VERSION );
		artefactoryFolder.appendFileWithString( action , fname , "WARFILENAME=" + WARFILE );
		artefactoryFolder.appendFileWithString( action , fname , "WARFILELS=" + fileInfo );
		artefactoryFolder.appendFileWithString( action , fname , "TAG=" + TAGNAME );
		artefactoryFolder.appendFileWithString( action , fname , "DATE=" + Common.getLogTimeStamp() );
		artefactoryFolder.appendFileWithString( action , fname , "BUILDMACHINE=" + action.context.account.getFullName() );
		artefactoryFolder.appendFileWithString( action , fname , "-------------" );
		
		MetaProductBuildSettings build = action.getBuildSettings( meta );
		artefactoryFolder.appendFileWithString( action , fname , "BUILDINFO v." + build.CONFIG_APPVERSION );
	}
	
}
