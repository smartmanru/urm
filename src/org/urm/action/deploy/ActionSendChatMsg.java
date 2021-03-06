package org.urm.action.deploy;

import org.urm.action.ActionBase;
import org.urm.engine.status.ScopeState;
import org.urm.engine.status.ScopeState.SCOPESTATE;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;

public class ActionSendChatMsg extends ActionBase {

	String msg;
	MetaEnv env;
	MetaEnvSegment sg;
	
	public static void sendMsg( ScopeState parentState , ActionBase action , String msg , MetaEnv env , MetaEnvSegment sg ) throws Exception {
		if( action.context.CTX_NOCHATMSG )
			return;
		
		//if( action.context.env.CHATROOMFILE.isEmpty() )
		//	return;

		//ActionSendChatMsg ca = new ActionSendChatMsg( action , null , msg , env , sg );
		//ca.runSimpleEnv( parentState , env , SecurityAction.ACTION_DEPLOY , true );
	}
	
	public ActionSendChatMsg( ActionBase action , String stream , String msg , MetaEnv env , MetaEnvSegment sg ) {
		super( action , stream , "Send chat message" );
		
		this.msg = msg;
		this.env = env;
		this.sg = sg;
	}

	@Override protected SCOPESTATE executeSimple( ScopeState state ) throws Exception {
		if( context.CTX_NOCHATMSG )
			return( SCOPESTATE.NotRun );
		
		//if( context.env.CHATROOMFILE.isEmpty() )
		//	return( SCOPESTATE.NotRun );

		//if( sg != null )
		//	msg += " (sg=" + sg.NAME + ")"; 
		
		//MetaProductSettings core = context.env.meta.getProductSettings();
		//String filePath = Common.getPath( core.CONFIG_PRODUCTHOME , env.CHATROOMFILE ); 
		//shell.appendFileWithString( this , filePath , msg );
		//trace( "ActionSendChatMsg: msg sent to " + filePath );
		return( SCOPESTATE.RunSuccess );
	}
	
}
