package ru.egov.urm.run.release;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;

public class ActionDescope extends ActionBase {

	DistStorage dist;
	
	public ActionDescope( ActionBase action , String stream , DistStorage dist ) {
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
		
		List<MetaReleaseTargetItem> items = new LinkedList<MetaReleaseTargetItem>();
		for( ActionScopeTargetItem item : target.getItems( this ) )
			items.add( item.releaseItem );
		
		dist.descopeTargetItems( this , items.toArray( new MetaReleaseTargetItem[0] ) );
		
		return( true );
	}

}
