package org.urm.messenger.skype;

import org.urm.messenger.ChatConversation;

import com.skype.api.Conversation;

public class SkypeChatConversation implements ChatConversation {
	Conversation conversation;

	public SkypeChatConversation( Conversation conversation ) {
		this.conversation = conversation; 
	}
	
	@Override
	public String getChatFileName() {
		String identity = conversation.getIdentity();
		identity = identity.replace( "#" , "_" );
		identity = identity.replace( "/" , "_" );
		identity = identity.replace( "\\" , "_" );
		identity = identity.replace( "$" , "_" );
		identity = identity.replace( ";" , "_" );
		String fileName = "skype." + identity + ".txt";
		return( fileName );
	}
	
	@Override
	public void PostText( String text ) {
		conversation.postText( text , false );
	}
}
