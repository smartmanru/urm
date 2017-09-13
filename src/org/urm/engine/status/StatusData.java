package org.urm.engine.status;

import java.util.Date;

import org.urm.engine.events.EngineEventsState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class StatusData extends EngineEventsState {
	
	public enum OBJECT_STATE {
		STATE_UNKNOWN ,
		STATE_NODATA ,
		STATE_NEVERQUERIED ,
		STATE_STOPPED ,
		STATE_UNABLE_GETSTATE ,
		STATE_ERRORS_FATAL ,
		STATE_ERRORS_ALERTS ,
		STATE_HEALTHY
	};
	
	public StatusSource source;
	public OBJECT_STATE state;
	public String[] log;
	public Date updated;
	public Date modified;

	public StatusData( StatusData copy ) {
		super( copy.source , copy.stateId );
		this.state = copy.state;
		this.log = copy.log;
		this.updated = copy.updated;
		this.modified = copy.modified;
	}
	
	public StatusData( StatusSource source ) {
		super( source , 0 );
		this.source = source;
		state = OBJECT_STATE.STATE_NODATA;
	}

	public void clear() {
		state = OBJECT_STATE.STATE_NODATA;
		updated = null;
		modified = null;
	}
	
	public boolean setState( OBJECT_STATE state ) {
		updated = new Date();
		if( this.state != state ) {
			this.state = state;
			modified = updated;
			return( true );
		}
		
		return( false );
	}

	public static OBJECT_STATE addState( OBJECT_STATE finalState , OBJECT_STATE addState ) {
		if( finalState == addState )
			return( finalState );

		if( addState == OBJECT_STATE.STATE_NODATA )
			return( finalState );
		if( finalState == OBJECT_STATE.STATE_NODATA )
			return( addState );
		
		if( addState == OBJECT_STATE.STATE_NEVERQUERIED )
			return( finalState );
		if( finalState == OBJECT_STATE.STATE_NEVERQUERIED )
			return( addState );
		
		if( finalState == OBJECT_STATE.STATE_UNKNOWN || addState == OBJECT_STATE.STATE_UNKNOWN )
			return( OBJECT_STATE.STATE_ERRORS_FATAL );

		if( finalState == OBJECT_STATE.STATE_ERRORS_FATAL || addState == OBJECT_STATE.STATE_ERRORS_FATAL )
			return( OBJECT_STATE.STATE_ERRORS_FATAL );
		
		if( finalState == OBJECT_STATE.STATE_NEVERQUERIED ) {
			if( addState == OBJECT_STATE.STATE_HEALTHY )
				return( OBJECT_STATE.STATE_HEALTHY );
			
			if( addState == OBJECT_STATE.STATE_ERRORS_ALERTS ||
				addState == OBJECT_STATE.STATE_STOPPED ||
				addState == OBJECT_STATE.STATE_UNABLE_GETSTATE )
				return( OBJECT_STATE.STATE_ERRORS_ALERTS );
			
			return( OBJECT_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == OBJECT_STATE.STATE_STOPPED ) {
			if( addState == OBJECT_STATE.STATE_ERRORS_ALERTS ||
				addState == OBJECT_STATE.STATE_NEVERQUERIED ||
				addState == OBJECT_STATE.STATE_UNABLE_GETSTATE )
				return( OBJECT_STATE.STATE_ERRORS_ALERTS );
			
			return( OBJECT_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == OBJECT_STATE.STATE_UNABLE_GETSTATE ) {
			if( addState == OBJECT_STATE.STATE_ERRORS_ALERTS ||
				addState == OBJECT_STATE.STATE_NEVERQUERIED ||
				addState == OBJECT_STATE.STATE_STOPPED )
				return( OBJECT_STATE.STATE_ERRORS_ALERTS );
			
			return( OBJECT_STATE.STATE_ERRORS_FATAL );
		}
		
		if( finalState == OBJECT_STATE.STATE_ERRORS_ALERTS ) {
			if( addState == OBJECT_STATE.STATE_UNABLE_GETSTATE ||
				addState == OBJECT_STATE.STATE_NEVERQUERIED ||
				addState == OBJECT_STATE.STATE_STOPPED )
				return( OBJECT_STATE.STATE_ERRORS_ALERTS );
			
			return( OBJECT_STATE.STATE_ERRORS_FATAL );
		}
		
		return( OBJECT_STATE.STATE_ERRORS_ALERTS );
	}

	public static OBJECT_STATE getState( SCOPESTATE state ) {
		if( state == SCOPESTATE.New || state == SCOPESTATE.NotRun )
			return( OBJECT_STATE.STATE_NEVERQUERIED );
		if( state == SCOPESTATE.RunBeforeFail || state == SCOPESTATE.RunFail )
			return( OBJECT_STATE.STATE_ERRORS_ALERTS );
		return( OBJECT_STATE.STATE_HEALTHY );
	}

	public void setLog( String[] log ) {
		this.log = log;
	}
		
}
