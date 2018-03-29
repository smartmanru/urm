package org.urm.engine.run;

import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodMetaRelease {

	public EngineMethodMeta emm;
	private  Release release;
	
	public Release releaseNew;
	public Release releaseOld;
	
	public boolean create;
	public boolean update;
	public boolean delete;
	
	public EngineMethodMetaRelease( EngineMethodMeta emm , Release release ) {
		this.emm = emm;
		this.release = release;
		create = false;
		update = false;
		delete = false;
	}

	public void setCreated() throws Exception {
		create = true;
		this.releaseNew = release;
	}
	
	public void setUpdated() throws Exception {
		update = true;
		if( releaseNew != null )
			return;

		release.modify( false );
		releaseOld = release;
		
		ReleaseRepository repo = emm.getReleaseRepository();
		release.modify( false );
		releaseNew = release.copy( repo );
		repo.replaceRelease( releaseNew );
	}
	
	public void setDeleted() throws Exception {
		delete = true;
		release.modify( false );
		releaseOld = release;
	}

	public void commit() throws Exception {
		ReleaseRepository repo = emm.getReleaseRepository();
		if( releaseNew != null && releaseNew.repo != repo )
			releaseNew.setRepository( repo );
	}
	
	public void abort() throws Exception {
		if( releaseOld != null )
			releaseOld.modify( true );
	}
	
}
