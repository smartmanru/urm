package org.urm.action.deploy;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionStopServer extends ActionBase {
	
	ActionScopeTarget target;
	MetaEnvServer server;
	
	public ActionStopServer( ActionBase action , String stream , ActionScopeTarget target ) {
		super( action , stream , "Stop server=" + target.envServer.NAME );
		this.target = target;
		this.server = target.envServer;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		List<ActionScopeTargetItem> nodes = target.getItems( this );
		if( nodes.isEmpty() ) {
			debug( "server=" + server.NAME + " has no nodes specified to stop. Skipped." );
			return( SCOPESTATE.NotRun );
		}
		
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );

		// stop proxy if any
		if( target.itemFull && server.proxyServer != null ) {
			info( "stop proxy server=" + server.proxyServer.NAME + " ..." );
			executeServerSingle( server.proxyServer , null );
		}

		// stop main
		info( "stop main server ..." );
		executeServerSingle( server , nodes );

		// then stop childs
		if( target.itemFull && server.subordinateServers != null && server.subordinateServers.length > 0 ) {
			info( "stop subordinate servers ..." );
			for( MetaEnvServer sub : server.subordinateServers )
				executeServerSingle( sub , null );
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	public MetaEnvServerNode[] getActionServerNodes( MetaEnvServer actionServer , List<ActionScopeTargetItem> targetNodes ) throws Exception {
		if( targetNodes == null )
			return( actionServer.getNodes() );
		
		List<MetaEnvServerNode> nodes = new LinkedList<MetaEnvServerNode>();
		for( ActionScopeTargetItem item : targetNodes )
			nodes.add( item.envServerNode );

		return( nodes.toArray( new MetaEnvServerNode[0] ) ); 
	}
	
	public void executeServerSingle( MetaEnvServer actionServer , List<ActionScopeTargetItem> targetNodes ) throws Exception {
		MetaEnvServerNode[] nodes = getActionServerNodes( actionServer , targetNodes );
		if( nodes.length == 0 ) {
			debug( "server=" + actionServer.NAME + " has no nodes specified to stop. Skipped." );
			return;
		}
	
		if( actionServer.isCommand() ) {
			if( !isForced() ) {
				debug( "server=" + actionServer.NAME + " is command server. Skipped." );
				return;
			}
		}
		
		if( actionServer.isStartable() ) {
			ServerCluster cluster = new ServerCluster( actionServer , nodes );
			if( !cluster.stop( this ) )
				super.fail1( _Error.ServerClusterStopFailed1 , "server cluster stop failed, server=" + actionServer.NAME , actionServer.NAME );
		}
		else
			debug( "server=" + server.NAME + ", type=" + actionServer.getServerTypeName( this ) + " is not supported for stop. Skipped." );
	}
	
}
