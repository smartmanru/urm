package org.urm.action.database;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;

public class ActionGetDB extends ActionBase {

	public Dist dist;
	public LocalFolder downloadFolder;
	public boolean copyDist;
	
	public ActionGetDB( ActionBase action , String stream , Dist dist , LocalFolder downloadFolder , boolean copyDist ) {
		super( action , stream , "Get configuration files, " + 
				( ( dist == null )? "default built" : "release=" + dist.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.dist = dist;
		this.downloadFolder = downloadFolder;
		this.copyDist = copyDist;
	}

	protected SCOPESTATE executeScopeTarget( ActionScopeTarget item ) throws Exception {
		info( "get database items of delivery=" + item.NAME + " ..." );

		LocalFolder workFolder = artefactory.getWorkFolder( this , "download" );
		workFolder.recreateThis( this );
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , item.meta , workFolder );
		if( !sourceStorage.downloadReleaseDatabaseFiles( this , dist , item.dbDelivery , workFolder ) )
			return( SCOPESTATE.NotRun );

		LocalFolder dbPreparedFolder = downloadFolder.getSubFolder( this , dist.getDeliveryDatabaseFolder( this , item.dbDelivery , dist.release.RELEASEVER ) );
		dbPreparedFolder.recreateThis( this );

		LocalFolder dbWorkFolder = workFolder.getSubFolder( this , SourceStorage.DATABASE_FOLDER );
		downloadDBDir( item , dbWorkFolder , dbPreparedFolder );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void downloadDBDir( ActionScopeTarget item , LocalFolder workFolder , LocalFolder preparedFolder ) throws Exception {
		DatabasePrepare prepare = new DatabasePrepare();
		
		debug( "prepare scripts dir=" + workFolder.folderPath + " ..." );
		if( !prepare.processDatabaseFiles( this , dist , item.dbDelivery , workFolder , preparedFolder ) ) {
			error( "script set check errors, do not copy to dist" );
			return;
		}
		
		// copy
		if( copyDist )
			dist.copyDatabaseFilesToDistr( this , item.dbDelivery , preparedFolder );
	}
}
