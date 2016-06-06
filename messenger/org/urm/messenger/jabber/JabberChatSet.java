package org.urm.messenger.jabber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.urm.messenger.ChatAgent;

public class JabberChatSet {
	XMPPConnection connection;
	String conferenceserver;

	boolean continueUpdate;
	ArrayList<ChatAgent> chatList = new ArrayList<ChatAgent>();
	Map<String,ChatAgent> chatMap = new HashMap<String,ChatAgent>();
	String[] includes;
	String[] excludes;

	public JabberChatSet( XMPPConnection connection , String conferenceserver ) {
		this.connection = connection;
		this.conferenceserver = conferenceserver;
		continueUpdate = true;
	}

	public void executeChatSet() throws Exception {
		startMessageProcessor();
		executeChatProcessor();
	}

	private void processMessage(Message message) {
	}
	
	private void startMessageProcessor() throws Exception {
	    PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
	    PacketListener myListener = new PacketListener() {
	        public void processPacket(Packet packet) {
	            if (packet instanceof Message) {
	                Message message = (Message) packet;
	                processMessage( message );
	            }
	        }
	    };
	    
	    connection.addPacketListener(myListener, filter);
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
		Collection<HostedRoom> rooms = MultiUserChat.getHostedRooms( connection , conferenceserver );

		for( HostedRoom room : rooms ) { 
			String jid = room.getJid();
            
			if( !jid.startsWith( "release." ) )
				continue;
            
			if( chatMap.get( jid ) != null )
				continue;

			if( !checkScope( jid ) )
				continue;
        	
			joinNewChat( jid );
		}
	}

	private void joinNewChat( String room ) throws Exception {
		ChatAgent agent = null;
		
		try {
			System.out.println( "join room jid=" + room );
			MultiUserChat muc = new MultiUserChat( connection , room );
			muc.join( "release-mgn" );
			
			String roomid = room.substring( 0 , room.indexOf( "@" ) );
			JabberChatConversation chat = new JabberChatConversation( muc , roomid );
			agent = new ChatAgent( chat );
		}
    	catch (Exception e) {
            e.printStackTrace();
			System.out.println( "unable to join room jid=" + room + ", ignored" );
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
}
