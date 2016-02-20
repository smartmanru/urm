package ru.egov.urm.messenger.rocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import ru.egov.urm.messenger.ChatAgent;

public class RocketChatSet {

	private final String USER_AGENT = "Mozilla/5.0";
	
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

	private void out( String s ) {
    	System.out.println( "chat: " + s );
	}
	
    private void executeChatProcessor() throws Exception {
    	while( continueUpdate ) {
        	out( "############### joining chats and reading messages...");
        	
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
			out( "join room id=" + room );
			RocketChatConversation chat = new RocketChatConversation( this , room );
			agent = new ChatAgent( chat );
		}
    	catch (Exception e) {
            e.printStackTrace();
			out( "unable to join room id=" + room + ", ignored" );
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
	
	private void exit( String s ) throws Exception {
		throw new RuntimeException( s );
	}
	
	// HTTP GET request
	private JSONObject query( String url , String data ) throws Exception {
		URL obj = new URL( url );
		HttpsURLConnection con = ( HttpsURLConnection )obj.openConnection();

		byte[] bytes = data.getBytes( "UTF-8" );
		
		// add reuqest header
		con.setRequestMethod( "POST" );
		con.setConnectTimeout( 2000 );
		
		con.setRequestProperty( "User-Agent", USER_AGENT );
		con.setRequestProperty( "Accept-Language", "en-US,en;q=0.5" );
		con.setRequestProperty( "Content-Length", Integer.toString( bytes.length ) );
		
		// Send post request
		con.setDoOutput( true );
		OutputStream wr = new DataOutputStream( con.getOutputStream() );
		
		wr.write( bytes );
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		if( responseCode != 200 )
			exit( "chat request, unexpected return code=" + responseCode );

		BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();

		while ((inputLine = in.readLine()) != null)
			responseBuffer.append( inputLine );
		in.close();
		
		String response = responseBuffer.toString();
		JSONParser parser = new JSONParser();
		JSONObject json = ( JSONObject )parser.parse( response );
		return( json );
	}
	
	private String jsonGetAttr( JSONObject obj , String attr ) throws Exception {
		Object value = obj.get( attr );
		if( value == null )
			return( "" );
		return( value.toString() );
	}
	
	private JSONObject jsonGetObject( JSONObject obj , String attr ) throws Exception {
		Object value = obj.get( attr );
		if( value == null )
			return( null );
		return( ( JSONObject )value );
	}
	
	private String[] getRooms() throws Exception {
		// login
		JSONObject login = query( server + "/api/login" , "password=" + password + "&user=" + account );
		String status = jsonGetAttr( login , "status" );
		if( !status.equals( "success" ) )
			exit( "unsuccessful login" );
		
		JSONObject data = jsonGetObject( login , "data" );
		String userId = jsonGetAttr( data , "userId" );
		String authToken = jsonGetAttr( data , "authToken" );
		out( "successful login: userId=" + userId + ", authToken=" + authToken );
		
		return( new String[0] );
	}

	public void sendMessage( String chatId , String text ) throws Exception {
	}
	
}
