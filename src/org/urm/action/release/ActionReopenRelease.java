package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
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
		
		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			Dist dist = distrepoUpdated.findDefaultDist( releaseUpdated );

			// change database
			DBRelease.reopen( method , this , releaseUpdated );
			
			// change dist
			dist.reopen( this );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
