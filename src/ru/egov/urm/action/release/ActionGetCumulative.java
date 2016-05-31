package ru.egov.urm.action.release;

import ru.egov.urm.action.ActionBase;
import ru.egov.urm.dist.Dist;
import ru.egov.urm.dist.DistRepository;
import ru.egov.urm.dist.Release;

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
		Release release = dist.release;
		
		String[] versions = release.getCumulativeVersions( this );
		for( String version : versions ) {
			if( !addCumulativeVersion( repo , release , version ) ) {
				super.setFailed();
				break;
			}	
		}

		dist.closeChange( this );
		return( true );
	}

	private boolean addCumulativeVersion( DistRepository repo , Release release , String version ) throws Exception {
		Dist dist = repo.getDistByLabel( this , version );
		if( !dist.isFinalized( this ) ) {
			error( "cannot settle cumulative release from non-finalized release version=" + version );
			return( false );
		}
		
		return( true );
	}
	
}
