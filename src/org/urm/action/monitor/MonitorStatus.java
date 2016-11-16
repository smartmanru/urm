package org.urm.action.monitor;

import org.urm.action.ActionCore;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.meta.engine.ServerMonitoringState.MONITORING_STATE;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;

public class MonitorStatus extends ScopeState {

	public MONITORING_STATE itemState;
	String[] log;

	public MonitorStatus( ActionCore action , MetaEnvDC dc ) {
		super( action , dc );
		itemState = MONITORING_STATE.STATE_NEVERQUERIED;
	}

	public MonitorStatus( ActionCore action , MetaEnvServer server ) {
		super( action , server );
		itemState = MONITORING_STATE.STATE_NEVERQUERIED;
	}

	public MonitorStatus( ScopeState parent , ActionScopeTarget item ) {
		super( parent , item );
		itemState = MONITORING_STATE.STATE_NEVERQUERIED;
	}
	
	public MonitorStatus( ScopeState parent , ActionScopeTargetItem item ) {
		super( parent , item );
		itemState = MONITORING_STATE.STATE_NEVERQUERIED;
	}
	
	public boolean isHealthy() {
		if( itemState == MONITORING_STATE.STATE_HEALTHY )
			return( true );
		return( false );
	}

	public boolean isFailed() {
		if( itemState == MONITORING_STATE.STATE_ERRORS_ALERTS ||
			itemState == MONITORING_STATE.STATE_ERRORS_FATAL ||
			itemState == MONITORING_STATE.STATE_STOPPED ||
			itemState == MONITORING_STATE.STATE_UNABLE_GETSTATE ||
			itemState == MONITORING_STATE.STATE_UNKNOWN )
			return( true );
		return( false );
	}
	
	public void setLog( String[] log ) {
		this.log = log;
	}
	
	public String[] getLog() {
		return( log );
	}
	
}
