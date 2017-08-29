package org.urm.action;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseDistSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.meta.Types.VarCATEGORY;
import org.urm.meta.Types.VarDISTITEMORIGIN;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProjectSet;

public class ActionReleaseScopeMaker {

	private ActionScope scope;
	private ActionBase action;
	private Meta meta;
	private Dist dist;
	
	public ActionReleaseScopeMaker( ActionBase action , Dist dist ) {
		scope = new ActionScope( action , meta );
		this.action = action;
		this.meta = dist.meta;
		this.dist = dist;
	}

	public ActionScope getScope() {
		return( scope );
	}
	
	public boolean isEmpty() throws Exception {
		return( scope.isEmpty() );
	}

	public void addScopeReleaseDatabaseManualItems( String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Database Delivery Index Scope, release=" + dist.RELEASEDIR + ", items=" + Common.getListSet( INDEXES ) );
		addReleaseDatabaseIndexScope( null , INDEXES );
	}
	
	public void addScopeReleaseDatabaseDeliveryItems( String DELIVERY , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Database Delivery Index Scope, release=" + dist.RELEASEDIR + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( INDEXES ) );
		addReleaseDatabaseIndexScope( DELIVERY , INDEXES );
	}
	
	public void addScopeReleaseDatabaseSchemes( String DELIVERY , String[] SCHEMES ) throws Exception {
		action.trace( "scope: Release Database Delivery Schemes Scope, release=" + dist.RELEASEDIR + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( SCHEMES ) );
		addReleaseDatabaseSchemaScope( DELIVERY , SCHEMES );
	}
	
	public void addScopeReleaseCategory( VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		addScopeReleaseSet( Common.getEnumLower( CATEGORY ) , TARGETS ); 
	}
	
