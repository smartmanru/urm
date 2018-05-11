package org.urm.action;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.MetaEnvStartGroup;

public class ActionEnvScopeMaker {

	private ActionScope scope;
	private ActionBase action;
	private MetaEnv env;
	
	public ActionEnvScopeMaker( ActionBase action , MetaEnv env ) {
		scope = new ActionScope( action , env );
		this.action = action;
		this.env = env;
	}

	public ActionScope getScope() {
		return( scope );
	}
	
	public boolean isEmpty() throws Exception {
		return( scope.isEmpty() );
	}

	public void addScopeEnvDatabase( Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Database Scope, release=" + dist.RELEASEDIR );
		else
			action.trace( "scope: Env Database Scope" );
		
		addEnvDatabaseScope( dist );
	}
	
	public void addScopeEnvServers( MetaEnvSegment sg , String[] SERVERS , Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Servers Scope, release=" + dist.RELEASEDIR + ", servers=" + Common.getListSet( SERVERS ) );
		else
			action.trace( "scope: Env Servers Scope, servers=" + Common.getListSet( SERVERS ) );
		
		if( SERVERS == null || SERVERS.length == 0 )
			action.exit0( _Error.MissingServers0 , "missing items (use \"all\" to reference all items)" );
		
		if( SERVERS.length == 1 && SERVERS[0].equals( "all" ) ) {
			if( sg == null )
				addScopeEnv( sg , dist );
			else
				addEnvServersScope( sg , null , dist );
			return;
		}
			
		if( sg == null )
			action.exit0( _Error.SegmentUndefined0 , "segment is undefined" );
		
		addEnvServersScope( sg , SERVERS , dist );
	}

	public void addScopeEnvServerNodes( MetaEnvSegment sg , String SERVER , String[] NODES , Dist dist ) throws Exception {
		if( dist != null )
			action.trace( "scope: Env Server Nodes Scope, release=" + dist.RELEASEDIR + ", server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		else
			action.trace( "scope: Env Server Nodes Scope, server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		
		if( SERVER == null || SERVER.isEmpty() )
			action.exit0( _Error.MissingServer0 , "missing server" );
		
		if( NODES == null || NODES.length == 0 )
			action.exit0( _Error.MissingServerNodes0 , "missing items (use \"all\" to reference all items)" );
		
		if( NODES.length == 1 && NODES[0].equals( "all" ) )
			addEnvServerNodesScope( sg , SERVER , null , dist );
		else
			addEnvServerNodesScope( sg , SERVER , NODES , dist );
	}

	public ActionScopeTarget addScopeEnvServerNodes( MetaEnvServer srv , MetaEnvServerNode[] nodes ) throws Exception {
		String nodeList = "";
		for( MetaEnvServerNode node : nodes )
			nodeList = Common.addToList( nodeList , "" + node.POS , " " );
			
		action.trace( "scope: Env Server Nodes Scope, server=" + srv.NAME + ", nodes=" + nodeList );
		return( addEnvServerNodesScope( srv.sg , srv , nodes ) );
	}
	
	public void addScopeEnv( MetaEnvSegment sg , Dist dist ) throws Exception {
		if( env == null )
			action.exit0( _Error.MissingEnvironment0 , "Missing environment" );
		
		String sgMask = null;
		if( sg != null )
			sgMask = sg.NAME;
		else
			sgMask = action.context.CTX_SEGMENT;
		
		if( sgMask.isEmpty() )
			scope.setFullEnv( action , true );
		
		for( MetaEnvSegment sgItem : env.getSegments() ) {
			if( sgMask.isEmpty() || sgItem.NAME.matches( sgMask ) ) {
				boolean specifiedExplicitly = ( sgMask.isEmpty() )? false : true;
				ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sgItem , specifiedExplicitly );
				addEnvServers( sset , null , dist );
			}
		}
	}

	private void addEnvServersScope( MetaEnvSegment sg , String[] SERVERS , Dist dist ) throws Exception {
		if( ( SERVERS == null || SERVERS.length == 0 ) && 
			env.getSegmentNames().length == 1 )
			scope.setFullEnv( action , true );
			
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		addEnvServers( sset , SERVERS , dist ); 
	}

	private void addEnvDatabaseScope( Dist dist ) throws Exception {
		for( MetaEnvSegment sg : env.getSegments() ) {
			if( !sg.hasDatabaseServers() )
				continue;
			
			ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , false );
			addEnvDatabases( sset , dist );
		}
	}
	
