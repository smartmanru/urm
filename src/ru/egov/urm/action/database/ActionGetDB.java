package ru.egov.urm.action.database;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionGetDB extends ActionBase {

	Dist dist;
	
	public ActionGetDB( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	protected boolean executeScopeTarget( ActionScopeTarget item ) throws Exception {
		info( "get database items of delivery=" + item.NAME + " ..." );

		LocalFolder workFolder = artefactory.getWorkFolder( this , "download" );
		workFolder.recreateThis( this );
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , workFolder );
		if( !sourceStorage.downloadReleaseDatabaseFiles( this , dist , item.dbDelivery , workFolder ) )
			return( false );

		LocalFolder downloadFolder = artefactory.getDownloadFolder( this );
		LocalFolder dbPreparedFolder = downloadFolder.getSubFolder( this , dist.getDeliveryDatabaseFolder( this , item.dbDelivery ) );
		dbPreparedFolder.recreateThis( this );

		LocalFolder dbWorkFolder = workFolder.getSubFolder( this , SourceStorage.DATABASE_FOLDER );
		downloadDBDir( item , dbWorkFolder , dbPreparedFolder );
		
		return( true );
	}
	
	private void downloadDBDir( ActionScopeTarget item , LocalFolder workFolder , LocalFolder preparedFolder ) throws Exception {
		DatabasePrepare prepare = new DatabasePrepare();
		
		debug( "prepare scripts dir=" + workFolder.folderPath + " ..." );
		if( !prepare.processDatabaseFiles( this , dist , item.dbDelivery , workFolder , preparedFolder ) ) {
			error( "script set check errors, do not copy to dist" );
			return;
		}
		
		// copy
		if( context.CTX_DIST )
			dist.copyDatabaseFilesToDistr( this , item.dbDelivery , preparedFolder );
	}
}
