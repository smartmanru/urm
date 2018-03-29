package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseDist;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class ActionCopyRelease extends ActionBase {

	public Release src;
	public String RELEASEDIR;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public Release release;
	
	public ActionCopyRelease( ActionBase action , String stream , Release src , String RELEASEDST , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Copy release src=" + src.RELEASEVER + ", dst=" + RELEASEDST );
		this.src = src;
		this.RELEASEDIR = RELEASEDST;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = src.getMeta();
		DistRepository distrepo = meta.getDistRepository();
		ReleaseLabelInfo info = distrepo.getLabelInfo( this , RELEASEDIR );
		if( info.master ) {
			super.fail0( _Error.CannotCopyProd0 , "Cannot create master distributive, use master command instead" );
			return( SCOPESTATE.RunFail );
		}
		
		ProductReleases releases = meta.getReleases();
		ReleaseRepository repo = releases.getReleaseRepository();
		release = DBReleaseRepository.createReleaseNormal( method , this , repo , info , releaseDate , lc );
		ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , this , release , info.VARIANT );
		
		// create distributive
		DistRepositoryItem item = distrepo.createRepositoryItem( method , this , info );
		
		Dist dist = distrepo.createDistNormal( method , this , item , releaseDist );
		DBReleaseDist.updateHash( method , this , release , releaseDist , dist );
		distrepo.addItem( item );
		
		DBReleaseRepository.copyScope( method , this , repo , release , src );
		
		return( SCOPESTATE.RunSuccess );
	}

}
