package org.urm.action.release;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTargetItem;
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
		dist.descopeSet( this , set.rset );
		return( SCOPESTATE.RunSuccess );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		if( target.itemFull ) {
			dist.reloadCheckOpenedForDataChange( this );
			dist.descopeTarget( this , target.releaseTarget );
			return( SCOPESTATE.RunSuccess );
		}
		
		List<ReleaseTargetItem> items = new LinkedList<ReleaseTargetItem>();
		for( ActionScopeTargetItem item : target.getItems( this ) )
			items.add( item.releaseItem );
		
		dist.reloadCheckOpenedForDataChange( this );
		dist.descopeTargetItems( this , items.toArray( new ReleaseTargetItem[0] ) );
		
		return( SCOPESTATE.RunSuccess );
	}

}
