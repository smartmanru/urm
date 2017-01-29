package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.Meta;

public class ActionCreateRelease extends ActionBase {

	Meta meta;
	public Dist release;
	String RELEASELABEL;
	
	public ActionCreateRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Create release, product=" + meta.name + ", label=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		release = artefactory.createDist( this , meta , RELEASELABEL );
		return( SCOPESTATE.RunSuccess );
	}
	
}
