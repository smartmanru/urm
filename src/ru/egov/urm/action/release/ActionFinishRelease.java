package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionFinishRelease extends ActionBase {

	DistStorage release;
	
	public ActionFinishRelease( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.finish( this );
		return( true );
	}
	
}
