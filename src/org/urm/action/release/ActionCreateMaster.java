package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseDist;
import org.urm.db.release.DBReleaseRepository;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ProductReleases;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class ActionCreateMaster extends ActionBase { 

	public Meta meta;
	public String RELEASEVER;
	public Release release;
	public Dist dist;
	public boolean copy;
	
	public ActionCreateMaster( ActionBase action , String stream , Meta meta , String RELEASEVER , boolean copy ) {
		super( action , stream , "Create production coverage distributive" );
		this.meta = meta;
		this.RELEASEVER = RELEASEVER;
		this.copy = copy;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repositories
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			Dist src = distrepoUpdated.getDistByLabel( this , RELEASEVER );
			
			// create release
			Release releaseNew = DBReleaseRepository.createReleaseMaster( method , this , repoUpdated , RELEASEVER );
			ReleaseDist releaseDist = DBReleaseDist.createReleaseDist( method , this , releaseNew , "" );
			
			// create distributive
			if( copy ) {
				dist = distrepoUpdated.createMasterCopy( method , this , src , release , releaseDist );
				createMasterFiles( dist , src );
			}
			else
				dist = distrepoUpdated.createMasterInitial( this , release , releaseDist );
			this.release = releaseNew;
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private void createMasterFiles( Dist dist , Dist src ) throws Exception {
		ReleaseDistScope scope = ReleaseDistScope.createScope( src.release );
		for( ReleaseDistScopeSet set : scope.getSets() ) {
			if( set.CATEGORY == DBEnumScopeCategoryType.BINARY ) {
				for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
					for( ReleaseDistScopeDeliveryItem item : delivery.getItems() )
						dist.copyBinaryItem( this , src , item.binary , true );
				}
			}
		}
	}
	
}
