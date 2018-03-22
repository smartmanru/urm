package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;

public class ActionAppendMaster extends ActionBase {

	public Dist dist;
	public Dist master;
	
	public ActionAppendMaster( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Append release=" + dist.RELEASEDIR + " to master distributive" );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Dist prod = super.getMasterDist( dist.meta );
		if( !prod.isFinalized() ) {
			super.fail0( _Error.NotFinalizedProd0 , "Unable to append to non-finalized master release" );
			return( SCOPESTATE.RunFail );
		}
			
		if( prod.isCompleted() ) {
			super.fail0( _Error.CannotChangeCompletedProd0 , "Unable to append to completed master release" );
			return( SCOPESTATE.RunFail );
		}

		if( !dist.isCompleted() ) {
			super.fail0( _Error.CannotAppendIncompleteRelease0 , "Unable to append incomplete release" );
			return( SCOPESTATE.RunFail );
		}
		
		DistRepository repo = dist.repo;
		VersionInfo infoProd = VersionInfo.getDistVersion( prod );
		VersionInfo infoDist = VersionInfo.getDistVersion( dist );
		String prodSortVersion = infoProd.getSortVersion();
		if( prodSortVersion.compareTo( infoDist.getSortVersion() ) >= 0 ) {
			super.fail1( _Error.CannotAppendOlderRelease1 , "Unable to append older release=" + dist.RELEASEDIR , dist.RELEASEDIR );
			return( SCOPESTATE.RunFail );
		}
		
		Dist next = repo.getNextDist( this , infoProd );
		if( next == null ) {
			if( !super.isForced() ) {
				String name = infoProd.getReleaseName();
				super.fail1( _Error.CannotFindMasterRelease1 , "Current master source release distributive not found, version=" + name + ", use -force to override" , name );
				return( SCOPESTATE.RunFail );
			}
		}
		else {
			VersionInfo infoNext = VersionInfo.getDistVersion( next );
			String nextVersion = infoNext.getFullVersion();
			if( !nextVersion.equals( infoDist.getFullVersion() ) ) {
				if( !super.isForced() ) {
					String name = infoNext.getReleaseName();
					super.fail1( _Error.CannotSkipRelease1 , "Unable to skip available release=" + name + ", use -force to override" , name );
					return( SCOPESTATE.RunFail );
				}
			}
		}
		
		copyFiles( prod , repo );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void copyFiles( Dist distMaster , DistRepository repo ) throws Exception {
		master = repo.copyDist( this , distMaster , distMaster.RELEASEDIR + "-new" , null );
		
		master.openForControl( this );
		master.appendMasterFiles( this , dist );
		master.saveReleaseXml( this );
		master.closeControl( this , DISTSTATE.RELEASED );
		
		repo.replaceDist( this , distMaster , master );
	}
	
}
