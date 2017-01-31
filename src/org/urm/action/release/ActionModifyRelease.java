package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionModifyRelease extends ActionBase {

	public Dist dist;
	
	public ActionModifyRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream , "Change properties of release=" + release.RELEASEDIR );
		this.dist = release;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		dist.openForChange( this );
		dist.release.setProperties( this );
		dist.saveReleaseXml( this );
		dist.closeChange( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
