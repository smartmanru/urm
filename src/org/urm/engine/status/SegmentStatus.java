package org.urm.engine.status;

import org.urm.meta.product.MetaEnvSegment;

public class SegmentStatus extends Status {

	public MetaEnvSegment sg;
	boolean sgTotal;
	
	public SegmentStatus( ObjectState parent , MetaEnvSegment sg ) {
		super( STATETYPE.TypeSegment , parent , sg );
		this.sg = sg;
	}

	public void setTotalStatus( boolean sgTotal ) {
		this.sgTotal = sgTotal;
	}
	
}
