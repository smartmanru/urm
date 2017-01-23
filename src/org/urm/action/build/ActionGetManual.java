package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.Meta;

public class ActionGetManual extends ActionBase {

	Meta meta;
	LocalFolder downloadFolder;
	boolean copyDist;
	Dist targetRelease;
	
	public ActionGetManual( ActionBase action , String stream , Meta meta , boolean copyDist , Dist targetRelease , LocalFolder downloadFolder ) {
		super( action , stream , "Download prebuilt items, " + 
				( ( targetRelease == null )? "default" : "release=" + targetRelease.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.meta = meta;
		this.copyDist = copyDist;
		this.targetRelease = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , meta , downloadFolder );
		
		LocalFolder manualFolder = downloadFolder.getSubFolder( this , "manual" );
		if( !sourceStorage.downloadReleaseManualFolder( this , targetRelease , manualFolder ) ) {
			debug( "no manual items to download" );
			return( SCOPESTATE.NotRun );
		}

		if( copyDist )
			targetRelease.copyManualFilesToDistr( this , manualFolder );
		return( SCOPESTATE.RunSuccess );
	}
	
}
