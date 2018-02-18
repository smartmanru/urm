package org.urm.action.codebase;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.Meta;

public class ActionGetManual extends ActionBase {

	public Meta meta;
	public LocalFolder downloadFolder;
	public boolean copyDist;
	public Dist dist;
	
	public ActionGetManual( ActionBase action , String stream , Meta meta , boolean copyDist , Dist targetRelease , LocalFolder downloadFolder ) {
		super( action , stream , "Download prebuilt items, " + 
				( ( targetRelease == null )? "default" : "release=" + targetRelease.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.meta = meta;
		this.copyDist = copyDist;
		this.dist = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , meta , downloadFolder );
		
		LocalFolder manualFolder = downloadFolder.getSubFolder( this , "manual" );
		if( !sourceStorage.downloadReleaseManualFolder( this , dist , manualFolder ) ) {
			debug( "no manual items to download" );
			return( SCOPESTATE.NotRun );
		}

		if( copyDist )
			dist.copyManualFilesToDistr( this , manualFolder );
		return( SCOPESTATE.RunSuccess );
	}
	
}
