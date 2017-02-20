package org.urm.engine;

import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.Types.VarLCTYPE;

public class ServerBlotterReleaseItem extends ServerBlotterItem {

	public DistRepositoryItem repoItem;
	
	public String INFO_PRODUCT;
	public VarLCTYPE INFO_LCTYPE;
	
	public ServerBlotterReleaseItem( ServerBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( DistRepositoryItem repoItem ) {
		this.repoItem = repoItem;
		this.INFO_PRODUCT = repoItem.dist.meta.name;
		INFO_LCTYPE = repoItem.dist.release.getLifecycleType();
	}
	
}
