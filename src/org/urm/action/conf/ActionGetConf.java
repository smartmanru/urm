package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;

public class ActionGetConf extends ActionBase {

	Dist release;
	LocalFolder downloadFolder;
	boolean copyDist;
	
	public ActionGetConf( ActionBase action , String stream , Dist release , LocalFolder downloadFolder , boolean copyDist ) {
		super( action , stream , "Get configuration files, " + 
				( ( release == null )? "default built" : "release=" + release.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.release = release;
		this.downloadFolder = downloadFolder;
		this.copyDist = copyDist;
	}

	protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeItem ) throws Exception {
		// export from source
		String KEY = scopeItem.confItem.KEY;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , scopeItem.meta , downloadFolder );
		info( "get configuration item " + KEY + " ..." );
		
		LocalFolder confFolder = downloadFolder.getSubFolder( this , sourceStorage.getConfFolderRelPath( this , scopeItem.confItem ) );
		confFolder.ensureExists( this );
		
		ConfSourceFolder sourceFolder = new ConfSourceFolder( scopeItem.meta );
		boolean res = false;
		if( release != null ) {
			sourceFolder.createReleaseConfigurationFolder( this , scopeItem.releaseTarget );
			res = sourceStorage.downloadReleaseConfigItem( this , release , sourceFolder , confFolder );
		}
		else {
			sourceFolder.createProductConfigurationFolder( this , scopeItem.confItem );
			res = sourceStorage.downloadProductConfigItem( this , sourceFolder , confFolder );
		}

		// copy to distributive 
		if( copyDist && res )
			release.copyConfToDistr( this , confFolder.getSubFolder( this , KEY ) , sourceFolder.distrComp );
		
		return( SCOPESTATE.RunSuccess );
	}

}
