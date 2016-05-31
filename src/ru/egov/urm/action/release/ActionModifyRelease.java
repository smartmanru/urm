package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;

public class ActionModifyRelease extends ActionBase {

	public Dist release;
	
	public ActionModifyRelease( ActionBase action , String stream , Dist release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.openForChange( this );
		release.release.setProperties( this );
		release.saveReleaseXml( this );
		release.closeChange( this );
		return( true );
	}
	
}
