package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;

public class ActionTouchRelease extends ActionBase {

	public Meta meta;
	public String RELEASELABEL;
	public Dist dist;
	
	public ActionTouchRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Touch release=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DistRepository repo = meta.getDistRepository();
		dist = repo.reloadDist( this , RELEASELABEL );
		return( SCOPESTATE.RunSuccess );
	}
	
}
