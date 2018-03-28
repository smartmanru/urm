package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.release.Release;

public class ActionModifyRelease extends ActionBase {

	public Release release;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public ActionModifyRelease( ActionBase action , String stream , Release release , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Change properties of release=" + release.RELEASEVER );
		this.release = release;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		return( SCOPESTATE.RunFail );
	}
	
}
