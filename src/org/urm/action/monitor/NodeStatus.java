package org.urm.action.monitor;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionCore;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.meta.engine.ServerMonitoringState;
import org.urm.meta.engine.WholeUrlFailed;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.Meta.VarPROCESSMODE;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class NodeStatus extends MonitorStatus {

	public MONITORING_STATE mainState;
	public VarPROCESSMODE mode;
	public MetaEnvServer proxy;
	public String unknownReason;
	public List<WholeUrlFailed> wholeUrls;
	
	public boolean manual;
	public boolean compFailed;
	public boolean processFailed;
	public boolean proxyFailed;
	public boolean wholeUrlFailed;

	public NodeStatus( ActionCore action , MetaEnvServerNode node ) {
		super( action , node );
		create();
	}
	
	public NodeStatus( ScopeState parent , ActionScopeTargetItem item ) {
		super( parent , item );
		create();
	}
	
	private void create() {
		mainState = MONITORING_STATE.STATE_NEVERQUERIED;
		manual = false;
		compFailed = false;
		processFailed = false;
		proxyFailed = false;
		wholeUrlFailed = false;
		wholeUrls = new LinkedList<WholeUrlFailed>();
	}

	public void setSkipManual() {
		manual = true;
	}

	public void setUnknown( String reason ) {
		itemState = MONITORING_STATE.STATE_UNKNOWN;
		unknownReason = reason;
	}
	
	public void setProcessMode( VarPROCESSMODE mode ) {
		this.mode = mode;
		if( mode == VarPROCESSMODE.STARTED ) {
			itemState = mainState = MONITORING_STATE.STATE_HEALTHY;
			return;
		}
		
		processFailed = true;
		if( mode == VarPROCESSMODE.UNKNOWN )
			mainState = MONITORING_STATE.STATE_UNABLE_GETSTATE;
		else
		if( mode == VarPROCESSMODE.ERRORS )
			mainState = MONITORING_STATE.STATE_ERRORS_FATAL;
		else
		if( mode == VarPROCESSMODE.STARTING )
			mainState = MONITORING_STATE.STATE_ERRORS_ALERTS;
		else
		if( mode == VarPROCESSMODE.STOPPED )
			mainState = MONITORING_STATE.STATE_STOPPED;
		
		itemState = mainState; 
	}

	public void setCompsFailed() {
		compFailed = true;
		itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_ERRORS_ALERTS );
	}
	
	public void setProxyFailed( MetaEnvServer server ) {
		proxy = server;
		proxyFailed = true;
		itemState = ServerMonitoringState.addState( itemState , MONITORING_STATE.STATE_ERRORS_ALERTS );
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
	
}
