package org.urm.messenger.jabber;

import org.jivesoftware.smackx.muc.MultiUserChat;
import org.urm.messenger.ChatConversation;


public class JabberChatConversation implements ChatConversation {

    private MultiUserChat chat;
    String chatId;

    public JabberChatConversation( MultiUserChat chat , String chatId ) {
    	this.chat = chat;
    	this.chatId = chatId;
    }
    
    @Override
	public String getChatFileName() {
		String identity = chatId;
		identity = identity.replace( "#" , "_" );
		identity = identity.replace( "/" , "_" );
		identity = identity.replace( "\\" , "_" );
		identity = identity.replace( "$" , "_" );
		identity = identity.replace( ";" , "_" );
		String fileName = "jabber." + identity + ".txt";
		return( fileName );
	}

	@Override
	public void PostText(String text) {
        try {
            chat.sendMessage( text );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
}
