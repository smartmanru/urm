package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServer;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.run.ActionScopeSet;
import ru.egov.urm.run.ActionScopeTarget;
import ru.egov.urm.run.ActionScopeTargetItem;

public class ActionList extends ActionBase {

	public ActionList( ActionBase action , String stream ) {
		super( action , stream );
	}

	@Override protected boolean executeScopeSet( ActionScopeSet set , ActionScopeTarget[] targets ) throws Exception {
		String s = "servers of datacenter=" + meta.dc.NAME + ":";
		super.printComment( s );
		return( false );
	}
	
	@Override protected boolean executeScopeTarget( ActionScopeTarget target ) throws Exception {
		showServerInfo( target.envServer );
		for( ActionScopeTargetItem item : target.getItems( this ) )
			showNodeInfo( item.envServerNode );
		return( true );
	}

	private void showServerInfo( MetaEnvServer server ) throws Exception {
		String s = "\tserver: " + server.NAME + " type=" + Common.getEnumLower( server.TYPE );
		if( server.OFFLINE )
			s += " (offline)";
		super.printComment( s );
	}
	
	private void showNodeInfo( MetaEnvServerNode node ) throws Exception {
		String s = "\t\tnode " + node.POS + ": " + node.HOSTLOGIN;
		if( node.OFFLINE )
			s += " (offline)";
		super.printComment( s );
	}
	
}
