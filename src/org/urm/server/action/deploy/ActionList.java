package org.urm.server.action.deploy;

import org.urm.common.Common;
import org.urm.server.action.ActionBase;
import org.urm.server.action.ActionScopeSet;
import org.urm.server.action.ActionScopeTarget;
import org.urm.server.action.ActionScopeTargetItem;
import org.urm.server.meta.MetaEnvServer;
import org.urm.server.meta.MetaEnvServerNode;

public class ActionList extends ActionBase {

	public ActionList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String s = "servers of datacenter=" + set.dc.NAME + ":";
		super.comment( s );
		return( false );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		showServerInfo( target.envServer );
		for( ActionScopeTargetItem item : target.getItems( this ) )
			showNodeInfo( item.envServerNode );
		return( true );
	}

	private void showServerInfo( MetaEnvServer server ) throws Exception {
		String s = "\tserver: " + server.NAME + " type=" + Common.getEnumLower( server.serverType );
		if( server.OFFLINE )
			s += " (offline)";
		super.comment( s );
	}
	
	private void showNodeInfo( MetaEnvServerNode node ) throws Exception {
		String s = "\t\tnode " + node.POS + ": " + node.HOSTLOGIN;
		if( node.OFFLINE )
			s += " (offline)";
		super.comment( s );
	}
	
}
