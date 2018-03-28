package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionDeleteRelease extends ActionBase {

	public Release release;
	boolean force;
	
	public ActionDeleteRelease( ActionBase action , String stream , Release release , boolean force ) {
		super( action , stream , "Drop release=" + release.RELEASEVER );
		this.release = release;
		this.force = force;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		ReleaseRepository repo = release.repo;
		
		DBReleaseRepository.dropRelease( super.method , this , repo , release );
		return( SCOPESTATE.RunSuccess );
	}
	
}
