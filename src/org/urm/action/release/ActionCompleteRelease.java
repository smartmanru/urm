package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionCompleteRelease extends ActionBase {

	public Dist dist;
	
	public ActionCompleteRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Complete release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.complete( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
