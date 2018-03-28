package org.urm.action;

import org.urm.common.Common;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.dist.ReleaseBuildScopeProject;
import org.urm.engine.dist.ReleaseBuildScopeSet;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.release.Release;

public class ActionReleaseScopeMaker {

	private ActionScope scope;
	private ActionBase action;
	private Meta meta;
	private Release release;
	
	public ActionReleaseScopeMaker( ActionBase action , Release release ) {
		scope = new ActionScope( action , release.getMeta() );
		this.action = action;
		this.meta = release.getMeta();
		this.release = release;
	}

	public ActionScope getScope() {
		return( scope );
	}
	
	public boolean isEmpty() throws Exception {
		return( scope.isEmpty() );
	}

	public void addScopeReleaseDatabaseManualItems( String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Database Delivery Index Scope, release=" + release.RELEASEVER + ", items=" + Common.getListSet( INDEXES ) );
		scope.setReleaseDistScope( release );
		addReleaseDatabaseIndexScope( null , INDEXES );
	}
	
	public void addScopeReleaseDeliveryDatabaseItems( String DELIVERY , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Database Delivery Index Scope, release=" + release.RELEASEVER + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( INDEXES ) );
		scope.setReleaseDistScope( release );
		addReleaseDatabaseIndexScope( DELIVERY , INDEXES );
	}
	
	public void addScopeReleaseDatabaseSchemes( String DELIVERY , String[] SCHEMES ) throws Exception {
		action.trace( "scope: Release Database Delivery Schemes Scope, release=" + release.RELEASEVER + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( SCHEMES ) );
		scope.setReleaseDistScope( release );
		addReleaseDatabaseSchemaScope( DELIVERY , SCHEMES );
	}
	
	public void addScopeReleaseDocs( String DELIVERY , String[] DOCS ) throws Exception {
		action.trace( "scope: Release Delivery Docs Scope, release=" + release.RELEASEVER + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( DOCS ) );
		scope.setReleaseDistScope( release );
		addReleaseDocScope( DELIVERY , DOCS );
	}
	
	public void addScopeReleaseDeliveryDatabaseSchemes( String DELIVERY , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Database Delivery Schemes Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		scope.setReleaseDistScope( release );
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addReleaseDeliveryDatabaseSchemes( DELIVERY , null );
		else
			addReleaseDeliveryDatabaseSchemes( DELIVERY , ITEMS );
	}

	public void addScopeReleaseDeliveryDocs( String DELIVERY , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Delivery Docs Scope, items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		scope.setReleaseDistScope( release );
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addReleaseDeliveryDocs( DELIVERY , null );
		else
			addReleaseDeliveryDocs( DELIVERY , ITEMS );
	}

	public void addScopeReleaseCategory( DBEnumScopeCategoryType CATEGORY , String[] TARGETS ) throws Exception {
		scope.setReleaseDistScope( release );
		addScopeReleaseSet( Common.getEnumLower( CATEGORY ) , TARGETS ); 
	}

	public void addScopeAll() throws Exception {
		addScopeReleaseSet( "all" , null );
	}
	
	public void addScopeReleaseSet( String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Release Set Scope, release=" + release.RELEASEVER + ", set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		if( set == null || set.isEmpty() )
			action.exit0( _Error.MissingSetName0 , "missing set name (use \"all\" to reference all sets)" );
			
		scope.setReleaseBuildScope( release );
		scope.setReleaseDistScope( release );
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
		scope.setReleaseDistScope( release );
		action.trace( "scope: Release Dist Items Scope, release=" + release.RELEASEVER + ", items=" + Common.getListSet( ITEMS ) );
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		scope.setReleaseDistScope( release );
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			addReleaseDistItemsScope( null , false );
		else
			addReleaseDistItemsScope( ITEMS , true );
	}

	public void addScopeReleaseProjectItems( String PROJECT , String[] ITEMS ) throws Exception {
		scope.setReleaseBuildScope( release );
		addScopeReleaseProjectItemsTarget( PROJECT , ITEMS );
	}
	
	public ActionScopeTarget addScopeReleaseProjectItemsTarget( String PROJECT , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Project Items Scope Target, release=" + release.RELEASEVER + ", project=" + PROJECT + ", items=" + Common.getListSet( ITEMS ) );
		if( PROJECT == null || PROJECT.isEmpty() )
			action.exit0( _Error.MissingProject0 , "missing project" );
		
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingProjectItems0 , "missing items (use \"all\" to reference all items)" );
		
		scope.setReleaseBuildScope( release );
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			return( addReleaseProjectItemsScope( PROJECT , null ) );
			
