package org.urm.action.release;

import org.urm.action.ActionBase;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.dist.DistState.DISTSTATE;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.product.Meta;
import org.urm.meta.release.Release;

public class ActionAppendMaster extends ActionBase {

	public Release release;
	public Release masterRelease;
	
	public ActionAppendMaster( ActionBase action , String stream , Release release ) {
		super( action , stream , "Append release=" + release.RELEASEVER + " to master distributive" );
		this.release = release;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		Meta meta = release.getMeta();
		DistRepository repo = meta.getDistRepository();
		Dist master = repo.findDefaultMasterDist();
		if( !master.isFinalized() ) {
			super.fail0( _Error.NotFinalizedProd0 , "Unable to append to non-finalized master release" );
			return( SCOPESTATE.RunFail );
		}
			
		if( master.isCompleted() ) {
			super.fail0( _Error.CannotChangeCompletedProd0 , "Unable to append to completed master release" );
			return( SCOPESTATE.RunFail );
		}

		Dist dist = repo.findDefaultDist( release );
		if( !dist.isCompleted() ) {
			super.fail0( _Error.CannotAppendIncompleteRelease0 , "Unable to append incomplete release" );
			return( SCOPESTATE.RunFail );
		}
		
		VersionInfo infoMaster = VersionInfo.getDistVersion( master );
		VersionInfo infoDist = VersionInfo.getDistVersion( dist );
		String prodSortVersion = infoMaster.getSortVersion();
		if( prodSortVersion.compareTo( infoDist.getSortVersion() ) >= 0 ) {
			super.fail1( _Error.CannotAppendOlderRelease1 , "Unable to append older release=" + dist.RELEASEDIR , dist.RELEASEDIR );
			return( SCOPESTATE.RunFail );
		}
		
		Dist next = repo.getNextDist( this , infoMaster );
		if( next == null ) {
			if( !super.isForced() ) {
				String name = infoMaster.getReleaseName();
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
		
		copyFiles( dist , master , repo );
		return( SCOPESTATE.RunSuccess );
	}

	private void copyFiles( Dist dist , Dist distMaster , DistRepository repo ) throws Exception {
		Dist master = repo.copyDist( this , distMaster , distMaster.RELEASEDIR + "-new" , null );
		
		master.openForControl( this );
		master.appendMasterFiles( this , dist );
		master.saveMetaFile( this );
		master.closeControl( this , DISTSTATE.RELEASED );
		
		repo.replaceDist( this , distMaster , master );
		masterRelease = master.release;
	}
	
}
