package ru.egov.urm.run.release;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionForceCloseRelease extends ActionBase {

	DistStorage release;
	
	public ActionForceCloseRelease( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.forceClose( this );
		return( true );
	}

}
