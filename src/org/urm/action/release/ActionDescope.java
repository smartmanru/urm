package org.urm.action.release;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTargetItem;

public class ActionDescope extends ActionBase {

	Dist dist;
	
	public ActionDescope( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !set.setFull )
			return( SCOPESTATE.NotRun );
		
		dist.descopeSet( this , set.rset );
		return( SCOPESTATE.RunSuccess );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		if( target.itemFull ) {
			dist.descopeTarget( this , target.releaseTarget );
			return( SCOPESTATE.RunSuccess );
		}
		
		List<ReleaseTargetItem> items = new LinkedList<ReleaseTargetItem>();
		for( ActionScopeTargetItem item : target.getItems( this ) )
			items.add( item.releaseItem );
		
		dist.descopeTargetItems( this , items.toArray( new ReleaseTargetItem[0] ) );
		
		return( SCOPESTATE.RunSuccess );
	}

}
