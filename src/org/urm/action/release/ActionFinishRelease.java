package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionFinishRelease extends ActionBase {

	public Release release;
	
	public ActionFinishRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Finalize release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		super.exit0( _Error.UnableFinalizeRelease0 , "Unable to finalize release" );
		return( SCOPESTATE.RunSuccess );
	}
	
}
