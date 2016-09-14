package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;

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
