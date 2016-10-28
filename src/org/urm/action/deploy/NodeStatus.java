package org.urm.action.deploy;

import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.Meta.VarPROCESSMODE;
import org.urm.meta.product.MetaEnvServer;

public class NodeStatus extends ScopeState {

	public MONITORING_STATE itemState;
	public MONITORING_STATE mainState;
	public VarPROCESSMODE mode;
	public MetaEnvServer proxy;
	public String unknownReason;
	
	public boolean manual;
	public boolean compFailed;
	public boolean processFailed;
	public boolean proxyFailed;

	String[] log;
	
	public NodeStatus( ScopeState parent , ActionScopeTargetItem item ) {
		super( parent , item );
		itemState = mainState = MONITORING_STATE.MONITORING_NEVERQUERIED;
		manual = false;
		compFailed = false;
		processFailed = false;
		proxyFailed = false;
	}

	public boolean isHealthy() {
		if( itemState == MONITORING_STATE.MONITORING_HEALTHY )
			return( true );
		return( false );
	}

	public boolean isFailed() {
		if( itemState == MONITORING_STATE.MONITORING_ERRORS_ALERTS ||
			itemState == MONITORING_STATE.MONITORING_ERRORS_FATAL ||
			itemState == MONITORING_STATE.MONITORING_STOPPED ||
			itemState == MONITORING_STATE.MONITORING_UNABLE_GETSTATE ||
			itemState == MONITORING_STATE.MONITORING_UNKNOWN )
			return( true );
		return( false );
	}

	public void setSkipManual() {
		manual = true;
	}

	public void setUnknown( String reason ) {
		itemState = MONITORING_STATE.MONITORING_UNKNOWN;
		unknownReason = reason;
	}
	
	public void setProcessMode( VarPROCESSMODE mode ) {
		this.mode = mode;
		if( mode == VarPROCESSMODE.STARTED ) {
			itemState = mainState = MONITORING_STATE.MONITORING_HEALTHY;
			return;
		}
		
		processFailed = true;
		if( mode == VarPROCESSMODE.UNKNOWN )
			mainState = MONITORING_STATE.MONITORING_UNABLE_GETSTATE;
		else
		if( mode == VarPROCESSMODE.ERRORS )
			mainState = MONITORING_STATE.MONITORING_ERRORS_FATAL;
		else
		if( mode == VarPROCESSMODE.STARTING )
			mainState = MONITORING_STATE.MONITORING_ERRORS_ALERTS;
		else
		if( mode == VarPROCESSMODE.STOPPED )
			mainState = MONITORING_STATE.MONITORING_STOPPED;
		
		itemState = mainState; 
	}

	public void setCompsFailed() {
		compFailed = true;
		itemState = MONITORING_STATE.MONITORING_ERRORS_ALERTS;
	}
	
	public void setProxyFailed( MetaEnvServer server ) {
		proxy = server;
		proxyFailed = true;
		itemState = MONITORING_STATE.MONITORING_ERRORS_ALERTS;
	}

	public void setLog( String[] log ) {
		this.log = log;
	}
	
	public String[] getLog() {
		return( log );
	}
	
}
