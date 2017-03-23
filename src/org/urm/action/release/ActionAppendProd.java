package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.VersionInfo;

public class ActionAppendProd extends ActionBase {

	public Dist dist;
	
	public ActionAppendProd( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Append release=" + dist.RELEASEDIR + " to master distributive" );
		this.dist = dist;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		Dist prod = super.getMasterDist( dist.meta );
		if( !prod.isFinalized() ) {
			super.fail0( _Error.NotFinalizedProd0 , "Unable to append to non-finalyzed master release" );
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
		
		DistRepository repo = artefactory.getDistRepository( this , dist.meta );
		VersionInfo infoProd = VersionInfo.getReleaseVersion( this , prod.RELEASEDIR );
		VersionInfo infoDist = VersionInfo.getReleaseVersion( this , dist.RELEASEDIR );
		String prodSortVersion = infoProd.getSortVersion();
		if( prodSortVersion.compareTo( infoDist.getSortVersion() ) >= 0 ) {
			super.fail1( _Error.CannotAppendOlderRelease1 , "Unable to append older release=" + dist.RELEASEDIR , dist.RELEASEDIR );
			return( SCOPESTATE.RunFail );
		}
		
		Dist next = repo.getNextDist( this , infoProd );
		if( next == null )
			super.exitUnexpectedState();
			
		VersionInfo infoNext = VersionInfo.getReleaseVersion( this , prod.RELEASEDIR );
		String nextVersion = infoNext.getFullVersion();
		if( !nextVersion.equals( infoProd.getFullVersion() ) ) {
			if( !super.isForced() ) {
				String name = infoNext.getReleaseName();
				super.fail1( _Error.CannotSkipRelease1 , "Unable to skip available release=" + name + ", use -force to override" , name );
				return( SCOPESTATE.RunFail );
			}
		}
		
		copyFiles( prod , repo );
		
		return( SCOPESTATE.RunSuccess );
	}

	private void copyFiles( Dist prod , DistRepository repo ) throws Exception {
		Dist masterNew = repo.copyDist( this , prod , prod.RELEASEDIR + "-new" );
		
		masterNew.openForDataChange( this );
		masterNew.saveReleaseXml( this );
		masterNew.appendMasterFiles( this , dist );
		masterNew.saveReleaseXml( this );
		masterNew.closeDataChange( this );
		
		repo.replaceDist( this , prod , masterNew );
	}
	
}