package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionCreateRelease extends ActionBase {

	public DistStorage release;
	String RELEASELABEL;
	
	public ActionCreateRelease( ActionBase action , String stream , String RELEASELABEL ) {
		super( action , stream );
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected boolean executeSimple() throws Exception {
		release = artefactory.createDistStorage( this , RELEASELABEL );
		return( true );
	}
	
}
