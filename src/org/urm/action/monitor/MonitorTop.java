package org.urm.action.monitor;

import org.urm.engine.status.EngineStatus;
import org.urm.meta.product.Meta;

public class MonitorTop {

	Meta meta;
	ActionMonitorTarget targetAction;
	
	public MonitorTop( ActionMonitorTarget targetAction ) {
		this.targetAction = targetAction;
		this.meta = targetAction.info.meta;
	}

	public void runMajorChecks( int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			targetAction.info( "product=" + meta.name + ": start major checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMajor();
			targetAction.info( "product=" + meta.name + ": major checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	public void runMinorChecks( int iteration ) throws Exception {
		synchronized( targetAction ) {
			updateTime();
			targetAction.info( "product=" + meta.name + ": start minor checks #" + iteration + ": " );
			long timePassed = targetAction.executeOnceMinor();
			targetAction.info( "product=" + meta.name + ": minor checks #" + iteration + " done in : " + timePassed + "ms" );
			targetAction.createGraph();
		}
	}
	
	private void updateTime() throws Exception {
		EngineStatus status = targetAction.getServerStatus();
		status.updateRunTime( targetAction , targetAction.target.getSegment( targetAction ) );
	}
	
}
