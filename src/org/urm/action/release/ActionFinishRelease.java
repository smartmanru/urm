package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionFinishRelease extends ActionBase {

	public Dist dist;
	
	public ActionFinishRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Finalize release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.finish( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
