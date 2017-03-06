package org.urm.engine;

import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.meta.Types.VarLCTYPE;

public class ServerBlotterReleaseItem extends ServerBlotterItem {

	public DistRepositoryItem repoItem;
	
	public String INFO_PRODUCT;
	public VarLCTYPE INFO_LCTYPE;
	public String SORTKEY;
	
	public ServerBlotterReleaseItem( ServerBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( DistRepositoryItem repoItem ) {
		this.repoItem = repoItem;
		this.INFO_PRODUCT = repoItem.dist.meta.name;
		INFO_LCTYPE = repoItem.dist.release.getLifecycleType();
		SORTKEY = getSortKey();
	}
	
	private String getSortKey() {
		Dist dist = repoItem.dist;
		String[] version = Common.splitDotted( dist.release.RELEASEVER );
		for( int k = 0; k < version.length; k++ ) {
			String s = "0000000000" + version[ k ];
			version[ k ] = s.substring( version[ k ].length() );
		}
			 
		return( dist.meta.name + "-" + Common.getList( version , "." ) + "-" + dist.RELEASEDIR );
	}
	
}
