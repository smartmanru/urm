package ru.egov.urm;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
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

import ru.egov.urm.meta.Metadata.VarNAMETYPE;
import ru.egov.urm.run.ActionBase;

public class ConfReader {

    private static DocumentBuilder xmlParser = null;
    public static String userHome;

    private ConfReader() {
    };
    
    public static void init() throws Exception {
    	if( xmlParser == null ) {
        	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        	xmlParser = dbf.newDocumentBuilder();
    	}

    	userHome = System.getenv( "HOME");
    }
    
    public static Document readXmlFile( ActionBase action , String path ) throws Exception {
    	init();
        InputStream inputStream = getResourceStream( action , path );
        if (inputStream == null)
            action.exit( "XML file " + path + " not found" );

        Document xml = xmlParser.parse( inputStream );
        return (xml);
    }

    public static Document readXmlString( ActionBase action , String text ) throws Exception {
    	init();
        // skip xml heading
        if (text.startsWith("<?xml"))
            text = text.substring(text.indexOf("?>") + 2);

        InputSource inputSource = new InputSource( new StringReader( text ) );
        Document xml = xmlParser.parse(inputSource);
        return (xml);
    }

    public static Properties readPropertyFile( ActionBase action , String path ) throws Exception {
    	action.debug( "read property file " + path + " ..." );
    	init();
    	
    	Properties props = new Properties();
        InputStream inputStream = getResourceStream( action , path );
        props.load( inputStream );
        
        // remove quotes if any
        for( Object key : props.keySet() ) {
        	String value = props.getProperty( ( String )key );
        	if( value.startsWith( "\"" ) && value.endsWith( "\"" ) )
        		props.setProperty( ( String )key, value.substring( 1 , value.length() - 1 ) );
        }
        
    	return( props );
    }

    public static String readStringFile( ActionBase action , String path ) throws Exception {
    	init();
    	List<String> list = readFileLines( action , path );
    	if( list == null || list.size() != 1 )
    		action.exit( path + " is not a single-line file" );
    	
    	return( list.get( 0 ) );
    }
    
	public static List<String> readFileLines( ActionBase action , String path ) throws Exception {
    	String fullPath = path;
    	if( fullPath.startsWith( "~/" ) )
    		fullPath = userHome + fullPath.substring( 1 );
    	
    	init();
    	
    	List<String> lines = null;
    	try {
    		lines = Files.readAllLines( Paths.get(fullPath), StandardCharsets.UTF_8 );
    	}
    	catch( Throwable e ) {
    		action.log( e );
    		action.exit( "unable to read file=" + path );
    	}
    	
    	return( lines );
    }
	
	public static String readFile( ActionBase action , String path ) throws Exception {
		List<String> lines = readFileLines( action , path );
		if( lines == null )
			return( "" );
		
		String res = "";
		for( String line : lines )
			res += line + "\n";
		return( res );
	}

    // implemnentation
    private static InputStream getResourceStream( ActionBase action , String path ) throws Exception {
    	String fullPath = path;
    	if( fullPath.startsWith( "~/" ) )
    		fullPath = userHome + fullPath.substring( 1 ); 
        InputStream inputStream = new FileInputStream( fullPath );
        return( inputStream );
    }

    // XML HANDLING
    public static Node xmlGetPathNode( ActionBase action , Node node , String path ) throws Exception {
        boolean attr = false;
        if (path.startsWith("attr:")) {
            path = path.substring(5);
            attr = true;
        }

        String[] pathItems = Common.split( path , "/" );

        if (attr) {
            for (int k = 0; k < pathItems.length - 1; k++) {
                node = xmlGetFirstChild( action , node , pathItems[k] );
                if (node == null)
                    return (null);
            }

            NamedNodeMap attrs = node.getAttributes();
            if (attrs == null)
                return (null);

            node = attrs.getNamedItem(pathItems[pathItems.length - 1]);
        } else {
            for (int k = 0; k < pathItems.length; k++) {
                node = xmlGetFirstChild( action , node , pathItems[k] );
                if (node == null)
                    return (null);
            }
        }

        return (node);
    }

    public static String xmlGetPathNodeText( ActionBase action , String xml , String path ) throws Exception {
        Document doc = readXmlString( action , xml );
        Node node = doc.getDocumentElement();
        node = xmlGetPathNode( action , node , path );
        if (node == null)
            return (null);

        if (path.startsWith("attr:"))
            return (node.getNodeValue());

        return (node.getTextContent());
    }

    public static Node xmlGetNamedNode( ActionBase action , Node node , String element , String name ) throws Exception {
        for (Node nc = node.getFirstChild(); nc != null; nc = nc.getNextSibling()) {
            if( element.equals( nc.getNodeName() ) )
            	if( name.equals( getAttrValue( action , nc , "name" ) ) )
            		return( nc );
        }
        return( null );
    }
    
    public static String xmlGetPathNodeText( ActionBase action , Node node , String path ) throws Exception {
        node = xmlGetPathNode( action , node , path );
        if (node == null)
            return (null);

        if (path.startsWith("attr:"))
            return (node.getNodeValue());

        return (node.getTextContent());
    }

    public static void xmlSetCDATA( ActionBase action , Node element , String value ) throws Exception {
        Document doc = element.getOwnerDocument();

        // convert value - ignore non-printable characters
        value = value.replace('\1', '.');
        CDATASection cdata = doc.createCDATASection(value);
        element.appendChild(cdata);
    }

