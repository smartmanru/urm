package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;

public class ActionCreateMaster extends ActionBase { 

	public Meta meta;
	public String RELEASEVER;
	public Dist dist;
	public boolean copy;
	
	public ActionCreateMaster( ActionBase action , String stream , Meta meta , String RELEASEVER , boolean copy ) {
		super( action , stream , "Create production coverage distributive" );
		this.meta = meta;
		this.RELEASEVER = RELEASEVER;
		this.copy = copy;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DistRepository repo = meta.getDistRepository();
		if( copy )
			dist = repo.createMasterCopy( this , RELEASEVER , null );
		else
			dist = repo.createMasterInitial( this , RELEASEVER , null );
		return( SCOPESTATE.RunSuccess );
	}
	
}
