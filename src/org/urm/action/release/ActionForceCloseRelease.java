package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionForceCloseRelease extends ActionBase {

	Dist release;
	
	public ActionForceCloseRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream , "Close release=" + release.RELEASEDIR );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		release.forceClose( this );
		return( SCOPESTATE.RunSuccess );
	}

}