    public static Node xmlGetRequiredChild( ActionBase action , Node node , String name ) throws Exception {
    	Node child = xmlGetFirstChild( action , node , name );
    	if( child == null )
    		action.exit( "unable to find child=" + name );
    	return( child );
    }

    public static Node xmlGetFirstChild( ActionBase action , Node node , String name ) throws Exception {
        for (Node nc = node.getFirstChild(); nc != null; nc = nc.getNextSibling()) {
            if (name.equals(nc.getNodeName()))
                return (nc);
        }
        return (null);
    }

    public static Node xmlGetNextSibling( ActionBase action , Node node , String name ) throws Exception {
        for (Node nc = node.getNextSibling(); nc != null; nc = nc.getNextSibling()) {
            if (name.equals(nc.getNodeName()))
                return (nc);
        }
        return (null);
    }

    public static String getNameAttr( ActionBase action , Node node , VarNAMETYPE nameType ) throws Exception {
    	String name = getRequiredAttrValue( action , node , "name" );
    	if( nameType == VarNAMETYPE.ANY )
    		return( name );
    	
    	String mask = null;
    	if( nameType == VarNAMETYPE.ALPHANUM )
    		mask = "[0-9a-zA-Z_]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOT )
    		mask = "[0-9a-zA-Z_.]+";
    	else
    	if( nameType == VarNAMETYPE.ALPHANUMDOTDASH )
    		mask = "[0-9a-zA-Z_.-]+";
    	else
    		action.exitUnexpectedState();
    		
    	if( !name.matches( mask ) )
    		action.exit( "name attribute should contain only alphanumeric or dot characters, value=" + name );
    	return( name );	
    }
    
    public static String getAttrValue( ActionBase action , Node node , String attrName ) throws Exception {
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

    public static void addAttributes( ActionBase action , Node node , Map<String,String> list ) throws Exception {
    	NamedNodeMap map = node.getAttributes();
    	if( map != null ) {
	    	for( int k = 0; k < map.getLength(); k++ ) {
	    		Node item = map.item( k );
	    		list.put( item.getNodeName() , item.getNodeValue() );
	    	}
    	}
    }
    
    public static Map<String,String> getAttributes( ActionBase action , Node node ) throws Exception {
    	Map<String,String> list = new HashMap<String,String>();
    	addAttributes( action , node , list );
		return( list );
    }
    
    public static String getAttrValue( ActionBase action , Node node , String attrName , String defaultValue ) throws Exception {
    	String value = getAttrValue( action , node , attrName );
    	if( value.isEmpty() )
    		return( defaultValue );
    	
    	return( value );
    }

    public static int getIntegerPropertyValue( ActionBase action , Node node , String propertyName , int defValue ) throws Exception {
    	String value = getPropertyValue( action , node , propertyName );
    	if( value == null || value.isEmpty() )
    		return( defValue );
    	return( Integer.parseInt( value ) );
    }

    public static String getPropertyValue( ActionBase action , Node node , String propertyName ) throws Exception {
    	Node child = xmlGetNamedNode( action , node , "property" , propertyName );
    	if( child == null )
    		return( null );
    	
    	String value = getAttrValue( action , child , "value" , null );
    	if( value == null )
    		return( null );
    	
    	return( value );
    }
    
    public static String getRequiredPropertyValue( ActionBase action , Node node , String propertyName ) throws Exception {
    	String value = getPropertyValue( action , node , propertyName );
    	if( value == null || value.isEmpty() )
    		action.exit( "unexpected xml file content - property " + propertyName + " is not set" );
    	
    	return( value );
    }
    
    public static String getRequiredAttrValue( ActionBase action , Node node , String attrName ) throws Exception {
    	String value = getAttrValue( action , node , attrName );
    	if( value.isEmpty() ) {
    		if( !attrName.equals( "id" ) ) {
    			String id = getAttrValue( action , node , "id" );
    			if( !id.isEmpty() )
    				action.exit( "unexpected xml file content - attribute " + attrName + " is empty, id=" + id );
    		}
    			
    		if( !attrName.equals( "name" ) ) {
    			String name = getAttrValue( action , node , "name" );
    			if( !name.isEmpty() )
    				action.exit( "unexpected xml file content - attribute " + attrName + " is empty, name=" + name );
    		}
    			
    		action.exit( "unexpected xml file content - attribute " + attrName + " is empty" );
    	}
    	
    	return( value );
    }

    public static String getNodeSubTree( ActionBase action , Node node ) throws Exception {
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
    
    public static boolean getBooleanAttrValue( ActionBase action , Node node , String attrName , boolean defValue ) throws Exception {
    	String value = getAttrValue( action , node , attrName );
    	if( value.isEmpty() )
    		return( defValue );

    	return( Common.getBooleanValue( value ) );
    }
    
    public static int getIntegerAttrValue( ActionBase action , Node node , String attrName , int defValue ) throws Exception {
    	String value = getAttrValue( action , node , attrName );
    	if( value.isEmpty() )
    		return( defValue );

    	return( Integer.parseInt( value ) );
    }
    
    public static Node[] xmlGetChildren( ActionBase action , Node node , String name ) throws Exception {
        int n = 0;
        for( Node x = xmlGetFirstChild( action , node , name ); x != null; x = xmlGetNextSibling( action , x , name ) )
            n++;

        if( n == 0 )
            return( null );

        int k = 0;
        Node[] nodes = new Node[n];
        for( Node x = xmlGetFirstChild( action , node , name ); x != null; x = xmlGetNextSibling( action , x , name ) ) {
            nodes[k] = x;
            k++;
        }

        return( nodes );
    }
}
