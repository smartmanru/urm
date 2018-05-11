package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionReopenRelease extends ActionBase {

	Dist release;
	
	public ActionReopenRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		release.reopen( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
