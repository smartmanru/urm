package org.urm.engine.run;

import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.DistRepositoryItem;

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
	
	public void setUpdated() throws Exception {
		update = true;
		if( itemNew != null )
			return;

		item.modify( false );
		itemOld = item;
		
		DistRepository repo = emm.getDistRepository();
		itemNew = item.copy( repo );
		repo.replaceItem( itemNew );
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
	}
	
	public void abort() throws Exception {
		if( itemOld != null )
			itemOld.modify( true );
	}
	
}
