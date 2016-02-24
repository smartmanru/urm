package ru.egov.urm.messenger.rocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
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

	String userId;
	String authToken;
	
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
    	out( "############### initializing ...");
    	while( continueUpdate ) {
        	out( "############### joining chats and reading messages ...");
        	
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
		Map<String,String> rooms = getRooms();
		for( String room : rooms.keySet() ) { 
			if( chatMap.get( room ) != null )
				continue;

			if( !checkScope( room ) )
				continue;
        	
			joinNewChat( room , rooms.get( room ) );
		}
	}

	private void joinNewChat( String roomName , String roomId ) throws Exception {
		ChatAgent agent = null;
		
		try {
			out( "join room id=" + roomName + " ..." );
			JSONObject join = query( server + "/api/rooms/" + roomId + "/join" , "{}" , true );
			String status = jsonGetAttr( join , "status" );
			if( !status.equals( "success" ) )
				exit( "unsuccessful join chat=" + roomName );
			
			RocketChatConversation chat = new RocketChatConversation( this , roomName , roomId );
			agent = new ChatAgent( chat );
		}
    	catch (Exception e) {
            e.printStackTrace();
			out( "unable to join room id=" + roomName + ", ignored" );
			return;
		}
			
		chatMap.put( roomName , agent );
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
	private void setRequestProperty( HttpsURLConnection con , String key , String value ) {
		// System.out.println( key + "=" + value );
		con.setRequestProperty( key , value );
	}
	
	private JSONObject query( String url , String data , boolean addAuth ) throws Exception {
		URL obj = new URL( url );
		HttpsURLConnection con = ( HttpsURLConnection )obj.openConnection();

		// add reuqest header
		// System.out.println( "url=" + url + ", data=" + data );
		con.setConnectTimeout( 2000 );
		
		setRequestProperty( con , "User-Agent" , USER_AGENT );
		setRequestProperty( con , "Accept-Language" , "en-US,en;q=0.5" );
		
		byte[] bytes = null;
		if( !data.isEmpty() ) {
			con.setRequestMethod( "POST" );
			bytes = data.getBytes( "UTF-8" );
			setRequestProperty( con , "Content-Length" , Integer.toString( bytes.length ) );
		}
		else {
			con.setRequestMethod( "GET" );
		}
		
		if( addAuth ) {
			if( !data.isEmpty() )
				setRequestProperty( con , "Content-Type" , "application/json" );
			
			setRequestProperty( con , "X-Auth-Token" , authToken );
			setRequestProperty( con , "X-User-Id" , userId );
		}
		
		// Send post request
		con.setDoOutput( true );
		if( bytes != null ) {
			OutputStream wr = new DataOutputStream( con.getOutputStream() );
			wr.write( bytes );
			wr.flush();
			wr.close();
		}

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
	
	private JSONArray jsonGetArray( JSONObject obj , String attr ) throws Exception {
		Object value = obj.get( attr );
		if( value == null )
			return( null );
		return( ( JSONArray )value );
	}
	
	private Map<String,String> getRooms() throws Exception {
		Map<String,String> roomMap = new HashMap<String,String>();
		
		// login
		JSONObject login = query( server + "/api/login" , "password=" + password + "&user=" + account , false );
		String status = jsonGetAttr( login , "status" );
		if( !status.equals( "success" ) )
			exit( "unsuccessful login" );
		
		JSONObject data = jsonGetObject( login , "data" );
		userId = jsonGetAttr( data , "userId" );
		authToken = jsonGetAttr( data , "authToken" );
		out( "successful login: userId=" + userId + ", authToken=" + authToken );
		
		JSONObject rooms = query( server + "/api/publicRooms" , "" , true );
		status = jsonGetAttr( rooms , "status" );
		if( !status.equals( "success" ) )
			exit( "unsuccessful get rooms" );
		
		JSONArray array = jsonGetArray( rooms , "rooms" );
		Iterator<?> i = array.iterator();
        while( i.hasNext() ) {
            JSONObject roomObj = ( JSONObject )i.next();
            String roomId = jsonGetAttr( roomObj , "_id" );
            String roomName = jsonGetAttr( roomObj , "name" );
            roomMap.put( roomName , roomId );
        }
		
		return( roomMap );
	}

	public void sendMessage( String chatName , String chatId , String text ) throws Exception {
		JSONObject send = query( server + "/api/rooms/" + chatId + "/send" , "{ \"msg\" : \"" + text + "\" }" , true );
		String status = jsonGetAttr( send , "status" );
		if( !status.equals( "success" ) )
			exit( "unsuccessful end message" );
	}
	
}
