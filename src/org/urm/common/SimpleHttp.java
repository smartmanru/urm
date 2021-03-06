package org.urm.common;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.bind.DatatypeConverter;

import org.urm.action.ActionBase;

public class SimpleHttp {

	public int responseCode;
	public String response;

	public static boolean check( ActionBase action , String url ) {
		return( check( action , url , null , null ) );
	}
	
	public static boolean check( ActionBase action , String url , String user , String password ) {
		SimpleHttp http = new SimpleHttp();
		try {
			http.checkURL( action , url , user , password );
			return( http.valid( action ) );
		}
		catch( Throwable e ) {
			http.responseCode = -1;
			action.handle( e );
		}
		return( false );
	}

	public static SimpleHttp get( ActionBase action , String url ) {
		return( get( action , url , null , null ) );
	}
	
	public static SimpleHttp get( ActionBase action , String url , String user , String password ) {
		SimpleHttp http = new SimpleHttp();
		try {
			http.sendGet( action , url , user , password );
		}
		catch( Throwable e ) {
			http.responseCode = -1;
			action.handle( e );
		}
		return( http );
	}
	
	public static SimpleHttp post( ActionBase action , String url , String data ) {
		SimpleHttp http = new SimpleHttp();
		try {
			http.sendPost( action , url , data );
		}
		catch( Throwable e ) {
			http.responseCode = -1;
			action.handle( e );
		}
		return( http );
	}

	public boolean valid( ActionBase action ) throws Exception {
		if( responseCode == HttpURLConnection.HTTP_OK )
			return( true );
		return( false );
	}
	
	private final String USER_AGENT = "Mozilla/5.0";

	// HTTP check URL
	private void checkURL( ActionBase action , String url , String user , String password ) throws Exception {
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection con = ( HttpURLConnection )new URL( url ).openConnection();
		
		if( user != null && !user.isEmpty() ) {
			String userpass = user + ":" + password;
			String encoded = DatatypeConverter.printBase64Binary( userpass.getBytes() );
			String basicAuth = "Basic " + encoded;
			con.setRequestProperty ("Authorization", basicAuth);
		}
		
		con.setRequestMethod( "GET" );
		con.setConnectTimeout( 2000 );

		action.trace( "sending GET (HEAD) request to URL: " + url );
		responseCode = con.getResponseCode();
		action.trace( "response code: " + responseCode );
		con.disconnect();
	}
	
	// HTTP GET request
	private void sendGet( ActionBase action , String url , String user , String password ) throws Exception {
		URL obj = new URL( url );
		HttpURLConnection con = ( HttpURLConnection )obj.openConnection();
		
		if( user != null && !user.isEmpty() ) {
			String userpass = user + ":" + password;
			String encoded = DatatypeConverter.printBase64Binary( userpass.getBytes() );
			String basicAuth = "Basic " + encoded;
			con.setRequestProperty ("Authorization", basicAuth);
		}

		// optional default is GET
		con.setRequestMethod( "GET" );
		con.setConnectTimeout( 2000 );

		// add request header
		con.setRequestProperty( "User-Agent" , USER_AGENT );

		action.trace( "sending GET request to URL: " + url );
		responseCode = con.getResponseCode();
		action.trace( "response code: " + responseCode );

		// read data
		BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseBuffer.append( inputLine );
		}
		in.close();
		
		response = responseBuffer.toString();
	}

	// HTTP POST request
	private void sendPost( ActionBase action , String url , String data ) throws Exception {
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
		action.trace( "sending POST request to URL: " + url );
		con.setDoOutput( true );
		OutputStream wr = new DataOutputStream( con.getOutputStream() );
		
		wr.write( bytes );
		wr.flush();
		wr.close();

		responseCode = con.getResponseCode();
		action.trace( "response code:" + responseCode );

		BufferedReader in = new BufferedReader( new InputStreamReader( con.getInputStream() ) );
		String inputLine;
		StringBuffer responseBuffer = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			responseBuffer.append( inputLine );
		}
		in.close();
		
		//print result
		response = responseBuffer.toString();
	}

}
