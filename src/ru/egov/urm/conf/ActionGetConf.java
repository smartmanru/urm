package ru.egov.urm.conf;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaSourceFolder;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionGetConf extends ActionBase {

	DistStorage release;
	
	public ActionGetConf( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeItem ) throws Exception {
		LocalFolder downloadFolder = artefactory.getDownloadFolder( this );
		
		// export from source
		String KEY = scopeItem.confItem.KEY;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , downloadFolder );
		log( "get configuration item " + KEY + " ..." );
		
		LocalFolder confFolder = downloadFolder.getSubFolder( this , Common.getPath( scopeItem.confItem.delivery.FOLDER , "config" ) );
		confFolder.ensureExists( this );
		
		MetaSourceFolder sourceFolder = new MetaSourceFolder( meta );  
		if( release != null ) {
			sourceFolder.createReleaseConfigurationFolder( this , scopeItem.releaseTarget );
			sourceStorage.downloadReleaseConfigItem( this , release , sourceFolder , confFolder );
		}
		else {
			sourceFolder.createProductConfigurationFolder( this , scopeItem.confItem );
			sourceStorage.downloadProductConfigItem( this , sourceFolder , confFolder );
		}

		// copy to distributive 
		boolean copyDistr = options.OPT_DIST;
		if( copyDistr )
			release.copyConfToDistr( this , confFolder.getSubFolder( this , KEY ) , sourceFolder.distrComp );
		
		return( true );
	}

}
