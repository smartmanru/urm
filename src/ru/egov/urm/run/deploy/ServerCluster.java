package ru.egov.urm.run.deploy;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeTarget;

public class ServerCluster {

	MetaEnvServer srv;
	List<MetaEnvServerNode> nodes;
	
	public ServerCluster( MetaEnvServer srv , List<MetaEnvServerNode> nodes ) {
		this.srv = srv;
		this.nodes = nodes;
	}

	public boolean stop( ActionBase action ) throws Exception {
		boolean res = true;
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			action.log( "stop " + Common.getEnumLower( srv.TYPE ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.stop( action ) )
				res = false;
		}	

		if( action.context.CTX_SHOWONLY )
			return( res );

		// ensure processes are stopped
		if( !waitStopped( action , startMillis ) )
			res = false;
		
		return( res );
	}
	
	public boolean waitStopped( ActionBase action , long startMillis ) throws Exception {
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			action.debug( "wait for stop " + Common.getEnumLower( srv.TYPE ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.waitStopped( action , startMillis ) )
				res = false;
		}	

		return( res );
	}
	
	public boolean start( ActionBase action ) throws Exception {
		boolean res = true;
		long startMillis = System.currentTimeMillis();
		for( MetaEnvServerNode node : nodes ) {
			action.log( "start " + Common.getEnumLower( srv.TYPE ) + " app=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.start( action ) )
				res = false;
		}	

		if( action.context.CTX_SHOWONLY )
			return( res );

		// enforce timeout before querying status
		action.sleep( 1000 );
		
		// ensure processes are started
		if( !waitStarted( action , startMillis ) )
			res = false;

		ActionCheckEnv ca = new ActionCheckEnv( action , null );
		ActionScopeTarget scope = ActionScope.getEnvServerNodesScope( action , srv , nodes ); 
		if( !ca.runSingleTarget( scope ) )
			res = false;
		
		return( res );
	}

	public boolean waitStarted( ActionBase action , long startMillis ) throws Exception {
		boolean res = true;
		for( MetaEnvServerNode node : nodes ) {
			action.debug( "wait for start " + Common.getEnumLower( srv.TYPE ) + " server=" + srv.NAME + ", node=" + node.POS + ", account=" + node.HOSTLOGIN + " ..." );
			
			ServerProcess process = new ServerProcess( srv , node ); 
			if( !process.waitStarted( action , startMillis ) )
				res = false;
		}	

		return( res );
	}
	
}
