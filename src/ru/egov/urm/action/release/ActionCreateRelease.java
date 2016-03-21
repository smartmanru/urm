package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.Metadata.VarBUILDMODE;
import ru.egov.urm.storage.DistStorage;

public class ActionCreateRelease extends ActionBase {

	public DistStorage release;
	String RELEASELABEL;
	VarBUILDMODE BUILDMODE;
	
	public ActionCreateRelease( ActionBase action , String stream , String RELEASELABEL , VarBUILDMODE BUILDMODE ) {
		super( action , stream );
		this.RELEASELABEL = RELEASELABEL;
		this.BUILDMODE = BUILDMODE;
	}

	@Override protected boolean executeSimple() throws Exception {
		release = artefactory.createDistStorage( this , RELEASELABEL , BUILDMODE );
		return( true );
	}
	
}
