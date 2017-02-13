package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;

public class ActionCopyRelease extends ActionBase {

	public Dist src;
	public String RELEASEDST;
	public Date releaseDate;
	
	public Dist dst;
	
	public ActionCopyRelease( ActionBase action , String stream , Dist src , String RELEASEDST , Date releaseDate ) {
		super( action , stream , "Copy distributive src=" + src.RELEASEDIR + ", dst=" + RELEASEDST );
		this.src = src;
		this.RELEASEDST = RELEASEDST;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		DistRepository repo = artefactory.getDistRepository( this , src.meta );
		dst = repo.createDist( this , RELEASEDST , releaseDate );
		dst.copyRelease( this , src );
		return( SCOPESTATE.RunSuccess );
	}

}
