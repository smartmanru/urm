package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.db.core.DBEnums.DBEnumScopeCategoryType;
import org.urm.db.release.DBRelease;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.ReleaseDistScope;
import org.urm.engine.dist.ReleaseDistScopeDelivery;
import org.urm.engine.dist.ReleaseDistScopeDeliveryItem;
import org.urm.engine.dist.ReleaseDistScopeSet;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.ProductReleases;
import org.urm.meta.release.Release;
import org.urm.meta.release.ReleaseRepository;

public class ActionAppendMaster extends ActionBase {

	public Release release;
	public Release masterRelease;
	
	public ActionAppendMaster( ActionBase action , String stream , Release release ) {
		super( action , stream , "Append release=" + release.RELEASEVER + " to master distributive" );
		this.release = release;
	}

	@Override 
	protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		EngineMethod method = super.method;
		
		Meta meta = release.getMeta();
		ProductReleases releases = meta.getReleases();
		synchronized( releases ) {
			// update repository
			ReleaseRepository repoUpdated = method.changeReleaseRepository( releases );
			Release releaseUpdated = method.changeRelease( repoUpdated , release );
			DistRepository distrepoUpdated = method.changeDistRepository( releases );
			Dist dist = distrepoUpdated.findDefaultDist( releaseUpdated );
			Dist master = distrepoUpdated.findDefaultMasterDist();

			// append dist
			if( !master.isFinalized() ) {
				super.fail0( _Error.NotFinalizedProd0 , "Unable to append to non-finalized master release" );
				return( SCOPESTATE.RunFail );
			}
			
			if( master.isCompleted() ) {
				super.fail0( _Error.CannotChangeCompletedProd0 , "Unable to append to completed master release" );
				return( SCOPESTATE.RunFail );
			}

			if( !dist.isCompleted() ) {
				super.fail0( _Error.CannotAppendIncompleteRelease0 , "Unable to append incomplete release" );
				return( SCOPESTATE.RunFail );
			}

			if( !appendMaster( method , distrepoUpdated , dist , master ) )
				return( SCOPESTATE.RunFail );

			// save
			DBRelease.setMasterVersion( method , this , master.release , releaseUpdated.RELEASEVER );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
	private boolean appendMaster( EngineMethod method , DistRepository repo , Dist dist , Dist master ) throws Exception {
		VersionInfo infoMaster = VersionInfo.getDistVersion( master );
		VersionInfo infoDist = VersionInfo.getDistVersion( dist );
		
		String prodSortVersion = infoMaster.getSortVersion();
		if( prodSortVersion.compareTo( infoDist.getSortVersion() ) >= 0 ) {
			super.fail1( _Error.CannotAppendOlderRelease1 , "Unable to append older release=" + dist.RELEASEDIR , dist.RELEASEDIR );
			return( false );
		}
		
		Dist next = repo.getNextDist( this , infoMaster );
		if( next == null ) {
			if( !super.isForced() ) {
				String name = infoMaster.getReleaseName();
				super.fail1( _Error.CannotFindMasterRelease1 , "Current master source release distributive not found, version=" + name + ", use -force to override" , name );
				return( false );
			}
		}
		else {
			VersionInfo infoNext = VersionInfo.getDistVersion( next );
			String nextVersion = infoNext.getFullVersion();
			if( !nextVersion.equals( infoDist.getFullVersion() ) ) {
				if( !super.isForced() ) {
					String name = infoNext.getReleaseName();
					super.fail1( _Error.CannotSkipRelease1 , "Unable to skip available release=" + name + ", use -force to override" , name );
					return( false );
				}
			}
		}
		
		copyFiles( method , repo , dist , master );
		return( true );
	}

	private void copyFiles( EngineMethod method , DistRepository repo , Dist dist , Dist distMaster ) throws Exception {
		Dist master = repo.copyDist( this , distMaster , distMaster.RELEASEDIR + "-new" , null );
		
		master.openForControl( this );
		appendMasterFiles( distMaster , dist );
		master.closeControl( this , DISTSTATE.RELEASED );
		
		repo.replaceDist( this , distMaster , master );
		masterRelease = master.release;
	}

	public void appendMasterFiles( Dist dist , Dist src ) throws Exception {
		ReleaseDistScope scope = ReleaseDistScope.createScope( src.release );
		for( ReleaseDistScopeSet set : scope.getSets() ) {
			if( set.CATEGORY == DBEnumScopeCategoryType.BINARY ) {
				for( ReleaseDistScopeDelivery delivery : set.getDeliveries() ) {
					for( ReleaseDistScopeDeliveryItem item : delivery.getItems() )
						dist.copyBinaryItem( this , src , item.binary , true );
				}
			}
		}
	}
	
}
