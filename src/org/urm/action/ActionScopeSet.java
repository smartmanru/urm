package org.urm.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.Release;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseSet;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.engine.shell.Account;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaEnvStartGroup;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.Meta.VarCATEGORY;
import org.urm.meta.product.Meta.VarDISTITEMORIGIN;

public class ActionScopeSet {

	ActionScope scope;
	public Meta meta;
	public String NAME;
	public VarCATEGORY CATEGORY;
	public boolean setFull;
	
	public MetaSourceProjectSet pset;
	public ReleaseSet rset;
	
	Map<String,ActionScopeTarget> targets = new HashMap<String,ActionScopeTarget>();
	ActionScopeTarget manualTarget;

	public MetaEnv env;
	public MetaEnvSegment dc;
	boolean specifiedExplicitly;
	
	public ActionScopeSet( ActionScope scope , boolean specifiedExplicitly ) {
		this.scope = scope;
		this.meta = scope.meta;
		this.specifiedExplicitly = specifiedExplicitly;
	}
	
	public boolean isEmpty( ActionBase action ) throws Exception {
		// manual files are by default
		if( CATEGORY == VarCATEGORY.MANUAL && setFull )
			return( false );
		
		return( targets.isEmpty() );
	}
	
	public Map<String,ActionScopeTarget> getTargets( ActionBase action ) throws Exception {
		return( targets );
	}
	
	public void create( ActionBase action , MetaSourceProjectSet pset ) throws Exception {
		this.pset = pset;
		this.NAME = pset.NAME;
		this.CATEGORY = pset.CATEGORY;
		this.setFull = false;
	}

	public void create( ActionBase action , ReleaseSet rset ) throws Exception {
		this.rset = rset;
		this.pset = rset.set;
		this.NAME = rset.NAME;
		this.CATEGORY = rset.CATEGORY;
		this.setFull = false;
	}

	public void create( ActionBase action , VarCATEGORY CATEGORY ) throws Exception {
		this.pset = null;
		this.NAME = Common.getEnumLower( CATEGORY );
		this.CATEGORY = CATEGORY;
		this.setFull = false;
	}

	public void create( ActionBase action , MetaEnv env , MetaEnvSegment dc ) throws Exception {
		this.pset = null;
		this.NAME = dc.NAME;
		this.CATEGORY = VarCATEGORY.ENV;
		this.setFull = false;
		this.env = env;
		this.dc = dc;
	}

	private void addTarget( ActionBase action , ActionScopeTarget target ) throws Exception {
		action.trace( "scope: add target=" + target.NAME );
		targets.put( target.NAME , target );
	}
	
	public ActionScopeTarget addSourceProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createSourceProjectTarget( this , sourceProject , specifiedExplicitly ); 
		addTarget( action , target );
		
		if( allItems )
			target.addProjectItems( action , null );
		
