package org.urm.engine.security;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.EngineDB;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;

public class CryptoContainer {

	public static String CRYPTO_TYPE = "JKS";
	
	public EngineSecurity security;
	
	public String name;
	public boolean valid;
	public boolean sync;
	
	private Map<String,String> data;
	private byte[] saltBytesReuse;
	private SecretKey secretKeyReuse;
	private String secretPassword;
	private int version;
	
	public CryptoContainer( EngineSecurity security , String name ) {
		this.security = security;
		this.name = name;
		this.valid = false;
		this.sync = false;
		
		data = new HashMap<String,String>(); 
	}

	private String getFileName() {
		return( name + ".crypto" );
	}
	
	private static String getFileName( String name ) {
		return( name + ".crypto" );
	}
	
	public void create( ActionBase action , String password ) throws Exception {
		data.clear();
		saltBytesReuse = null;
		secretKeyReuse = null;
		secretPassword = "";
		valid = false;
		
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder path = urm.getServerCryptoFolder( action );
		
		String fileName = getFileName();
		if( path.checkFileExists( action , fileName ) ) {
			String file = path.getLocalFilePath( action , fileName );
			Common.exit1( _Error.ContainerAlreadyExists1 , "container already exists at " + file , file );
		}

		try {
			valid = true;
			version = EngineDB.APP_VERSION;
			save( action , password );
		}
		catch( Throwable e ) {
			action.log( "unable to create container" , e );
			path.removeFiles( action , fileName );
			Common.exit1( _Error.UnableCreateContainer1 , "unable to create container name=" + name , name );
		}
	}

	public void save( ActionBase action , String password ) throws Exception {
		if( !valid )
			Common.exitUnexpected();
		
		if( password == null )
			password = secretPassword;
		
		if( sync ) {
			// check for change password
			if( password.equals( secretPassword ) )
				return;
		}

		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder path = urm.getServerCryptoFolder( action );
		String fileName = getFileName();
		
		FileOutputStream fos = null;
		DataOutputStream dos = null;
		try {
			fos = new FileOutputStream( path.getLocalFilePath( action , fileName ) );
			dos = new DataOutputStream( fos );
			
			// version
			dos.writeInt( version );
			
			if( data.isEmpty() ) {
				// write zero flag
				dos.writeInt( 0 );
			}
			else {
				// create key
				byte saltBytes[] = null;
				if( saltBytesReuse != null )
					saltBytes = saltBytesReuse;
				else {
				    SecureRandom random = new SecureRandom();
				    saltBytes = new byte[16];
				    random.nextBytes( saltBytes );
				    saltBytesReuse = saltBytes;
				}
			    
				// salt
			    dos.writeInt( saltBytes.length );
			    dos.write( saltBytes );

				// IV
				Cipher cipher = getCipher( password , saltBytes , true , null );
				AlgorithmParameters params = cipher.getParameters();
				byte[] iv = params.getParameterSpec( IvParameterSpec.class ).getIV();
				dos.writeInt( iv.length );
				dos.write( iv );
				
				// data
				byte[] rawData = exportData();
				byte[] ciphertext = cipher.doFinal( rawData );
				dos.writeInt( ciphertext.length );
				dos.write( ciphertext );
			}
		}
		finally {
			if( fos != null )
				fos.close();
			if( dos != null )
				dos.close();
	    }
		
		sync = true;
		secretPassword = password;
		path.copyFileRename( action , fileName , fileName + ".save" );
	}

	public static boolean checkExists( ActionBase action , String name ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder path = urm.getServerCryptoFolder( action );
		String fileName = getFileName( name );
		return( path.checkFileExists( action , fileName ) );
	}
	
	public void open( ActionBase action , String password ) throws Exception {
		data.clear();
		saltBytesReuse = null;
		secretKeyReuse = null;
		secretPassword = "";
		valid = false;
		
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder path = urm.getServerCryptoFolder( action );
		String fileName = getFileName();
		
	    FileInputStream fis = null;
	    DataInputStream din = null;
	    try {
	        fis = new java.io.FileInputStream( path.getLocalFilePath( action , fileName ) );
	        din = new DataInputStream( fis );
	        
	        // version
	        version = din.readInt();
	        if( version == 0 )
	        	Common.exitUnexpected();

	        // salt or zero sign
	        int saltSize = din.readInt();
	        if( saltSize != 0 ) {
	        	if( saltSize <= 0 || saltSize > 256 )
	        		Common.exitUnexpected();
	        	
	        	byte[] saltBytes = new byte[ saltSize ];
	        	if( din.read( saltBytes ) != saltSize )
		        	Common.exitUnexpected();
	        	saltBytesReuse = saltBytes;
	        
	        	// IV
	        	int ivSize = din.readInt();
		        if( ivSize < 0 || ivSize >= 256 )
		        	Common.exitUnexpected();
		        byte[] iv = new byte[ ivSize ];
		        if( din.read( iv ) != ivSize )
		        	Common.exitUnexpected();
		        
		        // read data
		        int cipherSize = din.readInt();
		        if( ivSize <= 0 || ivSize >= 2000000000 )
		        	Common.exitUnexpected();
		        byte[] ciphertext = new byte[ cipherSize ];
		        if( din.read( ciphertext ) != cipherSize )
		        	Common.exitUnexpected();
		        
		        // decrypt
				Cipher cipher = getCipher( password , saltBytes , false , iv );
				byte[] rawData = null;
				try {
					rawData = cipher.doFinal( ciphertext );
				}
				catch( Throwable e ) {
					Common.exit1( _Error.UnableOpenContainer1 , "unable to decode container=" + name , name );
				}
				
				importData( rawData );
				upgrade();
	        }
	    } finally {
	        if (fis != null) {
	            fis.close();
	        }
	        if (din != null) {
	        	din.close();
	        }
	    }
	    
	    valid = true;
	    sync = true;
	}
	
