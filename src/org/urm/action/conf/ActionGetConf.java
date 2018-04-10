package org.urm.action.conf;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.SourceStorage;

public class ActionGetConf extends ActionBase {

	public Dist dist;
	public LocalFolder downloadFolder;
	public boolean copyDist;
	
	public ActionGetConf( ActionBase action , String stream , Dist dist , LocalFolder downloadFolder , boolean copyDist ) {
		super( action , stream , "Get configuration files, " + 
				( ( dist == null )? "default built" : "release=" + dist.RELEASEDIR ) + 
				", change distr=" + copyDist );
		this.dist = dist;
		this.downloadFolder = downloadFolder;
		this.copyDist = copyDist;
	}

	protected SCOPESTATE executeScopeTarget( ActionScopeTarget scopeItem ) throws Exception {
		// export from source
		String KEY = scopeItem.confItem.NAME;
		SourceStorage sourceStorage = artefactory.getSourceStorage( this , scopeItem.meta , downloadFolder );
		info( "get configuration item " + KEY + " ..." );
		
		LocalFolder confFolder = downloadFolder.getSubFolder( this , sourceStorage.getConfFolderRelPath( this , scopeItem.confItem ) );
		confFolder.ensureExists( this );
		
		ConfSourceFolder sourceFolder = new ConfSourceFolder( scopeItem.meta );
		boolean res = false;
		if( dist != null ) {
			sourceFolder.createReleaseConfigurationFolder( this , scopeItem.releaseDistScopeDeliveryItem );
			res = sourceStorage.downloadReleaseConfigItem( this , dist , sourceFolder , confFolder );
		}
		else {
			sourceFolder.createProductConfigurationFolder( this , scopeItem.confItem );
			res = sourceStorage.downloadProductConfigItem( this , sourceFolder , confFolder );
		}

		// copy to distributive 
		if( copyDist && res )
			dist.copyConfToDistr( this , confFolder.getSubFolder( this , KEY ) , sourceFolder.distrComp );
		
		return( SCOPESTATE.RunSuccess );
	}

}
