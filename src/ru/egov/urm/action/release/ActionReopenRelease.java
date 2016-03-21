package ru.egov.urm.run.release;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionReopenRelease extends ActionBase {

	DistStorage release;
	
	public ActionReopenRelease( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.reopen( this );
		return( true );
	}
	
}
