package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;

public class ActionGetCumulative extends ActionBase {

	Meta meta;
	public Dist dist;
	
	public ActionGetCumulative( ActionBase action , String stream , Meta meta , Dist dist ) {
		super( action , stream , "Rebuild cumulative release=" + dist.RELEASEDIR );
		this.meta = meta;
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		dist.openForDataChange( this );
		dist.descopeAll( this );
		dist.saveReleaseXml( this );
		
		DistRepository repo = meta.getDistRepository();

		// dists - source releases sorted from last to most earlier
		String[] versions = dist.release.getCumulativeVersions();
		Dist[] dists = new Dist[ versions.length ];
		for( int k = 0; k < versions.length; k++ ) {
			Dist cumdist = repo.getDistByLabel( this , versions[ k ] );
			dists[ versions.length - k - 1 ] = cumdist;
			
			if( !addCumulativeVersion( repo , versions[ k ] , cumdist ) ) {
				super.fail1( _Error.AddCumulativeVersionFailed1 , "Cannot add to cumulative release version=" + versions[ k ] , versions[ k ] );
				dist.closeDataChange( this );
				return( SCOPESTATE.RunSuccess );
			}	
		}

		copyFiles( dists );
		
		dist.saveReleaseXml( this );
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
