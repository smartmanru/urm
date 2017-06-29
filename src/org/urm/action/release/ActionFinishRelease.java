package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

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
