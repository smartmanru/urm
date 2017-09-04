package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionCore;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.engine.WholeUrlFailed;
import org.urm.meta.Types.*;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class NodeStatus extends Status {

	public OBJECT_STATE mainState;
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
		mainState = OBJECT_STATE.STATE_NEVERQUERIED;
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
		itemState = OBJECT_STATE.STATE_UNKNOWN;
		unknownReason = reason;
	}
	
	public void setProcessMode( VarPROCESSMODE mode ) {
		this.mode = mode;
		if( mode == VarPROCESSMODE.STARTED ) {
			itemState = mainState = OBJECT_STATE.STATE_HEALTHY;
			return;
		}
		
		processFailed = true;
		if( mode == VarPROCESSMODE.UNKNOWN )
			mainState = OBJECT_STATE.STATE_UNABLE_GETSTATE;
		else
		if( mode == VarPROCESSMODE.ERRORS )
			mainState = OBJECT_STATE.STATE_ERRORS_FATAL;
		else
		if( mode == VarPROCESSMODE.STARTING )
			mainState = OBJECT_STATE.STATE_ERRORS_ALERTS;
		else
		if( mode == VarPROCESSMODE.STOPPED )
			mainState = OBJECT_STATE.STATE_STOPPED;
		
		itemState = mainState; 
	}

	public void setCompsFailed() {
		compFailed = true;
		itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
	}
	
	public void setProxyFailed( MetaEnvServer server ) {
		proxy = server;
		proxyFailed = true;
		itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
	}

	public void addWholeUrlStatus( String URL , String role , boolean ok ) throws Exception {
		if( !ok ) {
			wholeUrlFailed = true;
			wholeUrls.add( new WholeUrlFailed( URL , role ) );
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_ERRORS_ALERTS );
		}
		else
			itemState = StatusData.addState( itemState , OBJECT_STATE.STATE_HEALTHY );
	}
	
}
