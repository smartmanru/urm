package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;

public class ActionCopyRelease extends ActionBase {

	Dist src;
	String RELEASEDST;
	
	public ActionCopyRelease( ActionBase action , String stream , Dist src , String RELEASEDST ) {
		super( action , stream );
		this.src = src;
		this.RELEASEDST = RELEASEDST;
	}

	@Override protected boolean executeSimple() throws Exception {
		Dist dst = artefactory.createDist( this , RELEASEDST );
		dst.copyRelease( this , src );
		return( true );
	}

}
