package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.meta.MetaEnvServerNode;
import ru.egov.urm.run.ActionBase;

public class ActionLogin extends ActionBase {

	MetaEnvServerNode node;
	
	public ActionLogin( ActionBase action , String stream , MetaEnvServerNode node ) {
		super( action , stream );
		
		this.node = node;
	}

	@Override protected boolean executeSimple() throws Exception {
		// handle user options
		String F_HOSTLOGIN = node.HOSTLOGIN;
		if( !options.OPT_HOSTUSER.isEmpty() )
			F_HOSTLOGIN = Common.getAccount( options.OPT_HOSTUSER , node.HOST );
		else
		if( options.OPT_ROOTUSER )
			F_HOSTLOGIN = Common.getRootAccount( node.HOST );

		String F_KEY = meta.env.KEYNAME;
		if( !context.KEYNAME.isEmpty() )
			F_KEY = context.KEYNAME;

		log( "login dc=" + node.server.dc.NAME + ", server=" + node.server.NAME + 
				", node=" + node.POS + ", hostlogin=" + F_HOSTLOGIN + " ..." );
		
		session.setTimeoutUnlimited( this );
		context.pool.runInteractiveSsh( this , F_HOSTLOGIN , F_KEY );
		
		return( true );
	}
	
}
