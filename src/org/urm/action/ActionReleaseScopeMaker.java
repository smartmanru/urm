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
		scope = new ActionScope( action , dist.meta );
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
	
	public void addScopeReleaseDatabaseDeliverySchemes( String DELIVERY , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Database Delivery Schemes Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addReleaseDatabaseDeliverySchemes( DELIVERY , null );
		else
			addReleaseDatabaseDeliverySchemes( DELIVERY , ITEMS );
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
			
			ActionScopeTarget target = addReleaseManualDatabase( sset , all );
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
					ActionScopeTarget target = addReleaseDatabaseDelivery( sset , delivery , all , false );
					if( !all )
						target.addIndexItems( action , INDEXES );
				}
			}
			else {
				ReleaseDelivery delivery = dist.release.getDelivery( action , DELIVERY );
				ActionScopeTarget target = addReleaseDatabaseDelivery( sset , delivery , all , true );
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
				ActionScopeTarget target = addReleaseDatabaseDelivery( sset , delivery , all , false );
				if( !all )
					target.addDatabaseSchemes( action , SCHEMES );
			}
		}
		else {
			ReleaseDelivery delivery = dist.release.getDelivery( action , DELIVERY );
			ActionScopeTarget target = addReleaseDatabaseDelivery( sset , delivery , all , true );
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
		ActionScopeTarget target = addReleaseProjectItems( sset , releaseProject , ITEMS );
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
			
			ReleaseTarget rtarget = sset.rset.getTarget( action , item.sourceProjectItem.project.NAME );
			if( rtarget != null ) {
				ActionScopeTarget scopeProject = addReleaseSourceProject( sset , rtarget , false , true ); 
				scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
			}
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
				addReleaseSourceProjects( rset , TARGETS );
			}
		}
	}

 	private void addReleaseManualItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.MANUAL );
		if( set != null )
			addReleaseManualItems( set , ITEMS );
 	}
	
 	private void addAllReleaseManualItems() throws Exception {
 		addReleaseManualItems( null );
 	}
	
 	private void addReleaseDerivedItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.DERIVED );
		if( set != null )
			addReleaseDerivedItems( set , ITEMS );
 	}
	
 	private void addAllReleaseDerivedItems() throws Exception {
 		addReleaseDerivedItems( null );
 	}
	
	private void addAllReleaseProjects() throws Exception {
		for( ReleaseDistSet rset : dist.release.getSourceSets() ) {
			ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
			addReleaseSourceProjects( sset , null );
		}
	}
		
	private void addReleaseSourceProjects( ReleaseDistSet rset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
		addReleaseSourceProjects( sset , PROJECTS );
	}
		
	private void addReleaseConfigs( String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.CONFIG );
		if( sset != null )
			addReleaseConfigComps( sset , CONFCOMPS );
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
			addReleaseDatabaseDeliveries( sset , DELIVERIES );
	}

	private void addReleaseDatabaseDeliverySchemes( String DELIVERY , String[] SCHEMES ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , dist , VarCATEGORY.DB );
		if( sset != null )
			addReleaseDatabaseDeliverySchemes( sset , DELIVERY , SCHEMES );
	}

	
	private void addReleaseDatabaseDeliveries( ActionScopeSet set , String[] DELIVERIES ) throws Exception {
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseTarget item : set.rset.getTargets() )
				addReleaseTarget( set , item , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			ReleaseTarget item = set.rset.getTarget( action , key );
			addReleaseTarget( set , item , true );
		}
	}

	private ActionScopeTarget addReleaseTarget( ActionScopeSet set , ReleaseTarget releaseItem , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( set , releaseItem , specifiedExplicitly );
		set.addTarget( action , target );
		return( target );
	}

	private void addReleaseConfigComps( ActionScopeSet set , String[] COMPS ) throws Exception {
		if( COMPS == null || COMPS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseTarget item : set.rset.getTargets() )
				addReleaseTarget( set , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			ReleaseTarget item = set.rset.getTarget( action , key );
			addReleaseTarget( set , item , true );
		}
	}

	private void addReleaseManualItems( ActionScopeSet set , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseTarget item : set.rset.getTargets() )
				addReleaseTarget( set , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			ReleaseTarget item = set.rset.getTarget( action , key );
			addReleaseTarget( set , item , true );
		}
	}

	private void addReleaseDerivedItems( ActionScopeSet set , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseTarget item : set.rset.getTargets() )
				addReleaseTarget( set , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			ReleaseTarget item = set.rset.getTarget( action , key );
			addReleaseTarget( set , item , true );
		}
	}

	private void addReleaseDatabaseDeliverySchemes( ActionScopeSet set , String DELIVERY , String[] SCHEMES ) throws Exception {
		ReleaseTarget item = set.rset.getTarget( action , DELIVERY );
		ActionScopeTarget target = addReleaseTarget( set , item , true );
		target.addDatabaseSchemes( action , SCHEMES );
	}
	
	private void addReleaseSourceProjects( ActionScopeSet set , String[] PROJECTS ) throws Exception {
		if( PROJECTS == null || PROJECTS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseTarget project : set.rset.getTargets() )
				addReleaseSourceProject( set , project , true , false );
			return;
		}
		
		for( String name : PROJECTS ) {
			ReleaseTarget sourceProject = set.rset.getTarget( action ,  name );
			addReleaseSourceProject( set , sourceProject , true , true );
		}
	}

	public ActionScopeTarget addReleaseSourceProject( ActionScopeSet set , ReleaseTarget releaseProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( set , releaseProject , specifiedExplicitly ); 
		set.addTarget( action , target );
		
		if( allItems )
			target.addProjectItems( action , null );
		
		return( target );
	}
		
	public ActionScopeTarget addReleaseProjectItems( ActionScopeSet set , ReleaseTarget releaseProject , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( set, releaseProject , true );
		set.addTarget( action , target );
		target.addProjectItems( action , ITEMS );
		return( target );
	}

	public ActionScopeTarget addReleaseDatabaseDelivery( ActionScopeSet set , ReleaseDelivery delivery , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDatabaseDeliveryTarget( set , delivery.distDelivery , specifiedExplicitly , allItems );
		set.addTarget( action , target );
		
		if( allItems )
			target.addDatabaseSchemes( action , null );
		
		return( target );
	}
	
	public ActionScopeTarget addReleaseManualDatabase( ActionScopeSet set , boolean all ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseDatabaseManualTarget( set , all );
		set.addTarget( action , target );
		return( target );
	}
	
}
