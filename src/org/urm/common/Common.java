package org.urm.common;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Common {

	public static String getValueDefault( String value , String defValue ) {
		if( value == null || value.isEmpty() )
			return( defValue );
		return( value );
	}
	
	public static boolean hasDirPart( String path ) {
		if( path.indexOf( "/" ) < 0 )
			return( false );
		
		if( path.equals( "/" ) )
			return( false );
		
		return( true );
	}
	
	public static String getTopDir( String path ) {
		int pos = path.indexOf( "/" );
		if( pos < 0 )
			return( path );
		
		if( pos == 0 )
			return( "/" );
		
		return( path.substring( 0 , pos ) );
	}

	public static String getSubdir( String path ) {
		int pos = path.indexOf( "/" );
		if( pos < 0 )
			return( "" );
		
		return( path.substring( pos + 1 ) );
	}
	
	public static String ensureDir( String path ) {
		if( path.endsWith( "/" ) )
			return( path );
		
		return( path + "/" );
	}
	
	public static boolean isBasenameOnly( String path ) {
		int pos = path.lastIndexOf( "/" );
		if( pos < 0 )
			return( true );
		return( false );
	}
	
	public static String getDirName( String path ) {
		int pos = path.lastIndexOf( "/" );
		if( pos < 0 )
			return( "./" );
		
		if( pos == 0 )
			return( "/" );
		
		return( path.substring( 0 ,  pos ) );
	}

	public static String getBaseName( String path ) {
		int pos = path.lastIndexOf( "/" );
		if( pos < 0 )
			return( path );
		
		return( path.substring( pos + 1 ) );
	}
	
	public static String getExtension( String name ) {
		return( getPartAfterLast( name , "." ) );
	}
	
	public static String getBaseNameNoExtension( String name ) {
		return( getPartBeforeLast( name , "." ) );
	}
	
	public static int getDirCount( String path ) {
		String x = path;
		if( x.endsWith( "/" ) )
			x = x.substring( 0 , x.length() - 1 );
		
		int start = 0;
		int count = 0;
		while( true ) {
			int index = x.indexOf( "/" , start );
			if( index < 0 )
				break;
			
			count++;
			start = index + 1;
		}
		
		return( count );
	}

	public static PrintWriter createOutfileFile( RunContext execrc , String fname ) throws Exception {
		FileOutputStream outstream = new FileOutputStream( execrc.getLocalPath( fname ) );
		PrintWriter outfile = new PrintWriter( outstream );
		return( outfile );
	}
	
	public static void createFileFromString( RunContext execrc , String path , String content ) throws Exception {
		FileWriter writer = new FileWriter( execrc.getLocalPath( path ) );
		writer.write( content );
		writer.close();
	}

	public static void createFileFromStringList( RunContext execrc , String path , String[] content ) throws Exception {
		createFileFromStringList( execrc , path , content , StandardCharsets.UTF_8 );
	}
	
	public static void createFileFromStringList( RunContext execrc , String path , String[] content , Charset charset ) throws Exception {
		FileWriter writer = new FileWriter( execrc.getLocalPath( path ) );
		for( String s : content )
			writer.write( s + "\n" );
		writer.close();
	}
	
	public static void createFileFromStringList( RunContext execrc , String path , List<String> content ) throws Exception {
		createFileFromStringList( execrc , path , content , StandardCharsets.UTF_8 );
	}
	
	public static void createFileFromStringList( RunContext execrc , String path , List<String> content , Charset charset ) throws Exception {
		FileWriter writer = new FileWriter( path );
		for( String s : content )
			writer.write( s + "\n" );
		writer.close();
	}
	
	public static String getPartBeforeFirst( String s , String delimiter ) {
		int index = s.indexOf( delimiter );
		if( index == 0 )
			return( "" );
		
		if( index > 0 )
			return( s.substring( 0 , index ) );

		return( s );
	}

	public static String getPartAfterFirst( String s , String delimiter ) {
		int index = s.indexOf( delimiter );
		if( index < 0 )
			return( "" );
		
		return( s.substring( index + delimiter.length() ) );
	}

	public static String getPartBeforeLast( String s , String delimiter ) {
		int index = s.lastIndexOf( delimiter );
		if( index == 0 )
			return( "" );
		
		if( index > 0 )
			return( s.substring( 0 , index ) );

		return( s );
	}

	public static String getPartAfterLast( String s , String delimiter ) {
		int index = s.lastIndexOf( delimiter );
		if( index < 0 )
			return( "" );
		
		return( s.substring( index + delimiter.length() ) );
	}

	public static String getList( String[] items ) {
		return( getList( items , ", " ) );
	}
	
	public static String getList( String[] items , String delimiter ) {
		String value = "";
		if( items == null )
			return( value );
		
		for( int k = 0; k < items.length; k++ ) {
			if( k > 0 )
				value += delimiter;
			
			value += items[ k ];
		}
		
		return( value );
	}

	public static String[] getSortedKeys( Map<String,?> map ) {
		List<String> list = new LinkedList<String>( map.keySet() );
		return( getSortedList( list ) );
	}
	
	public static String[] getSortedList( List<String> list ) {
		Collections.sort( list );
		return( list.toArray( new String[0] ) );
	}
	
	public static String[] getSortedList( String[] set ) {
		List<String> list = new LinkedList<String>();
		for( String s : set )
			list.add( s );
		Collections.sort( list );
		return( list.toArray( new String[0] ) );
	}
	
	public static String getLogTimeStamp() {
		return( getTimeStamp( new Date() ) );
	}

	public static String getTimeStamp( Date date ) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat( "HH:mm:ss,SSS" );
        return( simpleFormat.format( date ) );
	}
	
	public static String getTimeStamp( long timeMillis ) {
		return( getTimeStamp( new Date( timeMillis ) ) );
	}
	
	public static String getTime( long timeMillis ) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat( "HH:mm:ss" );
        Date date = new Date( timeMillis );
        return( simpleFormat.format( date ) );
	}
	
	public static String getDate( long timeMillis ) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat( "d MMM yyyy, EEE" );
        return( simpleFormat.format( timeMillis ) );
	}
	
	public static String replace( String s , String from , String to ) {
		if( from.isEmpty() || from.equals( to ) )
			return( s );
		
		int idx = s.indexOf( from ); 
		if( idx < 0 )
			return( s );
		
		String value = s;
	    while( idx >= 0 ) {
	    	value = value.substring( 0 , idx ) + to + value.substring( idx + from.length() );
	    	idx = value.indexOf( from , idx + to.length() ); 
	    }
	    
	    return( value );
	}

	public static RunError getExitException( Throwable e ) {
		if( e.getClass() == RunError.class )
			return( ( RunError )e );
		
		Throwable ec = e.getCause();
		if( ec == null )
			return( null );
		
		while( true ) {
			Throwable ecc = ec.getCause();
			if( ecc == null )
				break;
			
			ec = ecc;
		}
		
		if( ec.getClass() == RunError.class )
			return( ( RunError )ec );
		
		return( null );
	}
	
	public static String replicate( String item , int count ) {
		String value = "";
		for( int k = 0; k < count; k++ )
			value += item;
		return( value );
	}

	public static List<String> getFileNames( String dir ) throws Exception {
		final Path rootDir = Paths.get( dir );
		List<String> files = new LinkedList<String>(); 
		getFileNamesWalk( files , rootDir );

		// remove start dir
		for( int k = 0; k < files.size(); k++ )
			files.set( k , getPartAfterFirst( files.get( k ) , rootDir.toAbsolutePath() + "/" ) );
		
		return( files );
	}
	
	private static void getFileNamesWalk( List<String> fileNames , Path dir ) throws Exception {
	    DirectoryStream<Path> stream = Files.newDirectoryStream( dir );
        for( Path path : stream ) {
            if( path.toFile().isDirectory() ) {
            	getFileNamesWalk( fileNames , path );
            } else {
                fileNames.add( path.toAbsolutePath().toString() );
            }
        }
	}

	public static String getPath( String path1 , String path2 , String path3 ) {
		return( getPath( getPath( path1 , path2 ) , path3 ) );
	}
	
	public static String getPath( String path1 , String path2 ) {
		if( path2.isEmpty() || path2.equals( "/" ) )
			return( path1 );
		if( path1.isEmpty() ) {
			if( path2.startsWith( "/" ) )
				return( "./" + path2 );
			return( path2 );
		}
		if( path2.equals( "." ) )
			return( path1 );
		return( path1 + "/" + path2 );
	}
	
	public static BufferedWriter openFile( String fileName ) throws Exception {
		File file = new File( fileName );
		FileWriter fstream = new FileWriter( file );
		BufferedWriter out = new BufferedWriter( fstream );
		return( out );
	}

	public static String cutExtension( String fileName ) throws Exception {
		int baseNamePos = fileName.lastIndexOf( '/' );
		if( baseNamePos < 0 )
			baseNamePos = 0;

		int extPos = fileName.lastIndexOf( '.' );
		if( extPos < baseNamePos )
			return( fileName );
		
		return( fileName.substring( 0 , extPos ) );
	}
	
	public static String getFileExtension( String fileName ) throws Exception {
		int baseNamePos = fileName.lastIndexOf( '/' );
		if( baseNamePos < 0 )
			baseNamePos = 0;
		
		int extPos = fileName.lastIndexOf( '.' );
		if( extPos < baseNamePos )
			return( "" );
		
		return( fileName.substring( extPos + 1 ) );
	}
	
	public static String getNameTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        String dates = appendZeros( calendar.get(Calendar.YEAR), 4) + "-" + 
                appendZeros(calendar.get(Calendar.MONTH) + 1, 2) + "-" + 
                appendZeros(calendar.get(Calendar.DAY_OF_MONTH), 2) + "." +
                appendZeros(calendar.get(Calendar.HOUR_OF_DAY),2) + "-" + 
                appendZeros(calendar.get(Calendar.MINUTE),2) + "-" + 
                appendZeros(calendar.get(Calendar.SECOND),2);
        return( dates );
	}

	public static String appendZeros( int value , int len ) {
        String appended = "" + value;
        while (appended.length() < len)
            appended = "0" + appended;

        return (appended);
	}

	public static String getLiteral( String s ) {
		return( s.replace( "\\." , "\\\\." ) );
	}

	public static Document xmlCreateDoc( String root ) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement( root );
		doc.appendChild(rootElement);
		return( doc );
	}
	
	public static Element xmlCreateElement( Document doc , Element parent , String element ) throws Exception {
		Element el = doc.createElement( element );
		parent.appendChild( el );
		return( el );
	}

	public static Element xmlCreateBooleanPropertyElement( Document doc , Element parent , String propName , boolean propValue ) throws Exception {
		String value = getBooleanValue( propValue );
		return( xmlCreatePropertyElement( doc , parent , propName , value ) );
	}
	
	public static Element xmlCreatePropertyElement( Document doc , Element parent , String propName , String propValue ) throws Exception {
		Element el = doc.createElement( "property" );
		parent.appendChild( el );
		xmlSetElementAttr( doc , el , "name" , propName );
		xmlSetElementAttr( doc , el , "value" , propValue );
		return( el );
	}

	public static void xmlSetElementAttr( Document doc , Element element , String attrName , String value ) throws Exception {
		Attr attr = doc.createAttribute( attrName );
		attr.setValue( value );
		element.setAttributeNode( attr );
	}
	
	public static void xmlSaveDoc( Document doc , String filePath ) throws Exception {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty( OutputKeys.METHOD , "xml" );
		transformer.setOutputProperty( OutputKeys.ENCODING , "UTF-8" );
		transformer.setOutputProperty( OutputKeys.INDENT , "yes" );
		transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount" , "4" );
		
		DOMSource source = new DOMSource( doc );
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		OutputStream outputStream = new FileOutputStream( filePath );
		StreamResult result = new StreamResult( new OutputStreamWriter( bos , "UTF-8" ) );
		transformer.transform( source , result );
		
		bos.writeTo( outputStream );
		outputStream.flush();
		outputStream.close();
	}
	
    public static void createPropertyFile( RunContext rc , String path , Properties props , String comment ) throws Exception {
        OutputStream outputStream = new FileOutputStream( path );
        OutputStreamWriter writer = new OutputStreamWriter( outputStream , "UTF8" );
        props.store( writer , comment );
        writer.close();
        outputStream.close();
    }
	
	public static String addToList( String s , String value , String delimiter ) {
		if( s.isEmpty() )
			return( value );
		return( s + delimiter + value );
	}
	
	public static String getCommentIfAny( String s ) {
		if( s.isEmpty() )
			return( "" );
		
		return( " (" + s + ")" );
	}
	
	public static String getQuoted( String s ) {
		return( "\"" + replace( s , "\"" , "\\\"" ) + "\"" );
	}

	public static String getSubList( List<String> list , int startPos ) {
		return( getSubList( list , startPos , " " ) );
	}
	
	public static String getSubList( List<String> list , int startPos , String delimiter ) {
		String s = "";
		for( int k = startPos; k < list.size(); k++ ) {
			if( k > startPos )
				s += delimiter;
			s += list.get( k );
		}
		return( s );	
	}
	
	public static boolean getBooleanValue( String s ) {
		if( s == null )
			return( false );
		
		if( s.equals( "yes" ) )
			return( true );
		
		if( !s.equals( "no" ) )
			throw new RuntimeException( "invalid boolean value=" + s ); 
			
		return( false );
	}
	
	public static String getBooleanValue( boolean v ) {
		if( v == true )
			return( "yes" );
		
		return( "no" );
	}

	public static Date getDateCurrentDay() {
		return( getDateDay( System.currentTimeMillis() ) );
	}
	
	public static Date getDateDay( long millis ) {
		long value = getDayNoTime( millis );
		Date day = new Date( value );
		return( day );
	}
	
	public static Date getDateValue( String s ) {
		if( s == null || s.isEmpty() )
			return( null );
		
		DateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
		try {
			Date value = format.parse( s );
			return( getDateDay( value.getTime() ) );
		}
		catch( Throwable e ) {
		}
		throw new RuntimeException( "invalid date value=" + s ); 
	}
	
	public static String getDateValue( Date v ) {
		if( v == null )
			return( "" );
		
		DateFormat format = new SimpleDateFormat( "yyyy-MM-dd" );
		return( format.format( v ) );
	}
	
	public static String concat( String value1 , String value2 , String delimiter ) {
		if( value1 == null || value1.isEmpty() ) {
			if( value2 == null )
				return( "" );
			return( value2 );
		}
		
		if( value2 == null || value2.isEmpty() )
			return( value1 );
		
		return( value1 + delimiter + value2 );
	}
	
	public static boolean checkPartOfSpacedList( String part , String spacedList ) {
		String s = " " + spacedList + " ";
		if( s.indexOf( " " + part + " " ) >= 0 )
			return( true );
		return( false );
	}

	public static String[] splitSpaced( String value ) {
		return( split( value , " " ) );
	}
	
	public static String[] splitLines( String value ) {
		return( split( value , "\n" ) );
	}
	
	public static String[] splitDotted( String value ) {
		return( split( value , "\\." ) );
	}
	
	public static String[] splitDashed( String value ) {
		return( split( value , "-" ) );
	}
	
	public static String[] split( String value , String delimiter ) {
		if( value == null )
			return( new String[0] );
		value = value.trim();
		if( value.isEmpty() )
			return( new String[0] );
		String[] res = value.split( delimiter );
		for( int k = 0; k < res.length; k++ )
			res[ k ] = res[ k ].trim();
		return( res );
	}
	
	public static String getListItem( String list , String delimiter , int index ) {
		if( list == null || list.isEmpty() )
			return( "" );
		
		int posFrom = 0;
		for( int k = 0; k <= index; k++ ) {
			int posTo = list.indexOf( delimiter , posFrom );
			if( posTo < 0 ) {
				if( k == index )
					return( list.substring( posFrom ) );
				return( "" );
			}
			
			if( k == index )
				return( list.substring( posFrom , posTo ) );
			
			posFrom = posTo + delimiter.length();
		}
		
		return( "" );
	}
	
	public static String enumToXmlValue( String enumName ) {
		if( enumName == null || enumName.isEmpty() )
			return( "" );
		return( Common.replace( enumName.toLowerCase() , "_" , "." ) );
	}
	
	public static String xmlToEnumValue( String xmlName ) {
		if( xmlName == null || xmlName.isEmpty() )
			return( "NONE" );
		return( Common.replace( xmlName.toUpperCase() , "." , "_" ) );
	}

	public static String getEnumLower( Enum<?> value ) {
		if( value == null )
			return( "" );
		return( enumToXmlValue( value.name() ) );
	}
	
	public static String getListSet( String[] set ) {
		return( "{" + getList( set ) + "}" );
	}

	public static String addListToUniqueSpacedList( String list , String addList ) {
		if( list == null || list.isEmpty() )
			return( addList );
		if( addList == null || addList.isEmpty() )
			return( list );
		
		String newList = list;
		for( String item : Common.splitSpaced( addList ) )
			newList = addItemToUniqueSpacedList( newList , item );
		return( newList );
	}
	
	public static String addItemToUniqueSpacedList( String list , String item ) {
		if( list == null || list.isEmpty() )
			return( item );
		
		String full = " " + list + " ";
		if( full.indexOf( " " + item + " " ) >= 0 )
			return( list );
		
		return( list + " " + item );
	}

	public static String getSortedUniqueSpacedList( String list ) {
		if( list == null || list.isEmpty() )
			return( "" );
		
		List<String> sortList = new LinkedList<String>();
		for( String s : Common.splitSpaced( list ) )
			sortList.add( s );
		Collections.sort( sortList );
		
		String v = null;
		for( String s : sortList ) {
			if( v == null )
				v = s;
			else
				v += " " + s;
		}
		
		return( v );
	}

	public static String getSQLQuoted( String value ) {
		if( value == null || value.isEmpty() )
			return( "'NULL'" );
		return( "'" + value + "'" );
	}
	
	public static int findItem( String value , String[] list ) {
		if( value == null || value.isEmpty() )
			return( -1 );
		
		for( int k = 0; k < list.length; k++ )
			if( list[ k ].equals( value ) )
				return( k );
		return( -1 );
	}

	public static boolean checkListItem( Object[] list , Object item ) {
		for( Object xitem : list ) {
			if( xitem.equals( item ) )
				return( true );
		}
		return( false );
	}
	
	public static int getIndexOf( String s , String item , int pos ) {
		int index = s.indexOf( item );
		if( index < 0 )
			return( -1 );
		
		for( int k = 1; k <= pos; k++ ) {
			index = s.indexOf( item , index + item.length() );
			if( index < 0 )
				return( -1 );
		}
		
		return( index );
	}

	public static int getIndexOf( String[] list , String item ) {
		for( int k = 0; k <= list.length; k++ ) {
			if( list[k].equals( item ) )
				return( k );
		}
		
		return( -1 );
	}

	public static String cutItem( String s , String delimiter , int pos ) {
		int index2 = Common.getIndexOf( s , "-" , pos );
		if( pos == 0 ) {
			if( index2 < 0 )
				return( s );
			return( s.substring( 0 , index2 ) );
		}

		int index1 = Common.getIndexOf( s , "-" , pos - 1 );
		if( index1 < 0 )
			return( "" );
		
		if( index2 < 0 )
			return( s.substring( index1 + delimiter.length() ) );
		
		return( s.substring( index1 + delimiter.length() , index2 ) );
	}

	public static void sleep( long millis ) {
		try {
			Thread.sleep( millis );
		}
		catch( InterruptedException e ) {
		}
	}

	public static Map<String,String> copyListToMap( List<String> list ) {
		Map<String,String> map = new HashMap<String,String>();
		for( String s : list )
			map.put( s , s );
		return( map );
	}

	public static String fileLinesToList( String text ) {
		String[] lines = splitLines( text );
		String s = "";
		for( int k = 0; k < lines.length; k++ ) {
			if( k > 0 )
				s += " ";
			if( lines[ k ].indexOf( ' ' ) >= 0 )
				s += getQuoted( lines[ k ] );
			else
				s += lines[ k ];
		}
		return( s );
	}

	public static String getWinPath( String dir ) {
		return( Common.replace( dir , "/" , "\\" ) );
	}
	
	public static String getLinuxPath( String dir ) {
		return( Common.replace( dir , "\\" , "/" ) );
	}
	
	public static String[] grep( String[] list , String mask ) throws Exception {
		List<String> xl = new LinkedList<String>();
		for( String s : list ) {
			if( s.matches( ".*" + mask + ".*" ) )
				xl.add( s );
		}
		
		return( xl.toArray( new String[0] ) );
	}

	public static String nonull( String v ) {
		return( ( v == null )? "" : v );
	}
	
	public static List<String> createList( String[] items ) {
		List<String> list = new LinkedList<String>();
		for( String s : items )
			list.add( s );
		return( list );
	}

	public static String trim( String s , char trimChar ) {
		if( s == null || s.isEmpty() )
			return( "" );
		
		int length = s.length();
		int indexFrom = 0;
		for( ; indexFrom < length; indexFrom++ ) {
			if( s.charAt( indexFrom ) != trimChar )
				break;
		}
		
		if( indexFrom == length )
			return( "" );
		
		int indexTo = length - 1;
		for( ; indexTo > 0; indexTo-- ) {
			if( s.charAt( indexFrom ) != trimChar )
				break;
		}
		
		if( indexFrom == 0 && indexTo == (length - 1) )
			return( s );
		
		return( s.substring( indexFrom , indexTo + 1 ) );
	}

	public static String[] listPart( String[] list , int from , int to ) {
		if( list == null || list.length == 0 || from >= list.length )
			return( new String[0] );

		if( to < 0 )
			to = list.length;
		
		if( from >= to )
			return( new String[0] );
		
		String[] part = new String[ to - from ];
		for( int k = from; k < to; k++ )
			part[ k - from ] = list[ k ];
		
		return( part );
	}

	public static boolean isAbsolutePath( String path ) {
		if( path.isEmpty() )
			return( false );
		if( path.startsWith( "/" ) ||
			path.startsWith( "\\" ) ||
			path.substring( 1 ).startsWith( ":" ) )
			return( true );
		return( false );
	}

	public static String getMD5( String value ) throws Exception {
		MessageDigest md = MessageDigest.getInstance( "MD5" );
		md.reset();
		byte digest[] = md.digest( value.getBytes() );
		String md5 = new BigInteger( 1 , digest ).toString( 16 );
		return( md5 );
	}

	public static void exitUnexpected() throws Exception {
		throw new RunError( _Error.UnexpectedState0 , "Unexpected State" , null );
	}
	
	public static void exit0( int errorCode , String msg ) throws Exception {
		throw new RunError( errorCode , msg , null );
	}
	
	public static void exit1( int errorCode , String msg , String param1 ) throws Exception {
		throw new RunError( errorCode , msg , new String[] { param1 } );
	}
	
	public static void exit2( int errorCode , String msg , String param1 , String param2 ) throws Exception {
		throw new RunError( errorCode , msg , new String[] { param1 , param2 } );
	}
	
	public static void exit3( int errorCode , String msg , String param1 , String param2 , String param3 ) throws Exception {
		throw new RunError( errorCode , msg , new String[] { param1 , param2 , param3 } );
	}

	public static String[] getEnumOptions( @SuppressWarnings("rawtypes") Class<? extends Enum> cls ) { 
		List<String> items = new LinkedList<String>();
		for( Enum<?> item : cls.getEnumConstants() ) {
			String sv = Common.getEnumLower( item );
			if( !sv.equals( "unknown" ) )
				items.add( sv );
		}
		return( items.toArray( new String[0] ) );
	}

	public static boolean isBasenameMask( String name ) {
		if( name.indexOf( '[') >= 0 || 
			name.indexOf( '\\') >= 0 || 
			name.indexOf( '*') >= 0 ||
			name.indexOf( '+') >= 0 ||
			name.indexOf( '!') >= 0 )
			return( true );
		return( false );
	}
	
	public static long getDayNoTime( long value ) {
		Calendar cal = Calendar.getInstance(); // locale-specific
		cal.setTime( new Date( value ) );
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long time = cal.getTimeInMillis();
		return( time );
	}

	public static Date addDays( Date point , int shift ) {
		long value = point.getTime() + shift * ( 24 * 60 * 60 * 1000 );
		return( new Date( value ) );
	}

	public static int getDateDiffDays( Date start , Date finish ) {
		return( getDateDiffDays( start.getTime() , finish.getTime() ) );
	}
	
	public static int getDateDiffDays( long start , long finish ) {
		long diff = finish - start;
		diff /= ( 24 * 60 * 60 * 1000 );
		return( ( int )diff );
	}

	public static String getRefDate( Date baseTime , Date refTime ) {
		if( baseTime == null || refTime == null )
			return( "" );
		return( getRefDate( baseTime.getTime() , refTime.getTime() ) );
	}
	
	public static String getRefDate( long baseTime , long refTime ) {
		long baseDay = getDayNoTime( baseTime ); 
		long refDay = getDayNoTime( refTime );
		if( refDay == baseDay )
			return( getTime( refTime ) );
		return( getDate( refTime ) );
	}

}
