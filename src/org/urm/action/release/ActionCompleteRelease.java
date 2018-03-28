package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBRelease;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionCompleteRelease extends ActionBase {

	public Release release;
	
	public ActionCompleteRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Complete release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DBRelease.complete( super.method , this , release );
		return( SCOPESTATE.RunSuccess );
	}
	
}
