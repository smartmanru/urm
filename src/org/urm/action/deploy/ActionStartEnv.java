package org.urm.action.deploy;

import java.util.List;

import org.urm.Common;
import org.urm.action.ActionBase;
import org.urm.action.ActionScope;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionSet;
import org.urm.meta.MetaEnvStartGroup;

public class ActionStartEnv extends ActionBase {

	public ActionStartEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		infoAction( "start environment (" + getMode() + ") ..." );
		if( isExecute() )
			ActionSendChatMsg.sendMsg( this , "[startenv] starting " + scope.getScopeInfo( this ) + " ..." , null );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( this , "[startenv] done." , null );
		infoAction( "done." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<MetaEnvStartGroup> groups = set.dc.startInfo.getForwardGroupList( this );
		for( MetaEnvStartGroup group : groups ) {
			if( !startServerGroup( set , group , targets ) )
				ifexit( "failed group operation" );
		}
		
		return( true );
	}

	private boolean startServerGroup( ActionScopeSet set , MetaEnvStartGroup group , ActionScopeTarget[] targets ) throws Exception {
		List<ActionScopeTarget> servers = set.getGroupServers( this , group );
		if( servers.isEmpty() ) {
			debug( "no servers specified to start in group=" + group.NAME );
			return( true );
		}
		
		// execute servers in parallel within subprocess
		infoAction( getMode() + " start group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( this , "start.dc" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStartServer startOne = new ActionStartServer( this , target.NAME , target );
			actions.runSimple( startOne );
		}

		// wait all
		if( actions.waitDone() ) {
			infoAction( "group=" + group.NAME + " successfully started." );
			return( true );
		}
		
		errorAction( "group=" + group.NAME + " failed to start." );
		setFailed();
		return( false );
	}
	
}
