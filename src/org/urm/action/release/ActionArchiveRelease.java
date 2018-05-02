package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionArchiveRelease extends ActionBase {

	public Release release;
	
	public ActionArchiveRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Archive release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			DistRepository distrepoUpdated = method.changeDistRepository( meta.getProduct() );
			Dist distUpdated = distrepoUpdated.findDefaultDist( releaseUpdated );
			
			// change database
			DBReleaseRepository.archiveRelease( super.method , this , repoUpdated , releaseUpdated );
			
			// move distributive to archive
			distrepoUpdated.archiveDist( this , distUpdated );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
