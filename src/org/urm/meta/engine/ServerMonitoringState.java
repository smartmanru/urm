package org.urm.meta.engine;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.ServerEventsState;

public class ServerMonitoringState extends ServerEventsState {
	
	public enum MONITORING_STATE {
		STATE_UNKNOWN ,
		STATE_NOMONITORING ,
		STATE_NEVERQUERIED ,
		STATE_STOPPED ,
		STATE_UNABLE_GETSTATE ,
		STATE_ERRORS_FATAL ,
		STATE_ERRORS_ALERTS ,
		STATE_HEALTHY
	};
	
	public ServerMonitoringSource monitoringSource;
	public MONITORING_STATE state;
	public String[] log;
	
	public ServerMonitoringState( ServerMonitoringSource source ) {
		super( source , 0 );
		monitoringSource = source;
		state = MONITORING_STATE.STATE_NOMONITORING;
	}

	public MONITORING_STATE getState() {
		return( state );
	}
	
	public void setState( MONITORING_STATE state ) {
		this.state = state;
	}

	public static MONITORING_STATE addState( MONITORING_STATE finalState , MONITORING_STATE addState ) {
		if( finalState == addState )
			return( finalState );

		if( addState == MONITORING_STATE.STATE_NOMONITORING )
			return( finalState );
		if( finalState == MONITORING_STATE.STATE_NOMONITORING )
			return( addState );
		
		if( addState == MONITORING_STATE.STATE_NEVERQUERIED )
			return( finalState );
		if( finalState == MONITORING_STATE.STATE_NEVERQUERIED )
			return( addState );
		
		if( finalState == MONITORING_STATE.STATE_UNKNOWN || addState == MONITORING_STATE.STATE_UNKNOWN )
			return( MONITORING_STATE.STATE_ERRORS_FATAL );

		if( finalState == MONITORING_STATE.STATE_ERRORS_FATAL || addState == MONITORING_STATE.STATE_ERRORS_FATAL )
			return( MONITORING_STATE.STATE_ERRORS_FATAL );
		
		if( finalState == MONITORING_STATE.STATE_NEVERQUERIED ) {
			if( addState == MONITORING_STATE.STATE_HEALTHY )
				return( MONITORING_STATE.STATE_HEALTHY );
			
			if( addState == MONITORING_STATE.STATE_ERRORS_ALERTS ||
				addState == MONITORING_STATE.STATE_STOPPED ||
				addState == MONITORING_STATE.STATE_UNABLE_GETSTATE )
				return( MONITORING_STATE.STATE_ERRORS_ALERTS );
			
			return( MONITORING_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.STATE_STOPPED ) {
			if( addState == MONITORING_STATE.STATE_ERRORS_ALERTS ||
				addState == MONITORING_STATE.STATE_NEVERQUERIED ||
				addState == MONITORING_STATE.STATE_UNABLE_GETSTATE )
				return( MONITORING_STATE.STATE_ERRORS_ALERTS );
			
			return( MONITORING_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.STATE_UNABLE_GETSTATE ) {
			if( addState == MONITORING_STATE.STATE_ERRORS_ALERTS ||
				addState == MONITORING_STATE.STATE_NEVERQUERIED ||
				addState == MONITORING_STATE.STATE_STOPPED )
				return( MONITORING_STATE.STATE_ERRORS_ALERTS );
			
			return( MONITORING_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.STATE_ERRORS_ALERTS ) {
			if( addState == MONITORING_STATE.STATE_UNABLE_GETSTATE ||
				addState == MONITORING_STATE.STATE_NEVERQUERIED ||
				addState == MONITORING_STATE.STATE_STOPPED )
				return( MONITORING_STATE.STATE_ERRORS_ALERTS );
			
			return( MONITORING_STATE.STATE_ERRORS_FATAL );
		}
		
		return( MONITORING_STATE.STATE_ERRORS_ALERTS );
	}

	public static MONITORING_STATE getState( SCOPESTATE state ) {
		if( state == SCOPESTATE.New || state == SCOPESTATE.NotRun )
			return( MONITORING_STATE.STATE_NEVERQUERIED );
		if( state == SCOPESTATE.RunBeforeFail || state == SCOPESTATE.RunFail )
			return( MONITORING_STATE.STATE_ERRORS_ALERTS );
		return( MONITORING_STATE.STATE_HEALTHY );
	}

	public void setLog( String[] log ) {
		this.log = log;
	}
		
}
