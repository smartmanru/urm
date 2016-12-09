package org.urm.client;

import java.io.Console;
import java.io.File;

import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;

public class ClientAuth {

	ClientEngine client;
	
	public static String urmUserVar = "urm.user";
	public static String urmPasswordVar = "urm.password";
	public static String urmKeyVar = "urm.key";

	public static String urmAuthFile = "auth.txt";
	
	public ClientAuth( ClientEngine client ) {
		this.client = client;
	}
	
	public boolean setAuth( CommandBuilder builder , CommandOptions options ) throws Exception {
		String value = runClientAuthGetFile( builder , options );
		if( value == null )
			return( false );

		RunContext execrc = builder.execrc;
		
		String authDir = execrc.authPath;
		if( authDir.isEmpty() )
			authDir = Common.getPath( execrc.userHome, ".auth" );
		String authFile = Common.getPath( authDir , urmAuthFile );
				
		authDir = execrc.getLocalPath( authDir );
		authFile = execrc.getLocalPath( authFile );
		
		File folder = new File( authDir );
		if( !folder.exists() ) {
			if( !folder.mkdirs() ) {
				builder.out( "Unable to create auth folder" );
				return( false );
			}
			
			folder.setReadable( true , true );
			folder.setWritable( true , true );
			folder.setExecutable( true , true );
		}
		
		Common.createFileFromString( authFile , "" );
		File file = new File( authFile );
		file.setReadable( true , true );
		file.setWritable( true , true );
		
		Common.createFileFromString( authFile , value );
		return( true );
	}
	
	private String runClientAuthGetFile( CommandBuilder builder , CommandOptions options ) throws Exception {
		String s = "";
		
		String user = options.getParamValue( "OPT_USER" );
		String key = options.getParamValue( "OPT_KEY" );
		String password = options.getParamValue( "OPT_PASSWORD" );
		if( user.isEmpty() ) {
			builder.out( "User parameter is required" );
			return( null );
		}
		
		if( key.isEmpty() && password.isEmpty() ) {
			Console console = System.console();
			while( true ) {
				console.printf( "Please enter your password: ");
				char[] passwordChars = console.readPassword();
				String passwordString = new String( passwordChars );
				if( !password.isEmpty() ) {
					s = setEnvVariable( s , urmUserVar , user );
					s = setEnvVariable( s , urmPasswordVar , passwordString );
					return( s );
				}
			}
		}
		
		if( key.isEmpty() == false && password.isEmpty() == false ) {
			builder.out( "Cannot set both key and password parameters" );
			return( null );
		}
		
		if( key.isEmpty() && password.isEmpty() == false ) {
			s = setEnvVariable( s , urmUserVar , user );
			s = setEnvVariable( s , urmPasswordVar , password );
			return( s );
		}
		
		s = setEnvVariable( s , urmUserVar , user );
		s = setEnvVariable( s , urmKeyVar , key );
		return( s );
	}

	public String setEnvVariable( String s , String var , String value ) {
		s += var + "=" + value + "\n";
		return( s );
	}

	public boolean getAuth( CommandBuilder builder , CommandOptions options ) throws Exception {
		return( true );
	}
	
}
