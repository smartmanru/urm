package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class ActionTouchRelease extends ActionBase {

	public String RELEASELABEL;
	public Release release;
	
	public ActionTouchRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Touch release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Meta meta = release.getMeta();
		DistRepository repo = meta.getDistRepository();
		repo.reloadDist( this , RELEASELABEL );
		return( SCOPESTATE.RunSuccess );
	}
	
}
