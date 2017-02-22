package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionCompleteRelease extends ActionBase {

	public Dist dist;
	
	public ActionCompleteRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Complete release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		dist.complete( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