	private ActionScopeTarget addEnvServerNodesScope( MetaEnvSegment sg , MetaEnvServer srv , MetaEnvServerNode[] nodes ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		return( addEnvServer( sset , srv , nodes , true ) );
	}
	
	private void addEnvServerNodesScope( MetaEnvSegment sg , String SERVER , String[] NODES , Dist dist ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		MetaEnvServer server = sg.getServer( SERVER );
		addEnvServerNodes( sset , server , NODES , true , dist );
	}

	public void addEnvServers( ActionScopeSet set , String[] SERVERS , Dist release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null )
			releaseServers = set.getReleaseServers( action , release );
	
		if( SERVERS == null || SERVERS.length == 0 ) {
			set.setFullContent( true ); 
			for( MetaEnvServer server : set.sg.getServers() ) {
				boolean addServer = ( release == null )? true : releaseServers.containsKey( server.NAME ); 
				if( addServer )
					addEnvServer( set , server , null , false );
				else
					action.trace( "scope: skip non-release server=" + server.NAME );
			}
			return;
		}
		
		Map<String,MetaEnvServer> added = new HashMap<String,MetaEnvServer>();
		for( String SERVER : SERVERS ) {
			MetaEnvServer server = set.sg.getServer( SERVER );
			boolean addServer = ( release == null )? true : releaseServers.containsKey( SERVER ); 
			if( addServer ) {
				added.put( server.NAME , server );
				addEnvServer( set , server , null , true );
			}
			else
				action.trace( "scope: skip non-release server=" + SERVER );
		}
	}

	public void addEnvDatabases( ActionScopeSet set , Dist dist ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = set.getEnvDatabaseServers( action , dist );
	
		if( action.context.CTX_DB.isEmpty() )
			set.setFullContent( true ); 
		else
			set.setFullContent( false );
		
		for( MetaEnvServer server : set.sg.getServers() ) {
			if( !server.isRunDatabase() )
				continue;
			
			boolean addServer = ( dist == null )? true : releaseServers.containsKey( server.NAME );
			if( addServer ) {
				if( action.context.CTX_DB.isEmpty() == false && action.context.CTX_DB.equals( server.NAME ) == false )
					action.trace( "scope: ignore not-action scope server=" + server.NAME );
				else
					addEnvServer( set , server , null , false );
			}
			else
				action.trace( "scope: skip non-release server=" + server.NAME );
		}
	}
	
	public ActionScopeTarget addEnvServer( ActionScopeSet set , MetaEnvServer server , MetaEnvServerNode[] nodes , boolean specifiedExplicitly ) throws Exception {
		if( !specifiedExplicitly ) {
			// check offline or not in given start group
			if( server.OFFLINE ) {
				if( !action.context.CTX_ALL ) {
					action.trace( "scope: ignore offline server=" + server.NAME );
					return( null );
				}
			}
			
			if( !action.context.CTX_STARTGROUP.isEmpty() ) {
				MetaEnvStartGroup startGroup = server.getStartGroup();
				if( startGroup == null ) {
					action.trace( "scope: ignore non-specified startgroup server=" + server.NAME );
					return( null );
				}
				
				if( !startGroup.NAME.equals( action.context.CTX_STARTGROUP ) ) {
					action.trace( "scope: ignore different startgroup server=" + server.NAME );
					return( null );
				}
			}
		}

		ActionScopeTarget target = ActionScopeTarget.createEnvServerTarget( set , server , specifiedExplicitly );
		set.addTarget( action , target );
		target.addServerNodes( action , nodes );
		return( target );
	}
	
	public ActionScopeTarget addEnvServerNodes( ActionScopeSet set , MetaEnvServer server , String[] NODES , boolean specifiedExplicitly , Dist release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null ) {
			releaseServers = set.getReleaseServers( action , release );
			if( !releaseServers.containsKey( server.NAME ) ) {
				action.trace( "scope: ignore non-release server=" + server.NAME );
				return( null );
			}
		}
		
		MetaEnvServerNode[] nodes = server.getNodes( action , NODES );
		return( addEnvServer( set , server , nodes , specifiedExplicitly ) );
	}

}
