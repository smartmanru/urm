package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseDelivery;
import org.urm.engine.dist.ReleaseTarget;
import org.urm.engine.dist.ReleaseTargetItem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrConfItem;

public class ActionGetCumulative extends ActionBase {

	Meta meta;
	public Dist dist;
	
	public ActionGetCumulative( ActionBase action , String stream , Meta meta , Dist dist ) {
		super( action , stream , "Rebuild cumulative release=" + dist.RELEASEDIR );
		this.meta = meta;
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		dist.openForDataChange( this );
		dist.descopeAll( this );
		dist.saveReleaseXml( this );
		
		DistRepository repo = artefactory.getDistRepository( this , meta );

		// dists - source releases sorted from last to most earlier
		String[] versions = dist.release.getCumulativeVersions( this );
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
		info( "add cumulative release version=" + cumver + " ..." );
		
		if( !cumdist.isFinalized() ) {
			error( "cannot settle cumulative release from non-finalized release version=" + cumver );
			return( false );
		}
		
		dist.release.addRelease( this , cumdist.release );
		dist.release.rebuildDeliveries( this );
		cumdist.openForUse( this );
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
