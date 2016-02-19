package ru.egov.urm.messenger.rocket;

import ru.egov.urm.messenger.ChatConversation;

public class RocketChatConversation implements ChatConversation {

	RocketChatSet chatSet;
	String chatId;

    public RocketChatConversation( RocketChatSet chatSet , String chatId ) {
    	this.chatSet = chatSet;
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
		String fileName = "rocket." + identity + ".txt";
		return( fileName );
	}

	@Override
	public void PostText(String text) {
        try {
            chatSet.sendMessage( chatId , text );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
}
