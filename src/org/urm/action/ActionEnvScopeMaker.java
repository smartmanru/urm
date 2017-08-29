package org.urm.action;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionEnvScopeMaker {

	private ActionScope scope;
	private ActionBase action;
	private Meta meta;
	private MetaEnv env;
	
	public ActionEnvScopeMaker( ActionBase action , MetaEnv env ) {
		scope = new ActionScope( action , meta );
		this.action = action;
		this.env = env;
		this.meta = env.meta;
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
				sset.addEnvServers( action , null , dist );
			}
		}
	}

	private void addEnvServersScope( MetaEnvSegment sg , String[] SERVERS , Dist dist ) throws Exception {
		if( ( SERVERS == null || SERVERS.length == 0 ) && 
			env.getSegmentNames().length == 1 )
			scope.setFullEnv( action , true );
			
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		sset.addEnvServers( action , SERVERS , dist ); 
	}

	private void addEnvDatabaseScope( Dist dist ) throws Exception {
		for( MetaEnvSegment sg : env.getSegments() ) {
			if( !sg.hasDatabaseServers() )
				continue;
			
			ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , false );
			sset.addEnvDatabases( action , dist );
		}
	}
	
	private ActionScopeTarget addEnvServerNodesScope( MetaEnvSegment sg , MetaEnvServer srv , MetaEnvServerNode[] nodes ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		return( sset.addEnvServer( action , srv , nodes , true ) );
	}
	
	private void addEnvServerNodesScope( MetaEnvSegment sg , String SERVER , String[] NODES , Dist dist ) throws Exception {
		ActionScopeSet sset = scope.makeEnvScopeSet( action , env , sg , true );
		MetaEnvServer server = sg.getServer( action , SERVER );
		
		sset.addEnvServerNodes( action , server , NODES , true , dist );
	}

}
