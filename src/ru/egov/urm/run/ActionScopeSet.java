package ru.egov.urm.run;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaDistrBinaryItem;
import ru.egov.urm.meta.MetaDistrConfItem;
import ru.egov.urm.meta.MetaDistrDelivery;
import ru.egov.urm.meta.MetaEnv;
import ru.egov.urm.meta.MetaEnvDC;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.MetaEnvStartGroup;
import ru.egov.urm.meta.MetaRelease;
import ru.egov.urm.meta.MetaReleaseDelivery;
import ru.egov.urm.meta.MetaReleaseSet;
import ru.egov.urm.meta.MetaReleaseTarget;
import ru.egov.urm.meta.MetaReleaseTargetItem;
import ru.egov.urm.meta.MetaSourceProject;
import ru.egov.urm.meta.MetaSourceProjectSet;
import ru.egov.urm.meta.Metadata;
import ru.egov.urm.meta.Metadata.VarCATEGORY;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.storage.DistStorage;

public class ActionScopeSet {

	ActionScope scope;
	Metadata meta;
	public String NAME;
	public VarCATEGORY CATEGORY;
	public boolean setFull;
	
	public MetaSourceProjectSet pset;
	public MetaReleaseSet rset;
	
	Map<String,ActionScopeTarget> targets = new HashMap<String,ActionScopeTarget>();
	ActionScopeTarget manualTarget;

	public MetaEnv env;
	public MetaEnvDC dc;
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

