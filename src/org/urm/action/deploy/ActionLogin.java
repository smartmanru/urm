package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.engine.shell.Account;
import org.urm.engine.shell.ShellInteractive;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnvServerNode;

public class ActionLogin extends ActionBase {

	MetaEnvServerNode node;
	
	public ActionLogin( ActionBase action , String stream , MetaEnvServerNode node ) {
		super( action , stream , "Open interactive session" );
		
		this.node = node;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		// handle user options
		Account account = getNodeAccount( node );
		if( !context.CTX_HOSTUSER.isEmpty() )
			account = account.getUserAccount( this , context.CTX_HOSTUSER );
		else
		if( context.CTX_ROOTUSER )
			account = account.getRootAccount( this );

		info( "login sg=" + node.server.sg.NAME + ", server=" + node.server.NAME + 
				", node=" + node.POS + ", hostlogin=" + account.getPrintName() + " ..." );
		
		ShellInteractive shell = engine.shellPool.createInteractiveShell( this , account , null );
		try {
			if( context.call != null ) {
				context.call.runInteractive( this , shell );
			}
			else {
				int timeout = setTimeoutUnlimited();
				shell.runInteractive( this );
				setTimeout( timeout );
			}
		}
		catch( Throwable e ) {
			super.log( "shell session" , e );
			String full = account.getFullName();
			super.fail1( _Error.LoginFailed1 , "Error in login to account=" + full , full );
			return( SCOPESTATE.RunFail );
		}
		
		return( SCOPESTATE.RunSuccess );
	}
	
}
