package ru.egov.urm.run.deploy;

import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;
import ru.egov.urm.shell.Account;

public class ActionLogin extends ActionBase {

	MetaEnvServerNode node;
	
	public ActionLogin( ActionBase action , String stream , MetaEnvServerNode node ) {
		super( action , stream );
		
		this.node = node;
	}

	@Override protected boolean executeSimple() throws Exception {
		// handle user options
		Account account = getAccount( node );
		if( !context.CTX_HOSTUSER.isEmpty() )
			account = account.getUserAccount( this , context.CTX_HOSTUSER );
		else
		if( context.CTX_ROOTUSER )
			account = account.getRootAccount( this );

		String F_KEY = meta.env.KEYNAME;
		if( !context.CTX_KEYNAME.isEmpty() )
			F_KEY = context.CTX_KEYNAME;

		log( "login dc=" + node.server.dc.NAME + ", server=" + node.server.NAME + 
				", node=" + node.POS + ", hostlogin=" + account.HOSTLOGIN + " ..." );
		
		session.setTimeoutUnlimited( this );
		context.pool.runInteractiveSsh( this , account , F_KEY );
		
		return( true );
	}
	
}
