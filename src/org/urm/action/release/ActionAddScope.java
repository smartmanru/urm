package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.Types;
import org.urm.meta.Types.*;

public class ActionAddScope extends ActionBase {

	public Dist dist;
	
	public ActionAddScope( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Add items to scope, release=" + dist.RELEASEDIR );
		this.dist = dist;
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
		if( Types.isSourceCategory( set.CATEGORY ) )
			return( dist.addAllSource( this , set.pset ) );
		return( dist.addAllCategory( this , set.CATEGORY ) );
	}
	
	private boolean addAllProductTargetElements( ActionScopeSet set , ActionScopeTarget target ) throws Exception {
		if( target.CATEGORY == EnumScopeCategory.CONFIG )
			return( dist.addConfItem( this , target.confItem ) );
		if( target.CATEGORY == EnumScopeCategory.DB )
			return( dist.addDatabaseDeliveryAllSchemes( this , target.dbDelivery ) );
		if( target.CATEGORY == EnumScopeCategory.MANUAL )
			return( dist.addManualItem( this , target.manualItem ) );
		if( Types.isSourceCategory( target.CATEGORY ) )
			return( dist.addProjectAllItems( this , target.sourceProject ) );

		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
	private boolean addTargetItem( ActionScopeSet set , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {
		if( Types.isSourceCategory( target.CATEGORY ) )
			return( dist.addProjectItem( this , target.sourceProject , item.sourceItem ) );
		if( target.CATEGORY == EnumScopeCategory.DB )
			return( dist.addDatabaseDeliverySchema( this , target.dbDelivery , item.schema ) );
		
		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
}
