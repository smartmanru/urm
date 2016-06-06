package org.urm.action.deploy;

import java.util.HashMap;
import java.util.Map;

import org.urm.Common;
import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.dist.Dist;
import org.urm.storage.RedistStorage;

public class ActionDeployRedist extends ActionBase {

	Dist dist;
	Map<ActionScopeTargetItem,ServerDeployment> deployments;
	ActionScopeTarget[] affectedTargets;

	public ActionDeployRedist( ActionBase action , String stream , Dist dist ) {
		super( action , stream );
		this.dist = dist;
	}

	@Override protected void runBefore( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		infoAction( "execute dc=" + set.dc.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + set.getScopeInfo( this ) + "} ..." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !getDeployments( set , targets ) ) {
			info( "nothing to deploy in release " + dist.RELEASEDIR + " to specified server" );
			return( true );
		}
		
		if( !stopServers( set ) ) {
			ifexit( "unable to stop servers" );
		}
		
		if( !rolloutServers( set ) ) {
			ifexit( "unable to rollout release" );
		}
	
		if( !startServers( set ) ) {
			exit( "unable to start servers after deployment" );
		}

		info( "RELEASE " + dist.RELEASEDIR + " SUCCESSFULLY DEPLOYED" );
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
