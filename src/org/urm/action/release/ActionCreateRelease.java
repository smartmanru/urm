package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseDist;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ProductReleases;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class ActionCreateRelease extends ActionBase {

	public Meta meta;
	public Release release;
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
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			
			// create release
			ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( this , meta , RELEASELABEL );
			release = DBReleaseRepository.createReleaseNormal( method , this , repoUpdated , info , releaseDate , lc );
			ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , this , release , info.VARIANT );
			
			// create distributive
			DistRepositoryItem item = distrepoUpdated.createRepositoryItem( method , this , info );
			Dist dist = distrepoUpdated.createDistNormal( method , this , item , releaseDist );
			DBReleaseDist.updateHash( method , this , release , releaseDist , dist );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
