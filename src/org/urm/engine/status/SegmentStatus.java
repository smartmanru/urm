package org.urm.engine.status;

import org.urm.meta.env.MetaEnvSegment;

public class SegmentStatus extends Status {

	public MetaEnvSegment sg;
	boolean sgTotal;
	
	public SegmentStatus( MetaEnvSegment sg ) {
		super( STATETYPE.TypeSegment , null , sg );
		this.sg = sg;
	}

	public void setTotalStatus( boolean sgTotal ) {
		this.sgTotal = sgTotal;
	}
	
}
