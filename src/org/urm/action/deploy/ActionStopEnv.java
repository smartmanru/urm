package org.urm.action.deploy;

import java.util.List;

import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionSet;
import org.urm.common.Common;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.product.MetaEnvStartGroup;

public class ActionStopEnv extends ActionBase {

	public ActionStopEnv( ActionBase action , String stream ) {
		super( action , stream , "Stop environment" );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		infoAction( "stop environment (" + getMode() + ") ..." );
		if( isExecute() )
			ActionSendChatMsg.sendMsg( this , "[stopenv] stopping " + scope.getScopeInfo( this ) + " ..." , context.env , context.sg );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( this , "[stopenv] done." , context.env , context.sg );
		infoAction( "done." );
	}
	
	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		for( MetaEnvStartGroup group : set.sg.startInfo.getReverseGroupList() ) {
			if( !stopServerGroup( set , group , targets ) )
				ifexit( _Error.FailedGroupOperation0 , "failed group operation" , null );
		}
		
		return( SCOPESTATE.RunSuccess );
	}

	private boolean stopServerGroup( ActionScopeSet set , MetaEnvStartGroup group , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> servers = set.getGroupServers( this , group );
		if( servers.isEmpty() ) {
			debug( "no servers specified to stop in group=" + group.NAME );
			return( true );
		}
		
		// execute servers in parallel within subprocess
		infoAction( getMode() + " stop group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( this , "stop.sg" );
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
