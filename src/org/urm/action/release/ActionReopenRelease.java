package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

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
