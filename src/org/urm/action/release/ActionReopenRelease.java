package org.urm.action.release;

import org.urm.engine.action.ActionBase;
import org.urm.engine.dist.Dist;

public class ActionReopenRelease extends ActionBase {

	Dist release;
	
	public ActionReopenRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.reopen( this );
		return( true );
	}
	
}
