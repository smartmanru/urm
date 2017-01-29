package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;

public class ActionCreateProd extends ActionBase { 

	Meta meta;
	String RELEASEVER;
	
	public ActionCreateProd( ActionBase action , String stream , Meta meta , String RELEASEVER ) {
		super( action , stream , "Create production coverage distributive" );
		this.meta = meta;
		this.RELEASEVER = RELEASEVER;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		DistRepository repo = artefactory.getDistRepository( this , meta );
		repo.createProd( this , RELEASEVER );
		return( SCOPESTATE.RunSuccess );
	}
	
}
