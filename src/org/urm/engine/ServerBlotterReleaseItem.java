package org.urm.engine;

import org.urm.common.Common;
import org.urm.engine.dist.DistRepositoryItem;

public class ServerBlotterReleaseItem extends ServerBlotterItem {

	public DistRepositoryItem repoItem;
	
	public String INFO_PRODUCT;
	public String INFO_TYPE;
	
	public ServerBlotterReleaseItem( ServerBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( DistRepositoryItem repoItem ) {
		this.repoItem = repoItem;
		this.INFO_PRODUCT = repoItem.dist.meta.name;
		
		int count = Common.splitDotted( Common.getPartBeforeFirst( repoItem.dist.RELEASEDIR , "-" ) ).length;
		String type = "";
		if( count == 1 || count == 2 )
			type = "major";
		else
		if( count == 3 )
			type = "minor";
		else
		if( count == 4 )
			type = "patch";
		this.INFO_TYPE = type;
	}
	
}
