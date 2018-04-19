package org.urm.engine.run;

import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodProductDist {

	public EngineMethodProduct emm;
	public DistRepositoryItem item;
	
	public DistRepositoryItem itemNew;
	public DistRepositoryItem itemOld;
	
	public boolean create;
	public boolean update;
	public boolean delete;
	
	public EngineMethodProductDist( EngineMethodProduct emm , DistRepositoryItem item ) {
		this.emm = emm;
		this.item = item;
		create = false;
		update = false;
		delete = false;
	}

	public void setCreated() throws Exception {
		create = true;
		this.itemNew = item;
	}
	
	public void setUpdated() throws Exception {
		update = true;
		if( itemNew != null )
			return;

		itemNew = item;
		item.modify( false );
		itemOld = item;
	}
	
	public void setDeleted() throws Exception {
		delete = true;
		item.modify( false );
		itemOld = item;
	}
	
	public void commit() throws Exception {
		DistRepository repo = emm.getDistRepository();
		if( itemNew != null && itemNew.repo != repo )
			itemNew.setRepository( repo );
		if( itemOld != null )
			itemOld.modify( true );
		
		if( itemNew != null ) {
			Dist dist = itemNew.dist;
			ReleaseRepository releases = emm.getReleaseRepository( dist.meta.getStorage() );
			Release release = releases.getRelease( dist.release.ID );
			ReleaseDist releaseDist = release.getDistVariant( dist.releaseDist.ID );
			dist.setReleaseDist( releaseDist );
		}
	}
	
	public void abort() throws Exception {
		if( itemOld != null )
			itemOld.modify( true );
	}
	
}
