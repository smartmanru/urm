package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionCopyRelease extends ActionBase {

	public Dist src;
	public String RELEASEDIR;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public Release release;
	public Dist dist;
	
	public ActionCopyRelease( ActionBase action , String stream , Dist src , String RELEASEDST , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Copy distributive src=" + src.RELEASEDIR + ", dst=" + RELEASEDST );
		this.src = src;
		this.RELEASEDIR = RELEASEDST;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Meta meta = src.meta;
		DistRepository distrepo = meta.getDistRepository();
		ReleaseLabelInfo info = distrepo.getLabelInfo( this , RELEASEDIR );
		if( info.master ) {
			super.fail0( _Error.CannotCopyProd0 , "Cannot create master distributive, use master command instead" );
			return( SCOPESTATE.RunFail );
		}
		
		ProductReleases releases = meta.getReleases();
		ReleaseRepository repo = releases.getReleaseRepository();
		release = DBReleaseRepository.createReleaseNormal( this , meta , repo , RELEASEDIR , releaseDate , lc );
		dist = distrepo.getDistByLabel( this , RELEASEDIR );
		
		dist.copyScope( this , src );
		return( SCOPESTATE.RunSuccess );
	}

}
