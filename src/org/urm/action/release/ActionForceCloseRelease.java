package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionForceCloseRelease extends ActionBase {

	public Dist dist;
	
	public ActionForceCloseRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Close release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.forceClose( this );
		return( SCOPESTATE.RunSuccess );
	}

}
