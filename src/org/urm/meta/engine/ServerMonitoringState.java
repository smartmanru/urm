package org.urm.meta.engine;

import org.urm.engine.ServerEventsState;

public class ServerMonitoringState extends ServerEventsState {
	
	public enum MONITORING_STATE {
		MONITORING_UNKNOWN ,
		MONITORING_NEVERQUERIED ,
		MONITORING_STOPPED ,
		MONITORING_UNABLE_GETSTATE ,
		MONITORING_ERRORS_FATAL ,
		MONITORING_ERRORS_ALERTS ,
		MONITORING_HEALTHY
	};
	
	public ServerMonitoringSource source;
	public int level;
	private MONITORING_STATE state;
	
	public ServerMonitoringState( ServerMonitoringSource source ) {
		super( source , 0 );
		state = MONITORING_STATE.MONITORING_NEVERQUERIED;
	}

	public MONITORING_STATE getState() {
		return( state );
	}

	public void setState( MONITORING_STATE state ) {
		this.state = state;
	}

}
