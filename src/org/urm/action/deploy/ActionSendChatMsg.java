package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaProductSettings;

public class ActionSendChatMsg extends ActionBase {

	String msg;
	MetaEnvDC dc;
	
	public static void sendMsg( ActionBase action , String msg , MetaEnvDC dc ) throws Exception {
		ActionSendChatMsg ca = new ActionSendChatMsg( action , null , msg , dc );
		ca.runSimple();
	}
	
	public ActionSendChatMsg( ActionBase action , String stream , String msg , MetaEnvDC dc ) {
		super( action , stream );
		
		this.msg = msg;
		this.dc = dc;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		if( context.CTX_NOCHATMSG )
			return( SCOPESTATE.NotRun );
		
		if( context.env.CHATROOMFILE.isEmpty() )
			return( SCOPESTATE.NotRun );

		if( dc != null )
			msg += " (dc=" + dc.NAME + ")"; 
		
		MetaProductSettings product = context.env.meta.getProductSettings( this );
		String filePath = Common.getPath( product.CONFIG_PRODUCTHOME , context.env.CHATROOMFILE ); 
		shell.appendFileWithString( this , filePath , msg );
		trace( "ActionSendChatMsg: msg sent to " + filePath );
		return( SCOPESTATE.RunSuccess );
	}
	
}
