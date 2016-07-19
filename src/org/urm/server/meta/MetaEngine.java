package org.urm.server.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.server.meta.Metadata.VarBUILDMODE;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class MetaEngine {

	PropertySet properties;

	public String CONNECTION_HTTP_PORT;
	public String CONNECTION_JMX_PORT;
	
	public String JABBER_ACCOUNT;
	public String JABBER_PASSWORD;
	public String JABBER_SERVER;
	public String JABBER_CONFERENCESERVER;
	public String JABBER_INCLUDE;
	public String JABBER_EXCLUDE;

	Map<String,MetaEngineSystem> mapSystems;
	Map<String,MetaEngineProduct> mapProducts;
	PropertySet defaultProductProperties;
	Map<VarBUILDMODE,PropertySet> mapBuildModeDefaults;
	
	public MetaEngine( FinalMetaLoader loader ) {
		mapSystems = new HashMap<String,MetaEngineSystem>();
		mapProducts = new HashMap<String,MetaEngineProduct>();
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
			MetaEngineSystem item = new MetaEngineSystem( this );
			item.load( itemNode );
			mapSystems.put( item.NAME , item );
			
			for( MetaEngineProduct product : item.mapProducts.values() )
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
	
}
