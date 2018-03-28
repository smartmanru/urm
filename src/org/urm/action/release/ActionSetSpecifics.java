package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.common.Common;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionSetSpecifics extends ActionBase {

	public Release release;
	
	public ActionSetSpecifics( ActionBase action , String stream , Release release ) {
		super( action , stream , "Set scope specifics, release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		Common.exitUnexpected();
		return( SCOPESTATE.RunSuccess );
	}

}
