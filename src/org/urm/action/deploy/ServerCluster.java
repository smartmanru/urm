package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionEnvScopeMaker;
import org.urm.action.ActionScopeTarget;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.status.ScopeState;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ServerCluster {

	public enum Facts {
		NodeStarted
	};
	
	MetaEnvServer srv;
	MetaEnvServerNode[] nodes;
	
	public ServerCluster( MetaEnvServer srv , MetaEnvServerNode[] nodes ) {
		this.srv = srv;
		this.nodes = nodes;
	}

	public boolean stop( ActionBase action , ScopeState parentState ) throws Exception {
		action.trace( "cluster stop ..." );
		
		boolean res = true;
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			ScopeState state = new ScopeState( parentState , node );
			action.info( action.getMode() + " stop " + srv.getServerTypeName( action ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node , state ); 
			if( !process.stop( action ) ) {
				action.trace( "process stop failed" );
				res = false;
			}
		}	

		if( !action.isExecute() )
			return( res );

		// ensure processes are stopped
		if( !waitStopped( action , parentState , startMillis ) ) {
			action.trace( "wait for stop failed" );
			res = false;
		}
		
		return( res );
	}
	
	public boolean waitStopped( ActionBase action , ScopeState parentState , long startMillis ) throws Exception {
		action.trace( "cluster wait stop ..." );
		
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			ScopeState state = new ScopeState( parentState , node );
			action.debug( "wait for stop " + srv.getServerTypeName( action ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node , state ); 
			if( !process.waitStopped( action , startMillis ) ) {
				action.trace( "process wait for stop failed" );
				res = false;
			}
		}	

		return( res );
	}
	
	public boolean start( ActionBase action , ScopeState parentState ) throws Exception {
		boolean res = true;
		
		action.trace( "cluster start ..." );
		
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			ScopeState state = new ScopeState( parentState , node );
			action.info( action.getMode() + " start " + srv.getServerTypeName( action ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node , state ); 
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
		if( !waitStarted( action , parentState , startMillis ) ) {
			action.trace( "wait for start failed" );
			res = false;
		}
		else {
			ActionCheckEnv ca = new ActionCheckEnv( action , null );
			ActionEnvScopeMaker maker = new ActionEnvScopeMaker( action , srv.sg.env );
			ActionScopeTarget scope = maker.addScopeEnvServerNodes( srv , nodes ); 
			if( !ca.runSingleTarget( parentState , scope , action.context.env , SecurityAction.ACTION_DEPLOY , false ) ) {
				action.trace( "checkenv failed" );
				res = false;
			}
		}
		
		return( res );
	}

	public boolean waitStarted( ActionBase action , ScopeState parentState , long startMillis ) throws Exception {
		action.trace( "cluster wait started ..." );
		
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			ScopeState state = new ScopeState( parentState , node );
			action.debug( "wait for start " + srv.getServerTypeName( action ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node , state ); 
			if( !process.waitStarted( action , startMillis ) ) {
				action.trace( "process wait start failed" );
				res = false;
			}
		}	

		return( res );
	}
	
}
