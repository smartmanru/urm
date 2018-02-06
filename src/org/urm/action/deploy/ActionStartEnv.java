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

public class ActionStartEnv extends ActionBase {

	public ActionStartEnv( ActionBase action , String stream ) {
		super( action , stream , "Start environment" );
	}

	@Override protected void runBefore( ScopeState state , ActionScope scope ) throws Exception {
		infoAction( "start environment (" + getMode() + ") ..." );
		if( isExecute() )
			ActionSendChatMsg.sendMsg( state , this , "[startenv] starting " + scope.getScopeInfo( this ) + " ..." , context.env , context.sg );
	}

	@Override protected void runAfter( ScopeState state , ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( state , this , "[startenv] done." , context.env , context.sg );
		infoAction( "done." );
	}
	
	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		MetaEnvStartInfo startInfo = set.sg.getStartInfo();
		for( MetaEnvStartGroup group : startInfo.getForwardGroupList() ) {
			if( !startServerGroup( state , set , group , targets ) )
				ifexit( _Error.FailedGroupOperation0 , "failed group operation" , null );
		}
		
		// if specific run handle servers not covered by start groups 
		if( !set.setFull ) {
			for( ActionScopeTarget target : targets ) {
				MetaEnvStartGroup startGroup = target.envServer.getStartGroup();
				if( startGroup == null ) {
					ActionStartServer startOne = new ActionStartServer( this , target.NAME , target );
					if( !startOne.runSimpleEnv( state , target.envServer.sg.env , SecurityAction.ACTION_DEPLOY , false ) )
						ifexit( _Error.StartenvFailed0 , "unable to start server" , null );
				}
			}
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean startServerGroup( ScopeState state , ActionScopeSet set , MetaEnvStartGroup group , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> servers = set.getGroupServers( this , group );
		if( servers.isEmpty() ) {
			debug( "no servers specified to start in group=" + group.NAME );
			return( true );
		}
		
		// execute servers in parallel within subprocess
		infoAction( getMode() + " start group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( state , this , "start.sg" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStartServer startOne = new ActionStartServer( this , target.NAME , target );
			actions.runSimpleEnv( startOne , group.startInfo.sg.env , SecurityAction.ACTION_DEPLOY , false );
		}

		// wait all
		if( actions.waitDone() ) {
			infoAction( "group=" + group.NAME + " successfully started." );
			return( true );
		}
		
		errorAction( "group=" + group.NAME + " failed to start." );
		super.fail1( _Error.StartGroupFailed1 , "group=" + group.NAME + " failed to start" , group.NAME );
		return( false );
	}
	
}
