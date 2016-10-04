package org.urm.action.build;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;
import org.urm.meta.product.Meta;

public class ActionGetManual extends ActionBase {

	Meta meta;
	LocalFolder downloadFolder;
	boolean copyDist;
	Dist targetRelease;
	
	public ActionGetManual( ActionBase action , Meta meta , String stream , boolean copyDist , Dist targetRelease , LocalFolder downloadFolder ) {
		super( action , stream );
		this.copyDist = copyDist;
		this.targetRelease = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override protected boolean executeSimple() throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , meta , downloadFolder );
		
		LocalFolder manualFolder = downloadFolder.getSubFolder( this , "manual" );
		if( !sourceStorage.downloadReleaseManualFolder( this , targetRelease , manualFolder ) ) {
			debug( "no manual items to download" );
			return( true );
		}

		if( copyDist )
			targetRelease.copyManualFilesToDistr( this , manualFolder );
		return( true );
	}
	
}
