package org.urm.action.release;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.action.ActionScopeSet;
import org.urm.engine.action.ActionScopeTarget;
import org.urm.engine.action.ActionScopeTargetItem;
import org.urm.engine.dist.Dist;
import org.urm.engine.meta.Meta.VarCATEGORY;

public class ActionAddScope extends ActionBase {

	Dist dist;
	
	public ActionAddScope( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		// full set scope
		if( set.setFull ) {
			if( !addAllProductSetElements( set ) )
				exit0( _Error.OperationCancelled0 , "operation cancelled" );
			return( true );
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
		
		return( true );
	}

	private boolean addAllProductSetElements( ActionScopeSet set ) throws Exception {
		if( meta.isSourceCategory( set.CATEGORY ) )
			return( dist.addAllSource( this , set.pset ) );
		return( dist.addAllCategory( this , set.CATEGORY ) );
	}
	
	private boolean addAllProductTargetElements( ActionScopeSet set , ActionScopeTarget target ) throws Exception {
		if( target.CATEGORY == VarCATEGORY.CONFIG )
			return( dist.addConfItem( this , target.confItem ) );
		if( target.CATEGORY == VarCATEGORY.DB )
			return( dist.addDatabaseItem( this , target.dbDelivery ) );
		if( target.CATEGORY == VarCATEGORY.MANUAL )
			return( dist.addManualItem( this , target.manualItem ) );
		if( meta.isSourceCategory( target.CATEGORY ) )
			return( dist.addProjectAllItems( this , target.sourceProject ) );

		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
	private boolean addTargetItem( ActionScopeSet set , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {
		return( dist.addProjectItem( this , target.sourceProject , item.sourceItem ) );
	}
	
}
