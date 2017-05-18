package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTarget;

public class ActionSetSpecifics extends ActionBase {

	public Dist dist;
	
	public ActionSetSpecifics( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Set scope specifics, release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		// full set scope
		if( set.setFull ) {
			set.rset.setSpecifics( this , context.CTX_BRANCH , context.CTX_TAG , context.CTX_VERSION );
			return( SCOPESTATE.RunSuccess );
		}
		
		// by target
		for( ActionScopeTarget target : targets )
			setTargetSpecifics( target.releaseTarget );
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean setTargetSpecifics( ReleaseTarget target ) throws Exception {
		if( !target.isProjectTarget() )
			return( false );
		
		target.setSpecifics( this , context.CTX_BRANCH , context.CTX_TAG , context.CTX_VERSION );
		return( true );
	}
	
}