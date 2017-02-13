package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;

public class ActionCreateRelease extends ActionBase {

	public Meta meta;
	public Dist dist;
	public String RELEASELABEL;
	public Date releaseDate;
	
	public ActionCreateRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL , Date releaseDate ) {
		super( action , stream , "Create release, product=" + meta.name + ", label=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
		this.releaseDate = releaseDate;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		checkRequired( RELEASELABEL , "RELEASELABEL" );
		DistRepository repo = artefactory.getDistRepository( this , meta );
		dist = repo.createDist( this , RELEASELABEL , releaseDate );
		return( SCOPESTATE.RunSuccess );
	}
	
}
