package ru.egov.urm.action.deploy;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.action.ActionScope;
import ru.egov.urm.action.ActionScopeSet;
import ru.egov.urm.action.ActionScopeTarget;
import ru.egov.urm.action.ActionSet;
import ru.egov.urm.meta.MetaEnvStartGroup;

public class ActionStartEnv extends ActionBase {

	public ActionStartEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		logAction( "start environment (" + getMode() + ") ..." );
		if( !context.CTX_SHOWONLY )
			ActionSendChatMsg.sendMsg( this , "[startenv] starting " + scope.getScopeInfo( this ) + " ..." , null );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( this , "[startenv] done." , null );
		logAction( "done." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<MetaEnvStartGroup> groups = set.dc.startInfo.getForwardGroupList( this );
		for( MetaEnvStartGroup group : groups ) {
			if( !startServerGroup( set , group , targets ) ) {
				if( !context.CTX_FORCE )
					exit( "cancel execution due to failed group operation" );
			}
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
		logAction( getMode() + " start group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( this , "start.dc" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStartServer startOne = new ActionStartServer( this , target.NAME , target );
			actions.runSimple( startOne );
		}

		// wait all
		if( actions.waitDone() ) {
			logAction( "group=" + group.NAME + " successfully started." );
			return( true );
		}
		
		logAction( "group=" + group.NAME + " failed to start." );
		setFailed();
		return( false );
	}
	
}
