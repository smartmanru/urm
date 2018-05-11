package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ActionScopeSet;
import org.urm.action.ActionScopeTarget;
import org.urm.action.ActionScopeTargetItem;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionList extends ActionBase {

	public ActionList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected SCOPESTATE executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String s = "servers of segment=" + set.sg.NAME + ":";
		info( s );
		return( SCOPESTATE.NotRun );
	}
	
	@Override protected SCOPESTATE executeScopeTarget( ActionScopeTarget target ) throws Exception {
		showServerInfo( target.envServer );
		for( ActionScopeTargetItem item : target.getItems( this ) )
			showNodeInfo( item.envServerNode );
		return( SCOPESTATE.RunSuccess );
	}

	private void showServerInfo( MetaEnvServer server ) throws Exception {
		String s = "\tserver: " + server.NAME + " type=" + server.getServerTypeName( this );
		if( server.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
	private void showNodeInfo( MetaEnvServerNode node ) throws Exception {
		String s = "\t\tnode " + node.POS + ": " + node.HOSTLOGIN;
		if( node.OFFLINE )
			s += " (offline)";
		info( s );
	}
	
}
