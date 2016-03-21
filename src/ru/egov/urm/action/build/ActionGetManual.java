package ru.egov.urm.action.build;

import ru.egov.urm.action.ActionBase;
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
