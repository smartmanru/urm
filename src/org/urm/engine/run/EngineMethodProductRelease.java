package org.urm.engine.run;

import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodProductRelease {

	public EngineMethodProduct emm;
	private  Release release;
	
	public Release releaseNew;
	public Release releaseOld;
	
	public boolean create;
	public boolean update;
	public boolean delete;
	
	public EngineMethodProductRelease( EngineMethodProduct emm , Release release ) {
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

		releaseNew = release;
		release.modify( false );
		releaseOld = release;
	}
	
	public void setDeleted() throws Exception {
		delete = true;
		release.modify( false );
		releaseOld = release;
	}

	public void commit() throws Exception {
		if( releaseNew != null ) {
			Meta meta = release.getMeta();
			ReleaseRepository repo = emm.getReleaseRepository( meta.getStorage() );
			if( releaseNew.repo != repo )
				releaseNew.setRepository( repo );
		}
			
		if( releaseOld != null )
			releaseOld.modify( true );
		
		DistRepository distrepo = emm.getDistRepository();
		if( releaseNew != null ) {
			ReleaseDist releaseDist = releaseNew.getDefaultReleaseDist();
			Dist dist = distrepo.findDefaultDist( releaseNew );
			if( dist != null )
				dist.setReleaseDist( releaseDist );
		}
	}
	
	public void abort() throws Exception {
		if( releaseOld != null )
			releaseOld.modify( true );
	}
	
}
