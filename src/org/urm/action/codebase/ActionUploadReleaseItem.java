package org.urm.action.codebase;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionUploadReleaseItem extends ActionBase {

	Dist release;
	
	public ActionUploadReleaseItem( ActionBase action , String stream , Dist release ) {
		super( action , stream , "Upload items from distributive, release=" + release.RELEASEDIR );
		this.release = release;
	}
	
	@Override 
	protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget scopeProject ) throws Exception {
		// load distr data for cross-product exports - thirdparty
		List<ActionScopeTargetItem> items = scopeProject.getItems( this );
		
		// get thirdparty information
		for( ActionScopeTargetItem scopeItem : items )
			uploadItem( scopeProject , release , scopeItem );
		return( SCOPESTATE.RunSuccess );
	}

	private void uploadItem( ActionScopeTarget scopeProject , Dist release , ActionScopeTargetItem scopeItem ) throws Exception {
	}
}
