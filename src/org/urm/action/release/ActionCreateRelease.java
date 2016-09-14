package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;

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
