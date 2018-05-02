package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.db.release.DBReleaseBuildTarget;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseBuildTarget;
import org.urm.meta.release.ReleaseRepository;
import org.urm.meta.release.ReleaseScope;

public class ActionSetSpecifics extends ActionBase {

	public Release release;
	
	public ActionSetSpecifics( ActionBase action , String stream , Release release ) {
		super( action , stream , "Set scope specifics, release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override 
	protected SCOPESTATE executeScope( ScopeState state , ActionScope scope ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( meta );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			
			if( scope.isFull() )
				setBuildAll( method , releaseUpdated );
			else {
				for( ActionScopeSet set : scope.getSourceSets( this ) ) {
					if( set.isFull() )
						setBuildSet( method , releaseUpdated , set.releaseBuildScopeSet.set );
					else {
						for( ActionScopeTarget target : set.getTargets() )
							setBuildProject( method , releaseUpdated , target.releaseBuildScopeProject.project );
					}
				}
			}
		}
			
		return( SCOPESTATE.RunSuccess );
	}

	private void setBuildAll( EngineMethod method , Release release ) throws Exception {
		ReleaseScope scope = release.getScope();
		ReleaseBuildTarget target = scope.findBuildAllTarget();
		if( target != null )
			DBReleaseBuildTarget.setTargetSpecifics( method , this , release , target , super.context.CTX_BRANCH , super.context.CTX_TAG , super.context.CTX_VERSION );
		else {
			Meta meta = release.getMeta();
			MetaSources sources = meta.getSources();
			for( MetaSourceProjectSet set : sources.getSets() )
				setBuildSet( method , release , set );
		}
	}
	
	private void setBuildSet( EngineMethod method , Release release , MetaSourceProjectSet set ) throws Exception {
		ReleaseScope scope = release.getScope();
		ReleaseBuildTarget target = scope.findBuildProjectSetTarget( set );
		if( target != null )
			DBReleaseBuildTarget.setTargetSpecifics( method , this , release , target , super.context.CTX_BRANCH , super.context.CTX_TAG , super.context.CTX_VERSION );
		else {
			for( MetaSourceProject project : set.getProjects() )
				setBuildProject( method , release , project );
		}
	}
	
	private void setBuildProject( EngineMethod method , Release release , MetaSourceProject project ) throws Exception {
		ReleaseScope scope = release.getScope();
		ReleaseBuildTarget target = scope.findBuildProjectTarget( project );
		if( target != null )
			DBReleaseBuildTarget.setTargetSpecifics( method , this , release , target , super.context.CTX_BRANCH , super.context.CTX_TAG , super.context.CTX_VERSION );
	}
	
}
