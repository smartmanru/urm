package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionSetScope extends ActionBase {

	public Dist dist;
	String[] pathItems;
	
	public ActionSetScope( ActionBase action , String stream , Dist dist , String[] pathItems ) {
		super( action , stream , "Set scope, release=" + dist.RELEASEDIR );
		this.dist = dist;
		this.pathItems = pathItems;
	}

	@Override 
	protected SCOPESTATE executeSimple() throws Exception {
		return( SCOPESTATE.RunFail );
	}

}