	private Cipher getCipher( String password , byte[] saltBytes , boolean encrypt , byte[] iv ) throws Exception {
		SecretKey secret = null;
		if( secretKeyReuse != null && secretPassword.equals( password ) )
			secret = secretKeyReuse;
		else {
			SecretKeyFactory factory = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA1" );
			KeySpec spec = new PBEKeySpec( password.toCharArray() , saltBytes , 256 , 128 );
			SecretKey tmp = factory.generateSecret( spec );
			secret = new SecretKeySpec( tmp.getEncoded() , "AES" );
			secretKeyReuse = secret;
			secretPassword = password;
		}
	
		// IV
		Cipher cipher = Cipher.getInstance( "AES/CBC/PKCS5Padding" );
		if( encrypt )
			cipher.init( Cipher.ENCRYPT_MODE , secret );
		else
			cipher.init( Cipher.DECRYPT_MODE , secret , new IvParameterSpec( iv ) );
		return( cipher );
	}
	
	private byte[] exportData() {
		// item count
		List<byte[]> list = new LinkedList<byte[]>();
		list.add( ByteBuffer.allocate( 4 ).putInt( data.size() ).array() );
		
		for( String key : data.keySet() ) {
			String value = data.get( key );
			
			// item key size/value
			byte[] keyBytes = key.getBytes();
			list.add( ByteBuffer.allocate( 4 ).putInt( keyBytes.length ).array() );
			list.add( keyBytes );
			
			// item data size/value
			byte[] valueBytes = value.getBytes();
			list.add( ByteBuffer.allocate( 4 ).putInt( valueBytes.length ).array() );
			if( valueBytes.length > 0 )
				list.add( valueBytes );
		}
		
		int size = 0;
		for( byte[] item : list )
			size += item.length;
		
		byte[] bytes = new byte[ size ];
		int offset = 0;
		for( byte[] item : list ) {
			for( int k = 0; k < item.length; k++ )
				bytes[ offset++ ] = item[ k ];
		}
		
		return( bytes );
	}
	
	private void importData( byte[] bytes ) throws Exception {
		// item count
		ByteBuffer buffer = ByteBuffer.wrap( bytes );
		int count = buffer.getInt();
		
		if( count < 0 || count >= 1000000 )
			Common.exitUnexpected();
		
		for( int k = 0; k < count; k++ ) {
			// item key size/value
			int keySize = buffer.getInt();
			if( keySize <= 0 || keySize >= 10000 )
				Common.exitUnexpected();
			byte[] keyBytes = new byte[ keySize ];
			buffer.get( keyBytes );
			String key = new String( keyBytes , "UTF-8" );
			
			// item data size/value
			String value = "";
			int valueSize = buffer.getInt();
			if( valueSize < 0 || valueSize >= 10000000 )
				Common.exitUnexpected();
			if( valueSize > 0 ) {
				byte[] valueBytes = new byte[ valueSize ];
				buffer.get( valueBytes );
				value = new String( valueBytes , "UTF-8" );
			}
			
			// set data
			data.put( key , value );
		}
	}
	
	public void delete( ActionBase action ) throws Exception {
		UrmStorage urm = action.artefactory.getUrmStorage();
		LocalFolder path = urm.getServerCryptoFolder( action );
		String fileName = getFileName();
		
		path.removeFiles( action , fileName + " " + fileName + ".save" );
		
		data.clear();
		this.valid = false;
		this.sync = false;
	}

	public void setKey( ActionBase action , String key , String value ) throws Exception {
		if( !valid )
			Common.exitUnexpected();

		if( sync ) {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder path = urm.getServerCryptoFolder( action );
			String fileName = getFileName();
			path.copyFileRename( action , fileName , fileName + ".save" );
			sync = false;
		}

		data.put( key , value );
	}

	public void deleteKey( ActionBase action , String key ) throws Exception {
		data.remove( key );
	}
	
	public void clearKeySet( ActionBase action , String key ) throws Exception {
		String keyFolder = key;
		if( !keyFolder.endsWith( "/" ) )
			keyFolder += "/";
			
		for( String dataKey : data.keySet().toArray( new String[0] ) ) {
			if( dataKey.startsWith( keyFolder ) )
				data.remove( dataKey );
		}
		data.remove( key );
	}
	
	public String getKey( ActionBase action , String key ) throws Exception {
		if( !valid )
			Common.exitUnexpected();

		String value = data.get( key );
		if( value == null )
			return( "" );
		return( value );
	}

	public void upgrade() throws Exception {
		if( version == EngineDB.APP_VERSION )
			return;
		
		version = EngineDB.APP_VERSION;
	}
	
}
