package org.urm.engine.action;

import org.urm.action.ActionBase;
import org.urm.common.action.CommandMethodMeta;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseLabelInfo;
import org.urm.engine.status.ScopeState;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

abstract public class CommandMethod {

	public CommandMethodMeta method;
	
	public abstract void run( ScopeState parentState , ActionBase action ) throws Exception;

	public void setMethod( CommandMethodMeta method ) {
		this.method = method;
	}

	public void wrongArgs( ActionBase action ) throws Exception {
		action.exit0( _Error.WrongArgs0 , "wrong args" );
	}

	public Release getRelease( ActionBase action , String RELEASELABEL ) throws Exception {
		Meta meta = action.getContextMeta();
		ReleaseRepository repo = meta.getReleaseRepository();
		ReleaseLabelInfo info = ReleaseLabelInfo.getLabelInfo( action , meta , RELEASELABEL );
		Release release = repo.findRelease( info.RELEASEVER );
		if( release == null )
			action.exit0( _Error.UnknownRelease1 , "unable to find release version=" + info.RELEASEVER );
		return( release );
	}
	
	public Dist getDist( ActionBase action , String RELEASELABEL ) throws Exception {
		Meta meta = action.getContextMeta();
		DistRepository distrepo = meta.getDistRepository();
		return( distrepo.getDistByLabel( action , RELEASELABEL ) );
	}
	
}
