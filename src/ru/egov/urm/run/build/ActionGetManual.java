package ru.egov.urm.run.build;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionGetManual extends ActionBase {

	LocalFolder downloadFolder;
	boolean copyDist;
	DistStorage targetRelease;
	
	public ActionGetManual( ActionBase action , String stream , boolean copyDist , DistStorage targetRelease , LocalFolder downloadFolder ) {
		super( action , stream );
		this.copyDist = copyDist;
		this.targetRelease = targetRelease;
		this.downloadFolder = downloadFolder;
	}

	@Override protected boolean executeSimple() throws Exception {
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , downloadFolder );
		
		if( !sourceStorage.downloadReleaseManualFolder( this , targetRelease , downloadFolder ) ) {
			debug( "no manual items to download" );
			return( true );
		}

		if( copyDist )
			targetRelease.copyManualFilesToDistr( this , downloadFolder );
		return( true );
	}
	
}