		return( target );
	}
		
	public ActionScopeTarget addReleaseProject( ActionBase action , ReleaseTarget releaseProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( this , releaseProject , specifiedExplicitly ); 
		addTarget( action , target );
		
		if( allItems )
			target.addProjectItems( action , null );
		
		return( target );
	}
		
	public ActionScopeTarget addManualDatabase( ActionBase action , boolean all ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDatabaseManualTarget( this , all );
		addTarget( action , target );
		return( target );
	}
	
	public ActionScopeTarget addDatabaseDelivery( ActionBase action , ReleaseDelivery delivery , boolean specifiedExplicitly , boolean all ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDatabaseDeliveryTarget( this , delivery.distDelivery , specifiedExplicitly , all );
		addTarget( action , target );
		return( target );
	}
	
	public ActionScopeTarget addReleaseProjectItems( ActionBase action , ReleaseTarget releaseProject , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( this, releaseProject , true );
		addTarget( action , target );
		target.addProjectItems( action , ITEMS );
		return( target );
	}

	public ActionScopeTarget findSourceTarget( ActionBase action , MetaSourceProject project ) throws Exception {
		return( targets.get( project.PROJECT ) );
	}
	
	public String getScopeInfo( ActionBase action ) throws Exception {
		if( targets.isEmpty() ) {
			if( CATEGORY == VarCATEGORY.MANUAL && setFull )
				return( "manual files" );
			return( "" );
		}
		
		String scope = NAME + "={";
		if( setFull ) {
			scope += "all}";
			return( scope );
		}

		String itemlist = "";
		if( CATEGORY == VarCATEGORY.MANUAL )
			itemlist = "manual files";
		for( ActionScopeTarget scopeTarget : targets.values() )
			itemlist = Common.concat( itemlist , scopeTarget.getScopeInfo( action ) , ", " );
		
		scope += itemlist + "}";
		return( scope );
	}

	public void addProjects( ActionBase action , String[] PROJECTS ) throws Exception {
		if( rset != null )
			addReleaseProjects( action , PROJECTS );
		else
			addSourceProjects( action , PROJECTS );
	}
	
	private void addSourceProjects( ActionBase action , String[] PROJECTS ) throws Exception {
		if( PROJECTS == null || PROJECTS.length == 0 ) {
			setFull = true; 
			for( MetaSourceProject project : pset.getProjects( action ).values() )
				addSourceProject( action , project , true , false );
			return;
		}
		
		MetaSource sources = meta.getSources( action );
		for( String name : PROJECTS ) {
			MetaSourceProject sourceProject = sources.getProject( action , name );
			addSourceProject( action , sourceProject , true , true );
		}
	}

	private void addReleaseProjects( ActionBase action , String[] PROJECTS ) throws Exception {
		if( PROJECTS == null || PROJECTS.length == 0 ) {
			setFull = true; 
			for( ReleaseTarget project : rset.getTargets( action ).values() )
				addReleaseProject( action , project , true , false );
			return;
		}
		
		for( String name : PROJECTS ) {
			ReleaseTarget sourceProject = rset.getTarget( action ,  name );
			addReleaseProject( action , sourceProject , true , true );
		}
	}

	public void addConfigComps( ActionBase action , String[] COMPS ) throws Exception {
		if( rset != null )
			addReleaseConfigComps( action , COMPS );
		else
			addProductConfigComps( action , COMPS );
	}
	
	private void addProductConfigComps( ActionBase action , String[] COMPS ) throws Exception {
		MetaDistr distr = meta.getDistr( action );
		if( COMPS == null || COMPS.length == 0 ) {
			setFull = true; 
			for( MetaDistrConfItem item : distr.getConfItems() )
				addProductConfig( action , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			MetaDistrConfItem comp = distr.getConfItem( action , key );
			addProductConfig( action , comp , true );
		}
	}

	private void addProductConfig( ActionBase action , MetaDistrConfItem distrComp , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createConfItemTarget( this , distrComp , specifiedExplicitly );
		addTarget( action , target );
	}

	private void addReleaseConfigComps( ActionBase action , String[] COMPS ) throws Exception {
		if( COMPS == null || COMPS.length == 0 ) {
			setFull = true; 
			for( ReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			ReleaseTarget item = rset.getTarget( action , key );
			addReleaseTarget( action , item , true );
		}
	}

	public void addManualItems( ActionBase action , String[] COMPS ) throws Exception {
		if( rset != null )
			addReleaseManualItems( action , COMPS );
		else
			addProductManualItems( action , COMPS );
	}
	
	private void addProductManualItems( ActionBase action , String[] ITEMS ) throws Exception {
		MetaDistr distr = meta.getDistr( action );
		if( ITEMS == null || ITEMS.length == 0 ) {
			setFull = true; 
			for( MetaDistrBinaryItem item : distr.getBinaryItems() )
				addProductManualItem( action , item , false );
			return;
		}
		
		for( String item : ITEMS ) {
			MetaDistrBinaryItem distitem = distr.getBinaryItem( action , item );
			if( distitem.distItemOrigin != VarDISTITEMORIGIN.MANUAL )
				action.exit1( _Error.UnexpectedNonManualItem1 , "unexpected non-manual item=" + item , item );
			
			addProductManualItem( action , distitem , true );
		}
	}

	private void addProductManualItem( ActionBase action , MetaDistrBinaryItem item , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createManualDistItemTarget( this , item , specifiedExplicitly );
		addTarget( action , target );
	}
	
	private void addReleaseManualItems( ActionBase action , String[] ITEMS ) throws Exception {
		if( ITEMS == null || ITEMS.length == 0 ) {
			setFull = true; 
			for( ReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			ReleaseTarget item = rset.getTarget( action , key );
			addReleaseTarget( action , item , true );
		}
	}

	private void addReleaseTarget( ActionBase action , ReleaseTarget releaseItem , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( this , releaseItem , specifiedExplicitly );
		addTarget( action , target );
	}

	public void addDatabaseItems( ActionBase action , String[] DELIVERIES ) throws Exception {
		if( rset != null )
			addReleaseDatabaseItems( action , DELIVERIES );
		else
			addProductDatabaseItems( action , DELIVERIES );
	}
	
	private void addProductDatabaseItems( ActionBase action , String[] DELIVERIES ) throws Exception {
		MetaDistr distr = meta.getDistr( action );
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			setFull = true; 
			for( MetaDistrDelivery item : distr.getDatabaseDeliveries() )
				addProductDatabase( action , item , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			MetaDistrDelivery item = distr.getDelivery( action , key );
			if( item.hasDatabaseItems() )
				addProductDatabase( action , item , true );
		}
	}

	private void addProductDatabase( ActionBase action , MetaDistrDelivery dbitem , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDatabaseDeliveryTarget( this , dbitem , specifiedExplicitly , true );
		addTarget( action , target );
	}

	private void addReleaseDatabaseItems( ActionBase action , String[] DELIVERIES ) throws Exception {
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			setFull = true; 
			for( ReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			ReleaseTarget item = rset.getTarget( action , key );
			addReleaseTarget( action , item , true );
		}
	}

	private boolean checkServerDatabaseDelivery( ActionBase action , MetaEnvServer server , ReleaseDelivery delivery ) throws Exception {
		return( server.hasDatabaseItemDeployment( action , delivery.distDelivery ) );
	}
	
	private boolean checkServerDelivery( ActionBase action , MetaEnvServer server , ReleaseDelivery delivery ) throws Exception {
		if( action.context.CTX_CONFDEPLOY ) {
			for( ReleaseTarget target : delivery.getConfItems( action ).values() ) {
				if( server.hasConfItemDeployment( action , target.distConfItem ) )
					return( true );
			}
		}
		
		ReleaseTarget dbtarget  = delivery.getDatabaseItem( action );
		if( dbtarget != null ) {
			if( server.hasDatabaseItemDeployment( action , dbtarget.distDatabaseItem ) )
				return( true );
		}

		if( action.context.CTX_DEPLOYBINARY ) {
			for( ReleaseTarget target : delivery.getManualItems( action ).values() ) {
				if( server.hasBinaryItemDeployment( action , target.distManualItem ) )
					return( true );
			}
			for( ReleaseTargetItem item : delivery.getProjectItems( action ).values() ) {
				if( server.hasBinaryItemDeployment( action , item.distItem ) )
					return( true );
			}
		}
		
		return( false );
	}
	
	private Map<String,MetaEnvServer> getReleaseServers( ActionBase action , Dist release ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		Release info = release.release;

		for( ReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaEnvServer server : dc.getServers() ) {
				if( checkServerDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	private Map<String,MetaEnvServer> getEnvDatabaseServers( ActionBase action , Dist dist ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		if( dist == null ) {
			for( MetaEnvServer server : dc.getServers() )
				mapServers.put( server.NAME , server );
			return( mapServers );
		}
		
		Release info = dist.release;

		for( ReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaEnvServer server : dc.getServers() ) {
				if( checkServerDatabaseDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	public void addEnvServers( ActionBase action , String[] SERVERS , Dist release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null )
			releaseServers = getReleaseServers( action , release );
	
		if( SERVERS == null || SERVERS.length == 0 ) {
			setFull = true; 
			for( MetaEnvServer server : dc.getServers() ) {
				boolean addServer = ( release == null )? true : releaseServers.containsKey( server.NAME ); 
				if( addServer )
					addEnvServer( action , server , null , false );
				else
					action.trace( "scope: skip non-release server=" + server.NAME );
			}
			return;
		}
		
		Map<String,MetaEnvServer> added = new HashMap<String,MetaEnvServer>();
		for( String SERVER : SERVERS ) {
			MetaEnvServer server = dc.getServer( action , SERVER );
			boolean addServer = ( release == null )? true : releaseServers.containsKey( SERVER ); 
			if( addServer ) {
				added.put( server.NAME , server );
				addEnvServer( action , server , null , true );
			}
			else
				action.trace( "scope: skip non-release server=" + SERVER );
		}
	}

	public void addEnvDatabases( ActionBase action , Dist dist ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = getEnvDatabaseServers( action , dist );
	
		if( action.context.CTX_DB.isEmpty() )
			setFull = true; 
		else
			setFull = false;
		
		for( MetaEnvServer server : dc.getServers() ) {
			if( !server.isDatabase() )
				continue;
			
			boolean addServer = ( dist == null )? true : releaseServers.containsKey( server.NAME );
			if( addServer ) {
				if( action.context.CTX_DB.isEmpty() == false && action.context.CTX_DB.equals( server.NAME ) == false )
					action.trace( "scope: ignore not-action scope server=" + server.NAME );
				else
					addEnvServer( action , server , null , false );
			}
			else
				action.trace( "scope: skip non-release server=" + server.NAME );
		}
	}
	
	public ActionScopeTarget addEnvServer( ActionBase action , MetaEnvServer server , List<MetaEnvServerNode> nodes , boolean specifiedExplicitly ) throws Exception {
		if( !specifiedExplicitly ) {
			// check offline or not in given start group
			if( server.OFFLINE ) {
				if( !action.context.CTX_ALL ) {
					action.trace( "scope: ignore offline server=" + server.NAME );
					return( null );
				}
			}
			
			if( !action.context.CTX_STARTGROUP.isEmpty() ) {
				if( server.startGroup == null ) {
					action.trace( "scope: ignore non-specified startgroup server=" + server.NAME );
					return( null );
				}
				
				if( !server.startGroup.NAME.equals( action.context.CTX_STARTGROUP ) ) {
					action.trace( "scope: ignore different startgroup server=" + server.NAME );
					return( null );
				}
			}
		}

		ActionScopeTarget target = ActionScopeTarget.createEnvServerTarget( this , server , specifiedExplicitly );
		addTarget( action , target );
		target.addServerNodes( action , nodes );
		return( target );
	}
	
	public ActionScopeTarget addEnvServerNodes( ActionBase action , MetaEnvServer server , String[] NODES , boolean specifiedExplicitly , Dist release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null ) {
			releaseServers = getReleaseServers( action , release );
			if( !releaseServers.containsKey( server.NAME ) ) {
				action.trace( "scope: ignore non-release server=" + server.NAME );
				return( null );
			}
		}
		
		List<MetaEnvServerNode> nodes = server.getNodes( action , NODES );
		return( addEnvServer( action , server , nodes , specifiedExplicitly ) );
	}

	public ActionScopeTarget findTarget( ActionBase action , String NAME ) throws Exception {
		ActionScopeTarget target = targets.get( NAME );
		return( target );
	}
	
	public Account[] getUniqueHosts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,Account> map = new HashMap<String,Account>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) ) {
				Account account = action.getNodeAccount( item.envServerNode );
				map.put( account.HOST , action.getNodeAccount( item.envServerNode ) );
			}
		}
		
		String[] keys = Common.getSortedKeys( map );
		Account[] accounts = new Account[ keys.length ];
		
		for( int k = 0; k < keys.length; k++ )
			accounts[ k ] = map.get( keys[ k ] );
		
		return( accounts );
	}
	
	public Account[] getUniqueAccounts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,MetaEnvServerNode> map = new HashMap<String,MetaEnvServerNode>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) )
				map.put( item.envServerNode.HOSTLOGIN , item.envServerNode );
		}
		
		String[] hostLogins = Common.getSortedKeys( map );
		Account[] accounts = new Account[ hostLogins.length ];
		
		for( int k = 0; k < hostLogins.length; k++ )
			accounts[ k ] = action.getNodeAccount( map.get( hostLogins[ k ] ) );
		return( accounts );
	}

	public List<ActionScopeTarget> getGroupServers( ActionBase action , MetaEnvStartGroup group ) throws Exception {
		List<ActionScopeTarget> groupTargets = new LinkedList<ActionScopeTarget>();
		for( MetaEnvServer server : group.getServers() ) {
			ActionScopeTarget target = targets.get( server.NAME );
			if( target != null )
				groupTargets.add( target );
		}
		return( groupTargets );
	}
}
