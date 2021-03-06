package org.urm.engine.status;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.Types.*;
import org.urm.meta.system.WholeUrlFailed;

public class NodeStatus extends Status {

	public MetaEnvServerNode node;
	
	public OBJECT_STATE mainState;
	public EnumProcessMode mode;
	public MetaEnvServer proxy;
	public String unknownReason;
	public List<WholeUrlFailed> wholeUrls;
	
	public boolean manual;
	public boolean compFailed;
	public boolean processFailed;
	public boolean proxyFailed;
	public boolean wholeUrlFailed;

	public NodeStatus( MetaEnvServerNode node ) {
		super( STATETYPE.TypeServerNode , null , node );
		this.node = node;
		
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
	
	public void setProcessMode( EnumProcessMode mode ) {
		this.mode = mode;
		if( mode == EnumProcessMode.STARTED ) {
			itemState = mainState = OBJECT_STATE.STATE_HEALTHY;
			return;
		}
		
		processFailed = true;
		if( mode == EnumProcessMode.UNKNOWN || mode == EnumProcessMode.UNREACHABLE )
			mainState = OBJECT_STATE.STATE_UNABLE_GETSTATE;
		else
		if( mode == EnumProcessMode.ERRORS )
			mainState = OBJECT_STATE.STATE_ERRORS_FATAL;
		else
		if( mode == EnumProcessMode.STARTING )
			mainState = OBJECT_STATE.STATE_ERRORS_ALERTS;
		else
		if( mode == EnumProcessMode.STOPPED )
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
