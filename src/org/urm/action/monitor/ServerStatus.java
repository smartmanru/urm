package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionCore;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ScopeState;
import org.urm.meta.engine.ServerMonitoringState;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.engine.RoleItemFailed;
import org.urm.meta.engine.WholeUrlFailed;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ServerStatus extends MonitorStatus {

	public boolean nodeFailed;
	public boolean roleFailed;
	public boolean wholeUrlFailed;
	public boolean databaseFailed;

	List<NodeStatus> nodes;
	List<RoleItemFailed> roles;
	List<WholeUrlFailed> wholeUrls;
	
	public ServerStatus( ActionCore action , MetaEnvServer server ) {
		super( action , server );
		nodeFailed = false;
		roleFailed = false;
		wholeUrlFailed = false;
		databaseFailed = false;
		nodes = new LinkedList<NodeStatus>(); 
		roles = new LinkedList<RoleItemFailed>();
		wholeUrls = new LinkedList<WholeUrlFailed>();
	}

	public ServerStatus( ScopeState parent , ActionScopeTarget item ) {
		super( parent , item );
		nodeFailed = false;
		roleFailed = false;
		wholeUrlFailed = false;
		databaseFailed = false;
		nodes = new LinkedList<NodeStatus>(); 
		roles = new LinkedList<RoleItemFailed>();
		wholeUrls = new LinkedList<WholeUrlFailed>();
	}

	public void addNodeStatus( NodeStatus status ) {
		if( status.isFailed() ) 
			nodeFailed = true;
		nodes.add( status );
		itemState = ServerMonitoringState.addState( itemState , status.itemState );
	}

	public void addRoleStatus( String role , MetaEnvServerNode node , boolean failed ) {
		if( failed ) {
			roleFailed = true;
			roles.add( new RoleItemFailed( role , node ) );
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_ERRORS_ALERTS );
		}
		else
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_HEALTHY );
	}

	public void addWholeUrlStatus( String URL , String role , boolean ok ) throws Exception {
		if( !ok ) {
			wholeUrlFailed = true;
			wholeUrls.add( new WholeUrlFailed( URL , role ) );
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_ERRORS_ALERTS );
		}
		else
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_HEALTHY );
	}

	public void addDatabaseStatus( boolean ok ) {
		if( ok )
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_HEALTHY );
		else {
			databaseFailed = true;
			itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_ERRORS_ALERTS );
		}
	}
	
}
