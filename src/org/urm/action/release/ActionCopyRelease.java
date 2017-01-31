package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionCopyRelease extends ActionBase {

	public Dist src;
	public String RELEASEDST;
	
	public Dist dst;
	
	public ActionCopyRelease( ActionBase action , String stream , Dist src , String RELEASEDST ) {
		super( action , stream , "Copy distributive src=" + src.RELEASEDIR + ", dst=" + RELEASEDST );
		this.src = src;
		this.RELEASEDST = RELEASEDST;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		dst = artefactory.createDist( this , src.meta , RELEASEDST );
		dst.copyRelease( this , src );
		return( SCOPESTATE.RunSuccess );
	}

}
