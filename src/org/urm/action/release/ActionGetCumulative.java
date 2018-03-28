package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class ActionGetCumulative extends ActionBase {

	Meta meta;
	public Release release;
	
	public ActionGetCumulative( ActionBase action , String stream , Meta meta , Release release ) {
		super( action , stream , "Rebuild cumulative release=" + release.RELEASEVER );
		this.meta = meta;
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Meta meta = release.getMeta();
		DistRepository repo = meta.getDistRepository();
		Dist dist = repo.findDefaultDist( release );
		dist.openForDataChange( this );
		DBRelease.descopeAll( super.method , this , release );
		dist.saveReleaseXml( this );
		
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
