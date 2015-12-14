package ru.egov.urm.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.MetaReleaseSet;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaSourceProjectSet;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.storage.DistStorage;

public class ActionScope {

	public Metadata meta;
	public DistStorage release;
	public boolean releaseBound;

	private Map<VarCATEGORY,ActionScopeSet> categoryMap = new HashMap<VarCATEGORY,ActionScopeSet>();
	private Map<String,ActionScopeSet> sourceMap = new HashMap<String,ActionScopeSet>();
	private Map<String,ActionScopeSet> envMap = new HashMap<String,ActionScopeSet>();

	public boolean scopeFull;
	
	public ActionScope( Metadata meta , DistStorage release ) {
		this.meta = meta;
		this.release = release;
		
		releaseBound = ( release == null )? false : true;
	}
	
	public boolean isPartial() {
		return( !scopeFull );
	}

	public static ActionScope getProductCategoryScope( ActionBase action , VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		return( getProductSetScope( action , Common.getEnumLower( CATEGORY ) , TARGETS ) );
	}

	public static ActionScope getProductSetScope( ActionBase action , String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Product Set Scope, set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		ActionScope scope = new ActionScope( action.meta , null );
		
		if( set == null || set.isEmpty() )
			action.exit( "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit( "targets cannot be specified without set" );
			
			scope.createFullProduct( action );
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit( "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				scope.createProductSet( action , set , null );
			else
				scope.createProductSet( action , set , TARGETS );
		}
		
		return( scope );
	}

	public static ActionScope getDatabaseManualItemsScope( ActionBase action , DistStorage release , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Manual Database Scope, release=" + release.RELEASEDIR + ", items=" + Common.getListSet( INDEXES ) );
		ActionScope scope = new ActionScope( action.meta , release );
		scope.getDatabaseItemsScope( action , release , null , INDEXES );
		return( scope );
	}
	
	public static ActionScope getDatabaseDeliveryItemsScope( ActionBase action , DistStorage release , String DELIVERY , String[] INDEXES ) throws Exception {
		action.trace( "scope: Release Delivery Database Scope, release=" + release.RELEASEDIR + ", delivery=" + DELIVERY + ", items=" + Common.getListSet( INDEXES ) );
		ActionScope scope = new ActionScope( action.meta , release );
		scope.getDatabaseItemsScope( action , release , DELIVERY , INDEXES );
		return( scope );
	}
	
	public static ActionScope getReleaseCategoryScope( ActionBase action , DistStorage release , VarCATEGORY CATEGORY , String[] TARGETS ) throws Exception {
		return( getReleaseSetScope( action , release , Common.getEnumLower( CATEGORY ) , TARGETS ) ); 
	}
	
	public static ActionScope getReleaseSetScope( ActionBase action , DistStorage release , String set , String[] TARGETS ) throws Exception {
		action.trace( "scope: Release Set Scope, release=" + release.RELEASEDIR + ", set=" + set + ", targets=" + Common.getListSet( TARGETS ) );
		ActionScope scope = new ActionScope( action.meta , release );
		
		if( set == null || set.isEmpty() )
			action.exit( "missing set name (use \"all\" to reference all sets)" );
			
		if( set.equals( "all" ) ) {
			if( TARGETS.length != 0 )
				action.exit( "targets cannot be specified without set" );
			
			scope.createFullRelease( action , release );
		}
		else {
			if( TARGETS == null || TARGETS.length == 0 )
				action.exit( "missing targets (use \"all\" to reference all targets)" );

			if( TARGETS.length == 1 && TARGETS[0].equals( "all" ) )
				scope.createReleaseSet( action , release , set , null );
			else
				scope.createReleaseSet( action , release , set , TARGETS );
		}
		
		return( scope );
	}

	public static ActionScope getProductDistItemsScope( ActionBase action , String[] ITEMS ) throws Exception {
		action.trace( "scope: Product Dist Items Scope, items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action.meta , null );

		if( ITEMS == null || ITEMS.length == 0 )
			action.exit( "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			scope.createProductDistItemsScope( action , null , false );
		else
			scope.createProductDistItemsScope( action , ITEMS , true );
		return( scope );
	}

	public static ActionScope getReleaseDistItemsScope( ActionBase action , DistStorage dist , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Dist Items Scope, release=" + dist.RELEASEDIR + ", items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action.meta , null );

		if( ITEMS == null || ITEMS.length == 0 )
			action.exit( "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			scope.createReleaseDistItemsScope( action , dist , null , false );
		else
			scope.createReleaseDistItemsScope( action , dist , ITEMS , true );
		return( scope );
	}

	public static ActionScope getReleaseProjectItemsScope( ActionBase action , DistStorage release , String PROJECT , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = getReleaseProjectItemsScopeTarget( action , release , PROJECT , ITEMS );
		return( target.set.scope );
	}
	
	public static ActionScopeTarget getReleaseProjectItemsScopeTarget( ActionBase action , DistStorage release , String PROJECT , String[] ITEMS ) throws Exception {
		action.trace( "scope: Release Project Items Scope Target, release=" + release.RELEASEDIR + ", project=" + PROJECT + ", items=" + Common.getListSet( ITEMS ) );
		ActionScope scope = new ActionScope( action.meta , release );

		if( PROJECT == null || PROJECT.isEmpty() )
			action.exit( "missing project" );
		
		if( ITEMS == null || ITEMS.length == 0 )
			action.exit( "missing items (use \"all\" to reference all items)" );
		
		if( ITEMS.length == 1 && ITEMS[0].equals( "all" ) )
			return( scope.createReleaseProjectItemsScope( action , release , PROJECT , null ) );
			
		return( scope.createReleaseProjectItemsScope( action , release , PROJECT , ITEMS ) );
	}

	public static ActionScope getEnvServerNodesScope( ActionBase action , String SERVER , String[] NODES , DistStorage release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Server Nodes Scope, release=" + release.RELEASEDIR + ", server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		else
			action.trace( "scope: Env Server Nodes Scope, server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		
		ActionScope scope = new ActionScope( action.meta , null );

		if( action.meta.dc == null )
			action.exit( "datacenter is underfined" );
		
		if( SERVER == null || SERVER.isEmpty() )
			action.exit( "missing server" );
		
		if( NODES == null || NODES.length == 0 )
			action.exit( "missing items (use \"all\" to reference all items)" );
		
		if( NODES.length == 1 && NODES[0].equals( "all" ) )
			scope.createEnvServerNodesScope( action , action.meta.dc , SERVER , null , release );
		else
			scope.createEnvServerNodesScope( action , action.meta.dc , SERVER , NODES , release );
		return( scope );
	}

	public static ActionScopeTarget getEnvServerNodesScope( ActionBase action , MetaEnvServer srv , List<MetaEnvServerNode> nodes ) throws Exception {
		ActionScope scope = new ActionScope( action.meta , null );
		
		String nodeList = "";
		for( MetaEnvServerNode node : nodes )
			nodeList = Common.addToList( nodeList , "" + node.POS , " " );
			
		action.trace( "scope: Env Server Nodes Scope, server=" + srv.NAME + ", nodes=" + nodeList );
		return( scope.createEnvServerNodesScope( action , action.meta.dc , srv , nodes ) );
	}
	
	public static ActionScope getEnvScope( ActionBase action , DistStorage release ) throws Exception {
		String[] SERVERS = new String[1];
		SERVERS[0] = "all";
		return( getEnvServersScope( action , SERVERS , release ) );
	}
	
	public static ActionScope getEnvDatabaseScope( ActionBase action , DistStorage release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Database Scope, release=" + release.RELEASEDIR );
		else
			action.trace( "scope: Env Database Scope" );
		
		ActionScope scope = new ActionScope( action.meta , null );
		scope.createEnvDatabaseScope( action , release , action.meta.dc );
		return( scope );
	}
	
	public static ActionScope getEnvServersScope( ActionBase action , String[] SERVERS , DistStorage release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Servers Scope, release=" + release.RELEASEDIR + ", servers=" + Common.getListSet( SERVERS ) );
		else
			action.trace( "scope: Env Servers Scope, servers=" + Common.getListSet( SERVERS ) );
		
		ActionScope scope = new ActionScope( action.meta , null );

		if( SERVERS == null || SERVERS.length == 0 )
			action.exit( "missing items (use \"all\" to reference all items)" );
		
		if( SERVERS.length == 1 && SERVERS[0].equals( "all" ) ) {
			if( action.meta.dc == null )
				scope.createEnvScope( action , release );
			else
				scope.createEnvServersScope( action , action.meta.dc , null , release );
			return( scope );
		}
			
		if( action.meta.dc == null )
			action.exit( "datacenter is underfined" );
		
		scope.createEnvServersScope( action , action.meta.dc , SERVERS , release );
		return( scope );
	}

	private void getDatabaseItemsScope( ActionBase action , DistStorage release , String DELIVERY , String[] INDEXES ) throws Exception {
		VarCATEGORY CATEGORY;

		if( INDEXES.length == 0 )
			action.exit( "use \"all\" to reference all items" );
		
		boolean all = ( INDEXES.length == 1 && INDEXES[0].equals( "all" ) )? true : false;
		
		if( DELIVERY == null ) {
			CATEGORY = VarCATEGORY.MANUAL;
			ActionScopeSet sset = createReleaseCategoryScopeSet( action , release , CATEGORY );
			if( sset == null )
				return;
			
			ActionScopeTarget target = sset.addManualDatabase( action , all );
			if( !all )
				target.addIndexItems( action , INDEXES );
		}
		else {
			CATEGORY = VarCATEGORY.DB;
			ActionScopeSet sset = createReleaseCategoryScopeSet( action , release , CATEGORY );
			if( sset == null )
				return;
			
			if( DELIVERY.equals( "all" ) ) {
				for( MetaReleaseDelivery delivery : release.info.getDeliveries( action ).values() ) {
					ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , false , all );
					if( !all )
						target.addIndexItems( action , INDEXES );
				}
			}
			else {
				MetaReleaseDelivery delivery = release.info.getDelivery( action , DELIVERY );
				ActionScopeTarget target = sset.addDatabaseDelivery( action , delivery , true , all );
				if( !all )
					target.addIndexItems( action , INDEXES );
			}
		}
	}
	
	private void createEnvScope( ActionBase action , DistStorage release ) throws Exception {
		this.release = release;
		String dcMask = action.options.OPT_DCMASK;
		
		if( dcMask.isEmpty() )
			scopeFull = true;
		else
			scopeFull = false;
		
		for( MetaEnvDC dc : action.meta.env.getDCMap( action ).values() ) {
			if( dcMask.isEmpty() || dc.NAME.matches( dcMask ) ) {
				boolean specifiedExplicitly = ( dcMask.isEmpty() )? false : true;
				ActionScopeSet sset = createEnvScopeSet( action , action.meta.env , dc , specifiedExplicitly );
				sset.addEnvServers( action , null , release );
			}
		}
	}

	private void createEnvServersScope( ActionBase action , MetaEnvDC dc , String[] SERVERS , DistStorage release ) throws Exception {
		this.release = release;
		scopeFull = false;
		if( ( SERVERS == null || SERVERS.length == 0 ) && 
			dc.env.getDCMap( action ).size() == 1 )
			scopeFull = true;
			
		ActionScopeSet sset = createEnvScopeSet( action , action.meta.env , dc , true );
		sset.addEnvServers( action , SERVERS , release ); 
	}

	private void createEnvDatabaseScope( ActionBase action , DistStorage release , MetaEnvDC dc ) throws Exception {
		this.release = release;
		scopeFull = true;
		ActionScopeSet sset = createEnvScopeSet( action , action.meta.env , dc , false );
		sset.addEnvDatabases( action , release ); 
	}
	
	private ActionScopeTarget createEnvServerNodesScope( ActionBase action , MetaEnvDC dc , MetaEnvServer srv , List<MetaEnvServerNode> nodes ) throws Exception {
		scopeFull = false;
		ActionScopeSet sset = createEnvScopeSet( action , action.meta.env , dc , true );
		return( sset.addEnvServer( action , srv , nodes , true ) );
	}
	
	private void createEnvServerNodesScope( ActionBase action , MetaEnvDC dc , String SERVER , String[] NODES , DistStorage release ) throws Exception {
		this.release = release;
		
		scopeFull = false;
		ActionScopeSet sset = createEnvScopeSet( action , action.meta.env , dc , true );
		MetaEnvServer server = dc.getServer( action , SERVER );
		
		sset.addEnvServerNodes( action , server , NODES , true , release );
	}

	private ActionScopeSet createEnvScopeSet( ActionBase action , MetaEnv env , MetaEnvDC dc , boolean specifiedExplicitly ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , VarCATEGORY.ENV );
		if( sset != null )
			return( sset );
		
		sset = new ActionScopeSet( this , specifiedExplicitly );
		sset.create( action , env , dc );
		addScopeSet( action , sset );
		return( sset );
	}
	
	private ActionScopeTarget createReleaseProjectItemsScope( ActionBase action , DistStorage release , String PROJECT , String[] ITEMS ) throws Exception {
		scopeFull = false;
		
		MetaReleaseTarget releaseProject = release.info.findBuildProject( action , PROJECT );
		if( releaseProject == null ) {
			action.log( "ignore non-release project=" + PROJECT );
			return( null );
		}

		ActionScopeSet sset = createProjectScopeSet( action , releaseProject.sourceProject.set );
		ActionScopeTarget target = sset.addReleaseProjectItems( action , releaseProject , ITEMS );
		return( target );
	}
	
	private void createProductDistItemsScope( ActionBase action , String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		scopeFull = false;
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = meta.distr.getBinaryItem( action , itemName );
			if( item.sourceItem == null )
				action.exit( "unknown distributive item=" + itemName );
			
			ActionScopeSet sset = null;
			if( item.MANUAL )
				sset = createProductCategoryScopeSet( action , VarCATEGORY.MANUAL );
			else
				sset = createProjectScopeSet( action , item.sourceItem.project.set );
			
			ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceItem.project , false , true ); 
			scopeProject.addProjectItem( action , item.sourceItem , specifiedExplicitly );
		}
	}
	
