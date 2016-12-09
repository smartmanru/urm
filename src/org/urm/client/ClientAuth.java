package org.urm.client;

import java.io.Console;
import java.io.File;
import java.util.Properties;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;

public class ClientAuth {

	ClientEngine client;
	
	public static String urmUserVar = "urm.user";
	public static String urmPasswordVar = "urm.password";
	public static String urmKeyVar = "urm.key";

	public static String urmAuthFile = "client-auth.txt";

	public String authUser;
	public String authKeyFile;
	public String authPassword;
	
	public ClientAuth( ClientEngine client ) {
		this.client = client;
	}
	
	private String getAuthDir( RunContext execrc ) {
		String authDir = execrc.authPath;
		if( authDir.isEmpty() )
			authDir = Common.getPath( execrc.userHome, ".auth" );
		return( authDir );
	}
	
	public boolean setAuth( CommandBuilder builder , CommandOptions options ) throws Exception {
		Properties props = runClientAuthGetFile( builder , options );
		if( props == null )
			return( false );

		RunContext execrc = builder.execrc;
		
		String authDir = getAuthDir( execrc );
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
		
		Common.createPropertyFile( execrc , authFile , props , "client auth" );
		builder.out( "Auth properties have been persisted to " + authFile );
		return( true );
	}
	
	private Properties runClientAuthGetFile( CommandBuilder builder , CommandOptions options ) throws Exception {
		String user = options.getParamValue( OptionsMeta.OPT_USER );
		String key = options.getParamValue( OptionsMeta.OPT_KEY );
		String password = options.getParamValue( OptionsMeta.OPT_PASSWORD );
		if( user.isEmpty() ) {
			builder.out( "User parameter is required" );
			return( null );
		}
		
		Properties props = new Properties();
		if( key.isEmpty() && password.isEmpty() ) {
			password = readConsole( "Please enter your password: " , true );
			if( !password.isEmpty() ) {
				props.setProperty( urmUserVar , user );
				props.setProperty( urmPasswordVar , password );
				return( props );
			}
		}
		
		if( key.isEmpty() == false && password.isEmpty() == false ) {
			builder.out( "Cannot set both key and password parameters" );
			return( null );
		}
		
		if( key.isEmpty() && password.isEmpty() == false ) {
			props.setProperty( urmUserVar , user );
			props.setProperty( urmPasswordVar , password );
			return( props );
		}
		
		props.setProperty( urmUserVar , user );
		props.setProperty( urmKeyVar , key );
		return( props );
	}

	public boolean getAuth( CommandBuilder builder , CommandOptions options ) throws Exception {
		String user = options.getParamValue( OptionsMeta.OPT_USER );
		String key = "";
		String password = "";
		
		RunContext execrc = builder.execrc;
		
		if( !user.isEmpty() ) {
			password = options.getParamValue( OptionsMeta.OPT_PASSWORD );
			if( password.isEmpty() )
				key = options.getParamValue( OptionsMeta.OPT_KEY );
		}
		else {
			String authDir = getAuthDir( execrc );
			String authFile = Common.getPath( authDir , urmAuthFile );
					
			authDir = execrc.getLocalPath( authDir );
			authFile = execrc.getLocalPath( authFile );
			
			File f = new File( authFile );
			if( f.isFile() ) {
				Properties props = ConfReader.readPropertyFile( execrc , authFile );
				user = props.getProperty( urmUserVar );
				password = props.getProperty( urmPasswordVar );
				key = props.getProperty( urmKeyVar );
			}
		}

		if( !key.isEmpty() ) {
			key = execrc.getLocalPath( key );
			File f = new File( key );
			if( !f.isFile() ) {
				builder.out( "Key file does not exist" );
				return( false );
			}
		}
		
		if( user.isEmpty() )
			user = readConsole( "User name: " , false );
		
		if( password.isEmpty() && key.isEmpty() )
			password = readConsole( "Password: " , true );

		authUser = user;
		authPassword = password;
		authKeyFile = key;
		
		return( true );
	}

	private String readConsole( String prompt , boolean password ) {
		Console console = System.console();
		
		String value = "";
		while( true ) {
			console.printf( prompt );
			
			if( password ) {
				char[] passwordChars = console.readPassword();
				if( passwordChars == null )
					System.exit( 1 );
				
				value = new String( passwordChars );
			}
			else {
				value = console.readLine();
				if( value == null )
					System.exit( 1 );
			}
			
			if( !value.isEmpty() )
				break;
		}
		
		return( value );
	}
	
}
