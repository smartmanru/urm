package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellInteractive;
import org.urm.meta.product.MetaEnvServerNode;

public class ActionLogin extends ActionBase {

	MetaEnvServerNode node;
	
	public ActionLogin( ActionBase action , String stream , MetaEnvServerNode node ) {
		super( action , stream );
		
		this.node = node;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		// handle user options
		Account account = getNodeAccount( node );
		if( !context.CTX_HOSTUSER.isEmpty() )
			account = account.getUserAccount( this , context.CTX_HOSTUSER );
		else
		if( context.CTX_ROOTUSER )
			account = account.getRootAccount( this );

		info( "login dc=" + node.server.dc.NAME + ", server=" + node.server.NAME + 
				", node=" + node.POS + ", hostlogin=" + account.getPrintName() + " ..." );
		
		ShellInteractive shell = engine.shellPool.createInteractiveShell( this , account );
		if( context.call != null )
			context.call.runInteractive( this , shell );
		else {
			int timeout = setTimeoutUnlimited();
			shell.runInteractive( this );
			setTimeout( timeout );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
