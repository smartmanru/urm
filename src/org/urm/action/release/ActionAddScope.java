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
			return( DBReleaseScope.addAllSource( super.method , this , release , set.pset ) );
		return( DBReleaseScope.addAllCategory( super.method , this , release , set.CATEGORY ) );
	}
	
	private boolean addAllProductTargetElements( ActionScopeSet set , ActionScopeTarget target ) throws Exception {
		if( target.CATEGORY == DBEnumScopeCategoryType.CONFIG )
			return( DBReleaseScope.addConfItem( super.method , this , release , target.confItem ) );
		if( target.CATEGORY == DBEnumScopeCategoryType.MANUAL )
			return( DBReleaseScope.addManualItem( super.method , this , release , target.manualItem ) );
		if( target.CATEGORY == DBEnumScopeCategoryType.DB )
			return( DBReleaseScope.addDeliveryAllDatabaseSchemes( super.method , this , release , target.delivery ) );
		if( target.CATEGORY == DBEnumScopeCategoryType.DOC )
			return( DBReleaseScope.addDeliveryAllDocs( super.method , this , release , target.delivery ) );
		if( target.CATEGORY.isSource() )
			return( DBReleaseScope.addProjectAllItems( super.method , this , release , target.sourceProject ) );

		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
	private boolean addTargetItem( ActionScopeSet set , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {
		if( target.CATEGORY.isSource() )
			return( DBReleaseScope.addProjectItem( super.method , this , release , target.sourceProject , item.sourceItem ) );
		if( target.CATEGORY == DBEnumScopeCategoryType.DB )
			return( DBReleaseScope.addDeliveryDatabaseSchema( super.method , this , release , target.delivery , item.schema ) );
		if( target.CATEGORY == DBEnumScopeCategoryType.DOC )
			return( DBReleaseScope.addDeliveryDoc( super.method , this , release , target.delivery , item.doc ) );
		
		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
}
