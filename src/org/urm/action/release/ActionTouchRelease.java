package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;

public class ActionTouchRelease extends ActionBase {

	public Meta meta;
	public String RELEASELABEL;
	public Release release;
	
	public ActionTouchRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Touch release=" + RELEASELABEL );
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			DistRepository distrepoUpdated = method.changeDistRepository( releases );

			// reload distributive
			Dist dist = distrepoUpdated.reloadDist( this , RELEASELABEL );
			release = dist.release;
			
			return( SCOPESTATE.RunSuccess );
		}
	}
	
}
