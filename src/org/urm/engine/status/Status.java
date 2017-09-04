package org.urm.engine.status;

import org.urm.action.ActionCore;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.engine.status.ServerStatusData.OBJECT_STATE;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class Status extends ScopeState {

	public OBJECT_STATE itemState;
	String[] log;

	public Status( ActionCore action , MetaEnvSegment sg ) {
		super( action , sg );
		itemState = OBJECT_STATE.STATE_NEVERQUERIED;
	}

	public Status( ActionCore action , MetaEnvServer server ) {
		super( action , server );
		itemState = OBJECT_STATE.STATE_NEVERQUERIED;
	}

	public Status( ActionCore action , MetaEnvServerNode node ) {
		super( action , node );
		itemState = OBJECT_STATE.STATE_NEVERQUERIED;
	}

	public Status( ScopeState parent , ActionScopeTarget item ) {
		super( parent , item );
		itemState = OBJECT_STATE.STATE_NEVERQUERIED;
	}
	
	public Status( ScopeState parent , ActionScopeTargetItem item ) {
		super( parent , item );
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
