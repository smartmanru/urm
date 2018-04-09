package org.urm.engine.run;

import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
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

		ReleaseRepository repo = emm.getReleaseRepository();
		releaseNew = release.copy( repo );
		repo.replaceRelease( releaseNew );
		
		release.modify( false );
		releaseOld = release;
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
		if( releaseOld != null )
			releaseOld.modify( true );
		
		DistRepository distrepo = emm.getDistRepository();
		if( releaseNew != null ) {
			ReleaseDist releaseDist = releaseNew.getDefaultReleaseDist();
			Dist dist = distrepo.findDefaultDist( releaseNew );
			dist.setReleaseDist( releaseDist );
		}
	}
	
	public void abort() throws Exception {
		if( releaseOld != null )
			releaseOld.modify( true );
	}
	
}
