package org.urm.server.action.release;

import org.urm.server.action.ActionBase;
import org.urm.server.dist.Dist;

public class ActionDeleteRelease extends ActionBase {

	Dist release;
	boolean force;
	
	public ActionDeleteRelease( ActionBase action , String stream , Dist release , boolean force ) {
		super( action , stream );
		this.release = release;
		this.force = force;
	}

	@Override protected boolean executeSimple() throws Exception {
		if( force )
			release.forceDrop( this );
		else
			release.dropRelease( this );
		return( true );
	}
	
}
