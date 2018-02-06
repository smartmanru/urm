package org.urm.action.deploy;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;

public class ActionStartServer extends ActionBase {
	
	public ActionScopeTarget target;
	public MetaEnvServer server;
	
	public ActionStartServer( ActionBase action , String stream , ActionScopeTarget target ) {
		super( action , stream , "Start server=" + target.envServer.NAME );
		this.target = target;
		this.server = target.envServer;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		List<ActionScopeTargetItem> nodes = target.getItems( this );
		if( nodes.isEmpty() ) {
			debug( "server=" + server.NAME + " has no nodes specified to start. Skipped." );
			return( SCOPESTATE.NotRun );
		}
		
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + server.getServerTypeName( this ) + " ..." );

		// first start childs
		if( target.itemFull && server.subordinateServers != null && server.subordinateServers.length != 0 ) {
			info( "start subordinate servers ..." );
			for( MetaEnvServer sub : server.subordinateServers )
				executeServerSingle( sub , state , null );
		}
		
		// start main
		info( "start main server ..." );
		executeServerSingle( server , state , nodes );

		if( target.itemFull && server.proxyServer != null ) {
			info( "start proxy server=" + server.proxyServer.NAME + " ..." );
			executeServerSingle( server.proxyServer , state , null );
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
	
	private void executeServerSingle( MetaEnvServer actionServer , ScopeState state , List<ActionScopeTargetItem> targetNodes ) throws Exception {
		MetaEnvServerNode[] nodes = getActionServerNodes( actionServer , targetNodes );
		if( nodes.length == 0 ) {
			debug( "server=" + actionServer.NAME + " has no nodes specified to start. Skipped." );
			return;
		}
	
		if( actionServer.isRunCommand() ) {
			if( !isForced() ) {
				debug( "server=" + actionServer.NAME + " is command server. Skipped." );
				return;
			}
		}
		
		if( actionServer.isStartable() ) {
			ServerCluster cluster = new ServerCluster( actionServer , nodes );
			if( !cluster.start( this , state ) ) {
				trace( "server cluster failed" );
				super.fail1( _Error.ServerClusterStartFailed1 , "server cluster start failed, server=" + actionServer.NAME , actionServer.NAME );
			}
		}
		else
			debug( "server=" + actionServer.NAME + ", type=" + actionServer.getServerTypeName( this ) + " is not supported for start. Skipped." );
	}
	
}
