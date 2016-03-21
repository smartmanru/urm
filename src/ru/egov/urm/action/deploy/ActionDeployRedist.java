package ru.egov.urm.run.deploy;

import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;
import ru.egov.urm.storage.DistStorage;
import ru.egov.urm.storage.RedistStorage;

public class ActionDeployRedist extends ActionBase {

	DistStorage dist;
	Map<ActionScopeTargetItem,ServerDeployment> deployments;
	ActionScopeTarget[] affectedTargets;

	public ActionDeployRedist( ActionBase action , String stream , DistStorage dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		logAction( "execute dc=" + set.dc.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + set.getScopeInfo( this ) + "} ..." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !getDeployments( set , targets ) ) {
			log( "nothing to deploy in release " + dist.RELEASEDIR + " to specified server" );
			return( true );
		}
		
		if( !stopServers( set ) ) {
			if( !context.CTX_FORCE )
				exit( "unable to stop servers, cancel deployment." );
		}
		
		if( !rolloutServers( set ) ) {
			if( !context.CTX_FORCE )
				exit( "unable to rollout release, cancel deployment." );
		}
	
		if( !startServers( set ) ) {
			if( !context.CTX_FORCE )
				exit( "unable to start after deployment, unsuccessful deployment" );
		}

		log( "RELEASE " + dist.RELEASEDIR + " SUCCESSFULLY DEPLOYED" );
		return( true );
	}

	private boolean getDeployments( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		Map<String,ActionScopeTarget> affectedServers = new HashMap<String,ActionScopeTarget>();
		deployments = new HashMap<ActionScopeTargetItem,ServerDeployment>();
		boolean isEmpty = true;
		
		for( ActionScopeTarget target : set.getTargets( this ).values() ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			for( ActionScopeTargetItem item : target.getItems( this ) ) {
				RedistStorage redist = artefactory.getRedistStorage( this , target.envServer , item.envServerNode );
				ServerDeployment deployment = redist.getDeployment( this , dist.RELEASEDIR );
				if( !deployment.isEmpty( this ) )
					isEmpty = false;
				
				deployments.put( item , deployment );
				if( !affectedServers.containsKey( target.envServer.NAME ) )
					affectedServers.put( target.envServer.NAME , target );
			}
		}
		
		if( isEmpty )
			return( false );

		affectedTargets = affectedServers.values().toArray( new ActionScopeTarget[0] );
		return( true );
	}

	private boolean stopServers( ActionScopeSet set ) throws Exception {
		ActionStopEnv ca = new ActionStopEnv( this , null );
		return( ca.runTargetList( set , affectedTargets ) );
	}
	
	private boolean rolloutServers( ActionScopeSet set ) throws Exception {
		ActionRollout ca = new ActionRollout( this , null , dist );
		return( ca.runTargetList( set , affectedTargets ) );
	}
	
	private boolean startServers( ActionScopeSet set ) throws Exception {
		ActionStartEnv ca = new ActionStartEnv( this , null );
		return( ca.runTargetList( set , affectedTargets ) );
	}
	
}
