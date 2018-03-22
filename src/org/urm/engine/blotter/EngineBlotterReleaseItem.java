package org.urm.engine.blotter;

import org.urm.common.Common;
import org.urm.meta.release.Release;

public class EngineBlotterReleaseItem extends EngineBlotterItem {

	public Release release;
	
	public String INFO_PRODUCT;
	public String SORTKEY;
	
	public EngineBlotterReleaseItem( EngineBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( Release release ) {
		this.release = release;
		this.INFO_PRODUCT = release.repo.meta.name;
		SORTKEY = getSortKey();
	}
	
	private String getSortKey() {
		String RELEASEVER = release.RELEASEVER;
		String[] version = Common.splitDotted( RELEASEVER );
		for( int k = 0; k < version.length; k++ ) {
			String s = "0000000000" + version[ k ];
			version[ k ] = s.substring( version[ k ].length() );
		}
			 
		return( release.repo.meta.name + "-" + Common.getListDotted( version ) );
	}
	
}
