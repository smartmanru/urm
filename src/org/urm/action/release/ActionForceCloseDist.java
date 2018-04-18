package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;

public class ActionForceCloseDist extends ActionBase {

	public Dist dist;
	
	public ActionForceCloseDist( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Close release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = dist.meta;
		EngineProduct ep = meta.getEngineProduct();
		synchronized( ep ) {
			// update repository
			DistRepository distrepoUpdated = method.changeDistRepository( meta.getProduct() );
			Dist distUpdated = distrepoUpdated.findDefaultDist( dist.release );
			
			distUpdated.forceClose( this );
		}
		
		return( SCOPESTATE.RunSuccess );
	}

}
