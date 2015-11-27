package ru.egov.urm.storage;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.run.ActionBase;

public class NexusStorage {

	Artefactory artefactory;
	public LocalFolder artefactoryFolder;
	Metadata meta;
	String repository;
	
	public NexusStorage( Artefactory artefactory , LocalFolder artefactoryFolder , String repository ) {
		this.artefactory = artefactory;
		this.artefactoryFolder = artefactoryFolder;
		this.repository = repository;
		this.meta = artefactoryFolder.meta;
	}

	public NexusDownloadInfo downloadNexus( ActionBase action , String GROUPID , String ARTEFACTID , String VERSION , String PACKAGING , String CLASSIFIER , MetaDistrBinaryItem item ) throws Exception {
		String REPOPATH = meta.product.CONFIG_NEXUS_BASE + "/content/repositories/" + repository;
		String NAME = ARTEFACTID + "-" + VERSION;
		if( !CLASSIFIER.isEmpty() )
			NAME += "-" + CLASSIFIER;
		
		NAME += "." + PACKAGING;

		NexusDownloadInfo info = new NexusDownloadInfo( artefactoryFolder ); 
		String GROUPIDSLASHED = GROUPID.replace( '.' , '/' );
		
		info.DOWNLOAD_FILENAME = Common.getPath( item.delivery.FOLDERPATH , NAME );
		info.DOWNLOAD_URL = REPOPATH + "/" + GROUPIDSLASHED + "/" + ARTEFACTID + "/" + VERSION + "/" + NAME;
		info.DOWNLOAD_URL_REQUEST = meta.product.CONFIG_NEXUS_BASE + "/service/local/artifact/maven/redirect?g=" + GROUPID + "&a=" + ARTEFACTID + "&v=" + VERSION + "&r=" + repository + "&e=" + PACKAGING + "&";

		info.BASENAME = ARTEFACTID;
		info.EXT = "." + PACKAGING;
		if( !CLASSIFIER.isEmpty() )
			info.EXT = "-" + CLASSIFIER + info.EXT;
		
		if( !CLASSIFIER.isEmpty() )
			info.DOWNLOAD_URL_REQUEST += "c=" + CLASSIFIER + "&";

		artefactoryFolder.download( action , info.DOWNLOAD_URL_REQUEST , info.DOWNLOAD_FILENAME );
		return( info );
	}

	public void repackageStatic( ActionBase action , String PROJECT , String VERSION , String WARFILE , String STATICFILE , String TAGNAME , MetaDistrBinaryItem distItem ) throws Exception {
		LocalFolder folder = artefactoryFolder.getSubFolder( action , distItem.delivery.FOLDERPATH );
		
		if( !folder.checkFileExists( action , STATICFILE ) )
			action.exit( "repackageStatic: " + STATICFILE + " not found" );

		String rePackageDir = "repackage";
		folder.recreateFolder( action , rePackageDir );
		folder.extractTarGz( action , STATICFILE , rePackageDir );
		folder.removeFiles( action , STATICFILE );
		String STATIC_CONTEXT = folder.getFolderContent( action , rePackageDir );  

		if( STATIC_CONTEXT.isEmpty() )
			action.exit( "repackageStatic: context not found in static file " + STATICFILE );

		if( !folder.checkFolderExists( action , rePackageDir + "/" + STATIC_CONTEXT + "/htdocs" ) )
			action.exit( "repackageStatic: invalid static file, context data not found: " + STATICFILE + "/" + STATIC_CONTEXT + "/htdocs" );

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
		artefactoryFolder.appendFileWithString( action , fname , "BUILDMACHINE=" + action.context.hostLogin );
		artefactoryFolder.appendFileWithString( action , fname , "-------------" );
		artefactoryFolder.appendFileWithString( action , fname , "BUILDINFO v." + meta.product.CONFIG_APPVERSION );
	}
	
}
