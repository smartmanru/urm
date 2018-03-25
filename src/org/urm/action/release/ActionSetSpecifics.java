package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionSetSpecifics extends ActionBase {

	public Dist dist;
	
	public ActionSetSpecifics( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Set scope specifics, release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		Common.exitUnexpected();
		return( SCOPESTATE.RunSuccess );
	}

}
