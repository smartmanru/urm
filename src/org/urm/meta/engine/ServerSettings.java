package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.product.Meta.VarBUILDMODE;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerSettings extends ServerObject {

	public ServerLoader loader;
	
	public ServerContext serverContext;

	private PropertySet defaultProductProperties;
	private PropertySet defaultProductBuildProperties;
	private Map<VarBUILDMODE,PropertySet> mapBuildModeDefaults;
	
	public ServerSettings( ServerLoader loader ) {
		super( null );
		this.loader = loader;
		
		serverContext = new ServerContext( this );
		mapBuildModeDefaults = new HashMap<VarBUILDMODE,PropertySet>();
	}

	public void load( String path , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , path );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + path , path );

		Node root = doc.getDocumentElement();
		serverContext.load( root , execrc );
		loadProductDefaults( root );
	}

	private void loadProductDefaults( Node root ) throws Exception {
		defaultProductProperties = new PropertySet( "product.primary" , serverContext.properties );
		defaultProductBuildProperties = new PropertySet( "build.common" , defaultProductProperties );
		
		Node node = ConfReader.xmlGetFirstChild( root , "defaults" );
		if( node == null )
			return;

		// top-level
		defaultProductProperties.loadFromNodeElements( node );
		defaultProductProperties.resolveRawProperties( true );
		
		Node build = ConfReader.xmlGetFirstChild( node , "build" );
		if( build == null )
			return;
			
		defaultProductBuildProperties.loadFromNodeElements( build );
		defaultProductBuildProperties.resolveRawProperties( true );
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( build , "mode" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
			VarBUILDMODE mode = VarBUILDMODE.valueOf( MODE.toUpperCase() );
			PropertySet set = new PropertySet( "build." + MODE.toLowerCase() , defaultProductBuildProperties );

			set.loadFromNodeElements( itemNode );
			set.resolveRawProperties( true );
			mapBuildModeDefaults.put( mode , set );
		}
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

	public ServerSettings copy() throws Exception {
		ServerSettings r = new ServerSettings( loader );
		r.serverContext = serverContext.copy();
		
		r.defaultProductProperties = defaultProductProperties.copy( serverContext.properties );
		r.defaultProductBuildProperties = defaultProductBuildProperties.copy( r.defaultProductProperties );
		
		for( VarBUILDMODE mode : mapBuildModeDefaults.keySet() ) {
			PropertySet set = mapBuildModeDefaults.get( mode );
			PropertySet rs = set.copy( r.defaultProductBuildProperties );
			r.mapBuildModeDefaults.put( mode , rs );
		}
		
		return( r );
	}
	
	public void save( String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "server" );
		Element root = doc.getDocumentElement();

		// properties
		serverContext.save( doc , root );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , "defaults" );
		defaultProductProperties.saveAsElements( doc , modeDefaults );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , "build" );
		defaultProductBuildProperties.saveAsElements( doc , modeBuild );
		
		// product defaults
		for( VarBUILDMODE mode : mapBuildModeDefaults.keySet() ) {
			PropertySet set = mapBuildModeDefaults.get( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , "mode" );
			Common.xmlSetElementAttr( doc , modeElement , "name" , mode.toString().toLowerCase() );
			set.saveAsElements( doc , modeElement );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void setServerProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		serverContext.setServerProperties( transaction , props );
		serverContext.resolveServerProperties( transaction );
	}

	public void setProductDefaultsProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		defaultProductProperties.updateProperties( props );
		defaultProductProperties.resolveRawProperties( true );
	}

	public void setProductBuildCommonDefaultsProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		defaultProductBuildProperties.updateProperties( props );
		defaultProductBuildProperties.resolveRawProperties( true );
	}
	
	public void setProductBuildModeDefaultsProperties( ServerTransaction transaction , VarBUILDMODE mode , PropertySet props ) throws Exception {
		PropertySet set = mapBuildModeDefaults.get( mode );
		if( set == null ) {
			set = new PropertySet( "build." + Common.getEnumLower( mode ) , defaultProductBuildProperties );
			mapBuildModeDefaults.put( mode , set );
		}
		
		set.updateProperties( props );
		set.resolveRawProperties( true );
	}

}
