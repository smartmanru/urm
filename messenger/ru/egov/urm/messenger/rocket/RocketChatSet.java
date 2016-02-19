package ru.egov.urm.messenger.rocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.messenger.ChatAgent;

public class RocketChatSet {
	
	boolean continueUpdate;
	ArrayList<ChatAgent> chatList = new ArrayList<ChatAgent>();
	Map<String,ChatAgent> chatMap = new HashMap<String,ChatAgent>();
	
	String server;
	String account;
	String password;
	String[] includes;
	String[] excludes;

	public RocketChatSet( String server , String account , String password ) {
		this.server = server;
		this.account = account;
		this.password = password;
		continueUpdate = true;
	}

	public void executeChatSet() throws Exception {
		executeChatProcessor();
	}

    private void executeChatProcessor() throws Exception {
    	while( continueUpdate ) {
        	System.out.println( "############### joining chats and reading messages...");
        	
    		readActiveChatsInternal();
    		for( int k = 0; k < 60; k++ ) {
    			if( !continueUpdate )
    				return;
    			
    			executeAgents();
    			Thread.sleep( 1000 );
    		}
    	}
    }

	public void executeAgents() throws Exception {
		for( int k = 0; k < chatList.size(); k++ ) {
			ChatAgent agent = chatList.get(k);
			agent.postMessages();
		}
	}

	public void readActiveChatsInternal() throws Exception {
		String[] rooms = getRooms();
		for( String room : rooms ) { 
			if( !room.startsWith( "release." ) )
				continue;
            
			if( chatMap.get( room ) != null )
				continue;

			if( !checkScope( room ) )
				continue;
        	
			joinNewChat( room );
		}
	}

	private void joinNewChat( String room ) throws Exception {
		ChatAgent agent = null;
		
		try {
			System.out.println( "join room id=" + room );
			RocketChatConversation chat = new RocketChatConversation( this , room );
			agent = new ChatAgent( chat );
		}
    	catch (Exception e) {
            e.printStackTrace();
			System.out.println( "unable to join room id=" + room + ", ignored" );
			return;
		}
			
		chatMap.put( room , agent );
		chatList.add( agent );
	}

	public void setInclude( String rooms ) {
		includes = rooms.split( "," );
	}

	public void setExclude( String rooms ) {
		excludes = rooms.split( "," );
	}
	
	private boolean checkScope( String room ) throws Exception {
		// check excluded
		for( String s : excludes ) {
			if( room.matches( s ) )
				return( false );
		}
		
		// check included
		for( String s : includes ) {
			if( room.matches( s ) )
				return( true );
		}
		
		return( false );
	}
	
	private String[] getRooms() throws Exception {
	}

	public void sendMessage( String chatId , String text ) throws Exception {
	}
	
}
