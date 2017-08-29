package org.urm.action;

import org.urm.common.Common;
import org.urm.meta.Types.VarCATEGORY;
import org.urm.meta.Types.VarDISTITEMORIGIN;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionProductScopeMaker {

	private ActionScope scope;
	private ActionBase action;
	private Meta meta;
	
	public ActionProductScopeMaker( ActionBase action , Meta meta ) {
		scope = new ActionScope( action , meta );
		this.action = action;
		this.meta = meta;
	}

	public ActionScope getScope() {
		return( scope );
	}
	
	public boolean isEmpty() throws Exception {
		return( scope.isEmpty() );
	}

	public void addScopeProductSet( String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Product Set Scope, set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		if( set == null || set.isEmpty() )
			action.exit0( _Error.MissingSetName0 , "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit0( _Error.TargetsWithoutSet0 , "targets cannot be specified without set" );
			
			addFullProduct();
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit0( _Error.MissingTargets0 , "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				addProductSet( set , null );
			else
				addProductSet( set , TARGETS );
		}
	}
	
	public void addScopeProductDistItems( String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Dist Items Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addProductDistItemsScope( null , false );
		else
			addProductDistItemsScope( ITEMS , true );
	}

	public void addScopeProductConfItems( String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Configuration Items Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addProductConfigs( null );
		else
			addProductConfigs( ITEMS );
	}

	public void addScopeProductDatabaseDeliveries( String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Database Deliveries Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addProductDatabaseDeliveries( null );
		else
			addProductDatabaseDeliveries( ITEMS );
	}

	public void addScopeProductDatabaseDeliverySchemes( String DELIVERY , String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Database Delivery Schemes Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addProductDatabaseDeliverySchemes( DELIVERY , null );
		else
			addProductDatabaseDeliverySchemes( DELIVERY , ITEMS );
	}

	public void addScopeProductCategory( VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		addScopeProductSet( Common.getEnumLower( CATEGORY ) , TARGETS );
	}

	private void addFullProduct() throws Exception {
		addAllSourceProjects();
		addAllProductConfigs();
		addAllProductDatabase();
		addAllManualItems();
		addAllDerivedItems();
		scope.setFullProduct( action , true );
	}
	
	private void addProductSet( String set , String[] TARGETS ) throws Exception {
		if( set.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) )
			addProductConfigs( TARGETS );
		else 
		if( set.equals( Common.getEnumLower( VarCATEGORY.DB ) ) )
			addProductDatabaseDeliveries( TARGETS );
		else 
		if( set.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) )
			addManualItems( TARGETS );
		else 
		if( set.equals( Common.getEnumLower( VarCATEGORY.DERIVED ) ) )
			addDerivedItems( TARGETS );
		else {
			MetaSource sources = meta.getSources( action );
			MetaSourceProjectSet pset = sources.getProjectSet( action , set );  
			addSourceProjects( pset , TARGETS );
		}
	}
	
	private void addProductDistItemsScope( String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		MetaDistr distr = meta.getDistr( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( action , itemName );
			if( item == null )
				action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + itemName , itemName );
			
			ActionScopeSet sset = null;
			if( item.distItemOrigin == VarDISTITEMORIGIN.MANUAL ) {
				sset = scope.makeProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
				sset.addManualItems( action , new String[] { itemName } );
			}
			else
			if( item.distItemOrigin == VarDISTITEMORIGIN.DERIVED ) {
				sset = scope.makeProductCategoryScopeSet( action , VarCATEGORY.DERIVED );
				sset.addDerivedItems( action , new String[] { itemName } );
			}
			else {
				sset = scope.makeProjectScopeSet( action , item.sourceProjectItem.project.set );
			
				ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceProjectItem.project , false , true ); 
				scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
			}
		}
	}
	
	private void addAllSourceProjects() throws Exception {
		MetaSource sources = meta.getSources( action );
		for( MetaSourceProjectSet pset : sources.getSets() ) {
			ActionScopeSet sset = scope.makeProjectScopeSet( action , pset );
			sset.addProjects( action , null );
		}
	}

	private void addSourceProjects( MetaSourceProjectSet pset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = scope.makeProjectScopeSet( action , pset );
		sset.addProjects( action , PROJECTS );
	}
		
	private void addAllProductConfigs() throws Exception {
		addProductConfigs( null );
	}
	
	private void addProductConfigs( String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = scope.makeProductCategoryScopeSet( action , VarCATEGORY.CONFIG );
		sset.addConfigComps( action , CONFCOMPS );
	}

	private void addAllProductDatabase() throws Exception {
		addProductDatabaseDeliveries( null );
	}
	
	private void addProductDatabaseDeliveries( String[] DELIVERIES ) throws Exception {
		ActionScopeSet set = scope.makeProductCategoryScopeSet( action , VarCATEGORY.DB );
		set.addDatabaseDeliveries( action , DELIVERIES );
	}

	private void addAllManualItems() throws Exception {
		addManualItems( null );
	}

	private void addManualItems( String[] DISTITEMS ) throws Exception {
		ActionScopeSet sset = scope.makeProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
		sset.addManualItems( action , DISTITEMS );
	}
	
	private void addAllDerivedItems() throws Exception {
		addDerivedItems( null );
	}

	private void addDerivedItems( String[] DISTITEMS ) throws Exception {
		ActionScopeSet sset = scope.makeProductCategoryScopeSet( action , VarCATEGORY.DERIVED );
		sset.addDerivedItems( action , DISTITEMS );
	}
	
	private void addProductDatabaseDeliverySchemes( String DELIVERY , String[] SCHEMES ) throws Exception {
		ActionScopeSet set = scope.makeProductCategoryScopeSet( action , VarCATEGORY.DB );
		set.addDatabaseDeliverySchemes( action , DELIVERY , SCHEMES );
	}

}
