package ru.egov.urm.run.release;

import ru.egov.urm.run.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionDeleteRelease extends ActionBase {

	DistStorage release;
	boolean force;
	
	public ActionDeleteRelease( ActionBase action , String stream , DistStorage release , boolean force ) {
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
