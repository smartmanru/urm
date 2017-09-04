package org.urm.engine.status;

import org.urm.action.ActionCore;
import org.urm.action.monitor.MonitorStatus;
import org.urm.meta.product.MetaEnvSegment;

public class SegmentStatus extends MonitorStatus {

	public SegmentStatus( ActionCore action , MetaEnvSegment sg ) {
		super( action , sg );
	}

}