	private void createReleaseDistItemsScope( ActionBase action , DistStorage dist , String ITEMS[] , boolean specifiedExplicitly ) throws Exception {
		scopeFull = false;
		for( String itemName : ITEMS ) {
			MetaDistrBinaryItem item = meta.distr.getBinaryItem( action , itemName );
			if( item.sourceItem == null )
				action.exit( "unknown distributive item=" + itemName );
			
			ActionScopeSet sset = null;
			if( item.MANUAL )
				sset = createReleaseCategoryScopeSet( action , dist , VarCATEGORY.MANUAL );
			else {
				MetaReleaseSet rset = release.info.getSourceSet( action , item.sourceItem.project.set.NAME );
				sset = createReleaseScopeSet( action , rset );
			}
			
			ActionScopeTarget scopeProject = sset.addSourceProject( action , item.sourceItem.project , false , true ); 
			scopeProject.addProjectItem( action , item.sourceItem , specifiedExplicitly );
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
			MetaSourceProjectSet pset = meta.sources.getProjectSet( action , set );  
			addSourceProjects( action , pset , TARGETS );
		}
	}
	
	private void createFullRelease( ActionBase action , DistStorage release )	throws Exception {
		scopeFull = true;
		addAllReleaseProjects( action , release );
		addAllReleaseConfigs( action , release );
		addAllReleaseDatabase( action , release );
		addAllReleaseManualItems( action , release );
	}
	
