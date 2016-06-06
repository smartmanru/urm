package org.urm.server.action.deploy;

import java.util.List;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScope;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionSet;
import org.urm.server.meta.MetaEnvStartGroup;

public class ActionStopEnv extends ActionBase {

	public ActionStopEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		infoAction( "stop environment (" + getMode() + ") ..." );
		if( isExecute() )
			ActionSendChatMsg.sendMsg( this , "[stopenv] stopping " + scope.getScopeInfo( this ) + " ..." , null );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( this , "[stopenv] done." , null );
		infoAction( "done." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<MetaEnvStartGroup> groups = set.dc.startInfo.getReverseGroupList( this );
		for( MetaEnvStartGroup group : groups ) {
			if( !stopServerGroup( set , group , targets ) )
				ifexit( "failed group operation" );
		}
		
		return( true );
	}

	private boolean stopServerGroup( ActionScopeSet set , MetaEnvStartGroup group , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> servers = set.getGroupServers( this , group );
		if( servers.isEmpty() ) {
			debug( "no servers specified to stop in group=" + group.NAME );
			return( true );
		}
		
		// execute servers in parallel within subprocess
		infoAction( getMode() + " stop group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( this , "stop.dc" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStopServer stopOne = new ActionStopServer( this , target.NAME , target );
			actions.runSimple( stopOne );
		}

		// wait all
		if( actions.waitDone() ) {
			infoAction( "group=" + group.NAME + " successfully stopped." );
			return( true );
		}
		
		errorAction( "group=" + group.NAME + " failed to stop." );
		setFailed();
		return( false );
	}
	
}
