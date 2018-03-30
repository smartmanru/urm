package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionDescope extends ActionBase {

	public Release release;
	
	public ActionDescope( ActionBase action , String stream , Release release ) {
		super( action , stream , "Descope items from release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !set.setFull )
			return( SCOPESTATE.NotRun );
		
		if( set.releaseBuildScopeSet != null )
			DBReleaseScope.descopeSet( super.method , this , release , set.releaseBuildScopeSet );
		else
			DBReleaseScope.descopeSet( super.method , this , release , set.releaseDistScopeSet );
		
		return( SCOPESTATE.RunSuccess );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		return( SCOPESTATE.RunFail );
	}

}
