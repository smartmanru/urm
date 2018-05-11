package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBRelease;
import org.urm.db.release.DBReleaseSchedule;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseSchedule;

public class ActionModifyRelease extends ActionBase {

	public Meta meta;
	public Release release;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public ActionModifyRelease( ActionBase action , String stream , Meta meta , Release release , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Change properties of release=" + release.RELEASEVER );
		this.meta = meta;
		this.release = release;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );

			if( releaseUpdated.isFinalized() )
				Common.exitUnexpected();
			
			// update release
			if( releaseDate != null ) {
				ReleaseSchedule scheduleUpdated = releaseUpdated.getSchedule();
				if( lc != null || !releaseDate.equals( scheduleUpdated.RELEASE_DATE ) )
					DBReleaseSchedule.changeReleaseDate( method , this , releaseUpdated , scheduleUpdated , releaseDate , lc );
			}
			
			DBRelease.setProperties( method , this , releaseUpdated );

			// update distributive
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			Dist distUpdated = distrepoUpdated.findDefaultDist( releaseUpdated );
			try {
				distUpdated.openForDataChange( this );
				distUpdated.saveMetaFile( this );
			}
			finally {
				distUpdated.closeDataChange( this );
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
