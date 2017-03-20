package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.action.CommandContext;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class ActionScope {

	public Meta meta;
	public CommandContext context;

	private Map<VarCATEGORY,ActionScopeSet> categoryMap = new HashMap<VarCATEGORY,ActionScopeSet>();
	private Map<String,ActionScopeSet> sourceMap = new HashMap<String,ActionScopeSet>();
	private Map<String,ActionScopeSet> envMap = new HashMap<String,ActionScopeSet>();

	public boolean scopeFull;
	
	private ActionScope( ActionBase action , Meta meta ) {
		this.meta = meta;
		this.context = action.context;
	}
	
	public boolean isPartial() {
		return( !scopeFull );
	}

	public static ActionScope getProductCategoryScope( ActionBase action , Meta meta , VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		return( getProductSetScope( action , meta , Common.getEnumLower( CATEGORY ) , TARGETS ) );
	}

	public static ActionScope getProductSetScope( ActionBase action , Meta meta , String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Product Set Scope, set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		ActionScope scope = new ActionScope( action , meta );
		
		if( set == null || set.isEmpty() )
			action.exit0( _Error.MissingSetName0 , "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit0( _Error.TargetsWithoutSet0 , "targets cannot be specified without set" );
			
			scope.createFullProduct( action );
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit0( _Error.MissingTargets0 , "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				scope.createProductSet( action , set , null );
			else
				scope.createProductSet( action , set , TARGETS );
		}
		
		return( scope );
	}

	public static ActionScope getReleaseDatabaseManualItemsScope( ActionBase action , Dist dist , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Manual Database Scope, release=" + dist.RELEASEDIR + ", items=" + Common.getListSet( INDEXES ) );
		return( getDatabaseItemsScope( action , dist.meta , dist , null , INDEXES ) );
	}
	
	public static ActionScope getReleaseDatabaseDeliveryItemsScope( ActionBase action , Dist dist , String DELIVERY , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Delivery Database Scope, release=" + dist.RELEASEDIR + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( INDEXES ) );
		return( getDatabaseItemsScope( action , dist.meta , dist , DELIVERY , INDEXES ) );
	}
	
	public static ActionScope getReleaseCategoryScope( ActionBase action , Dist dist , VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		return( getReleaseSetScope( action , dist , Common.getEnumLower( CATEGORY ) , TARGETS ) ); 
	}
	
	public static ActionScope getReleaseSetScope( ActionBase action , Dist dist , String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Release Set Scope, release=" + dist.RELEASEDIR + ", set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		ActionScope scope = new ActionScope( action , dist.meta );
		
		if( set == null || set.isEmpty() )
			action.exit0( _Error.MissingSetName0 , "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit0( _Error.TargetsWithoutSet0 , "targets cannot be specified without set" );
			
			scope.createFullRelease( action , dist );
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit0( _Error.MissingTargets0 , "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				scope.createReleaseSet( action , dist , set , null );
			else
				scope.createReleaseSet( action , dist , set , TARGETS );
		}
		
		return( scope );
	}

	public static ActionScope getProductDistItemsScope( ActionBase action , Meta meta , String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Dist Items Scope, items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action , meta );

		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			scope.createProductDistItemsScope( action , null , false );
		else
			scope.createProductDistItemsScope( action , ITEMS , true );
		return( scope );
	}

	public static ActionScope getReleaseDistItemsScope( ActionBase action , Dist dist , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Dist Items Scope, release=" + dist.RELEASEDIR + ", items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action , dist.meta );

		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingTargetItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			scope.createReleaseDistItemsScope( action , dist , null , false );
		else
			scope.createReleaseDistItemsScope( action , dist , ITEMS , true );
		return( scope );
	}

	public static ActionScope getReleaseProjectItemsScope( ActionBase action , Dist dist , String PROJECT , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = getReleaseProjectItemsScopeTarget( action , dist , PROJECT , ITEMS );
		return( target.set.scope );
	}
	
	public static ActionScopeTarget getReleaseProjectItemsScopeTarget( ActionBase action , Dist dist , String PROJECT , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Project Items Scope Target, release=" + dist.RELEASEDIR + ", project=" + PROJECT + ", items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action , dist.meta );

		if( PROJECT == null || PROJECT.isEmpty() )
			action.exit0( _Error.MissingProject0 , "missing project" );
		
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit0( _Error.MissingProjectItems0 , "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			return( scope.createReleaseProjectItemsScope( action , dist , PROJECT , null ) );
			
		return( scope.createReleaseProjectItemsScope( action , dist , PROJECT , ITEMS ) );
	}

	public static ActionScope getEnvServerNodesScope( ActionBase action , MetaEnvSegment sg , String SERVER , String[] NODES , Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Server Nodes Scope, release=" + dist.RELEASEDIR + ", server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		else
			action.trace( "scope: Env Server Nodes Scope, server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		
		ActionScope scope = new ActionScope( action , sg.meta );

		if( SERVER == null || SERVER.isEmpty() )
			action.exit0( _Error.MissingServer0 , "missing server" );
		
		if( NODES == null || NODES.length == 0 )
			action.exit0( _Error.MissingServerNodes0 , "missing items (use \"all\" to reference all items)" );
		
		if( NODES.length == 1 && NODES[0].equals( "all" ) )
			scope.createEnvServerNodesScope( action , sg , SERVER , null , dist );
		else
			scope.createEnvServerNodesScope( action , sg , SERVER , NODES , dist );
		return( scope );
	}

	public static ActionScopeTarget getEnvServerNodesScope( ActionBase action , MetaEnvServer srv , List<MetaEnvServerNode> nodes ) throws Exception {
		ActionScope scope = new ActionScope( action , srv.meta );
		
		String nodeList = "";
		for( MetaEnvServerNode node : nodes )
			nodeList = Common.addToList( nodeList , "" + node.POS , " " );
			
		action.trace( "scope: Env Server Nodes Scope, server=" + srv.NAME + ", nodes=" + nodeList );
		return( scope.createEnvServerNodesScope( action , srv.sg , srv , nodes ) );
	}
	
	public static ActionScope getEnvScope( ActionBase action , MetaEnv env , MetaEnvSegment sg , Dist dist ) throws Exception {
		ActionScope scope = new ActionScope( action , env.meta );
		scope.createEnvScope( action , env , sg , dist );
		return( scope );
	}
	
	public static ActionScope getEnvDatabaseScope( ActionBase action , Meta meta , Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Database Scope, release=" + dist.RELEASEDIR );
		else
			action.trace( "scope: Env Database Scope" );
		
		ActionScope scope = new ActionScope( action , meta );
		scope.createEnvDatabaseScope( action , dist );
		return( scope );
	}
	
	public static ActionScope getEnvServersScope( ActionBase action , Meta meta , MetaEnvSegment sg , String[] SERVERS , Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Servers Scope, release=" + dist.RELEASEDIR + ", servers=" + Common.getListSet( SERVERS ) );
		else
			action.trace( "scope: Env Servers Scope, servers=" + Common.getListSet( SERVERS ) );
		
		ActionScope scope = new ActionScope( action , meta );

		if( SERVERS == null || SERVERS.length == 0 )
			action.exit0( _Error.MissingServers0 , "missing items (use \"all\" to reference all items)" );
		
		if( SERVERS.length == 1 && SERVERS[0].equals( "all" ) ) {
			if( sg == null )
				scope.createEnvScope( action , action.context.env , sg , dist );
			else
				scope.createEnvServersScope( action , sg , null , dist );
			return( scope );
		}
			
		if( sg == null )
			action.exit0( _Error.SegmentUndefined0 , "segment is undefined" );
		
		scope.createEnvServersScope( action , sg , SERVERS , dist );
		return( scope );
	}

	private static ActionScope getDatabaseItemsScope( ActionBase action , Meta meta , Dist dist , String DELIVERY , String[] INDEXES ) throws Exception {
		ActionScope scope = new ActionScope( action , meta );
		
		VarCATEGORY CATEGORY;

		if( INDEXES.length == 0 )
			action.exit0( _Error.MissingDatabaseItems0 , "use \"all\" to reference all items" );
		
		boolean all = ( INDEXES.length == 1 && INDEXES[0].equals( "all" ) )? true : false;
		
		if( DELIVERY == null ) {
			CATEGORY = VarCATEGORY.MANUAL;
			ActionScopeSet sset = scope.createReleaseCategoryScopeSet( action , dist , CATEGORY );
			if( sset == null )
				return( scope );
			
			ActionScopeTarget target = sset.addManualDatabase( action , all );
			if( !all )
				target.addIndexItems( action , INDEXES );
		}
		else {
			CATEGORY = VarCATEGORY.DB;
			ActionScopeSet sset = scope.createReleaseCategoryScopeSet( action , dist , CATEGORY );
			if( sset == null )
				return( scope );
			
			if( DELIVERY.equals( "all" ) ) {
				for( ReleaseDelivery delivery : dist.release.getDeliveries( action ).values() ) {
					ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , false , all );
					if( !all )
						target.addIndexItems( action , INDEXES );
				}
			}
			else {
				ReleaseDelivery delivery = dist.release.getDelivery( action , DELIVERY );
				ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , true , all );
				if( !all )
					target.addIndexItems( action , INDEXES );
			}
		}
		
		return( scope );
	}
	
	private void createEnvScope( ActionBase action , MetaEnv env , MetaEnvSegment sg , Dist dist ) throws Exception {
		String sgMask = null;
		if( sg != null )
			sgMask = sg.NAME;
		else
			sgMask = action.context.CTX_SEGMENT;
		
		if( sgMask.isEmpty() )
			scopeFull = true;
		else
			scopeFull = false;
		
		for( MetaEnvSegment sgItem : env.getSegments() ) {
			if( sgMask.isEmpty() || sgItem.NAME.matches( sgMask ) ) {
				boolean specifiedExplicitly = ( sgMask.isEmpty() )? false : true;
				ActionScopeSet sset = createEnvScopeSet( action , sgItem.env , sgItem , specifiedExplicitly );
				sset.addEnvServers( action , null , dist );
			}
		}
	}

	private void createEnvServersScope( ActionBase action , MetaEnvSegment sg , String[] SERVERS , Dist dist ) throws Exception {
		scopeFull = false;
		if( ( SERVERS == null || SERVERS.length == 0 ) && 
			sg.env.getSegmentNames().length == 1 )
			scopeFull = true;
			
		ActionScopeSet sset = createEnvScopeSet( action , context.env , sg , true );
		sset.addEnvServers( action , SERVERS , dist ); 
	}

	private void createEnvDatabaseScope( ActionBase action , Dist dist ) throws Exception {
		scopeFull = true;
		for( MetaEnvSegment sg : context.env.getSegments() ) {
			if( !sg.hasDatabaseServers( action ) )
				continue;
			
			ActionScopeSet sset = createEnvScopeSet( action , context.env , sg , false );
			sset.addEnvDatabases( action , dist );
		}
	}
	
	private ActionScopeTarget createEnvServerNodesScope( ActionBase action , MetaEnvSegment sg , MetaEnvServer srv , List<MetaEnvServerNode> nodes ) throws Exception {
		scopeFull = false;
		ActionScopeSet sset = createEnvScopeSet( action , context.env , sg , true );
		return( sset.addEnvServer( action , srv , nodes , true ) );
	}
	
	private void createEnvServerNodesScope( ActionBase action , MetaEnvSegment sg , String SERVER , String[] NODES , Dist dist ) throws Exception {
		scopeFull = false;
		ActionScopeSet sset = createEnvScopeSet( action , context.env , sg , true );
		MetaEnvServer server = sg.getServer( action , SERVER );
		
		sset.addEnvServerNodes( action , server , NODES , true , dist );
	}

	private ActionScopeSet createEnvScopeSet( ActionBase action , MetaEnv env , MetaEnvSegment sg , boolean specifiedExplicitly ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , VarCATEGORY.ENV );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , specifiedExplicitly );
		sset.create( action , env , sg );
		addScopeSet( action , sset );
		return( sset );
	}
	
	private ActionScopeTarget createReleaseProjectItemsScope( ActionBase action , Dist dist , String PROJECT , String[] ITEMS ) throws Exception {
		scopeFull = false;
		
		ReleaseTarget releaseProject = dist.release.findBuildProject( action , PROJECT );
		if( releaseProject == null ) {
			action.debug( "scope: ignore non-release project=" + PROJECT );
			return( null );
		}

		ActionScopeSet sset = createProjectScopeSet( action , releaseProject.sourceProject.set );
		ActionScopeTarget target = sset.addReleaseProjectItems( action , releaseProject , ITEMS );
		return( target );
	}
	
	private void createProductDistItemsScope( ActionBase action , String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		scopeFull = false;
		MetaDistr distr = meta.getDistr( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( action , itemName );
			if( item == null )
				action.exit1( _Error.UnknownDistributiveItem1 , "unknown distributive item=" + itemName , itemName );
			
			ActionScopeSet sset = null;
			if( item.distItemOrigin == VarDISTITEMORIGIN.MANUAL ) {
				sset = createProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
				sset.addManualItems( action , new String[] { itemName } );
			}
			else {
				sset = createProjectScopeSet( action , item.sourceProjectItem.project.set );
			
				ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceProjectItem.project , false , true ); 
				scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
			}
		}
	}
	
	private void createReleaseDistItemsScope( ActionBase action , Dist dist , String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		scopeFull = false;
		MetaDistr distr = meta.getDistr( action );
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = distr.getBinaryItem( action , itemName );
			if( item.sourceProjectItem == null )
				action.exit1( _Error.UnknownDistributiveItem1 ,"unknown distributive item=" + itemName , itemName );
			
			ActionScopeSet sset = null;
			if( item.distItemOrigin == VarDISTITEMORIGIN.MANUAL )
				sset = createReleaseCategoryScopeSet( action , dist , VarCATEGORY.MANUAL );
			else {
				ReleaseSet rset = dist.release.getSourceSet( action , item.sourceProjectItem.project.set.NAME );
				sset = createReleaseScopeSet( action , rset );
			}
			
			ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceProjectItem.project , false , true ); 
			scopeProject.addProjectItem( action , item.sourceProjectItem , specifiedExplicitly );
		}
	}
	
	private void createFullProduct( ActionBase action ) throws Exception {
		scopeFull = true;
		addAllSourceProjects( action );
		addAllProductConfigs( action );
		addAllProductDatabase( action );
		addAllManualItems( action );
	}
	
	private void createProductSet( ActionBase action , String set , String[] TARGETS ) throws Exception {
		scopeFull = false;
		if( set.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) )
			addProductConfigs( action , TARGETS );
		else 
		if( set.equals( Common.getEnumLower( VarCATEGORY.DB ) ) )
			addProductDatabase( action , TARGETS );
		else 
		if( set.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) )
			addManualItems( action , TARGETS );
		else {
			MetaSource sources = meta.getSources( action );
			MetaSourceProjectSet pset = sources.getProjectSet( action , set );  
			addSourceProjects( action , pset , TARGETS );
		}
	}
	
	private void createFullRelease( ActionBase action , Dist release )	throws Exception {
		scopeFull = true;
		addAllReleaseProjects( action , release );
		addAllReleaseConfigs( action , release );
		addAllReleaseDatabase( action , release );
		addAllReleaseManualItems( action , release );
	}
	
	private void createReleaseSet( ActionBase action , Dist release , String SET , String[] TARGETS )	throws Exception {
		scopeFull = false;
		if( SET.equals( Common.getEnumLower( VarCATEGORY.CONFIG ) ) )
			addReleaseConfigs( action , release , TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( VarCATEGORY.DB ) ) )
			addReleaseDatabase( action , release , TARGETS );
		else 
		if( SET.equals( Common.getEnumLower( VarCATEGORY.MANUAL ) ) )
			addReleaseManualItems( action , release , TARGETS );
		else {
			MetaSource sources = meta.getSources( action );
			MetaSourceProjectSet set = sources.getProjectSet( action , SET );
			if( release.release.addSourceSet( action , set , false ) ) {
				ReleaseSet rset = release.release.getSourceSet( action , SET );  
				addReleaseProjects( action , release , rset , TARGETS );
			}
		}
	}

	private ActionScopeSet getCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		return( categoryMap.get( CATEGORY ) );
	}
	
	private ActionScopeSet createReleaseCategoryScopeSet( ActionBase action , Dist release , VarCATEGORY CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset != null )
			return( sset );
		
		ReleaseSet rset = release.release.findCategorySet( action , CATEGORY );
		if( rset == null ) {
			action.debug( "ignore non-release set=" + Common.getEnumLower( CATEGORY ) );
			return( null );
		}
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , rset );
		action.trace( "add scope set category=" + Common.getEnumLower( CATEGORY ) );
		addScopeSet( action , sset );
		return( sset );
	}
	
	private ActionScopeSet createProductCategoryScopeSet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , CATEGORY );
		addScopeSet( action , sset );
		return( sset );
	}
	
	private ActionScopeSet createProjectScopeSet( ActionBase action , MetaSourceProjectSet pset ) throws Exception {
		ActionScopeSet sset = sourceMap.get( pset.NAME );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , true );
		sset.create( action , pset );
		addScopeSet( action , sset );
		return( sset );
	}

	private ActionScopeSet getScopeSet( ActionBase action , VarCATEGORY CATEGORY , String name ) throws Exception {
		if( Meta.isSourceCategory( CATEGORY ) )
			return( sourceMap.get( name ) );
		if( CATEGORY == VarCATEGORY.ENV )
			return( envMap.get( name ) );
		return( categoryMap.get( CATEGORY ) );
	}

	private ActionScopeSet createReleaseScopeSet( ActionBase action , ReleaseSet rset ) throws Exception {
		ActionScopeSet sset = getScopeSet( action , rset.CATEGORY , rset.NAME );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , false );
		sset.create( action , rset );
		addScopeSet( action , sset );
		return( sset );
	}

	public boolean hasCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset == null || sset.isEmpty( action ) )
			return( false );
		
		return( true );
	}
	
	public boolean hasConfig( ActionBase action ) throws Exception {
		return( hasCategorySet( action , VarCATEGORY.CONFIG ) );
	}
	
	public boolean hasDatabase( ActionBase action ) throws Exception {
		return( hasCategorySet( action , VarCATEGORY.DB ) );
	}

	public boolean hasManual( ActionBase action ) throws Exception {
		return( hasCategorySet( action , VarCATEGORY.MANUAL ) );
	}

	public List<ActionScopeSet> getSetList( ActionBase action ) throws Exception {
		List<ActionScopeSet> list = new LinkedList<ActionScopeSet>();
		list.addAll( sourceMap.values() );
		list.addAll( categoryMap.values() );
		list.addAll( envMap.values() );
		return( list );
	}
	
	public String getScopeInfo( ActionBase action , VarCATEGORY[] categories ) throws Exception {
		String scope = "";
		
		boolean all = true;
		for( ActionScopeSet set : getSetList( action ) ) {
			boolean add = true;
			if( categories != null ) {
				add = false;
				for( VarCATEGORY CATEGORY : categories ) {
					if( Meta.checkCategoryProperty( set.CATEGORY , CATEGORY ) )
						add = true;
				}
			}
			
			if( add )
				scope = Common.concat( scope , set.getScopeInfo( action ) , "; " );
			else
				all = false;
		}

		if( all && scopeFull )
			return( "all" );
		
		if( scope.isEmpty() )
			return( "nothing" );
		return( scope );
	}
	
	public String getScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , null ) );
	}
	
	public String getBuildScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , new VarCATEGORY[] { VarCATEGORY.BUILDABLE } ) );
	}
	
	public String getSourceScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , Meta.getAllSourceCategories() ) );
	}
	
	public boolean isEmpty( ActionBase action , VarCATEGORY[] categories ) throws Exception {
		for( ActionScopeSet set : getSetList( action ) ) {
			if( categories == null ) {
				if( !set.isEmpty( action ) )
					return( false );
				continue;
			}
			
			for( VarCATEGORY CATEGORY : categories ) {
				if( Meta.checkCategoryProperty( set.CATEGORY , CATEGORY ) && !set.isEmpty( action ) )
					return( false );
			}
		}
		
		return( true );
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		for( ActionScopeSet set : getSetList( action ) ) {
			if( !set.isEmpty( action ) )
				return( false );
		}
		
		return( true );
	}
	
	public ActionScopeSet[] getSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : getSetList( action ) ) {
			if( !set.isEmpty( action ) )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getSourceSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : sourceMap.values() ) {
			if( !set.isEmpty( action ) )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getCategorySets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : categoryMap.values() ) {
			if( !set.isEmpty( action ) )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getEnvSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : envMap.values() ) {
			if( !set.isEmpty( action ) )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}

	public ActionScopeSet[] getBuildableSets( ActionBase action ) throws Exception {
		List<ActionScopeSet> x = new LinkedList<ActionScopeSet>();
		for( ActionScopeSet set : sourceMap.values() ) {
			if( set.CATEGORY == VarCATEGORY.PROJECT && !set.isEmpty( action ) )
				x.add( set );
		}
		return( x.toArray( new ActionScopeSet[0] ) );
	}
	
	public Map<String,ActionScopeTarget> getCategorySetTargets( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		ActionScopeSet set = getCategorySet( action , CATEGORY );
		if( set == null )
			return( new HashMap<String,ActionScopeTarget>() );
		return( set.targets );
	}
	
	// implementation
	private void addAllManualItems( ActionBase action ) throws Exception {
		ActionScopeSet set = createProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
		set.addManualItems( action , null );
	}

 	private void addReleaseManualItems( ActionBase action , Dist release , String[] ITEMS ) throws Exception {
		ActionScopeSet set = createReleaseCategoryScopeSet( action , release , VarCATEGORY.MANUAL );
		if( set != null )
			set.addManualItems( action , ITEMS );
 	}
	
 	private void addAllReleaseManualItems( ActionBase action , Dist release ) throws Exception {
 		addReleaseManualItems( action , release , null );
 	}
	
	private void addAllSourceProjects( ActionBase action ) throws Exception {
		MetaSource sources = meta.getSources( action );
		for( MetaSourceProjectSet pset : sources.getSets() ) {
			ActionScopeSet sset = createProjectScopeSet( action , pset );
			sset.addProjects( action , null );
		}
	}

	private void addSourceProjects( ActionBase action , MetaSourceProjectSet pset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = createProjectScopeSet( action , pset );
		sset.addProjects( action , PROJECTS );
	}
		
	private void addAllReleaseProjects( ActionBase action , Dist release ) throws Exception {
		for( ReleaseSet rset : release.release.getSourceSets( action ).values() ) {
			ActionScopeSet sset = createReleaseScopeSet( action , rset );
			sset.addProjects( action , null );
		}
	}
		
	private void addReleaseProjects( ActionBase action , Dist release , ReleaseSet rset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = createReleaseScopeSet( action , rset );
		sset.addProjects( action , PROJECTS );
	}
		
	private void addAllProductConfigs( ActionBase action ) throws Exception {
		addProductConfigs( action , null );
	}
	
	private void addProductConfigs( ActionBase action , String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = createProductCategoryScopeSet( action , VarCATEGORY.CONFIG );
		sset.addConfigComps( action , CONFCOMPS );
	}

	private void addReleaseConfigs( ActionBase action , Dist release , String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = createReleaseCategoryScopeSet( action , release , VarCATEGORY.CONFIG );
		if( sset != null )
			sset.addConfigComps( action , CONFCOMPS );
	}

	private void addAllReleaseConfigs( ActionBase action , Dist release ) throws Exception {
		addReleaseConfigs( action , release , null );
	}
	
	private void addAllProductDatabase( ActionBase action ) throws Exception {
		addProductDatabase( action , null );
	}
	
	private void addProductDatabase( ActionBase action , String[] DBSETS ) throws Exception {
		ActionScopeSet set = createProductCategoryScopeSet( action , VarCATEGORY.DB );
		set.addDatabaseItems( action , DBSETS );
	}

	private void addManualItems( ActionBase action , String[] DISTITEMS ) throws Exception {
		ActionScopeSet sset = createProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
		sset.addManualItems( action , DISTITEMS );
	}
	
 	private void addAllReleaseDatabase( ActionBase action , Dist release ) throws Exception {
		addReleaseDatabase( action , release , null );
	}

	private void addReleaseDatabase( ActionBase action , Dist release , String[] DBSETS ) throws Exception {
		ActionScopeSet sset = createReleaseCategoryScopeSet( action , release ,VarCATEGORY.DB );
		if( sset != null )
			sset.addDatabaseItems( action , DBSETS );
	}
	
	private void addScopeSet( ActionBase action , ActionScopeSet sset ) throws Exception {
		action.trace( "scope: scope add set category=" + Common.getEnumLower( sset.CATEGORY ) + ", name=" + sset.NAME );
		
		if( Meta.isSourceCategory( sset.CATEGORY ) )
			sourceMap.put( sset.NAME , sset );
		else
		if( sset.CATEGORY == VarCATEGORY.ENV )
			envMap.put( sset.NAME , sset );
		else
			categoryMap.put( sset.CATEGORY , sset );
	}
	
	public ActionScopeSet findSet( ActionBase action , VarCATEGORY CATEGORY , String NAME ) throws Exception {
		if( Meta.isSourceCategory( CATEGORY ) )
			return( sourceMap.get( NAME ) );
		if( CATEGORY == VarCATEGORY.ENV )
			return( envMap.get( NAME ) );
		return( categoryMap.get( CATEGORY ) );
	}

	public static String getList( List<ActionScopeTarget> list ) {
		String s = "";
		for( ActionScopeTarget target : list )
			s = Common.addToList( s , target.NAME , "," );
		return( s );
	}
}
