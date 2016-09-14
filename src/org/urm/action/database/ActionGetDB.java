package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;

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
		LocalFolder dbPreparedFolder = downloadFolder.getSubFolder( this , dist.getDeliveryDatabaseFolder( this , item.dbDelivery , dist.release.RELEASEVER ) );
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
