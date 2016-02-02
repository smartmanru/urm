package ru.egov.urm.run.deploy;

import java.util.List;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvStartGroup;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScope;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionSet;

public class ActionStopEnv extends ActionBase {

	public ActionStopEnv( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected void runBefore( ActionScope scope ) throws Exception {
		logAction( "stop environment dc=" + meta.dc.NAME + " (" + getMode() + ") ..." );
		if( !context.CTX_SHOWONLY )
			ActionSendChatMsg.sendMsg( this , "[stopenv] stopping " + scope.getScopeInfo( this ) + " ..." , false );
	}

	@Override protected void runAfter( ActionScope scope ) throws Exception {
		ActionSendChatMsg.sendMsg( this , "[stopenv] done." , false );
		logAction( "done." );
	}
	
	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		List<MetaEnvStartGroup> groups = set.dc.startInfo.getReverseGroupList( this );
		for( MetaEnvStartGroup group : groups ) {
			if( !stopServerGroup( set , group , targets ) ) {
				if( !context.CTX_FORCE )
					exit( "cancel execution due to failed group operation" );
			}
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
		logAction( getMode() + " stop group=" + group.NAME + " servers=(" + ActionScope.getList( servers ) + ") ..." );

		ActionSet actions = new ActionSet( this , "stop.dc" );
		for( ActionScopeTarget target : servers ) {
			if( !Common.checkListItem( targets , target ) )
				continue;
			
			ActionStopServer stopOne = new ActionStopServer( this , target.NAME , target );
			actions.runSimple( stopOne );
		}

		// wait all
		if( actions.waitDone() ) {
			logAction( "group=" + group.NAME + " successfully stopped." );
			return( true );
		}
		
		logAction( "group=" + group.NAME + " failed to stop." );
		setFailed();
		return( false );
	}
	
}
