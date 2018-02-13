package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;

public class ActionGetDocs extends ActionBase {

	public Dist dist;
	public LocalFolder downloadFolder;
	public boolean copyDist;
	
	public ActionGetDocs( ActionBase action , String stream , Dist dist , LocalFolder downloadFolder , boolean copyDist ) {
		super( action , stream , "Get document files, " + 
				( ( dist == null )? "default built" : "release=" + dist.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.dist = dist;
		this.downloadFolder = downloadFolder;
		this.copyDist = copyDist;
	}

	protected SCOPESTATE executeScopeTarget( ActionScopeTarget item ) throws Exception {
		info( "get doc items of delivery=" + item.NAME + " ..." );

		LocalFolder workFolder = artefactory.getWorkFolder( this , "download" );
		workFolder.recreateThis( this );
		
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , item.meta , workFolder );
		if( !sourceStorage.downloadReleaseDocFiles( this , dist , item.delivery , workFolder ) )
			return( SCOPESTATE.NotRun );

		LocalFolder docWorkFolder = workFolder.getSubFolder( this , SourceStorage.DOC_FOLDER );
		downloadDocDir( item , docWorkFolder );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private void downloadDocDir( ActionScopeTarget item , LocalFolder workFolder ) throws Exception {
		// copy
		if( copyDist )
			dist.copyDatabaseFilesToDistr( this , item.delivery , workFolder );
	}
}
