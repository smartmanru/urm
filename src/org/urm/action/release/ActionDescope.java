package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ProductReleases;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionDescope extends ActionBase {

	public Release release;
	
	public ActionDescope( ActionBase action , String stream , Release release ) {
		super( action , stream , "Descope items from release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			
			if( set.setFull ) {
				if( set.releaseBuildScopeSet != null )
					DBReleaseScope.descopeSet( super.method , this , releaseUpdated , set.releaseBuildScopeSet );
				else
					DBReleaseScope.descopeSet( super.method , this , releaseUpdated , set.releaseDistScopeSet );
			}
			else {
				for( ActionScopeTarget target : targets ) {
					if( target.releaseBuildScopeProject != null ) {
						MetaSourceProject project = target.releaseBuildScopeProject.project;
						DBReleaseScope.descopeProject( super.method , this , releaseUpdated , project.set , project );
					}
					else
					if( target.releaseDistScopeDelivery != null )
						DBReleaseScope.descopeDelivery( method , this , releaseUpdated , target.releaseDistScopeDelivery );
					else
					if( target.releaseDistScopeDeliveryItem != null ) {
						if( target.releaseDistScopeDeliveryItem.isBinary() )
							DBReleaseScope.descopeBinaryItem( method , this , releaseUpdated , target.releaseDistScopeDeliveryItem.binary );
						else
						if( target.releaseDistScopeDeliveryItem.isConf() )
							DBReleaseScope.descopeConfItem( method , this , releaseUpdated , target.releaseDistScopeDeliveryItem.conf );
					}
				}
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

}
