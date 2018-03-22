package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionArchiveRelease extends ActionBase {

	public Dist dist;
	
	public ActionArchiveRelease( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Archive release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DistRepository repo = dist.repo;
		if( !dist.isCompleted() ) {
			super.fail1( _Error.ArchiveNotCompleted1 , "Cannot archive not completed release=" + dist.RELEASEDIR , dist.RELEASEDIR );
			return( SCOPESTATE.RunFail );
		}
		
		repo.archiveDist( this , dist );
		return( SCOPESTATE.RunSuccess );
	}
	
}
