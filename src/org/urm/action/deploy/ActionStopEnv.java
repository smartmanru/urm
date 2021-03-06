package org.urm.action.deploy;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionSet;
import org.urm.common.Common;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaEnvStartInfo;

public class ActionStopEnv extends ActionBase {

	public ActionStopEnv( ActionBase action , String stream ) {
		super( action , stream , "Stop environment" );
	}

	@Override protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		infoAction( "stop environment (" + getMode() + ") ..." );
		if( isExecute() )
			ActionSendChatMsg.sendMsg( state , this , "[stopenv] stopping " + scope.getScopeInfo( this ) + " ..." , context.env , context.sg );
	}

	@Override protected void runAfter( ScopeState state , ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( state , this , "[stopenv] done." , context.env , context.sg );
		infoAction( "done." );
	}
	
	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		MetaEnvStartInfo startInfo = set.sg.getStartInfo();
		for( MetaEnvStartGroup group : startInfo.getReverseGroupList() ) {
			if( !stopServerGroup( state , set , group , targets ) )
				ifexit( _Error.FailedGroupOperation0 , "failed group operation" , null );
		}
		
		// if specific run handle servers not covered by start groups 
		if( !set.setFull ) {
			for( ActionScopeTarget target : targets ) {
				MetaEnvStartGroup startGroup = target.envServer.getStartGroup();
				if( startGroup == null ) {
					ActionStopServer stopOne = new ActionStopServer( this , target.NAME , target );
					if( !stopOne.runSimpleEnv( state , target.envServer.sg.env , SecurityAction.ACTION_DEPLOY , false ) )
						ifexit( _Error.StopenvFailed0 , "unable to stop server" , null );
				}
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean stopServerGroup( ScopeState state , ActionScopeSet set , MetaEnvStartGroup group , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> servers = set.getGroupServers( this , group );
		if( servers.isEmpty() ) {
			debug( "no servers specified to stop in group=" + group.NAME );
			return( true );
		}
		
		// execute servers in parallel within subprocess
		infoAction( getMode() + " stop group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( state , this , "stop.sg" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStopServer stopOne = new ActionStopServer( this , target.NAME , target );
			actions.runSimpleEnv( stopOne , set.sg.env , SecurityAction.ACTION_DEPLOY , false );
		}

		// wait all
		if( actions.waitDone() ) {
			infoAction( "group=" + group.NAME + " successfully stopped." );
			return( true );
		}
		
		errorAction( "group=" + group.NAME + " failed to stop." );
		super.fail1( _Error.StopGroupFailed1 , "group=" + group.NAME + " failed to stop" , group.NAME );
		return( false );
	}
	
}
