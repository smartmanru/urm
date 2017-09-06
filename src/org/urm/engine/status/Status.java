package org.urm.engine.status;

import org.urm.engine.status.StatusData.OBJECT_STATE;

public class Status extends ObjectState {

	public OBJECT_STATE itemState;
	String[] log;

	public Status( STATETYPE type , ObjectState parent , Object object ) {
		super( type , parent , object );
		itemState = OBJECT_STATE.STATE_NEVERQUERIED;
	}

	public boolean isHealthy() {
		if( itemState == OBJECT_STATE.STATE_HEALTHY )
			return( true );
		return( false );
	}

	public boolean isFailed() {
		if( itemState == OBJECT_STATE.STATE_ERRORS_ALERTS ||
			itemState == OBJECT_STATE.STATE_ERRORS_FATAL ||
			itemState == OBJECT_STATE.STATE_STOPPED ||
			itemState == OBJECT_STATE.STATE_UNABLE_GETSTATE ||
			itemState == OBJECT_STATE.STATE_UNKNOWN )
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
