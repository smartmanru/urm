package org.urm.engine.blotter;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineDirectory;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class EngineBlotterReleaseItem extends EngineBlotterItem {

	public int productId;
	public int repoId;
	public int metaId;
	public int releaseId;
	
	public String SORTKEY;
	
	public EngineBlotterReleaseItem( EngineBlotterSet blotterSet , String ID ) {
		super( blotterSet , ID );
	}

	public void createReleaseItem( Release release ) {
		this.releaseId = release.ID;
		this.repoId = release.repo.ID;
		Meta meta = release.getMeta();
		this.metaId = meta.getId();
		AppProduct product = meta.getProduct();
		this.productId = product.ID;
		SORTKEY = getSortKey( release );
	}
	
	private String getSortKey( Release release ) {
		String RELEASEVER = release.RELEASEVER;
		String[] version = Common.splitDotted( RELEASEVER );
		for( int k = 0; k < version.length; k++ ) {
			String s = "0000000000" + version[ k ];
			version[ k ] = s.substring( version[ k ].length() );
		}
			 
		return( release.repo.meta.name + "-" + Common.getListDotted( version ) );
	}

	public Release getRelease( ActionBase action ) {
		try {
			DataService data = super.blotterSet.blotter.engine.getData();
			EngineDirectory directory = data.getDirectory();
			AppProduct product = directory.findProduct( productId );
			Meta meta = product.getMeta( action );
			ReleaseRepository repo = meta.getReleaseRepository();
			Release release = repo.getRelease( releaseId );
			return( release );
		}
		catch( Throwable e ) {
			action.log( "get release" , e );
			return( null );
		}
	}

	public AppProduct getProduct( ActionBase action ) {
		DataService data = super.blotterSet.blotter.engine.getData();
		EngineDirectory directory = data.getDirectory();
		AppProduct product = directory.findProduct( productId );
		return( product );
	}
	
}
