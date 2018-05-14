package org.urm.action.monitor;

import org.urm.engine.StateService;
import org.urm.engine.status.ScopeState;
import org.urm.meta.system.AppProduct;

public class MonitorTop {

	ActionMonitorTarget targetAction;
	
	public MonitorTop( ActionMonitorTarget targetAction ) {
		this.targetAction = targetAction;
	}

	public void runMajorChecks( ScopeState state , int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			AppProduct product = targetAction.target.product;
			targetAction.info( "product=" + product.NAME + ": start major checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMajor( state );
			targetAction.info( "product=" + product.NAME + ": major checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	public void runMinorChecks( ScopeState state , int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			AppProduct product = targetAction.target.product;
			targetAction.info( "product=" + product.NAME + ": start minor checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMinor( state );
			targetAction.info( "product=" + product.NAME + ": minor checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	private void updateTime() throws Exception {
		StateService status = targetAction.getEngineStatus();
		status.updateRunTime( targetAction , targetAction.target.findSegment() );
	}
	
}
