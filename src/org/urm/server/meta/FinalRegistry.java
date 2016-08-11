package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.server.ServerTransaction;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class FinalRegistry {

	public FinalLoader loader;
	PropertySet properties;

	public String CONNECTION_HTTP_PORT;
	public String CONNECTION_JMX_PORT;
	
	public String JABBER_ACCOUNT;
	public String JABBER_PASSWORD;
	public String JABBER_SERVER;
	public String JABBER_CONFERENCESERVER;
	public String JABBER_INCLUDE;
	public String JABBER_EXCLUDE;

	private Map<String,FinalMetaSystem> mapSystems;
	private Map<String,FinalMetaProduct> mapProducts;
	private PropertySet defaultProductProperties;
	private Map<VarBUILDMODE,PropertySet> mapBuildModeDefaults;
	
	public FinalRegistry( FinalLoader loader ) {
		this.loader = loader;
		
		mapSystems = new HashMap<String,FinalMetaSystem>();
		mapProducts = new HashMap<String,FinalMetaProduct>();
		mapBuildModeDefaults = new HashMap<VarBUILDMODE,PropertySet>();
	}

	public void load( String path , RunContext execrc ) throws Exception {
		properties = new PropertySet( "engine" , null );
		Document doc = ConfReader.readXmlFile( execrc , path );
		if( doc == null )
			throw new ExitException( "unable to reader engine property file " + path );

		Node root = doc.getDocumentElement();
		properties.loadRawFromElements( root );
		scatterSystemProperties();
		
		loadDirectory( root );
		loadProductDefaults( root );
	}

	private void scatterSystemProperties() throws Exception {
		CONNECTION_HTTP_PORT = properties.getSystemRequiredStringProperty( "connection.http.port" );
		CONNECTION_JMX_PORT = properties.getSystemRequiredStringProperty( "connection.jmx.port" );

		JABBER_ACCOUNT = properties.getSystemRequiredStringProperty( "jabber.account" );
		JABBER_PASSWORD = properties.getSystemRequiredStringProperty( "jabber.password" );
		JABBER_SERVER = properties.getSystemRequiredStringProperty( "jabber.server" );
		JABBER_CONFERENCESERVER = properties.getSystemRequiredStringProperty( "jabber.conferenceserver" );
		JABBER_INCLUDE = properties.getSystemRequiredStringProperty( "jabber.include" );
		JABBER_EXCLUDE = properties.getSystemRequiredStringProperty( "jabber.exclude" );
		
		properties.finishRawProperties();
	}

	private void loadDirectory( Node root ) throws Exception {
		Node node = ConfReader.xmlGetFirstChild( root , "directory" );
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , "system" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			FinalMetaSystem item = new FinalMetaSystem( this );
			item.load( itemNode );
			mapSystems.put( item.NAME , item );
			
			for( FinalMetaProduct product : item.mapProducts.values() )
				mapProducts.put( product.NAME , product );
		}
	}
	
	private void loadProductDefaults( Node root ) throws Exception {
		defaultProductProperties = new PropertySet( "default" , properties );
		
		Node node = ConfReader.xmlGetFirstChild( root , "defaults" );
		if( node == null )
			return;

		// top-level
		defaultProductProperties.loadRawFromElements( node );
		defaultProductProperties.moveRawAsIs();
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( node , "mode" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
			VarBUILDMODE mode = VarBUILDMODE.valueOf( MODE.toUpperCase() );
			PropertySet set = new PropertySet( MODE.toLowerCase() , defaultProductProperties );
			
			set.loadRawFromElements( itemNode );
			set.moveRawAsIs();
			
			mapBuildModeDefaults.put( mode , set );
		}
	}
	
	public String[] getSystems( ActionBase action ) {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public String[] getProducts( ActionBase action ) {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public FinalMetaSystem findSystem( ActionBase action , String name ) {
		return( mapSystems.get( name ) );
	}
	
	public FinalMetaProduct findProduct( ActionBase action , String name ) {
		return( mapProducts.get( name ) );
	}
	
	public PropertySet getDefaultProductProperties( ActionBase action ) {
		return( defaultProductProperties );
	}

	public PropertySet[] getBuildModeDefaults( ActionBase action ) {
		return( mapBuildModeDefaults.values().toArray( new PropertySet[0] ) );
	}

	public FinalRegistry copy( ActionBase action ) throws Exception {
		FinalRegistry r = new FinalRegistry( loader );
		r.properties = properties.copy( null );
		r.scatterSystemProperties();
		
		for( FinalMetaSystem system : mapSystems.values() ) {
			FinalMetaSystem rs = system.copy( r );
			r.mapSystems.put( rs.NAME , rs );
			
			for( FinalMetaProduct rp : rs.mapProducts.values() )
				r.mapProducts.put( rp.NAME , rp );
		}

		r.defaultProductProperties = defaultProductProperties.copy( null );
		
		for( VarBUILDMODE mode : mapBuildModeDefaults.keySet() ) {
			PropertySet set = mapBuildModeDefaults.get( mode );
			PropertySet rs = set.copy( r.defaultProductProperties );
			r.mapBuildModeDefaults.put( mode , rs );
		}
		
		return( r );
	}
	
	public void save( String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();

		// properties
		properties.saveAsElements( doc , root );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , "defaults" );
		defaultProductProperties.saveAsElements( doc , modeDefaults );
		
		// product defaults
		for( VarBUILDMODE mode : mapBuildModeDefaults.keySet() ) {
			PropertySet set = mapBuildModeDefaults.get( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeDefaults , "mode" );
			Common.xmlSetElementAttr( doc , modeElement , "name" , mode.toString().toLowerCase() );
			set.saveAsElements( doc , modeElement );
		}
		
		// directory 
		Element elementDir = Common.xmlCreateElement( doc , root , "directory" );
		for( FinalMetaSystem system : mapSystems.values() ) {
			Element elementSystem = Common.xmlCreateElement( doc , elementDir , "system" );
			Common.xmlSetElementAttr( doc , elementSystem , "name" , system.NAME );
			Common.xmlSetElementAttr( doc , elementSystem , "desc" , system.DESC );
			
			for( String productName : system.getProducts() ) {
				FinalMetaProduct product = system.getProduct( productName );
				Element elementProduct = Common.xmlCreateElement( doc , elementSystem , "product" );
				Common.xmlSetElementAttr( doc , elementProduct , "name" , product.NAME );
				Common.xmlSetElementAttr( doc , elementProduct , "desc" , product.DESC );
				Common.xmlSetElementAttr( doc , elementProduct , "path" , product.PATH );
			}
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addSystem( ServerTransaction transaction , FinalMetaSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			transaction.action.exitUnexpectedState();
		mapSystems.put( system.NAME , system );
	}

	public void deleteSystem( ServerTransaction transaction , FinalMetaSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			transaction.action.exitUnexpectedState();
		
		for( String productName : system.getProducts() )
			mapProducts.remove( productName );
		
		mapSystems.remove( system.NAME );
	}

	public FinalMetaSystem getSystem( ActionBase action , String name ) throws Exception {
		FinalMetaSystem system = findSystem( action , name );
		if( system == null )
			action.exit( "unknown system=" + system );
		return( system );
	}

	public FinalMetaProduct getProduct( ActionBase action , String name ) throws Exception {
		FinalMetaProduct product = findProduct( action , name );
		if( product == null )
			action.exit( "unknown product=" + name );
		return( product );
	}

	public void createProduct( ServerTransaction transaction , FinalMetaProduct product ) throws Exception {
		if( mapProducts.containsKey( product.NAME ) )
			transaction.action.exitUnexpectedState();
		mapProducts.put( product.NAME , product );
		product.system.addProduct( transaction , product );
	}
	
	public void deleteProduct( ServerTransaction transaction , FinalMetaProduct product ) throws Exception {
		if( mapProducts.get( product.NAME ) != product )
			transaction.action.exitUnexpectedState();
		
		mapProducts.remove( product.NAME );
		product.system.removeProduct( transaction , product );
	}

}
