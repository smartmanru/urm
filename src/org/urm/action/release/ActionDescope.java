package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionDescope extends ActionBase {

	public Dist dist;
	
	public ActionDescope( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Descope items from release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !set.setFull )
			return( SCOPESTATE.NotRun );
		
		dist.reloadCheckOpenedForDataChange( this );
		if( set.releaseBuildScopeSet != null )
			dist.descopeSet( this , set.releaseBuildScopeSet );
		else
			dist.descopeSet( this , set.releaseDistScopeSet );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		return( SCOPESTATE.RunFail );
	}

}
