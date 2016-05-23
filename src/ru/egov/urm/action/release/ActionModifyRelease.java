package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.storage.DistStorage;

public class ActionModifyRelease extends ActionBase {

	public DistStorage release;
	
	public ActionModifyRelease( ActionBase action , String stream , DistStorage release ) {
		super( action , stream );
		this.release = release;
	}

	@Override protected boolean executeSimple() throws Exception {
		release.openForChange( this );
		release.info.setProperties( this );
		release.saveReleaseXml( this );
		return( true );
	}
	
}
