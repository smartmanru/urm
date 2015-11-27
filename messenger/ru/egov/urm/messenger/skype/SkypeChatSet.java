package ru.egov.urm.messenger.skype;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.egov.urm.messenger.ChatAgent;
import ru.egov.urm.messenger.skype.wrapper.MySession;

import com.skype.api.Conversation;

public class SkypeChatSet {

	MySession session;
	Map<String,ChatAgent> mapTopics = new HashMap<String,ChatAgent>();
	ArrayList<ChatAgent> chatList = new ArrayList<ChatAgent>();
	boolean continueUpdate = false;
	ThreadUpdater updater;
	
	public SkypeChatSet( MySession session ) {
		this.session = session;
		this.updater = new ThreadUpdater();
	}

	private void readActiveChatsInternal() throws Exception {
		// List of Conversations (using GetConversationList)
        SimpleDateFormat simpleFormat = new SimpleDateFormat("HH:mm:ss,SSS zzz");
        String ts = simpleFormat.format(new Date());
		MySession.myConsole.printf( ts + ": REFRESH CONVERSATIONS...%n");
		Conversation[] myInbox = session.mySkype.getConversationList(Conversation.ListType.INBOX_CONVERSATIONS);
		
		int nConv = myInbox.length;
		for (int i = 0; i < nConv; i++) {
			Conversation myConversation = myInbox[i];
			
			// getting conversation properties
			Conversation.Type conversationType = myConversation.getType();
			if( !conversationType.toString().equals( "CONFERENCE" ) )
				continue;
			
			// check new
			if( mapTopics.get( myConversation.getIdentity() ) != null )
				continue;
			
			// add new
			SkypeChatConversation chat = new SkypeChatConversation( myConversation );
			ChatAgent agent = new ChatAgent( chat ); 
			mapTopics.put( myConversation.getIdentity() , agent );
			chatList.add( agent );
			
			String displayName = myConversation.getDisplayName();
			String fileName = agent.getFileName();
			MySession.myConsole.printf("\tADDED CONFERENCE: Type = " + conversationType.toString() +
				", ID=" + myConversation.getIdentity() + 
				", Display Name = " + displayName + ", fileName=" + fileName + "%n" );
		}
	}

	public String[] getNameList() {
		return( mapTopics.keySet().toArray( new String[0] ) );
	}
	
	public String[] readActiveChats() throws Exception {
		readActiveChatsInternal();
		return( getNameList() );
	}
	
	public void setUpdateOncePerMinute() throws Exception {
    	continueUpdate = true;
        Thread thread = new Thread( null , updater , "updaterThread" );
        thread.start();
	}

	public void cancelUpdates() throws Exception {
    	continueUpdate = false;
	}
	
	public void executeAgents() throws Exception {
		for( int k = 0; k < chatList.size(); k++ ) {
			ChatAgent agent = chatList.get(k);
			agent.postMessages();
		}
	}
	
	// reading thread
    class ThreadUpdater implements Runnable {
        public ThreadUpdater() {
        }

        public void run() {
            try {
           		runLoop();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        private void runLoop() throws Exception {
        	while( continueUpdate ) {
        		readActiveChatsInternal();
        		for( int k = 0; k < 60; k++ ) {
        			if( !continueUpdate )
        				return;
        			
        			executeAgents();
        			Thread.sleep( 1000 );
        		}
        	}
        }
    }
}
