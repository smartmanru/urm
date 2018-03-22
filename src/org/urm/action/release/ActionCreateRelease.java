package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionCreateRelease extends ActionBase {

	public Meta meta;
	public Release release;
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
		ProductReleases releases = meta.getReleases();
		ReleaseRepository repo = releases.getReleaseRepository();
		
		release = DBReleaseRepository.createReleaseNormal( this , meta , repo , RELEASELABEL , releaseDate , lc );
		DistRepository distrepo = meta.getDistRepository();
		dist = distrepo.getDistByLabel( this , RELEASELABEL );
		return( SCOPESTATE.RunSuccess );
	}
	
}
