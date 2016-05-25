package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;

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
