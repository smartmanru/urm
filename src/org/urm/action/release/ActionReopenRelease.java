package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBRelease;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionReopenRelease extends ActionBase {

	public Release release;
	
	public ActionReopenRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Reopen release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DBRelease.reopen( super.method , this , release );
		return( SCOPESTATE.RunSuccess );
	}
	
}
