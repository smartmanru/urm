package org.urm.action;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.release.Release;

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

	public void addScopeEnvDatabase( Release release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Database Scope, release=" + release.RELEASEVER );
		else
			action.trace( "scope: Env Database Scope" );
		
		if( release != null )
			scope.setReleaseDistScope( release );
		
		addEnvDatabaseScope( release );
	}
	
	public void addScopeEnvServers( MetaEnvSegment sg , String[] SERVERS , Release release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Servers Scope, release=" + release.RELEASEVER + ", servers=" + Common.getListSet( SERVERS ) );
		else
			action.trace( "scope: Env Servers Scope, servers=" + Common.getListSet( SERVERS ) );
		
		if( SERVERS == null || SERVERS.length == 0 )
			action.exit0( _Error.MissingServers0 , "missing items (use \"all\" to reference all items)" );
		
		if( release != null )
			scope.setReleaseDistScope( release );
		
		if( SERVERS.length == 1 && SERVERS[0].equals( "all" ) ) {
			if( sg == null )
				addScopeEnv( sg , release );
			else
				addEnvServersScope( sg , null , release );
			return;
		}
			
		if( sg == null )
			action.exit0( _Error.SegmentUndefined0 , "segment is undefined" );
		
		addEnvServersScope( sg , SERVERS , release );
	}

	public void addScopeEnvServerNodes( MetaEnvSegment sg , String SERVER , String[] NODES , Release release ) throws Exception {
		if( release != null )
			action.trace( "scope: Env Server Nodes Scope, release=" + release.RELEASEVER + ", server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		else
			action.trace( "scope: Env Server Nodes Scope, server=" + SERVER + ", nodes=" + Common.getListSet( NODES ) );
		
		if( SERVER == null || SERVER.isEmpty() )
			action.exit0( _Error.MissingServer0 , "missing server" );
		
		if( NODES == null || NODES.length == 0 )
			action.exit0( _Error.MissingServerNodes0 , "missing items (use \"all\" to reference all items)" );
		
		if( release != null )
			scope.setReleaseDistScope( release );
		
		if( NODES.length == 1 && NODES[0].equals( "all" ) )
			addEnvServerNodesScope( sg , SERVER , null , release );
		else
			addEnvServerNodesScope( sg , SERVER , NODES , release );
	}

	public ActionScopeTarget addScopeEnvServerNodes( MetaEnvServer srv , MetaEnvServerNode[] nodes ) throws Exception {
		String nodeList = "";
		for( MetaEnvServerNode node : nodes )
			nodeList = Common.addToList( nodeList , "" + node.POS , " " );
			
		action.trace( "scope: Env Server Nodes Scope, server=" + srv.NAME + ", nodes=" + nodeList );
		return( addEnvServerNodesScope( srv.sg , srv , nodes ) );
	}
	
	public void addScopeEnv( MetaEnvSegment sg , Release release ) throws Exception {
		if( env == null )
			action.exit0( _Error.MissingEnvironment0 , "Missing environment" );
		
		String sgMask = null;
		if( sg != null )
			sgMask = sg.NAME;
		else
			sgMask = action.context.CTX_SEGMENT;
		
		if( sgMask.isEmpty() )
			scope.setFullEnv( action , true );
		
		if( release != null )
			scope.setReleaseDistScope( release );
		
		for( MetaEnvSegment sgItem : env.getSegments() ) {
			if( sgMask.isEmpty() || sgItem.NAME.matches( sgMask ) ) {
				boolean specifiedExplicitly = ( sgMask.isEmpty() )? false : true;
				ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sgItem , specifiedExplicitly );
				addEnvServers( sset , null , release );
			}
		}
	}

	private void addEnvServersScope( MetaEnvSegment sg , String[] SERVERS , Release release ) throws Exception {
		if( ( SERVERS == null || SERVERS.length == 0 ) && 
			env.getSegmentNames().length == 1 )
			scope.setFullEnv( action , true );
			
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		addEnvServers( sset , SERVERS , release ); 
	}

	private void addEnvDatabaseScope( Release release ) throws Exception {
		for( MetaEnvSegment sg : env.getSegments() ) {
			if( !sg.hasDatabaseServers() )
				continue;
			
			ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , false );
			addEnvDatabases( sset , release );
		}
	}
	
	private ActionScopeTarget addEnvServerNodesScope( MetaEnvSegment sg , MetaEnvServer srv , MetaEnvServerNode[] nodes ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		return( addEnvServer( sset , srv , nodes , true ) );
	}
	
	private void addEnvServerNodesScope( MetaEnvSegment sg , String SERVER , String[] NODES , Release release ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		MetaEnvServer server = sg.getServer( SERVER );
		addEnvServerNodes( sset , server , NODES , true , release );
	}

	private void addEnvServers( ActionScopeSet set , String[] SERVERS , Release release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null )
			releaseServers = set.getReleaseServers( action );
	
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

	private void addEnvDatabases( ActionScopeSet set , Release release ) throws Exception {
		Map<String,MetaEnvServer> servers = null;
		if( release == null )
			servers = set.getEnvDatabaseServers( action );
		else
			servers = set.getEnvDatabaseReleaseServers( action );
	
		if( action.context.CTX_DB.isEmpty() )
			set.setFullContent( true ); 
		else
			set.setFullContent( false );
		
		for( MetaEnvServer server : set.sg.getServers() ) {
			if( !server.isRunDatabase() )
				continue;
			
			boolean addServer = ( release == null )? true : servers.containsKey( server.NAME );
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
	
	private ActionScopeTarget addEnvServer( ActionScopeSet set , MetaEnvServer server , MetaEnvServerNode[] nodes , boolean specifiedExplicitly ) throws Exception {
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
	
	private ActionScopeTarget addEnvServerNodes( ActionScopeSet set , MetaEnvServer server , String[] NODES , boolean specifiedExplicitly , Release release ) throws Exception {
		Map<String,MetaEnvServer> releaseServers = null;
		if( release != null ) {
			releaseServers = set.getReleaseServers( action );
			if( !releaseServers.containsKey( server.NAME ) ) {
				action.trace( "scope: ignore non-release server=" + server.NAME );
				return( null );
			}
		}
		
		MetaEnvServerNode[] nodes = server.getNodes( action , NODES );
		return( addEnvServer( set , server , nodes , specifiedExplicitly ) );
	}

}
