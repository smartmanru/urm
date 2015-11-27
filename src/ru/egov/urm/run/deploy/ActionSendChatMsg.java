package ru.egov.urm.run.deploy;

import ru.egov.urm.Common;
import ru.egov.urm.run.ActionBase;

public class ActionSendChatMsg extends ActionBase {

	String msg;
	boolean forAll;
	
	public static void sendMsg( ActionBase action , String msg , boolean forAll ) throws Exception {
		ActionSendChatMsg ca = new ActionSendChatMsg( action , null , msg , forAll );
		ca.runSimple();
	}
	
	public ActionSendChatMsg( ActionBase action , String stream , String msg , boolean forAll ) {
		super( action , stream );
		
		this.msg = msg;
		this.forAll = forAll;
	}

	@Override protected boolean executeSimple() throws Exception {
		if( options.OPT_NOCHATMSG )
			return( false );
		
		if( meta.env.CHATROOMFILE.isEmpty() )
			return( false );

		if( forAll )
			msg += " (dc=" + meta.dc.NAME + ")"; 
		
		String filePath = Common.getPath( meta.product.CONFIG_PRODUCTHOME , meta.env.CHATROOMFILE ); 
		session.appendFileWithString( this , filePath , msg );
		trace( "ActionSendChatMsg: msg sent to " + filePath );
		return( true );
	}
	
}
