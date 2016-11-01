package org.urm.meta.engine;

import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.ServerEventsState;

public class ServerMonitoringState extends ServerEventsState {
	
	public enum MONITORING_STATE {
		MONITORING_UNKNOWN ,
		MONITORING_NOMONITORING ,
		MONITORING_NEVERQUERIED ,
		MONITORING_STOPPED ,
		MONITORING_UNABLE_GETSTATE ,
		MONITORING_ERRORS_FATAL ,
		MONITORING_ERRORS_ALERTS ,
		MONITORING_HEALTHY
	};
	
	public ServerMonitoringSource monitoringSource;
	public MONITORING_STATE state;
	
	public ServerMonitoringState( ServerMonitoringSource source ) {
		super( source , 0 );
		monitoringSource = source;
		state = MONITORING_STATE.MONITORING_NOMONITORING;
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

		if( addState == MONITORING_STATE.MONITORING_NOMONITORING )
			return( finalState );
		if( finalState == MONITORING_STATE.MONITORING_NOMONITORING )
			return( addState );
		
		if( addState == MONITORING_STATE.MONITORING_NEVERQUERIED )
			return( finalState );
		if( finalState == MONITORING_STATE.MONITORING_NEVERQUERIED )
			return( addState );
		
		if( finalState == MONITORING_STATE.MONITORING_UNKNOWN || addState == MONITORING_STATE.MONITORING_UNKNOWN )
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );

		if( finalState == MONITORING_STATE.MONITORING_ERRORS_FATAL || addState == MONITORING_STATE.MONITORING_ERRORS_FATAL )
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );
		
		if( finalState == MONITORING_STATE.MONITORING_NEVERQUERIED ) {
			if( addState == MONITORING_STATE.MONITORING_HEALTHY )
				return( MONITORING_STATE.MONITORING_HEALTHY );
			
			if( addState == MONITORING_STATE.MONITORING_ERRORS_ALERTS ||
				addState == MONITORING_STATE.MONITORING_STOPPED ||
				addState == MONITORING_STATE.MONITORING_UNABLE_GETSTATE )
				return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
			
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.MONITORING_STOPPED ) {
			if( addState == MONITORING_STATE.MONITORING_ERRORS_ALERTS ||
				addState == MONITORING_STATE.MONITORING_NEVERQUERIED ||
				addState == MONITORING_STATE.MONITORING_UNABLE_GETSTATE )
				return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
			
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.MONITORING_UNABLE_GETSTATE ) {
			if( addState == MONITORING_STATE.MONITORING_ERRORS_ALERTS ||
				addState == MONITORING_STATE.MONITORING_NEVERQUERIED ||
				addState == MONITORING_STATE.MONITORING_STOPPED )
				return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
			
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );
		}
		
		if( finalState == MONITORING_STATE.MONITORING_ERRORS_ALERTS ) {
			if( addState == MONITORING_STATE.MONITORING_UNABLE_GETSTATE ||
				addState == MONITORING_STATE.MONITORING_NEVERQUERIED ||
				addState == MONITORING_STATE.MONITORING_STOPPED )
				return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
			
			return( MONITORING_STATE.MONITORING_ERRORS_FATAL );
		}
		
		return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
	}

	public static MONITORING_STATE getState( SCOPESTATE state ) {
		if( state == SCOPESTATE.New || state == SCOPESTATE.NotRun )
			return( MONITORING_STATE.MONITORING_NEVERQUERIED );
		if( state == SCOPESTATE.RunBeforeFail || state == SCOPESTATE.RunFail )
			return( MONITORING_STATE.MONITORING_ERRORS_ALERTS );
		return( MONITORING_STATE.MONITORING_HEALTHY );
	}
	
}
