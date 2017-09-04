package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.meta.engine.ReleaseLifecycle;

public class ActionModifyRelease extends ActionBase {

	public Dist dist;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public ActionModifyRelease( ActionBase action , String stream , Dist release , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Change properties of release=" + release.RELEASEDIR );
		this.dist = release;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.openForDataChange( this );
		if( releaseDate != null && releaseDate.equals( dist.release.schedule.releaseDate ) == false )
			dist.changeReleaseDate( this , releaseDate , lc );
		dist.release.setProperties( this );
		dist.saveReleaseXml( this );
		dist.closeDataChange( this );
		return( SCOPESTATE.RunSuccess );
	}
	
}
