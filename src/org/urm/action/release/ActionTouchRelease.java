package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class ActionTouchRelease extends ActionBase {

	public Meta meta;
	public String RELEASELABEL;
	public Release release;
	
	public ActionTouchRelease( ActionBase action , String stream , Meta meta , String RELEASELABEL ) {
		super( action , stream , "Touch release=" + RELEASELABEL );
		this.meta = meta;
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repository
			DistRepository distrepoUpdated = method.changeDistRepository( meta.getProduct() );

			// reload distributive
			ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( this , meta , RELEASELABEL );
			DistRepositoryItem item = distrepoUpdated.findNormalItem( info.RELEASEDIR );
			if( item == null )
				Common.exitUnexpected();
				
			DistRepositoryItem itemUpdated = method.changeDistItem( distrepoUpdated , item );
			Dist dist = distrepoUpdated.reloadDist( this , itemUpdated );
			release = dist.release;
			
			return( SCOPESTATE.RunSuccess );
		}
	}
	
}
