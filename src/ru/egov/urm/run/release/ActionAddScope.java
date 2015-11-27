package ru.egov.urm.run.release;

import ru.egov.urm.Common;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;

public class ActionAddScope extends ActionBase {

	DistStorage dist;
	
	public ActionAddScope( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		// full set scope
		if( set.setFull )
			return( addAllProductSetElements( set ) );
		
		// by target
		for( ActionScopeTarget target : set.getTargets( this ).values() ) {
			if( !Common.checkListItem( targets ,  target ) )
				continue;
			
			if( target.itemFull ) {
				if( !addAllProductTargetElements( set , target ) )
					return( false );
				continue;
			}
			
			for( ActionScopeTargetItem item : target.getItems( this ) ) { 
				if( !addTargetItem( set , target , item ) )
					return( false );
			}
		}
		
		return( true );
	}

	private boolean addAllProductSetElements( ActionScopeSet set ) throws Exception {
		if( meta.isSourceCategory( this , set.CATEGORY ) )
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
		if( meta.isSourceCategory( this , target.CATEGORY ) )
			return( dist.addProjectAllItems( this , target.sourceProject ) );

		this.exitUnexpectedCategory( target.CATEGORY );
		return( false );
	}
	
	private boolean addTargetItem( ActionScopeSet set , ActionScopeTarget target , ActionScopeTargetItem item ) throws Exception {
		return( dist.addProjectItem( this , target.sourceProject , item.sourceItem ) );
	}
	
}
