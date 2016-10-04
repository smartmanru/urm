package org.urm.action.deploy;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeTarget;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ServerCluster {

	MetaEnvServer srv;
	List<MetaEnvServerNode> nodes;
	
	public ServerCluster( MetaEnvServer srv , List<MetaEnvServerNode> nodes ) {
		this.srv = srv;
		this.nodes = nodes;
	}

	public boolean stop( ActionBase action ) throws Exception {
		action.trace( "cluster stop ..." );
		
		boolean res = true;
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			action.info( action.getMode() + " stop " + srv.getServerTypeName( action ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.stop( action ) ) {
				action.trace( "process stop failed" );
				res = false;
			}
		}	

		if( !action.isExecute() )
			return( res );

		// ensure processes are stopped
		if( !waitStopped( action , startMillis ) ) {
			action.trace( "wait for stop failed" );
			res = false;
		}
		
		return( res );
	}
	
	public boolean waitStopped( ActionBase action , long startMillis ) throws Exception {
		action.trace( "cluster wait stop ..." );
		
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			action.debug( "wait for stop " + srv.getServerTypeName( action ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.waitStopped( action , startMillis ) ) {
				action.trace( "process wait for stop failed" );
				res = false;
			}
		}	

		return( res );
	}
	
	public boolean start( ActionBase action ) throws Exception {
		boolean res = true;
		
		action.trace( "cluster start ..." );
		
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			action.info( action.getMode() + " start " + srv.getServerTypeName( action ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.start( action ) ) {
				action.trace( "process start failed" );
				res = false;
			}
		}	

		if( !action.isExecute() )
			return( res );

		// enforce timeout before querying status
		action.sleep( 1000 );
		
		// ensure processes are started
		if( !waitStarted( action , startMillis ) ) {
			action.trace( "wait for start failed" );
			res = false;
		}
		else {
			ActionCheckEnv ca = new ActionCheckEnv( action , null );
			ActionScopeTarget scope = ActionScope.getEnvServerNodesScope( action , srv , nodes ); 
			if( !ca.runSingleTarget( scope ) ) {
				action.trace( "checkenv failed" );
				res = false;
			}
		}
		
		return( res );
	}

	public boolean waitStarted( ActionBase action , long startMillis ) throws Exception {
		action.trace( "cluster wait started ..." );
		
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			action.debug( "wait for start " + srv.getServerTypeName( action ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.waitStarted( action , startMillis ) ) {
				action.trace( "process wait start failed" );
				res = false;
			}
		}	

		return( res );
	}
	
}
