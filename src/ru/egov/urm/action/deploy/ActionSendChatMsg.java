package ru.egov.urm.action.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.action.ActionBase;
import ru.egov.urm.meta.MetaEnvDC;

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

	@Override protected boolean executeSimple() throws Exception {
		if( context.CTX_NOCHATMSG )
			return( false );
		
		if( context.env.CHATROOMFILE.isEmpty() )
			return( false );

		if( dc != null )
			msg += " (dc=" + dc.NAME + ")"; 
		
		String filePath = Common.getPath( meta.product.CONFIG_PRODUCTHOME , context.env.CHATROOMFILE ); 
		session.appendFileWithString( this , filePath , msg );
		trace( "ActionSendChatMsg: msg sent to " + filePath );
		return( true );
	}
	
}
