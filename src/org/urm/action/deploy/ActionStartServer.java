package org.urm.action.deploy;

import java.util.LinkedList;
import java.util.List;

import org.urm.common.Common;
import org.urm.engine.action.ActionBase;
import org.urm.engine.action.ActionScopeTarget;
import org.urm.engine.action.ActionScopeTargetItem;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.meta.MetaEnvServerNode;

public class ActionStartServer extends ActionBase {
	
	ActionScopeTarget target;
	MetaEnvServer server;
	
	public ActionStartServer( ActionBase action , String stream , ActionScopeTarget target ) {
		super( action , stream );
		this.target = target;
		this.server = target.envServer;
	}

	@Override protected boolean executeSimple() throws Exception {
		List<ActionScopeTargetItem> nodes = target.getItems( this );
		if( nodes.isEmpty() ) {
			debug( "server=" + server.NAME + " has no nodes specified to start. Skipped." );
			return( true );
		}
		
		info( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.serverType ) + " ..." );

		// first start childs
		if( target.itemFull && server.subordinateServers != null && server.subordinateServers.length != 0 ) {
			info( "start subordinate servers ..." );
			for( MetaEnvServer sub : server.subordinateServers )
				executeServerSingle( sub , null );
		}
		
		// start main
		info( "start main server ..." );
		executeServerSingle( server , nodes );

		if( target.itemFull && server.proxyServer != null ) {
			info( "start proxy server=" + server.proxyServer.NAME + " ..." );
			executeServerSingle( server.proxyServer , null );
		}

		return( true );
	}

	public List<MetaEnvServerNode> getActionServerNodes( MetaEnvServer actionServer , List<ActionScopeTargetItem> targetNodes ) throws Exception {
		if( targetNodes == null )
			return( actionServer.getNodes( this ) );
		
		List<MetaEnvServerNode> nodes = new LinkedList<MetaEnvServerNode>();
		for( ActionScopeTargetItem item : targetNodes )
			nodes.add( item.envServerNode );

		return( nodes ); 
	}
	
	public void executeServerSingle( MetaEnvServer actionServer , List<ActionScopeTargetItem> targetNodes ) throws Exception {
		List<MetaEnvServerNode> nodes = getActionServerNodes( actionServer , targetNodes );
		if( nodes.isEmpty() ) {
			debug( "server=" + actionServer.NAME + " has no nodes specified to start. Skipped." );
			return;
		}
	
		if( actionServer.isCommand( this ) ) {
			if( !context.CTX_FORCE ) {
				debug( "server=" + actionServer.NAME + " is command server. Skipped." );
				return;
			}
		}
		
		if( actionServer.isStartable( this ) ) {
			ServerCluster cluster = new ServerCluster( actionServer , nodes );
			if( !cluster.start( this ) ) {
				trace( "server cluster failed" );
				setFailed();
			}
		}
		else
			debug( "server=" + server.NAME + ", type=" + Common.getEnumLower( actionServer.serverType ) + " is not supported for start. Skipped." );
	}
	
}
