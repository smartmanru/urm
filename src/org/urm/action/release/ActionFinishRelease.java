package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseChanges;
import org.urm.meta.release.ReleaseRepository;

public class ActionFinishRelease extends ActionBase {

	public Release release;
	
	public ActionFinishRelease( ActionBase action , String stream , Release release ) {
		super( action , stream , "Finalize release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			Dist dist = distrepoUpdated.findDefaultDist( releaseUpdated );
			
			if( !finish( method , releaseUpdated , dist ) )
				super.exit0( _Error.UnableFinalizeRelease0 , "Unable to finalize release" );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean finish( EngineMethod method , Release release , Dist dist ) throws Exception {
		if( release.isFinalized() ) {
			super.info( "release is already finalized" );
			return( true );
		}
		
		dist.openForDataChange( this );
		
		// check tickets
		if( !release.MASTER ) {
			ReleaseChanges changes = release.getChanges();
			if( !changes.isCompleted() ) {
				super.error( "release changes are not completed" );
				dist.closeDataChange( this );
				return( false );
			}
		}
		
		// finish state
		if( !dist.finish( this ) ) {
			super.error( "unable to finalize files" );
			return( false );
		}
		
		// save in database
		DBRelease.finish( method , this , release );
		dist.saveMetaFile( this );
		return( true );
	}
	
}
