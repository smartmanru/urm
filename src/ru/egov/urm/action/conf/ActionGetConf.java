package ru.egov.urm.action.conf;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.storage.LocalFolder;
import ru.egov.urm.storage.SourceStorage;

public class ActionGetConf extends ActionBase {

	Dist release;
	
	public ActionGetConf( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	protected boolean executeScopeTarget( ActionScopeTarget scopeItem ) throws Exception {
		LocalFolder downloadFolder = artefactory.getDownloadFolder( this );
		
		// export from source
		String KEY = scopeItem.confItem.KEY;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , downloadFolder );
		log( "get configuration item " + KEY + " ..." );
		
		LocalFolder confFolder = downloadFolder.getSubFolder( this , sourceStorage.getConfFolderRelPath( this , scopeItem.confItem ) );
		confFolder.ensureExists( this );
		
		ConfSourceFolder sourceFolder = new ConfSourceFolder( meta );  
		if( release != null ) {
			sourceFolder.createReleaseConfigurationFolder( this , scopeItem.releaseTarget );
			sourceStorage.downloadReleaseConfigItem( this , release , sourceFolder , confFolder );
		}
		else {
			sourceFolder.createProductConfigurationFolder( this , scopeItem.confItem );
			sourceStorage.downloadProductConfigItem( this , sourceFolder , confFolder );
		}

		// copy to distributive 
		boolean copyDistr = context.CTX_DIST;
		if( copyDistr )
			release.copyConfToDistr( this , confFolder.getSubFolder( this , KEY ) , sourceFolder.distrComp );
		
		return( true );
	}

}
