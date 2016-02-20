package ru.egov.urm.messenger.rocket;

import ru.egov.urm.messenger.ChatConversation;

public class RocketChatConversation implements ChatConversation {

	RocketChatSet chatSet;
	String chatName;
	String chatId;

    public RocketChatConversation( RocketChatSet chatSet , String chatName , String chatId ) {
    	this.chatSet = chatSet;
    	this.chatName = chatName;
    	this.chatId = chatId;
    }
    
    @Override
	public String getChatFileName() {
		String identity = chatName;
		identity = identity.replace( "#" , "_" );
		identity = identity.replace( "/" , "_" );
		identity = identity.replace( "\\" , "_" );
		identity = identity.replace( "$" , "_" );
		identity = identity.replace( ";" , "_" );
		String fileName = "rocket." + identity + ".txt";
		return( fileName );
	}

	@Override
	public void PostText(String text) {
        try {
            chatSet.sendMessage( chatName , chatId , text );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
}
