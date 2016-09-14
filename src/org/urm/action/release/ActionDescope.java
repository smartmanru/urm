package org.urm.action.release;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.action.ActionBase;
import org.urm.engine.action.ActionScopeSet;
import org.urm.engine.action.ActionScopeTarget;
import org.urm.engine.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseTargetItem;

public class ActionDescope extends ActionBase {

	Dist dist;
	
	public ActionDescope( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !set.setFull )
			return( false );
		
		dist.descopeSet( this , set.rset );
		return( true );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		if( target.itemFull ) {
			dist.descopeTarget( this , target.releaseTarget );
			return( true );
		}
		
		List<ReleaseTargetItem> items = new LinkedList<ReleaseTargetItem>();
		for( ActionScopeTargetItem item : target.getItems( this ) )
			items.add( item.releaseItem );
		
		dist.descopeTargetItems( this , items.toArray( new ReleaseTargetItem[0] ) );
		
		return( true );
	}

}
