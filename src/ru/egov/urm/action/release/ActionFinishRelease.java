package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;

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