	public void create( ActionBase action , MetaReleaseSet rset ) throws Exception {
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

	public void create( ActionBase action , MetaEnv env , MetaEnvDC dc ) throws Exception {
		this.pset = null;
		this.NAME = dc.NAME;
		this.CATEGORY = VarCATEGORY.ENV;
		this.setFull = false;
		this.env = env;
		this.dc = dc;
	}

	private void addTarget( ActionBase action , ActionScopeTarget target ) throws Exception {
		action.trace( "scope add target=" + target.NAME );
		targets.put( target.NAME , target );
	}
	
	public ActionScopeTarget addSourceProject( ActionBase action , MetaSourceProject sourceProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createSourceProjectTarget( this , sourceProject , specifiedExplicitly ); 
		addTarget( action , target );
		
		if( allItems )
			target.addProjectItems( action , null );
		
		return( target );
	}
		
	public ActionScopeTarget addReleaseProject( ActionBase action , MetaReleaseTarget releaseProject , boolean allItems , boolean specifiedExplicitly ) throws Exception {
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
	
	public ActionScopeTarget addDatabaseDelivery( ActionBase action , MetaReleaseDelivery delivery , boolean specifiedExplicitly , boolean all ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createDatabaseDeliveryTarget( this , delivery.distDelivery , specifiedExplicitly , all );
		addTarget( action , target );
		return( target );
	}
	
	public ActionScopeTarget addReleaseProjectItems( ActionBase action , MetaReleaseTarget releaseProject , String[] ITEMS ) throws Exception {
		ActionScopeTarget target = ActionScopeTarget.createReleaseSourceProjectTarget( this, releaseProject , true );
		addTarget( action , target );
		target.addProjectItems( action , ITEMS );
		return( target );
	}

	public ActionScopeTarget findSourceTarget( ActionBase action , MetaSourceProject project ) throws Exception {
		String key = MetaReleaseTarget.getTargetKey( action , CATEGORY , project.PROJECT );
		return( targets.get( key ) );
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
		
		for( String name : PROJECTS ) {
			MetaSourceProject sourceProject = meta.sources.getProject( action , name );
			addSourceProject( action , sourceProject , true , true );
		}
	}

	private void addReleaseProjects( ActionBase action , String[] PROJECTS ) throws Exception {
		if( PROJECTS == null || PROJECTS.length == 0 ) {
			setFull = true; 
			for( MetaReleaseTarget project : rset.getTargets( action ).values() )
				addReleaseProject( action , project , true , false );
			return;
		}
		
		for( String name : PROJECTS ) {
			MetaReleaseTarget sourceProject = rset.getTargetByOriginalName( action ,  name );
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
		if( COMPS == null || COMPS.length == 0 ) {
			setFull = true; 
			for( MetaDistrConfItem item : meta.distr.getConfItems( action ).values() )
				addProductConfig( action , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			MetaDistrConfItem comp = meta.distr.getConfItem( action , key );
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
			for( MetaReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : COMPS ) {
			MetaReleaseTarget item = rset.getTargetByOriginalName( action , key );
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
		if( ITEMS == null || ITEMS.length == 0 ) {
			setFull = true; 
			for( MetaDistrBinaryItem item : meta.distr.getBinaryItems( action ).values() )
				addProductManualItem( action , item , false );
			return;
		}
		
		for( String item : ITEMS ) {
			MetaDistrBinaryItem distitem = meta.distr.getBinaryItem( action , item );
			if( !distitem.MANUAL )
				action.exit( "unexpected non-manual item=" + item );
			
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
			for( MetaReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : ITEMS ) {
			MetaReleaseTarget item = rset.getTargetByOriginalName( action , key );
			addReleaseTarget( action , item , true );
		}
	}

	private void addReleaseTarget( ActionBase action , MetaReleaseTarget releaseItem , boolean specifiedExplicitly ) throws Exception {
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
		if( DELIVERIES == null || DELIVERIES.length == 0 ) {
			setFull = true; 
			for( MetaDistrDelivery item : meta.distr.getDeliveries( action ).values() )
				addProductDatabase( action , item , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			MetaDistrDelivery item = meta.distr.getDelivery( action , key );
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
			for( MetaReleaseTarget item : rset.getTargets( action ).values() )
				addReleaseTarget( action , item , false );
			return;
		}
		
		for( String key : DELIVERIES ) {
			MetaReleaseTarget item = rset.getTargetByOriginalName( action , key );
			addReleaseTarget( action , item , true );
		}
	}

	private boolean checkServerDatabaseDelivery( ActionBase action , MetaEnvServer server , MetaReleaseDelivery delivery ) throws Exception {
		return( server.hasDatabaseItemDeployment( action , delivery.distDelivery ) );
	}
	
	private boolean checkServerDelivery( ActionBase action , MetaEnvServer server , MetaReleaseDelivery delivery ) throws Exception {
		if( action.context.CONF_DEPLOY ) {
			for( MetaReleaseTarget target : delivery.getConfItems( action ).values() ) {
				if( server.hasConfItemDeployment( action , target.distConfItem ) )
					return( true );
			}
		}
		
		MetaReleaseTarget dbtarget  = delivery.getDatabaseItem( action );
		if( dbtarget != null ) {
			if( server.hasDatabaseItemDeployment( action , dbtarget.distDatabaseItem ) )
				return( true );
		}

		if( action.options.OPT_DEPLOYBINARY ) {
			for( MetaReleaseTarget target : delivery.getManualItems( action ).values() ) {
				if( server.hasBinaryItemDeployment( action , target.distManualItem ) )
					return( true );
			}
			for( MetaReleaseTargetItem item : delivery.getProjectItems( action ).values() ) {
				if( server.hasBinaryItemDeployment( action , item.distItem ) )
					return( true );
			}
		}
		
		return( false );
	}
	
	private Map<String,MetaEnvServer> getReleaseServers( ActionBase action , DistStorage release ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		MetaRelease info = release.info;

		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaEnvServer server : dc.getServerMap( action ).values() ) {
				if( checkServerDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	private Map<String,MetaEnvServer> getReleaseDatabaseServers( ActionBase action , DistStorage release ) throws Exception {
		Map<String,MetaEnvServer> mapServers = new HashMap<String,MetaEnvServer>();
		MetaRelease info = release.info;

		for( MetaReleaseDelivery delivery : info.getDeliveries( action ).values() ) {
			for( MetaEnvServer server : dc.getServerMap( action ).values() ) {
				if( checkServerDatabaseDelivery( action , server , delivery ) )
					mapServers.put( server.NAME , server );
			}
		}
		return( mapServers );
	}
	
	public void addEnvServers( ActionBase action , String[] SERVERS , DistStorage release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null )
			releaseServers = getReleaseServers( action , release );
	
		if( SERVERS == null || SERVERS.length == 0 ) {
			setFull = true; 
			for( MetaEnvServer server : dc.getServerMap( action ).values() ) {
				boolean addServer = ( release == null )? true : releaseServers.containsKey( server.NAME ); 
				if( addServer )
					addEnvServer( action , server , null , false );
				else
					action.trace( "scope skip non-release server=" + server.NAME );
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
				action.trace( "scope skip non-release server=" + SERVER );
		}
	}

	public void addEnvDatabases( ActionBase action , DistStorage release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = getReleaseDatabaseServers( action , release );
	
		if( action.options.OPT_DB.isEmpty() )
			setFull = true; 
		else
			setFull = false;
		
		for( MetaEnvServer server : dc.getServerMap( action ).values() ) {
			if( server.TYPE != VarSERVERTYPE.DATABASE )
				continue;
			
			boolean addServer = ( release == null )? true : releaseServers.containsKey( server.NAME );
			if( addServer ) {
				if( action.options.OPT_DB.isEmpty() == false && action.options.OPT_DB.equals( server.NAME ) == false )
					action.trace( "ignore not-action scope server=" + server.NAME );
				else
					addEnvServer( action , server , null , false );
			}
			else
				action.trace( "scope skip non-release server=" + server.NAME );
		}
	}
	
	public ActionScopeTarget addEnvServer( ActionBase action , MetaEnvServer server , List<MetaEnvServerNode> nodes , boolean specifiedExplicitly ) throws Exception {
		if( !specifiedExplicitly ) {
			// check offline or not in given start group
			if( server.OFFLINE ) {
				if( !action.options.OPT_ALL )
					return( null );
			}
			
			if( !action.options.OPT_STARTGROUP.isEmpty() ) {
				if( server.startGroup == null )
					return( null );
				
				if( !server.startGroup.NAME.equals( action.options.OPT_STARTGROUP ) )
					return( null );
			}
		}

		ActionScopeTarget target = ActionScopeTarget.createEnvServerTarget( this , server , specifiedExplicitly );
		addTarget( action , target );
		target.addServerNodes( action , nodes );
		return( target );
	}
	
	public ActionScopeTarget addEnvServerNodes( ActionBase action , MetaEnvServer server , String[] NODES , boolean specifiedExplicitly , DistStorage release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null ) {
			releaseServers = getReleaseServers( action , release );
			if( !releaseServers.containsKey( server.NAME ) )
				return( null );
		}
		
		List<MetaEnvServerNode> nodes = server.getNodes( action , NODES );
		return( addEnvServer( action , server , nodes , specifiedExplicitly ) );
	}

	public ActionScopeTarget findTarget( ActionBase action , String NAME ) throws Exception {
		ActionScopeTarget target = targets.get( NAME );
		return( target );
	}
	
	public String[] getUniqueHosts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,MetaEnvServerNode> map = new HashMap<String,MetaEnvServerNode>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) )
				map.put( item.envServerNode.HOST , item.envServerNode );
		}
		return( Common.getSortedKeys( map ) );
	}
	
	public String[] getUniqueAccounts( ActionBase action , ActionScopeTarget[] targets ) throws Exception {
		Map<String,MetaEnvServerNode> map = new HashMap<String,MetaEnvServerNode>(); 
		for( ActionScopeTarget target : targets ) {
			for( ActionScopeTargetItem item : target.getItems( action ) )
				map.put( item.envServerNode.HOSTLOGIN , item.envServerNode );
		}
		return( Common.getSortedKeys( map ) );
	}

	public List<ActionScopeTarget> getGroupServers( ActionBase action , MetaEnvStartGroup group ) throws Exception {
		List<ActionScopeTarget> groupTargets = new LinkedList<ActionScopeTarget>();
		for( MetaEnvServer server : group.getServers( action ).values() ) {
			ActionScopeTarget target = targets.get( server.NAME );
			if( target != null )
				groupTargets.add( target );
		}
		return( groupTargets );
	}
}
