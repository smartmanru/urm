package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionArchiveRelease extends ActionBase {

	public Release release;
	
	public ActionArchiveRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Archive release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		if( !release.isCompleted() ) {
			super.fail1( _Error.ArchiveNotCompleted1 , "Cannot archive not completed release=" + release.RELEASEVER , release.RELEASEVER );
			return( SCOPESTATE.RunFail );
		}
		
		DBReleaseRepository.archiveRelease( super.method , this , release.repo , release );
		return( SCOPESTATE.RunSuccess );
	}
	
}
