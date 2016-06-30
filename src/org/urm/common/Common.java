package org.urm.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

	public static PrintWriter createOutfileFile( String fname ) throws Exception {
		FileOutputStream outstream = new FileOutputStream( fname );
		PrintWriter outfile = new PrintWriter( outstream );
		return( outfile );
	}
	
	public static void createFileFromString( String path , String content ) throws Exception {
		FileWriter writer = new FileWriter( path );
		writer.write( content );
		writer.close();
	}

	public static void createFileFromStringList( String path , String[] content ) throws Exception {
		createFileFromStringList( path , content , StandardCharsets.UTF_8 );
	}
	
	public static void createFileFromStringList( String path , String[] content , Charset charset ) throws Exception {
		FileWriter writer = new FileWriter( path );
		for( String s : content )
			writer.write( s + "\n" );
		writer.close();
	}
	
	public static void createFileFromStringList( String path , List<String> content ) throws Exception {
		createFileFromStringList( path , content , StandardCharsets.UTF_8 );
	}
	
	public static void createFileFromStringList( String path , List<String> content , Charset charset ) throws Exception {
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
		Collections.sort( list );
		return( list.toArray( new String[0] ) );
	}
	
	public static String getLogTimeStamp() {
		return( getTimeStamp( new Date() ) );
	}

	public static String getTimeStamp( Date date ) {
        SimpleDateFormat simpleFormat = new SimpleDateFormat( "HH:mm:ss,SSS zzz" );
        return simpleFormat.format( date );
	}
	
	public static String getTimeStamp( long timeMillis ) {
		return( getTimeStamp( new Date( timeMillis ) ) );
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

	public static ExitException getExitException( Throwable e ) {
		if( e.getClass() == ExitException.class )
			return( ( ExitException )e );
		
		Throwable ec = e.getCause();
		if( ec == null )
			return( null );
		
		while( true ) {
			Throwable ecc = ec.getCause();
			if( ecc == null )
				break;
			
			ec = ecc;
		}
		
		if( ec.getClass() == ExitException.class )
			return( ( ExitException )ec );
		
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
		transformer.setOutputProperty( OutputKeys.INDENT , "yes" );
		transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount" , "4" );
		
		DOMSource source = new DOMSource( doc );
		StreamResult result = new StreamResult( new File( filePath ) );
		transformer.transform( source , result );
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

	public static String getSortedKeySet( Map<String,?> map ) {
		return( getListSet( getSortedKeys( map ) ) );
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

	public static void sleep( Object object , long millis ) throws Exception {
    	Thread.sleep( millis );
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
}
