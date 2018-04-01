package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionDeleteRelease extends ActionBase {

	public Meta meta;
	public Release release;
	boolean force;
	
	public ActionDeleteRelease( ActionBase action , String stream , Meta meta , Release release , boolean force ) {
		super( action , stream , "Drop release=" + release.RELEASEVER );
		this.meta = meta;
		this.release = release;
		this.force = force;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
		
			// drop in database
			Release releaseUpdated = method.deleteRelease( repoUpdated , release );
			DBReleaseRepository.dropRelease( super.method , this , repoUpdated , releaseUpdated );
			
			// drop in file repository
			DistRepositoryItem item = distrepoUpdated.findDefaultItem( releaseUpdated );
			DistRepositoryItem itemUpdated = method.deleteDistItem( distrepoUpdated , item );
			distrepoUpdated.dropDist( method , this , itemUpdated , context.CTX_FORCE );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
