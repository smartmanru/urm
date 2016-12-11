package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.action.ScopeState.SCOPESTATE;
import org.urm.common.Common;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaProductSettings;

public class ActionSendChatMsg extends ActionBase {

	String msg;
	MetaEnvSegment sg;
	
	public static void sendMsg( ActionBase action , String msg , MetaEnvSegment sg ) throws Exception {
		ActionSendChatMsg ca = new ActionSendChatMsg( action , null , msg , sg );
		ca.runSimple();
	}
	
	public ActionSendChatMsg( ActionBase action , String stream , String msg , MetaEnvSegment sg ) {
		super( action , stream );
		
		this.msg = msg;
		this.sg = sg;
	}

	@Override protected SCOPESTATE executeSimple() throws Exception {
		if( context.CTX_NOCHATMSG )
			return( SCOPESTATE.NotRun );
		
		if( context.env.CHATROOMFILE.isEmpty() )
			return( SCOPESTATE.NotRun );

		if( sg != null )
			msg += " (sg=" + sg.NAME + ")"; 
		
		MetaProductSettings product = context.env.meta.getProductSettings( this );
		String filePath = Common.getPath( product.CONFIG_PRODUCTHOME , context.env.CHATROOMFILE ); 
		shell.appendFileWithString( this , filePath , msg );
		trace( "ActionSendChatMsg: msg sent to " + filePath );
		return( SCOPESTATE.RunSuccess );
	}
	
}
