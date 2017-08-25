package org.urm.engine;

import org.urm.common.Common;
import org.urm.engine.dist.DistRepositoryItem;
import org.urm.engine.dist.VersionInfo;

public class ServerBlotterReleaseItem extends ServerBlotterItem {

	public DistRepositoryItem repoItem;
	
	public String INFO_PRODUCT;
	public String SORTKEY;
	
	public ServerBlotterReleaseItem( ServerBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( DistRepositoryItem repoItem ) {
		this.repoItem = repoItem;
		this.INFO_PRODUCT = repoItem.repo.meta.name;
		SORTKEY = getSortKey();
	}
	
	private String getSortKey() {
		String RELEASEVER = VersionInfo.getReleaseVersion( repoItem.RELEASEDIR );
		String RELEASEVARIANT = VersionInfo.getReleaseVariant( repoItem.RELEASEDIR );
		String[] version = Common.splitDotted( RELEASEVER );
		for( int k = 0; k < version.length; k++ ) {
			String s = "0000000000" + version[ k ];
			version[ k ] = s.substring( version[ k ].length() );
		}
			 
		return( repoItem.repo.meta.name + "-" + Common.getList( version , "." ) + "-" + RELEASEVARIANT );
	}
	
}
