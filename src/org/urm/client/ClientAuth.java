package org.urm.client;

import java.io.Console;
import java.io.File;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Calendar;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.common.action.CommandBuilder;
import org.urm.common.action.CommandOptions;
import org.urm.common.action.OptionsMeta;

public class ClientAuth {

	public static class DecodeValue {
		
	    private byte[] bytes;
	    private int pos;
		
		public DecodeValue( String part ) {
			bytes = DatatypeConverter.parseBase64Binary( part );
	        pos = 0;	
		}
		
		public int decodeInt() {
			int value = ((bytes[pos] & 0xFF) << 24) | ((bytes[pos+1] & 0xFF) << 16)
	                | ((bytes[pos+2] & 0xFF) << 8) | (bytes[pos+3] & 0xFF);
			pos += 4;
			return( value );
	    }

		public BigInteger decodeBigInt() {
	        int len = decodeInt();
	        if( pos + len > bytes.length )
	        	return( null );
	        	
	        byte[] bigIntBytes = new byte[len];
	        System.arraycopy(bytes, pos, bigIntBytes, 0, len);
	        pos += len;
	        return new BigInteger(bigIntBytes);
	    }
		
	};
	
	ClientEngineConsole client;
	
	public static String signatureData = "Fa0LSuhEyLbZDdBqGPDjTTDyUP9kiX34234$#34234"; 
	
	public static String urmUserVar = "urm.user";
	public static String urmPasswordVar = "urm.password";
	public static String urmKeyVar = "urm.key";

	public static String urmAuthFile = "client-auth.txt";

	public String authUser;
	public String authPassword;

	public ClientAuth( ClientEngineConsole client ) {
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
		
		Common.createFileFromString( execrc , authFile , "" );
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
			user = readConsole( "Please enter user name: " , false );
			if( user.isEmpty() ) {
				builder.out( "User parameter is required" );
				return( null );
			}
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
				if( password.isEmpty() )
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
		if( key.isEmpty() )
			authPassword = password;
		else
			authPassword = getKeyPassword( builder , execrc , user , key );
		
		return( true );
	}

	private String getKeyPassword( CommandBuilder builder , RunContext execrc , String user , String keyFile ) {
		final String beginDSA = "-----BEGIN DSA PRIVATE KEY-----\n";
		final String beginRSA = "-----BEGIN RSA PRIVATE KEY-----\n";
		final String endDSA = "-----ENV DSA PRIVATE KEY-----";
		final String endRSA = "-----ENV RSA PRIVATE KEY-----";
		
		try {
		    String privateKeyPEM = ConfReader.readFile( execrc , keyFile );
		    String checkMessage = getCheckMessage( user );
		    
		    if( privateKeyPEM.startsWith( beginDSA ) ) {
		    	privateKeyPEM = privateKeyPEM.replace( beginDSA , "" );
		    	privateKeyPEM = privateKeyPEM.replace( endDSA , "" );
		    	privateKeyPEM = privateKeyPEM.replace( "\n" , "" );
		    	
			    byte[] encoded = DatatypeConverter.parseBase64Binary( privateKeyPEM );
			    KeyFactory kf = KeyFactory.getInstance( "DSA" );
			    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( encoded );
			    PrivateKey privKey = ( PrivateKey )kf.generatePrivate( keySpec );
			    String signature = getSignedString( privKey , checkMessage , false );
			    return( signature );
		    }
		    
		    if( privateKeyPEM.startsWith( beginRSA ) ) {
		    	privateKeyPEM = privateKeyPEM.replace( beginRSA , "" );
		    	privateKeyPEM = privateKeyPEM.replace( endRSA , "" );
		    	privateKeyPEM = privateKeyPEM.replace( "\n" , "" );
		    	
			    byte[] encoded = DatatypeConverter.parseBase64Binary( privateKeyPEM );
			    KeyFactory kf = KeyFactory.getInstance( "RSA" );
			    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( encoded );
			    PrivateKey privKey = ( PrivateKey )kf.generatePrivate( keySpec );
			    String signature = getSignedString( privKey , checkMessage , true );
			    return( signature );
		    }
		}
		catch( Throwable e ) {
			e.printStackTrace();
			System.exit( 1 );
		}
	    
		builder.out( "Key file has unknown format (DSA/RSA expected)" );
		System.exit( 1 );
		return( null );
	}
 
	public static String getCheckMessage( String user ) {
        Calendar calendar = Calendar.getInstance();
        String dates = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH) + "." +
                calendar.get(Calendar.HOUR_OF_DAY) + "-" + calendar.get(Calendar.MINUTE); 
	    String checkMessage = signatureData + user + dates;
		return( checkMessage );
	}
	
	private String getSignedString( PrivateKey privateKey , String message , boolean rsa ) throws Exception {
		String type = ( rsa )? "SHA1withRSA" : "SHA1withDSA";
	    Signature sign = Signature.getInstance( type );
	    sign.initSign( privateKey );
	    sign.update( message.getBytes( "UTF-8" ) );
	    String res = DatatypeConverter.printBase64Binary( sign.sign() );
	    return( res );
	}

	public static boolean verifySigned( String message , String signature , String publicKey ) throws Exception {
		PublicKey key = getPublicKey( publicKey );
		if( key == null )
			return( false );
		
		String type = ( publicKey.startsWith( "ssh-rsa" ) )? "SHA1withRSA" : "SHA1withDSA";
	    Signature sign = Signature.getInstance( type );
	    sign.initVerify( key );
	    sign.update( message.getBytes( "UTF-8" ) );
	    return( sign.verify( signature.getBytes( "UTF-8" ) ) );
	}
	
	private static PublicKey getPublicKey( String publicKey ) throws Exception {
		String[] parts = Common.splitSpaced( publicKey );
		String type = parts[0];
		
		if( type.equals( "ssh-rsa" ) ) {
			DecodeValue value = new DecodeValue( parts[1] );
			BigInteger e = value.decodeBigInt();
			BigInteger m = value.decodeBigInt();
			RSAPublicKeySpec spec = new RSAPublicKeySpec( m , e );
			PublicKey key = KeyFactory.getInstance( "RSA" ).generatePublic( spec );
			return( key );
		}
		
		if( type.equals( "ssh-dss" ) ) {
			DecodeValue value = new DecodeValue( parts[1] );
			BigInteger p = value.decodeBigInt();
			BigInteger q = value.decodeBigInt();
			BigInteger g = value.decodeBigInt();
			BigInteger y = value.decodeBigInt();
			DSAPublicKeySpec spec = new DSAPublicKeySpec( y , p , q , g );
			PublicKey key = KeyFactory.getInstance( "DSA" ).generatePublic( spec );
			return( key );
        }
		
		return( null );
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
