package org.urm.server;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.ExitException;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.server.meta.Meta.VarBUILDMODE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerRegistry {

	public ServerLoader loader;
	public RunContext execrc;
	
	public ServerContext serverContext;

	private Map<String,ServerSystem> mapSystems;
	private Map<String,ServerProduct> mapProducts;
	private PropertySet defaultProductProperties;
	private PropertySet defaultProductBuildProperties;
	private Map<VarBUILDMODE,PropertySet> mapBuildModeDefaults;
	
	public ServerRegistry( ServerLoader loader ) {
		this.loader = loader;
		
		serverContext = new ServerContext( this );
		mapSystems = new HashMap<String,ServerSystem>();
		mapProducts = new HashMap<String,ServerProduct>();
		mapBuildModeDefaults = new HashMap<VarBUILDMODE,PropertySet>();
	}

	public void load( String path , RunContext execrc ) throws Exception {
		this.execrc = execrc;
		Document doc = ConfReader.readXmlFile( execrc , path );
		if( doc == null )
			throw new ExitException( "unable to reader engine property file " + path );

		Node root = doc.getDocumentElement();
		serverContext.load( root , execrc );
		loadDirectory( root );
		loadProductDefaults( root );
	}

	private void loadDirectory( Node root ) throws Exception {
		Node node = ConfReader.xmlGetFirstChild( root , "directory" );
		if( node == null )
			return;
		
		Node[] items = ConfReader.xmlGetChildren( node , "system" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			ServerSystem item = new ServerSystem( this );
			item.load( itemNode );
			mapSystems.put( item.NAME , item );
			
			for( ServerProduct product : item.mapProducts.values() )
				mapProducts.put( product.NAME , product );
		}
	}
	
	private void loadProductDefaults( Node root ) throws Exception {
		defaultProductProperties = new PropertySet( "primary" , serverContext.properties );
		defaultProductBuildProperties = new PropertySet( "build.common" , defaultProductProperties );
		
		Node node = ConfReader.xmlGetFirstChild( root , "defaults" );
		if( node == null )
			return;

		// top-level
		defaultProductProperties.loadOriginalFromNodeElements( node );
		
		Node build = ConfReader.xmlGetFirstChild( node , "build" );
		if( build != null )
			return;
			
		defaultProductBuildProperties.loadOriginalFromNodeElements( build );
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( build , "mode" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
			VarBUILDMODE mode = VarBUILDMODE.valueOf( MODE.toUpperCase() );
			PropertySet set = new PropertySet( "build." + MODE.toLowerCase() , defaultProductBuildProperties );

			set.loadOriginalFromNodeElements( itemNode );
			mapBuildModeDefaults.put( mode , set );
		}
	}
	
	public String[] getSystems() {
		return( Common.getSortedKeys( mapSystems ) );
	}

	public String[] getProducts() {
		return( Common.getSortedKeys( mapProducts ) );
	}
	
	public ServerSystem findSystem( ServerSystem system ) {
		if( system == null )
			return( null );
		return( mapSystems.get( system.NAME ) );
	}
	
	public ServerSystem findSystem( String name ) {
		return( mapSystems.get( name ) );
	}
	
	public ServerProduct findProduct( ServerProduct product ) {
		if( product == null )
			return( null );
		return( mapProducts.get( product.NAME ) );
	}
	
	public ServerProduct findProduct( String name ) {
		return( mapProducts.get( name ) );
	}
	
	public ServerContext getServerContext() {
		return( serverContext );
	}

	public PropertySet getDefaultProductProperties() {
		return( defaultProductProperties );
	}

	public PropertySet getDefaultProductBuildProperties() {
		return( defaultProductBuildProperties );
	}

	public PropertySet getDefaultProductBuildProperties( VarBUILDMODE mode ) {
		return( mapBuildModeDefaults.get( mode ) );
	}
	
	public PropertySet[] getBuildModeDefaults() {
		return( mapBuildModeDefaults.values().toArray( new PropertySet[0] ) );
	}

	public ServerRegistry copy() throws Exception {
		ServerRegistry r = new ServerRegistry( loader );
		r.execrc = execrc;
		r.serverContext = serverContext.copy();
		
		for( ServerSystem system : mapSystems.values() ) {
			ServerSystem rs = system.copy( r );
			r.mapSystems.put( rs.NAME , rs );
			
			for( ServerProduct rp : rs.mapProducts.values() )
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
		serverContext.save( doc , root );

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
		for( ServerSystem system : mapSystems.values() ) {
			Element elementSystem = Common.xmlCreateElement( doc , elementDir , "system" );
			Common.xmlSetElementAttr( doc , elementSystem , "name" , system.NAME );
			Common.xmlSetElementAttr( doc , elementSystem , "desc" , system.DESC );
			
			for( String productName : system.getProducts() ) {
				ServerProduct product = system.getProduct( productName );
				Element elementProduct = Common.xmlCreateElement( doc , elementSystem , "product" );
				Common.xmlSetElementAttr( doc , elementProduct , "name" , product.NAME );
				Common.xmlSetElementAttr( doc , elementProduct , "desc" , product.DESC );
				Common.xmlSetElementAttr( doc , elementProduct , "path" , product.PATH );
			}
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void addSystem( ServerTransaction transaction , ServerSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != null )
			transaction.exit( "system=" + system.NAME + " is not unique" );
		mapSystems.put( system.NAME , system );
	}

	public void deleteSystem( ServerTransaction transaction , ServerSystem system ) throws Exception {
		if( mapSystems.get( system.NAME ) != system )
			transaction.exit( "system=" + system.NAME + " is unknown or mismatched" );
		
		for( String productName : system.getProducts() )
			mapProducts.remove( productName );
		
		mapSystems.remove( system.NAME );
	}

	public ServerSystem getSystem( String name ) throws Exception {
		ServerSystem system = findSystem( name );
		if( system == null )
			throw new ExitException( "unknown system=" + system );
		return( system );
	}

	public ServerProduct getProduct( String name ) throws Exception {
		ServerProduct product = findProduct( name );
		if( product == null )
			throw new ExitException( "unknown product=" + name );
		return( product );
	}

	public void createProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		if( mapProducts.containsKey( product.NAME ) )
			transaction.exit( "product=" + product.NAME + " is not unique" );
		mapProducts.put( product.NAME , product );
		product.system.addProduct( transaction , product );
	}
	
	public void deleteProduct( ServerTransaction transaction , ServerProduct product ) throws Exception {
		if( mapProducts.get( product.NAME ) != product )
			transaction.exit( "product=" + product.NAME + " is unknown or mismatched" );
		
		mapProducts.remove( product.NAME );
		product.system.removeProduct( transaction , product );
	}

	public void setRegistryServerProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		serverContext.setRegistryServerProperties( transaction , props );
		serverContext.resolveRegistryServerProperties( transaction );
	}

}