	public void addScopeReleaseSet( String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Release Set Scope, release=" + dist.RELEASEDIR + ", set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		if( set == null || set.isEmpty() )
			action.exit0( _Error.MissingSetName0 , "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit0( _Error.TargetsWithoutSet0 , "targets cannot be specified without set" );
			
			addFullRelease();
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit0( _Error.MissingTargets0 , "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				addReleaseSet( set , null );
			else
				addReleaseSet( set , TARGETS );
		}
	}

	public void addScopeReleaseDistItems( String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Dist Items Scope, release=" + dist.RELEASEDIR + ", items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addReleaseDistItemsScope( null , false );
		else
			addReleaseDistItemsScope( ITEMS , true );
	}

	public void addScopeReleaseProjectItems( String PROJECT , String[] ITEMS ) throws Exception {
		addScopeReleaseProjectItemsTarget( PROJECT , ITEMS );
	}
	
	public ActionScopeTarget addScopeReleaseProjectItemsTarget( String PROJECT , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Project Items Scope Target, release=" + dist.RELEASEDIR + ", project=" + PROJECT + ", items=" + Common.getListSet( ITEMS ) );
		if( PROJECT == null || PROJECT.isEmpty() )
			action.exit0( _Error.MissingProject0 , "missing project" );
		
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingProjectItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			return( addReleaseProjectItemsScope( PROJECT , null ) );
			
		return( addReleaseProjectItemsScope( PROJECT , ITEMS ) );
	}

	private void addReleaseDatabaseIndexScope( String DELIVERY , String[] INDEXES ) throws Exception {
		if( INDEXES.length == 0 )
			action.exit0( _Error.MissingDatabaseItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( INDEXES.length == 1 && INDEXES[0].equals( "all" ) )? true : false;
		
		VarCATEGORY CATEGORY;
		if( DELIVERY == null ) {
			CATEGORY = VarCATEGORY.MANUAL;
			ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , CATEGORY );
			if( sset == null )
				return;
			
			ActionScopeTarget target = sset.addManualDatabase( action , all );
			if( !all )
				target.addIndexItems( action , INDEXES );
		}
		else {
			CATEGORY = VarCATEGORY.DB;
			ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , CATEGORY );
			if( sset == null )
				return;
			
			if( DELIVERY.equals( "all" ) ) {
				for( ReleaseDelivery delivery : dist.release.getDeliveries() ) {
					ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , all , false );
					if( !all )
						target.addIndexItems( action , INDEXES );
				}
			}
			else {
				ReleaseDelivery delivery = dist.release.getDelivery( action , DELIVERY );
				ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , all , true );
				if( !all )
					target.addIndexItems( action , INDEXES );
			}
		}
	}
	
	private void addReleaseDatabaseSchemaScope( String DELIVERY , String[] SCHEMES ) throws Exception {
		if( SCHEMES.length == 0 )
			action.exit0( _Error.MissingDatabaseItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( SCHEMES.length == 1 && SCHEMES[0].equals( "all" ) )? true : false;
		
		VarCATEGORY CATEGORY = VarCATEGORY.DB;
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , CATEGORY );
		if( sset == null )
			return;
		
		if( DELIVERY.equals( "all" ) ) {
			for( ReleaseDelivery delivery : dist.release.getDeliveries() ) {
				ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , all , false );
				if( !all )
					target.addDatabaseSchemes( action , SCHEMES );
			}
		}
		else {
			ReleaseDelivery delivery = dist.release.getDelivery( action , DELIVERY );
			ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , all , true );
			if( !all )
				target.addDatabaseSchemes( action , SCHEMES );
		}
	}
	
	private ActionScopeTarget addReleaseProjectItemsScope( String PROJECT , String[] ITEMS ) throws Exception {
		ReleaseTarget releaseProject = dist.release.findBuildProject( action , PROJECT );
		if( releaseProject == null ) {
			action.debug( "scope: ignore non-release project=" + PROJECT );
			return( null );
		}

		ActionScopeSet sset = scope.makeProjectScopeSet( action , releaseProject.sourceProject.set );
		ActionScopeTarget target = sset.addReleaseProjectItems( action , releaseProject , ITEMS );
		return( target );
	}
	
	private void addReleaseDistItemsScope( String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		MetaDistr distr = meta.getDistr( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( action , itemName );
			if( item.sourceProjectItem == null )
				action.exit1( _Error.UnknownDistributiveItem1 ,"unknown distributive item=" + itemName , itemName );
			
			ActionScopeSet sset = null;
			if( item.distItemOrigin == VarDISTITEMORIGIN.MANUAL )
				sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.MANUAL );
			else
			if( item.distItemOrigin == VarDISTITEMORIGIN.DERIVED )
				sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.DERIVED );
			else {
				ReleaseDistSet rset = dist.release.getSourceSet( action , item.sourceProjectItem.project.set.NAME );
				sset = scope.makeReleaseScopeSet( action , rset );
			}
			
			ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceProjectItem.project , false , true ); 
			scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
		}
	}
	
	private void addFullRelease()	throws Exception {
		addAllReleaseProjects();
		addAllReleaseConfigs();
		addAllReleaseDatabase();
		addAllReleaseManualItems();
		addAllReleaseDerivedItems();
		scope.setFullRelease( action , true );
	}
	
	private void addReleaseSet( String SET , String[] TARGETS )	throws Exception {
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) )
			addReleaseConfigs( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) )
			addReleaseDatabaseDeliveries( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) )
			addReleaseManualItems( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DERIVED ) ) )
			addReleaseDerivedItems( TARGETS );
		else {
			MetaSource sources = meta.getSources( action );
			MetaSourceProjectSet set = sources.getProjectSet( action , SET );
			if( dist.release.addSourceSet( action , set , false ) ) {
				ReleaseDistSet rset = dist.release.getSourceSet( action , SET );  
				addReleaseProjects( rset , TARGETS );
			}
		}
	}

 	private void addReleaseManualItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.MANUAL );
		if( set != null )
			set.addManualItems( action , ITEMS );
 	}
	
 	private void addAllReleaseManualItems() throws Exception {
 		addReleaseManualItems( null );
 	}
	
 	private void addReleaseDerivedItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.DERIVED );
		if( set != null )
			set.addDerivedItems( action , ITEMS );
 	}
	
 	private void addAllReleaseDerivedItems() throws Exception {
 		addReleaseDerivedItems( null );
 	}
	
	private void addAllReleaseProjects() throws Exception {
		for( ReleaseDistSet rset : dist.release.getSourceSets() ) {
			ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
			sset.addProjects( action , null );
		}
	}
		
	private void addReleaseProjects( ReleaseDistSet rset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
		sset.addProjects( action , PROJECTS );
	}
		
	private void addReleaseConfigs( String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.CONFIG );
		if( sset != null )
			sset.addConfigComps( action , CONFCOMPS );
	}

	private void addAllReleaseConfigs() throws Exception {
		addReleaseConfigs( null );
	}
	
 	private void addAllReleaseDatabase() throws Exception {
		addReleaseDatabaseDeliveries( null );
	}

	private void addReleaseDatabaseDeliveries( String[] DELIVERIES ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.DB );
		if( sset != null )
			sset.addDatabaseDeliveries( action , DELIVERIES );
	}
	
}
