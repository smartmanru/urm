package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;

public class ActionModifyRelease extends ActionBase {

	public Dist dist;
	public Date releaseDate;
	
	public ActionModifyRelease( ActionBase action , String stream , Dist release , Date releaseDate ) {
		super( action , stream , "Change properties of release=" + release.RELEASEDIR );
		this.dist = release;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		dist.openForChange( this );
		if( releaseDate != null )
			dist.release.setReleaseDate( this , releaseDate );
		dist.release.setProperties( this );
		dist.saveReleaseXml( this );
		dist.closeChange( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
