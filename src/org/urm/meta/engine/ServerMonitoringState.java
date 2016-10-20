package org.urm.meta.engine;

import org.urm.engine.ServerEventsState;

public class ServerMonitoringState extends ServerEventsState {
	
	public enum MONITORING_STATE {
		MONITORING_UNKNOWN ,
		MONITORING_STOPPED ,
		MONITORING_UNABLE_GETSTATE ,
		MONITORING_ERRORS_FATAL ,
		MONITORING_ERRORS_ALERTS ,
		MONITORING_HEALTHY
	};
	
	public ServerMonitoring mon;
	public int level;
	public int stateId;
	private MONITORING_STATE state;
	
	public ServerMonitoringState( ServerMonitoring mon , int level , int stateId ) {
	}

	public MONITORING_STATE getState() {
		return( state );
	}

	public void setState( MONITORING_STATE state ) {
		this.state = state;
	}

}
