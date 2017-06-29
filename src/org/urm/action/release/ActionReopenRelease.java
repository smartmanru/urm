package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionReopenRelease extends ActionBase {

	public Dist dist;
	
	public ActionReopenRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Reopen release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.reopen( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
