package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.db.release.DBReleaseDist;
import org.urm.db.release.DBReleaseRepository;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
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
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			DistRepository distrepoUpdated = method.changeDistRepository( meta.getProduct() );

			// create release
			ReleaseLabelInfo info = distrepoUpdated.getLabelInfo( this , meta , RELEASEDIR );
			if( info.master ) {
				super.fail0( _Error.CannotCopyProd0 , "Cannot create master distributive, use master command instead" );
				return( SCOPESTATE.RunFail );
			}
			
			release = DBReleaseRepository.createReleaseNormal( method , this , repoUpdated , info , releaseDate , lc );
			ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , this , release , info.VARIANT );
			
			// create distributive
			DistRepositoryItem item = distrepoUpdated.createRepositoryItem( method , this , info );
			
			Dist dist = distrepoUpdated.createDistNormal( method , this , item , releaseDist );
			DBReleaseDist.updateHash( method , this , release , releaseDist , dist );
			
			DBReleaseScope.copyScope( method , this , repoUpdated , release , src );
			Dist srcDist = distrepoUpdated.findDefaultDist( src );
			item.copyFiles( method , this , srcDist );
		}
		
		return( SCOPESTATE.RunSuccess );
	}

}
