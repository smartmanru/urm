package ru.egov.urm.run.deploy;

import java.util.LinkedList;
import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.meta.Metadata.VarSERVERTYPE;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;

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
		
		log( "============================================ " + getMode() + " server=" + server.NAME + ", type=" + Common.getEnumLower( server.TYPE ) + " ..." );

		// first start childs
		if( target.itemFull && server.subordinateServers != null && server.subordinateServers.length != 0 ) {
			log( "start subordinate servers ..." );
			for( MetaEnvServer sub : server.subordinateServers )
				executeServerSingle( sub , null );
		}
		
		// start main
		log( "start main server ..." );
		executeServerSingle( server , nodes );

		if( target.itemFull && server.proxyServer != null ) {
			log( "start proxy server=" + server.proxyServer.NAME + " ..." );
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
	
		if( actionServer.TYPE == VarSERVERTYPE.GENERIC_COMMAND ) {
			if( !options.OPT_FORCE ) {
				debug( "server=" + actionServer.NAME + " is command server. Skipped." );
				return;
			}
		}
		
		if( actionServer.TYPE == VarSERVERTYPE.GENERIC_SERVER || 
			actionServer.TYPE == VarSERVERTYPE.GENERIC_WEB || 
			actionServer.TYPE == VarSERVERTYPE.GENERIC_COMMAND || 
			actionServer.TYPE == VarSERVERTYPE.SERVICE ) {
			ServerCluster cluster = new ServerCluster( actionServer , nodes );
			if( !cluster.start( this ) )
				setFailed();
		}
		else
			debug( "server=" + server.NAME + ", type=" + Common.getEnumLower( actionServer.TYPE ) + " is not supported for start. Skipped." );
	}
	
}
