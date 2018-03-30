package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.release.Release;

public class ActionAddScope extends ActionBase {

	public Release release;
	
	public ActionAddScope( ActionBase action , String stream , Release release ) {
		super( action , stream , "Add items to scope, release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override 
	protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		// full set scope
		if( set.setFull ) {
			if( !addAllProductSetElements( set ) )
				exit0( _Error.OperationCancelled0 , "operation cancelled" );
			return( SCOPESTATE.RunSuccess );
		}
		
		// by target
		for( ActionScopeTarget target : set.getTargets( this ).values() ) {
			if( !Common.checkListItem( targets ,  target ) )
				continue;
			
			if( target.itemFull ) {
				if( !addAllProductTargetElements( set , target ) )
					exit0( _Error.OperationCancelled0 , "operation cancelled" );
				continue;
			}
			
			for( ActionScopeTargetItem item : target.getItems( this ) ) { 
				if( !addTargetItem( set , target , item ) )
					exit0( _Error.OperationCancelled0 , "operation cancelled" );
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean addAllProductSetElements( ActionScopeSet set ) throws Exception {
		if( set.CATEGORY.isSource() )
			DBReleaseScope.addAllSourceSet( super.method , this , release , set.pset );
		else
			DBReleaseScope.addAllCategory( super.method , this , release , set.CATEGORY );
		return( true );
	}
	
	private boolean addAllProductTargetElements( ActionScopeSet set , ActionScopeTarget target ) throws Exception {
		if( target.CATEGORY == DBEnumScopeCategoryType.CONFIG )
			DBReleaseScope.addConfItem( super.method , this , release , target.confItem );
		else
		if( target.CATEGORY == DBEnumScopeCategoryType.MANUAL )
			DBReleaseScope.addManualItem( super.method , this , release , target.manualItem );
		else
		if( target.CATEGORY == DBEnumScopeCategoryType.DB )
			DBReleaseScope.addDeliveryAllDatabaseSchemes( super.method , this , release , target.delivery );
		else
		if( target.CATEGORY == DBEnumScopeCategoryType.DOC )
			DBReleaseScope.addDeliveryAllDocs( super.method , this , release , target.delivery );
		else
		if( target.CATEGORY.isSource() )
			DBReleaseScope.addAllProjectItems( super.method , this , release , target.sourceProject );
		else
			this.exitUnexpectedCategory( target.CATEGORY );
		return( true );
	}
	
	private boolean addTargetItem( ActionScopeSet set , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {
		if( target.CATEGORY.isSource() )
			DBReleaseScope.addProjectItem( super.method , this , release , target.sourceProject , item.sourceItem );
		else
		if( target.CATEGORY == DBEnumScopeCategoryType.DB )
			DBReleaseScope.addDeliveryDatabaseSchema( super.method , this , release , target.delivery , item.schema );
		else
		if( target.CATEGORY == DBEnumScopeCategoryType.DOC )
			DBReleaseScope.addDeliveryDoc( super.method , this , release , target.delivery , item.doc );
		else
			this.exitUnexpectedCategory( target.CATEGORY );
		return( true );
	}
	
}
