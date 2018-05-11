package org.urm.engine.run;

import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.release.ReleaseDist;
import org.urm.meta.release.ReleaseRepository;

public class EngineMethodMetaDistItem {

	public EngineMethodMeta emm;
	public DistRepositoryItem item;
	
	public DistRepositoryItem itemNew;
	public DistRepositoryItem itemOld;
	
	public boolean create;
	public boolean update;
	public boolean delete;
	
	public EngineMethodMetaDistItem( EngineMethodMeta emm , DistRepositoryItem item ) {
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
	
	public void setUpdated( ReleaseRepository repoReleases ) throws Exception {
		update = true;
		if( itemNew != null )
			return;

		DistRepository repo = emm.getDistRepository();
		ReleaseDist releaseDist = repoReleases.findReleaseDist( item.dist );
		itemNew = item.copy( repo , releaseDist );
		repo.replaceItem( item , itemNew );
		
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
		
		ReleaseRepository releaserepo = emm.getReleaseRepository();
		if( itemNew != null ) {
			Dist dist = itemNew.dist;
			ReleaseDist releaseDist = releaserepo.findReleaseDist( dist );
			dist.setReleaseDist( releaseDist );
		}
	}
	
	public void abort() throws Exception {
		if( itemOld != null )
			itemOld.modify( true );
	}
	
}
