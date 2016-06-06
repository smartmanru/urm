package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.dist.Dist;

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
