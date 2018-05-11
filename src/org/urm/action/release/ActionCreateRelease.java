package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;

public class ActionCreateRelease extends ActionBase {

	public Meta meta;
	public Dist dist;
	public String RELEASELABEL;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public ActionCreateRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Create release, product=" + meta.name + ", label=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		checkRequired( RELEASELABEL , "RELEASELABEL" );
		DistRepository repo = artefactory.getDistRepository( this , meta );
		dist = repo.createDist( this , RELEASELABEL , releaseDate , lc );
		return( SCOPESTATE.RunSuccess );
	}
	
}
