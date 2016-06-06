package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.dist.Dist;

public class ActionForceCloseRelease extends ActionBase {

	Dist release;
	
	public ActionForceCloseRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.forceClose( this );
		return( true );
	}

}
