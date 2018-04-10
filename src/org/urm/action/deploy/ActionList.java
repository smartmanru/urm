package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.engine.HostAccount;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;

public class ActionList extends ActionBase {

	public ActionList( ActionBase action , String stream ) {
		super( action , stream , "List environment structure" );
	}

	@Override protected SCOPESTATE executeScopeSet( ScopeState state , ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String s = "servers of segment=" + set.sg.NAME + ":";
		info( s );
		return( SCOPESTATE.NotRun );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ScopeState state , ActionScopeTarget target ) throws Exception {
		showServerInfo( target.envServer );
		for( ActionScopeTargetItem item : target.getItems( this ) )
			showNodeInfo( item.envServerNode );
		return( SCOPESTATE.RunSuccess );
	}

	private void showServerInfo( MetaEnvServer server ) throws Exception {
		String s = "\tserver: " + server.NAME + " type=" + server.getServerTypeName();
		if( server.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
	private void showNodeInfo( MetaEnvServerNode node ) throws Exception {
		HostAccount hostAccount = node.getHostAccount(); 
		String s = "\t\tnode " + node.POS + ": " + hostAccount.getFinalAccount();
		if( node.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
}