		return( addReleaseProjectItemsScope( PROJECT , ITEMS ) );
	}

	private ActionScopeTarget addReleaseManualDatabase( ActionScopeSet set , boolean all ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseDatabaseManualTarget( set , all );
		set.addTarget( action , target );
		return( target );
	}
	
	private ActionScopeTarget addReleaseDocDelivery( ActionScopeSet set , ReleaseDistScopeDelivery releaseDelivery , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDeliveryDocTarget( set , releaseDelivery.distDelivery , specifiedExplicitly , allItems );
		set.addTarget( action , target );
		
		if( allItems )
			target.addDocs( action , null );
		
		return( target );
	}
	
	private ActionScopeTarget addReleaseDatabaseDelivery( ActionScopeSet set , ReleaseDistScopeDelivery releaseDelivery , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDeliveryDatabaseTarget( set , releaseDelivery.distDelivery , specifiedExplicitly , allItems );
		set.addTarget( action , target );
		
		if( allItems )
			target.addDatabaseSchemes( action , null );
		
		return( target );
	}
	
	private ActionScopeTarget addReleaseProjectItems( ActionScopeSet set , ReleaseBuildScopeProject releaseProject , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( set, releaseProject , true );
		set.addTarget( action , target );
		target.addProjectItems( action , ITEMS );
		return( target );
	}

	private ActionScopeTarget addReleaseSourceProject( ActionScopeSet set , ReleaseBuildScopeProject releaseProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( set , releaseProject , specifiedExplicitly ); 
		set.addTarget( action , target );
		
		if( allItems )
			target.addProjectItems( action , null );
		
		return( target );
	}
		
	private void addReleaseDatabaseIndexScope( String DELIVERY , String[] INDEXES ) throws Exception {
		if( INDEXES.length == 0 )
			action.exit0( _Error.MissingDatabaseItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( INDEXES.length == 1 && INDEXES[0].equals( "all" ) )? true : false;
		
		DBEnumScopeCategoryType CATEGORY;
		if( DELIVERY == null ) {
			CATEGORY = DBEnumScopeCategoryType.MANUAL;
			ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , CATEGORY );
			if( sset == null )
				return;
			
			ActionScopeTarget target = addReleaseManualDatabase( sset , all );
			if( !all )
				target.addIndexItems( action , INDEXES );
		}
		else {
			CATEGORY = DBEnumScopeCategoryType.DB;
			ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , CATEGORY );
			if( sset == null )
				return;
			
			if( DELIVERY.equals( "all" ) ) {
				for( ReleaseDistScopeDelivery releaseDistScopeDelivery : sset.releaseDistScopeSet.getDeliveries() ) {
					ActionScopeTarget target = addReleaseDatabaseDelivery( sset , releaseDistScopeDelivery , all , false );
					if( !all )
						target.addIndexItems( action , INDEXES );
				}
			}
			else {
				ReleaseDistScopeDelivery releaseDistScopeDelivery = sset.releaseDistScopeSet.findDelivery( DELIVERY );
				if( releaseDistScopeDelivery == null )
					return;
							
				ActionScopeTarget target = addReleaseDatabaseDelivery( sset , releaseDistScopeDelivery , all , true );
				if( !all )
					target.addIndexItems( action , INDEXES );
			}
		}
	}
	
	private void addReleaseDatabaseSchemaScope( String DELIVERY , String[] SCHEMES ) throws Exception {
		if( SCHEMES.length == 0 )
			action.exit0( _Error.MissingDatabaseItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( SCHEMES.length == 1 && SCHEMES[0].equals( "all" ) )? true : false;
		
		DBEnumScopeCategoryType CATEGORY = DBEnumScopeCategoryType.DB;
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , CATEGORY );
		if( sset == null )
			return;
		
		if( DELIVERY.equals( "all" ) ) {
			for( ReleaseDistScopeDelivery releaseDistScopeDelivery : sset.releaseDistScopeSet.getDeliveries() ) {
				ActionScopeTarget target = addReleaseDatabaseDelivery( sset , releaseDistScopeDelivery , all , false );
				if( !all )
					target.addDatabaseSchemes( action , SCHEMES );
			}
		}
		else {
			ReleaseDistScopeDelivery releaseDistScopeDelivery = sset.releaseDistScopeSet.findDelivery( DELIVERY );
			if( releaseDistScopeDelivery == null )
				return;
						
			ActionScopeTarget target = addReleaseDatabaseDelivery( sset , releaseDistScopeDelivery , all , true );
			if( !all )
				target.addDatabaseSchemes( action , SCHEMES );
		}
	}
	
	private void addReleaseDocScope( String DELIVERY , String[] SCHEMES ) throws Exception {
		if( SCHEMES.length == 0 )
			action.exit0( _Error.MissingDocItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( SCHEMES.length == 1 && SCHEMES[0].equals( "all" ) )? true : false;
		
		DBEnumScopeCategoryType CATEGORY = DBEnumScopeCategoryType.DOC;
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , CATEGORY );
		if( sset == null )
			return;
		
		if( DELIVERY.equals( "all" ) ) {
			for( ReleaseDistScopeDelivery releaseDistScopeDelivery : sset.releaseDistScopeSet.getDeliveries() ) {
				ActionScopeTarget target = addReleaseDocDelivery( sset , releaseDistScopeDelivery , all , false );
				if( !all )
					target.addDocs( action , SCHEMES );
			}
		}
		else {
			ReleaseDistScopeDelivery releaseDistScopeDelivery = sset.releaseDistScopeSet.findDelivery( DELIVERY );
			if( releaseDistScopeDelivery == null )
				return;
						
			ActionScopeTarget target = addReleaseDocDelivery( sset , releaseDistScopeDelivery , all , true );
			if( !all )
				target.addDocs( action , SCHEMES );
		}
	}
	
	private ActionScopeTarget addReleaseProjectItemsScope( String PROJECT , String[] ITEMS ) throws Exception {
		ReleaseBuildScopeProject releaseProject = scope.releaseBuildScope.findProject( PROJECT );
		if( releaseProject == null ) {
			action.debug( "scope: ignore non-release project=" + PROJECT );
			return( null );
		}

		ActionScopeSet sset = scope.makeProjectScopeSet( action , releaseProject.project.set );
		ActionScopeTarget target = addReleaseProjectItems( sset , releaseProject , ITEMS );
		return( target );
	}
	
	private void addReleaseDistItemsScope( String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		MetaDistr distr = meta.getDistr();
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( itemName );
			if( item.sourceProjectItem == null )
				action.exit1( _Error.UnknownDistributiveItem1 ,"unknown distributive item=" + itemName , itemName );
			
			ActionScopeSet sset = null;
			if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.MANUAL )
				sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.MANUAL );
			else
			if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.DERIVED )
				sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DERIVED );
			else
			if( item.ITEMORIGIN_TYPE == DBEnumItemOriginType.BUILD )
				sset = scope.makeProjectScopeSet( action , item.sourceProjectItem.project.set );
			else
				Common.exitUnexpected();
			
			ReleaseBuildScopeProject rtarget = sset.releaseBuildScopeSet.findProject( item.sourceProjectItem.project );
			if( rtarget != null ) {
				ActionScopeTarget scopeProject = addReleaseSourceProject( sset , rtarget , false , true ); 
				scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
			}
		}
	}
	
	private void addFullRelease() throws Exception {
		addAllReleaseProjects();
		addAllReleaseConfigs();
		addAllReleaseDatabase();
		addAllReleaseDocs();
		addAllReleaseManualItems();
		addAllReleaseDerivedItems();
		scope.setFullRelease( action , true );
	}
	
	private void addReleaseSet( String SET , String[] TARGETS )	throws Exception {
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.CONFIG ) ) )
			addReleaseConfigs( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.MANUAL ) ) )
			addReleaseManualItems( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.DERIVED ) ) )
			addReleaseDerivedItems( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.DB ) ) )
			addReleaseDatabaseDeliveries( TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( DBEnumScopeCategoryType.DOC ) ) )
			addReleaseDocDeliveries( TARGETS );
		else {
			ReleaseBuildScopeSet rset = scope.releaseBuildScope.findSet( SET );
			if( rset != null )
				addReleaseSourceProjects( rset , TARGETS );
		}
	}

 	private void addReleaseManualItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.MANUAL );
		if( set != null )
			addReleaseManualItems( set , ITEMS );
 	}
	
 	private void addAllReleaseManualItems() throws Exception {
 		addReleaseManualItems( null );
 	}
	
 	private void addReleaseDerivedItems( String[] ITEMS ) throws Exception {
		ActionScopeSet set = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DERIVED );
		if( set != null )
			addReleaseDerivedItems( set , ITEMS );
 	}
	
 	private void addAllReleaseDerivedItems() throws Exception {
 		addReleaseDerivedItems( null );
 	}
	
	private void addAllReleaseProjects() throws Exception {
		for( ReleaseBuildScopeSet rset : scope.releaseBuildScope.getSets() ) {
			ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
			addReleaseSourceProjects( sset , null );
		}
	}
		
	private void addReleaseSourceProjects( ReleaseBuildScopeSet rset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseScopeSet( action , rset );
		addReleaseSourceProjects( sset , PROJECTS );
	}
		
	private void addReleaseConfigs( String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.CONFIG );
		if( sset != null )
			addReleaseConfigComps( sset , CONFCOMPS );
	}

	private void addAllReleaseConfigs() throws Exception {
		addReleaseConfigs( null );
	}
	
 	private void addAllReleaseDatabase() throws Exception {
		addReleaseDatabaseDeliveries( null );
	}

 	private void addAllReleaseDocs() throws Exception {
		addReleaseDocDeliveries( null );
	}

	private void addReleaseDatabaseDeliveries( String[] DELIVERIES ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DB );
		if( sset != null )
			addReleaseDatabaseDeliveries( sset , DELIVERIES );
	}

	private void addReleaseDocDeliveries( String[] DELIVERIES ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DOC );
		if( sset != null )
			addReleaseDocDeliveries( sset , DELIVERIES );
	}

	private void addReleaseDeliveryDatabaseSchemes( String DELIVERY , String[] SCHEMES ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DB );
		if( sset != null )
			addReleaseDeliveryDatabaseSchemes( sset , DELIVERY , SCHEMES );
	}

	private void addReleaseDeliveryDocs( String DELIVERY , String[] DOCS ) throws Exception {
		ActionScopeSet sset = scope.makeReleaseCategoryScopeSet( action , DBEnumScopeCategoryType.DOC );
		if( sset != null )
			addReleaseDeliveryDocs( sset , DELIVERY , DOCS );
	}
	
	private void addReleaseDatabaseDeliveries( ActionScopeSet set , String[] DELIVERIES ) throws Exception {
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseDistScopeDelivery item : set.releaseDistScopeSet.getDeliveries() )
				addReleaseDatabaseDelivery( set , item , true , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( key );
			addReleaseDatabaseDelivery( set , item , true , true );
		}
	}

	private void addReleaseDocDeliveries( ActionScopeSet set , String[] DELIVERIES ) throws Exception {
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseDistScopeDelivery item : set.releaseDistScopeSet.getDeliveries() )
				addReleaseDocDelivery( set , item , true , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( key );
			addReleaseDocDelivery( set , item , true , true );
		}
	}

	private ActionScopeTarget addReleaseSingleDeliveryTarget( ActionScopeSet set , ReleaseDistScopeDelivery releaseItem , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseDeliveryTarget( set , releaseItem , specifiedExplicitly );
		set.addTarget( action , target );
		return( target );
	}
	
	private void addReleaseConfigComps( ActionScopeSet set , String[] COMPS ) throws Exception {
		if( COMPS == null || COMPS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseDistScopeDelivery item : set.releaseDistScopeSet.getDeliveries() )
				addReleaseSingleDeliveryTarget( set , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( key );
			addReleaseSingleDeliveryTarget( set , item , true );
		}
	}

	private void addReleaseManualItems( ActionScopeSet set , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseDistScopeDelivery item : set.releaseDistScopeSet.getDeliveries() )
				addReleaseSingleDeliveryTarget( set , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( key );
			addReleaseSingleDeliveryTarget( set , item , true );
		}
	}

	private void addReleaseDerivedItems( ActionScopeSet set , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseDistScopeDelivery item : set.releaseDistScopeSet.getDeliveries() )
				addReleaseSingleDeliveryTarget( set , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( key );
			addReleaseSingleDeliveryTarget( set , item , true );
		}
	}

	private void addReleaseDeliveryDatabaseSchemes( ActionScopeSet set , String DELIVERY , String[] SCHEMES ) throws Exception {
		ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( DELIVERY );
		ActionScopeTarget target = addReleaseDatabaseDelivery( set , item , false , true );
		target.addDatabaseSchemes( action , SCHEMES );
	}
	
	private void addReleaseDeliveryDocs( ActionScopeSet set , String DELIVERY , String[] DOCS ) throws Exception {
		ReleaseDistScopeDelivery item = set.releaseDistScopeSet.findDelivery( DELIVERY );
		ActionScopeTarget target = addReleaseDocDelivery( set , item , false , true );
		target.addDocs( action , DOCS );
	}
	
	private void addReleaseSourceProjects( ActionScopeSet set , String[] PROJECTS ) throws Exception {
		if( PROJECTS == null || PROJECTS.length == 0 ) {
			set.setFullContent( true ); 
			for( ReleaseBuildScopeProject project : set.releaseBuildScopeSet.getProjects() )
				addReleaseSourceProject( set , project , true , false );
			return;
		}
		
		for( String name : PROJECTS ) {
			ReleaseBuildScopeProject sourceProject = set.releaseBuildScopeSet.findProject( name );
			addReleaseSourceProject( set , sourceProject , true , true );
		}
	}

}
