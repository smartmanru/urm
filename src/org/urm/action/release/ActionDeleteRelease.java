package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionDeleteRelease extends ActionBase {

	public Dist dist;
	boolean force;
	
	public ActionDeleteRelease( ActionBase action , String stream , Dist dist , boolean force ) {
		super( action , stream , "Drop release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.force = force;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		if( force )
			dist.forceDrop( this );
		else
			dist.dropRelease( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
