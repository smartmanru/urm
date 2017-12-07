package org.urm.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class ConfReader {

    private static DocumentBuilder xmlParser = null;

    private ConfReader() {
    };
    
    public synchronized static void init() throws Exception {
    	if( xmlParser == null ) {
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	xmlParser = dbf.newDocumentBuilder();
    	}
    }

    private static void exit( int errorCode , String msg , String[] params ) throws Exception {
    	throw new RunError( errorCode , msg , params );
    }
    
	public static List<String> readFileLines( RunContext rc , String path , Charset charset ) throws Exception {
    	String fullPath = path;
    	if( fullPath.startsWith( "~/" ) )
    		fullPath = rc.userHome + fullPath.substring( 1 );
    	
    	List<String> lines = null;
    	try {
    		if( rc.isWindows() )
    			fullPath = Common.getWinPath( fullPath );
    		if( charset == null )
    			charset = StandardCharsets.UTF_8;
   			lines = Files.readAllLines( Paths.get( fullPath ) , charset );
    	}
    	catch( Throwable e ) {
    		exit( _Error.UnableToReadFile1 , "unable to read file=" + path , new String[] { path } );
    	}
    	
    	return( lines );
    }
	
    public static Document readXmlFile( RunContext rc , String path ) throws Exception {
    	init();
        InputStream inputStream = getResourceStream( rc , path );
        if (inputStream == null)
            exit( _Error.UnableToReadFile1 , "unable to read file=" + path , new String[] { path } );

        Document xml = xmlParser.parse( inputStream );
        return (xml);
    }

    public static Document readXmlString( String text ) throws Exception {
    	init();
        // skip xml heading
        if (text.startsWith("<?xml"))
            text = text.substring(text.indexOf("?>") + 2);

        InputSource inputSource = new InputSource( new StringReader( text ) );
        Document xml = xmlParser.parse(inputSource);
        return (xml);
    }

    public static Properties readPropertyFile( RunContext rc , String path ) throws Exception {
    	init();
    	
    	Properties props = new Properties();
        InputStream inputStream = getResourceStream( rc , path );
        InputStreamReader reader = new InputStreamReader( inputStream , "UTF8" );
        props.load( reader );
        
        // remove quotes if any
        for( Object key : props.keySet() ) {
        	String value = props.getProperty( ( String )key );
        	if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
        		props.setProperty( ( String )key, value.substring( 1 , value.length() - 1 ) );
        }
        
    	return( props );
    }

    public static String readStringFile( RunContext rc , String path ) throws Exception {
    	init();
    	List<String> list = readFileLines( rc , path );
    	if( list == null || list.size() != 1 )
    		exit( _Error.NotSingleLineFile1 , path + " is not a single-line file" , new String[] { path } );
    	
    	return( list.get( 0 ) );
    }

	public static List<String> readFileLines( RunContext rc , String path ) throws Exception {
		return( readFileLines( rc , path , StandardCharsets.UTF_8 ) );
	}
    
	public static String readFile( RunContext rc , String path ) throws Exception {
		List<String> lines = readFileLines( rc , path );
		if( lines == null )
			return( "" );
		
		String res = "";
		for( String line : lines )
			res += line + "\n";
		return( res );
	}

    // implemnentation
    private static InputStream getResourceStream( RunContext rc , String path ) throws Exception {
    	String fullPath = path;
    	if( fullPath.startsWith( "~/" ) )
    		fullPath = rc.userHome + fullPath.substring( 1 ); 
        InputStream inputStream = new FileInputStream( fullPath );
        return( inputStream );
    }

    // XML HANDLING
    public static Node xmlGetPathNode( Node node , String path ) throws Exception {
        boolean attr = false;
        if (path.startsWith("attr:")) {
            path = path.substring(5);
            attr = true;
        }

        String[] pathItems = Common.split( path , "/" );

        if (attr) {
            for (int k = 0; k < pathItems.length - 1; k++) {
                node = xmlGetFirstChild( node , pathItems[k] );
                if (node == null)
                    return (null);
            }

            NamedNodeMap attrs = node.getAttributes();
            if (attrs == null)
                return (null);

            node = attrs.getNamedItem(pathItems[pathItems.length - 1]);
        } else {
            for (int k = 0; k < pathItems.length; k++) {
                node = xmlGetFirstChild( node , pathItems[k] );
                if (node == null)
                    return (null);
            }
        }

        return (node);
    }

    public static String xmlGetPathNodeText( String xml , String path ) throws Exception {
        Document doc = readXmlString( xml );
        Node node = doc.getDocumentElement();
        node = xmlGetPathNode( node , path );
        if (node == null)
            return (null);

        if (path.startsWith("attr:"))
            return (node.getNodeValue());

        return (node.getTextContent());
    }

    public static Node xmlGetNamedNode( Node node , String element , String name ) throws Exception {
        for (Node nc = node.getFirstChild(); nc != null; nc = nc.getNextSibling()) {
            if( element.equals( nc.getNodeName() ) )
            	if( name.equals( getAttrValue( nc , "name" ) ) )
            		return( nc );
        }
        return( null );
    }

    public static String[] xmlGetNamedElements( Node node , String element ) throws Exception {
    	Node[] nodes = xmlGetChildren( node , element );
    	if( nodes == null )
    		return( new String[0] );
    	
    	String[] names = new String[ nodes.length ];
    	for( int k = 0; k < nodes.length; k++ )
    		names[ k ] = getAttrValue( node , "name" );
    	return( names );
    }
    
    public static String xmlGetPathNodeText( Node node , String path ) throws Exception {
        node = xmlGetPathNode( node , path );
        if (node == null)
            return (null);

        if (path.startsWith("attr:"))
            return (node.getNodeValue());

        return (node.getTextContent());
    }

    public static void xmlSetCDATA( Node element , String value ) throws Exception {
        Document doc = element.getOwnerDocument();

        // convert value - ignore non-printable characters
        value = value.replace('\1', '.');
        CDATASection cdata = doc.createCDATASection(value);
        element.appendChild(cdata);
    }

    public static Node xmlGetRequiredChild( Node node , String name ) throws Exception {
    	Node child = xmlGetFirstChild( node , name );
    	if( child == null )
    		exit( _Error.UnableFindChild1 , "unable to find child=" + name , new String[] { name } );
    	return( child );
    }

    public static Node xmlGetFirstChild( Node node , String name ) throws Exception {
        for (Node nc = node.getFirstChild(); nc != null; nc = nc.getNextSibling()) {
            if (name.equals(nc.getNodeName()))
                return (nc);
        }
        return (null);
    }

    public static Node xmlGetNextSibling( Node node , String name ) throws Exception {
        for (Node nc = node.getNextSibling(); nc != null; nc = nc.getNextSibling()) {
            if (name.equals(nc.getNodeName()))
                return (nc);
        }
        return (null);
    }

    public static String getAttrValue( Node node , String attrName ) throws Exception {
    	if( node == null )
    		return( "" );
    		
    	NamedNodeMap map = node.getAttributes();
    	if( map == null )
    		return( "" );
    	
    	Node attr = map.getNamedItem( attrName );
    	if( attr == null )
    		return( "" );

    	String value = attr.getNodeValue();
    	if( value == null )
    		return( "" );
    	
    	return( value );
    }

    public static void addAttributes( Node node , Map<String,String> list ) throws Exception {
    	NamedNodeMap map = node.getAttributes();
    	if( map != null ) {
	    	for( int k = 0; k < map.getLength(); k++ ) {
	    		Node item = map.item( k );
	    		list.put( item.getNodeName() , item.getNodeValue() );
	    	}
    	}
    }
    
    public static Map<String,String> getAttributes( Node node ) throws Exception {
    	Map<String,String> list = new HashMap<String,String>();
    	addAttributes( node , list );
		return( list );
    }
    
    public static String getAttrValue( Node node , String attrName , String defaultValue ) throws Exception {
    	String value = getAttrValue( node , attrName );
    	if( value.isEmpty() )
    		return( defaultValue );
    	
    	return( value );
    }

    public static boolean getBooleanPropertyValue( Node node , String propertyName , boolean defValue ) throws Exception {
    	String value = getPropertyValue( node , propertyName );
    	if( value == null || value.isEmpty() )
    		return( defValue );
    	return( Common.getBooleanValue( value ) );
    }

    public static int getIntegerPropertyValue( Node node , String propertyName , int defValue ) throws Exception {
    	String value = getPropertyValue( node , propertyName );
    	if( value == null || value.isEmpty() )
    		return( defValue );
    	return( Integer.parseInt( value ) );
    }

    public static String getPropertyValue( Node node , String propertyName , String defValue ) throws Exception {
    	String value = getPropertyValue( node , propertyName );
    	if( value == null || value.isEmpty() )
    		return( defValue );
    	return( value );
    }
    
    public static String getPropertyValue( Node node , String propertyName ) throws Exception {
    	Node child = xmlGetNamedNode( node , "property" , propertyName );
    	if( child == null )
    		return( null );
    	
    	String value = getAttrValue( child , "value" , null );
    	if( value == null )
    		return( null );
    	
    	return( value );
    }
    
    public static String getRequiredPropertyValue( Node node , String propertyName ) throws Exception {
    	String value = getPropertyValue( node , propertyName );
    	if( value == null || value.isEmpty() )
    		exit( _Error.PropertyNotSet1 , "unexpected xml file content - property " + propertyName + " is not set" , new String[] { propertyName } );
    	
    	return( value );
    }
    
    public static String getRequiredAttrValue( Node node , String attrName ) throws Exception {
    	String value = getAttrValue( node , attrName );
    	if( value.isEmpty() ) {
    		if( !attrName.equals( "id" ) ) {
    			String id = getAttrValue( node , "id" );
    			if( !id.isEmpty() )
    				exit( _Error.AttributeIsEmptyId2 , "unexpected xml file content - attribute " + attrName + " is empty, id=" + id , new String[] { attrName , id } );
    		}
    			
    		if( !attrName.equals( "name" ) ) {
    			String name = getAttrValue( node , "name" );
    			if( !name.isEmpty() )
    				exit( _Error.AttributeIsEmptyName2 , "unexpected xml file content - attribute " + attrName + " is empty, name=" + name , new String[] { attrName , name } );
    		}
    			
    		exit( _Error.AttributeIsEmpty1 , "unexpected xml file content - attribute " + attrName + " is empty" , new String[] { attrName } );
    	}
    	
    	return( value );
    }

    public static String getNodeSubTree( Node node ) throws Exception {
	    StringWriter sw = new StringWriter();
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty( OutputKeys.METHOD , "xml" );
	    transformer.setOutputProperty( OutputKeys.INDENT , "no" );
	    transformer.setOutputProperty( OutputKeys.ENCODING , "UTF-8" );
	    transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION , "yes" );
	    transformer.transform( new DOMSource( node ), new StreamResult( sw ) );
	    return( sw.toString() );
    }
    
    public static boolean getBooleanAttrValue( Node node , String attrName , boolean defValue ) throws Exception {
    	String value = getAttrValue( node , attrName );
    	if( value.isEmpty() )
    		return( defValue );

    	return( Common.getBooleanValue( value ) );
    }
    
    public static int getIntegerAttrValue( Node node , String attrName , int defValue ) throws Exception {
    	String value = getAttrValue( node , attrName );
    	if( value.isEmpty() )
    		return( defValue );

    	return( Integer.parseInt( value ) );
    }
    
    public static long getLongAttrValue( Node node , String attrName , long defValue ) throws Exception {
    	String value = getAttrValue( node , attrName );
    	if( value.isEmpty() )
    		return( defValue );

    	return( Long.parseLong( value ) );
    }
    
    public static Node[] xmlGetChildren( Node node , String name ) throws Exception {
        int n = 0;
        for( Node x = xmlGetFirstChild( node , name ); x != null; x = xmlGetNextSibling( x , name ) )
            n++;

        if( n == 0 )
            return( null );

        int k = 0;
        Node[] nodes = new Node[n];
        for( Node x = xmlGetFirstChild( node , name ); x != null; x = xmlGetNextSibling( x , name ) ) {
            nodes[k] = x;
            k++;
        }

        return( nodes );
    }
}
