package org.urm.action.release;

import java.util.Date;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistLabelInfo;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.ReleaseLifecycle;

public class ActionCopyRelease extends ActionBase {

	public Dist src;
	public String RELEASEDST;
	public Date releaseDate;
	public ReleaseLifecycle lc;
	
	public Dist dst;
	
	public ActionCopyRelease( ActionBase action , String stream , Dist src , String RELEASEDST , Date releaseDate , ReleaseLifecycle lc ) {
		super( action , stream , "Copy distributive src=" + src.RELEASEDIR + ", dst=" + RELEASEDST );
		this.src = src;
		this.RELEASEDST = RELEASEDST;
		this.releaseDate = releaseDate;
		this.lc = lc;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		DistRepository repo = artefactory.getDistRepository( this , src.meta );
		DistLabelInfo info = repo.getLabelInfo( this , RELEASEDST );
		if( info.prod ) {
			super.fail0( _Error.CannotCopyProd0 , "Cannot create prod distributive, use prod command instead" );
			return( SCOPESTATE.RunFail );
		}
		
		dst = repo.createDist( this , RELEASEDST , releaseDate , lc );
		dst.copyScope( this , src );
		return( SCOPESTATE.RunSuccess );
	}

}