	private void createReleaseSet( ActionBase action , DistStorage release , String SET , String[] TARGETS )	throws Exception {
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
			MetaSourceProjectSet set = meta.sources.getProjectSet( action , SET );
			if( release.info.addSourceSet( action , set , false ) ) {
				MetaReleaseSet rset = release.info.getSourceSet( action , SET );  
				addReleaseProjects( action , release , rset , TARGETS );
			}
		}
	}

	private ActionScopeSet getCategorySet( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		return( categoryMap.get( CATEGORY ) );
	}
	
	private ActionScopeSet createReleaseCategoryScopeSet( ActionBase action , DistStorage release , VarCATEGORY CATEGORY ) throws Exception {
		ActionScopeSet sset = getCategorySet( action , CATEGORY );
		if( sset != null )
			return( sset );
		
		MetaReleaseSet rset = release.info.findCategorySet( action , CATEGORY );
		if( rset == null ) {
			action.log( "ignore non-release set=" + Common.getEnumLower( CATEGORY ) );
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
		if( meta.isSourceCategory( action , CATEGORY ) )
			return( sourceMap.get( name ) );
		if( CATEGORY == VarCATEGORY.ENV )
			return( envMap.get( name ) );
		return( categoryMap.get( CATEGORY ) );
	}

	private ActionScopeSet createReleaseScopeSet( ActionBase action , MetaReleaseSet rset ) throws Exception {
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
					if( set.CATEGORY == CATEGORY )
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
		return( getScopeInfo( action , meta.getAllBuildableCategories( action ) ) );
	}
	
	public String getSourceScopeInfo( ActionBase action ) throws Exception {
		return( getScopeInfo( action , meta.getAllSourceCategories( action ) ) );
	}
	
	public boolean isEmpty( ActionBase action , VarCATEGORY[] categories ) throws Exception {
		for( ActionScopeSet set : getSetList( action ) ) {
			if( categories == null ) {
				if( !set.isEmpty( action ) )
					return( false );
				continue;
			}
			
			for( VarCATEGORY CATEGORY : categories ) {
				if( set.CATEGORY == CATEGORY && !set.isEmpty( action ) )
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
			if( meta.isBuildableCategory( action , set.CATEGORY ) && !set.isEmpty( action ) )
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

 	private void addReleaseManualItems( ActionBase action , DistStorage release , String[] ITEMS ) throws Exception {
		ActionScopeSet set = createReleaseCategoryScopeSet( action , release , VarCATEGORY.MANUAL );
		if( set != null )
			set.addManualItems( action , ITEMS );
 	}
	
 	private void addAllReleaseManualItems( ActionBase action , DistStorage release ) throws Exception {
 		addReleaseManualItems( action , release , null );
 	}
	
	private void addAllSourceProjects( ActionBase action ) throws Exception {
		for( MetaSourceProjectSet pset : meta.sources.getSets( action ).values() ) {
			ActionScopeSet sset = createProjectScopeSet( action , pset );
			sset.addProjects( action , null );
		}
	}

	private void addSourceProjects( ActionBase action , MetaSourceProjectSet pset , String[] PROJECTS ) throws Exception {
		ActionScopeSet sset = createProjectScopeSet( action , pset );
		sset.addProjects( action , PROJECTS );
	}
		
	private void addAllReleaseProjects( ActionBase action , DistStorage release ) throws Exception {
		for( MetaReleaseSet rset : release.info.getSourceSets( action ).values() ) {
			ActionScopeSet sset = createReleaseScopeSet( action , rset );
			sset.addProjects( action , null );
		}
	}
		
	private void addReleaseProjects( ActionBase action , DistStorage release , MetaReleaseSet rset , String[] PROJECTS ) throws Exception {
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

	private void addReleaseConfigs( ActionBase action , DistStorage release , String[] CONFCOMPS ) throws Exception {
		ActionScopeSet sset = createReleaseCategoryScopeSet( action , release , VarCATEGORY.CONFIG );
		if( sset != null )
			sset.addConfigComps( action , CONFCOMPS );
	}

	private void addAllReleaseConfigs( ActionBase action , DistStorage release ) throws Exception {
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
	
 	private void addAllReleaseDatabase( ActionBase action , DistStorage release ) throws Exception {
		addReleaseDatabase( action , release , null );
	}

	private void addReleaseDatabase( ActionBase action , DistStorage release , String[] DBSETS ) throws Exception {
		ActionScopeSet sset = createReleaseCategoryScopeSet( action , release ,VarCATEGORY.DB );
		if( sset != null )
			sset.addDatabaseItems( action , DBSETS );
	}
	
	private void addScopeSet( ActionBase action , ActionScopeSet sset ) throws Exception {
		action.trace( "scope add set category=" + Common.getEnumLower( sset.CATEGORY ) + ", name=" + sset.NAME );
		
		if( meta.isSourceCategory( action , sset.CATEGORY ) )
			sourceMap.put( sset.NAME , sset );
		else
		if( sset.CATEGORY == VarCATEGORY.ENV )
			envMap.put( sset.NAME , sset );
		else
			categoryMap.put( sset.CATEGORY , sset );
	}
	
	public ActionScopeSet findSet( ActionBase action , VarCATEGORY CATEGORY , String NAME ) throws Exception {
		if( meta.isSourceCategory( action , CATEGORY ) )
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
