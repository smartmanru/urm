package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBReleaseScope;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class ActionGetCumulative extends ActionBase {

	public Release release;
	
	public ActionGetCumulative( ActionBase action , String stream , Release release ) {
		super( action , stream , "Rebuild cumulative release=" + release.RELEASEVER );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Meta meta = release.getMeta();
		EngineProduct ep = meta.getEngineProduct();
		DistRepository repo = ep.getDistRepository();
		Dist dist = repo.findDefaultDist( release );
		
		dist.openForDataChange( this );
		
		DBReleaseScope.descopeAll( super.method , this , release );
		dist.saveMetaFile( this );
		
		// dists - source releases sorted from last to most earlier
		String[] versions = dist.release.getCumulativeVersions();
		Dist[] dists = new Dist[ versions.length ];
		for( int k = 0; k < versions.length; k++ ) {
			Dist cumdist = repo.getDistByLabel( this , meta , versions[ k ] );
			dists[ versions.length - k - 1 ] = cumdist;
			
			if( !addCumulativeVersion( repo , versions[ k ] , cumdist ) ) {
				super.fail1( _Error.AddCumulativeVersionFailed1 , "Cannot add to cumulative release version=" + versions[ k ] , versions[ k ] );
				dist.closeDataChange( this );
				return( SCOPESTATE.RunSuccess );
			}	
		}

		copyFiles( dists );
		
		dist.saveMetaFile( this );
		dist.closeDataChange( this );
		return( SCOPESTATE.RunSuccess );
	}

	private boolean addCumulativeVersion( DistRepository repo , String cumver , Dist cumdist ) throws Exception {
		return( false );
	}
	
	private void copyFiles( Dist[] cumdists ) throws Exception {
		Common.exitUnexpected();
	}

}
