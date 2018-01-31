package org.urm.action.deploy;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.dist.Dist;
import org.urm.engine.dist.VersionInfo;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.engine.storage.RedistStorage;

public class ActionDeployRedist extends ActionBase {

	Dist dist;
	Map<ActionScopeTargetItem,ServerDeployment> deployments;
	ActionScopeTarget[] affectedTargets;

	public ActionDeployRedist( ActionBase action , String stream , Dist dist ) {
		super( action , stream , "Deploy from redist, release=" + dist.RELEASEDIR );
		this.dist = dist;
	}

	@Override protected void runBefore( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		infoAction( "execute sg=" + set.sg.NAME + ", releasedir=" + dist.RELEASEDIR + ", servers={" + set.getScopeInfo( this ) + "} ..." );
	}
	
	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		if( !getDeployments( set , targets ) ) {
			info( "nothing to deploy in release " + dist.RELEASEDIR + " to specified server" );
			return( SCOPESTATE.NotRun );
		}
		
		if( !stopServers( state , set ) ) {
			ifexit( _Error.UnableStopServers0 , "unable to stop servers" , null );
		}
		
		if( !rolloutServers( state , set ) ) {
			ifexit( _Error.UnableRolloutRelease0 , "unable to rollout release" , null );
		}
	
		if( !startServers( state , set ) ) {
			exit0( _Error.UnableStartAfterSeployment0 , "unable to start servers after deployment" );
		}

		info( "RELEASE " + dist.RELEASEDIR + " SUCCESSFULLY DEPLOYED" );
		return( SCOPESTATE.RunSuccess );
	}

	private boolean getDeployments( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		Map<String,ActionScopeTarget> affectedServers = new HashMap<String,ActionScopeTarget>();
		deployments = new HashMap<ActionScopeTargetItem,ServerDeployment>();
		boolean isEmpty = true;
		
		VersionInfo version = VersionInfo.getDistVersion( dist ); 
		for( ActionScopeTarget target : set.getTargets( this ).values() ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			for( ActionScopeTargetItem item : target.getItems( this ) ) {
				RedistStorage redist = artefactory.getRedistStorage( this , target.envServer , item.envServerNode );
				ServerDeployment deployment = redist.getDeployment( this , version );
				if( !deployment.isEmpty( this ) ) {
					isEmpty = false;
				
					deployments.put( item , deployment );
					if( !affectedServers.containsKey( target.envServer.NAME ) )
						affectedServers.put( target.envServer.NAME , target );
				}
			}
		}
		
		if( isEmpty )
			return( false );

		affectedTargets = affectedServers.values().toArray( new ActionScopeTarget[0] );
		return( true );
	}

	private boolean stopServers( ScopeState parentState , ActionScopeSet set ) throws Exception {
		ActionStopEnv ca = new ActionStopEnv( this , null );
		return( ca.runTargetList( parentState , set , affectedTargets , set.sg.env , SecurityAction.ACTION_DEPLOY , false ) );
	}
	
	private boolean rolloutServers( ScopeState parentState , ActionScopeSet set ) throws Exception {
		ActionRollout ca = new ActionRollout( this , null , dist );
		return( ca.runTargetList( parentState , set , affectedTargets , set.sg.env , SecurityAction.ACTION_DEPLOY , false ) );
	}
	
	private boolean startServers( ScopeState parentState , ActionScopeSet set ) throws Exception {
		ActionStartEnv ca = new ActionStartEnv( this , null );
		return( ca.runTargetList( parentState , set , affectedTargets , set.sg.env , SecurityAction.ACTION_DEPLOY , false ) );
	}
	
}
