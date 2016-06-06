package org.urm.server.action.release;

import org.urm.dist.Dist;
import org.urm.server.action.ActionBase;

public class ActionFinishRelease extends ActionBase {

	Dist release;
	
	public ActionFinishRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.finish( this );
		return( true );
	}
	
}
