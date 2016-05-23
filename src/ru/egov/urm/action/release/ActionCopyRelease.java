package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionCopyRelease extends ActionBase {

	DistStorage src;
	String RELEASEDST;
	
	public ActionCopyRelease( ActionBase action , String stream , DistStorage src , String RELEASEDST ) {
		super( action , stream );
		this.src = src;
		this.RELEASEDST = RELEASEDST;
	}

	@Override protected boolean executeSimple() throws Exception {
		DistStorage dst = artefactory.createDistStorage( this , RELEASEDST );
		dst.copyRelease( this , src );
		return( true );
	}

}
