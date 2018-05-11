package org.urm.action.monitor;

import org.urm.engine.StateService;
import org.urm.engine.status.ScopeState;
import org.urm.meta.product.Meta;

public class MonitorTop {

	Meta meta;
	ActionMonitorTarget targetAction;
	
	public MonitorTop( ActionMonitorTarget targetAction ) {
		this.targetAction = targetAction;
		this.meta = targetAction.info.meta;
	}

	public void runMajorChecks( ScopeState state , int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			targetAction.info( "product=" + meta.name + ": start major checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMajor( state );
			targetAction.info( "product=" + meta.name + ": major checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	public void runMinorChecks( ScopeState state , int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			targetAction.info( "product=" + meta.name + ": start minor checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMinor( state );
			targetAction.info( "product=" + meta.name + ": minor checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	private void updateTime() throws Exception {
		StateService status = targetAction.getServerStatus();
		status.updateRunTime( targetAction , targetAction.target.getSegment() );
	}
	
}
