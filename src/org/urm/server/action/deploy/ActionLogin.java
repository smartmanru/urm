package org.urm.server.action.deploy;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaEnvServerNode;
import org.urm.server.shell.Account;

public class ActionLogin extends ActionBase {

	MetaEnvServerNode node;
	
	public ActionLogin( ActionBase action , String stream , MetaEnvServerNode node ) {
		super( action , stream );
		
		this.node = node;
	}

	@Override protected boolean executeSimple() throws Exception {
		// handle user options
		Account account = getNodeAccount( node );
		if( !context.CTX_HOSTUSER.isEmpty() )
			account = account.getUserAccount( this , context.CTX_HOSTUSER );
		else
		if( context.CTX_ROOTUSER )
			account = account.getRootAccount( this );

		String F_KEY = context.env.KEYNAME;
		if( !context.CTX_KEYNAME.isEmpty() )
			F_KEY = context.CTX_KEYNAME;

		info( "login dc=" + node.server.dc.NAME + ", server=" + node.server.NAME + 
				", node=" + node.POS + ", hostlogin=" + account.getPrintName() + " ..." );
		
		int timeout = setTimeoutUnlimited();
		engine.pool.runInteractiveSsh( this , account , F_KEY );
		setTimeout( timeout );
		
		return( true );
	}
	
}
