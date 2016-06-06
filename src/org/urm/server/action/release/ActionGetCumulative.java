package org.urm.server.action.release;

import org.urm.server.action.ActionBase;
import org.urm.server.dist.Dist;
import org.urm.server.dist.DistRepository;
import org.urm.server.dist.ReleaseDelivery;
import org.urm.server.dist.ReleaseTarget;
import org.urm.server.dist.ReleaseTargetItem;
import org.urm.server.meta.MetaDistrBinaryItem;
import org.urm.server.meta.MetaDistrConfItem;

public class ActionGetCumulative extends ActionBase {

	Dist dist;
	
	public ActionGetCumulative( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected boolean executeSimple() throws Exception {
		dist.openForChange( this );
		dist.descopeAll( this );
		dist.saveReleaseXml( this );
		
		DistRepository repo = artefactory.getDistRepository( this );

		// dists - source releases sorted from last to most earlier
		String[] versions = dist.release.getCumulativeVersions( this );
		Dist[] dists = new Dist[ versions.length ];
		for( int k = 0; k < versions.length; k++ ) {
			Dist cumdist = repo.getDistByLabel( this , versions[ k ] );
			dists[ versions.length - k - 1 ] = cumdist;
			
			if( !addCumulativeVersion( repo , versions[ k ] , cumdist ) ) {
				super.setFailed();
				dist.closeChange( this );
				return( true );
			}	
		}

		copyFiles( dists );
		
		dist.saveReleaseXml( this );
		dist.closeChange( this );
		return( true );
	}

	private boolean addCumulativeVersion( DistRepository repo , String cumver , Dist cumdist ) throws Exception {
		info( "add cumulative release version=" + cumver + " ..." );
		
		if( !cumdist.isFinalized( this ) ) {
			error( "cannot settle cumulative release from non-finalized release version=" + cumver );
			return( false );
		}
		
		dist.release.addRelease( this , cumdist.release );
		dist.release.rebuildDeliveries( this );
		cumdist.open( this );
		return( true );
	}
	
	private void copyFiles( Dist[] cumdists ) throws Exception {
		for( ReleaseDelivery delivery : dist.release.getDeliveries( this ).values() ) {
			if( delivery.hasDatabaseItems( this ) )
				copyDatabaseItems( cumdists , delivery );
			for( ReleaseTargetItem item : delivery.getProjectItems( this ).values() )
				copyBinaryItem( cumdists , delivery , item.distItem );
			for( ReleaseTarget item : delivery.getManualItems( this ).values() )
				copyBinaryItem( cumdists , delivery , item.distManualItem );
			for( ReleaseTarget item : delivery.getConfItems( this ).values() )
				copyConfItem( cumdists , delivery , item.distConfItem );
		}
	}

	private void copyDatabaseItems( Dist[] cumdists , ReleaseDelivery delivery ) throws Exception {
		for( Dist cumdist : cumdists )
			dist.copyDatabaseDistrToDistr( this , delivery , cumdist );
	}
	
	private void copyBinaryItem( Dist[] cumdists , ReleaseDelivery delivery , MetaDistrBinaryItem item ) throws Exception {
		for( Dist cumdist : cumdists ) {
			String file = cumdist.getBinaryDistItemFile( this , item );
			if( !file.isEmpty() ) {
				dist.copyBinaryDistrToDistr( this , delivery , cumdist , file );
				return;
			}
		}
	}
	
	private void copyConfItem( Dist[] cumdists , ReleaseDelivery delivery , MetaDistrConfItem item ) throws Exception {
		// find last full
		int lastIndex = cumdists.length - 1;
		for( int k = 0; k < cumdists.length; k++ ) {
			ReleaseTarget target = cumdists[k].release.findConfComponent( this , item.KEY );
			if( target != null && target.ALL ) {
				lastIndex = k;
				break;
			}
		}
		
		for( int k = lastIndex; k >= 0; k-- ) {
			Dist cumdist = cumdists[k];
			ReleaseTarget target = cumdist.release.findConfComponent( this , item.KEY );
			if( target == null )
				continue;
			
			dist.appendConfDistrToDistr( this , delivery , cumdist , item );
		}
	}
	
}
