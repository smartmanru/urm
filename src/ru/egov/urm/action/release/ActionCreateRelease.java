package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;

public class ActionCreateRelease extends ActionBase {

	public Dist release;
	String RELEASELABEL;
	
	public ActionCreateRelease( ActionBase action , String stream , String RELEASELABEL ) {
		super( action , stream );
		this.RELEASELABEL = RELEASELABEL;
	}

	@Override protected boolean executeSimple() throws Exception {
		release = artefactory.createDist( this , RELEASELABEL );
		return( true );
	}
	
}
