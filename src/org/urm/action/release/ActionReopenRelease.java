package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionReopenRelease extends ActionBase {

	public Release release;
	
	public ActionReopenRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Reopen release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		if( !release.isFinalized() )
			Common.exitUnexpected();
		
		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			DistRepositoryItem item = distrepoUpdated.findDefaultItem( releaseUpdated );
			DistRepositoryItem itemUpdated = method.changeDistItem( distrepoUpdated , item );

			// change database
			DBRelease.reopen( method , this , releaseUpdated );
			
			// change dist
			itemUpdated.dist.reopen( this );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
