package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;

public class ActionTouchRelease extends ActionBase {

	Meta meta;
	String RELEASELABEL;
	public Dist dist;
	
	public ActionTouchRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Touch release=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		DistRepository repo = artefactory.getDistRepository( this , meta );
		dist = repo.reloadDist( this , RELEASELABEL );
		return( SCOPESTATE.RunSuccess );
	}
	
}
