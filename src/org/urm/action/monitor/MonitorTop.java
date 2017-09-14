package org.urm.action.monitor;

import java.util.Date;
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
			Date start = new Date();
			targetAction.info( "product=" + meta.name + ": start major checks #" + iteration + ": " );
			targetAction.executeOnceMajor();
			Date stop = new Date();
			targetAction.info( "product=" + meta.name + ": major checks #" + iteration + " done in : " + ( stop.getTime() - start.getTime() ) + "ms" );
			targetAction.createGraph();
		}
	}
	
	public void runMinorChecks( int iteration ) throws Exception {
		synchronized( targetAction ) {
			Date start = new Date();
			targetAction.info( "product=" + meta.name + ": start minor checks #" + iteration + ": " );
			targetAction.executeOnceMinor();
			Date stop = new Date();
			targetAction.info( "product=" + meta.name + ": minor checks #" + iteration + " done in : " + ( stop.getTime() - start.getTime() ) + "ms" );
			targetAction.createGraph();
		}
	}		
	
}
