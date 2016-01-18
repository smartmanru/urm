package ru.egov.urm.run.database;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionGetDB extends ActionBase {

	DistStorage release;
	
	public ActionGetDB( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	protected boolean executeScopeTarget( ActionScopeTarget item ) throws Exception {
		log( "get database items of delivery=" + item.NAME + " ..." );

		LocalFolder workFolder = artefactory.getWorkFolder( this , "download" );
		workFolder.recreateThis( this );
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , workFolder );
		if( !sourceStorage.downloadReleaseDatabaseFiles( this , release , item.dbDelivery , workFolder ) )
			return( false );

		LocalFolder downloadFolder = artefactory.getDownloadFolder( this );
		LocalFolder deliveryFolder = downloadFolder.getSubFolder( this , item.dbDelivery.FOLDER );
		LocalFolder dbPreparedFolder = deliveryFolder.getSubFolder( this , "db" );
		deliveryFolder.ensureExists( this );
		dbPreparedFolder.recreateThis( this );

		LocalFolder dbWorkFolder = workFolder.getSubFolder( this , "db" );
		downloadDBDir( item , dbWorkFolder , dbPreparedFolder );
		
		return( true );
	}
	
	private void downloadDBDir( ActionScopeTarget item , LocalFolder workFolder , LocalFolder preparedFolder ) throws Exception {
		DatabasePrepare prepare = new DatabasePrepare();
		
		debug( "prepare scripts dir=" + workFolder.folderPath + " ..." );
		if( !prepare.processDatabaseFiles( this , release , item.dbDelivery , workFolder , preparedFolder ) ) {
			log( "script set check errors, do not copy to dist" );
			return;
		}
		
		// copy
		if( options.OPT_DIST )
			release.copyDatabaseFilesToDistr( this , item.dbDelivery , preparedFolder );
	}
}
