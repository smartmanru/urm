package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.EngineTransaction;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.Types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineSettings extends EngineObject {

	public EngineLoader loader;
	
	public EngineContext serverContext;

	private PropertySet defaultProductProperties;
	private PropertySet defaultProductBuildProperties;
	private Map<VarBUILDMODE,PropertySet> mapBuildModeDefaults;
	
	public EngineSettings( EngineLoader loader ) {
		super( null );
		this.loader = loader;
		
		serverContext = new EngineContext( this );
		mapBuildModeDefaults = new HashMap<VarBUILDMODE,PropertySet>();
	}

	@Override
	public String getName() {
		return( "server-settings" );
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
		defaultProductProperties.loadFromNodeElements( node , false );
		defaultProductProperties.resolveRawProperties( true );
		
		Node build = ConfReader.xmlGetFirstChild( node , "build" );
		if( build == null )
			return;
			
		defaultProductBuildProperties.loadFromNodeElements( build , false );
		defaultProductBuildProperties.resolveRawProperties( true );
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( build , "mode" );
		if( items == null )
			return;
		
		for( Node itemNode : items ) {
			String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
			VarBUILDMODE mode = VarBUILDMODE.valueOf( MODE.toUpperCase() );
			PropertySet set = new PropertySet( "build." + MODE.toLowerCase() , defaultProductBuildProperties );

			set.loadFromNodeElements( itemNode , false );
			set.resolveRawProperties( true );
			mapBuildModeDefaults.put( mode , set );
		}
	}
	
	public EngineContext getServerContext() {
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

	public EngineSettings copy() throws Exception {
		EngineSettings r = new EngineSettings( loader );
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
		defaultProductProperties.saveAsElements( doc , modeDefaults , false );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , "build" );
		defaultProductBuildProperties.saveAsElements( doc , modeBuild , false );
		
		// product defaults
		for( VarBUILDMODE mode : mapBuildModeDefaults.keySet() ) {
			PropertySet set = mapBuildModeDefaults.get( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , "mode" );
			Common.xmlSetElementAttr( doc , modeElement , "name" , mode.toString().toLowerCase() );
			set.saveAsElements( doc , modeElement , false );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void setServerProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		serverContext.setServerProperties( transaction , props );
		serverContext.resolveServerProperties( transaction );
	}

	public void setProductDefaultsProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		defaultProductProperties.updateProperties( props , true );
		defaultProductProperties.resolveRawProperties( true );
	}

	public void setProductBuildCommonDefaultsProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		defaultProductBuildProperties.updateProperties( props , true );
		defaultProductBuildProperties.resolveRawProperties( true );
	}
	
	public void setProductBuildModeDefaultsProperties( EngineTransaction transaction , VarBUILDMODE mode , PropertySet props ) throws Exception {
		PropertySet set = mapBuildModeDefaults.get( mode );
		if( set == null ) {
			set = new PropertySet( "build." + Common.getEnumLower( mode ) , defaultProductBuildProperties );
			mapBuildModeDefaults.put( mode , set );
		}
		
		set.updateProperties( props , true );
		set.resolveRawProperties( true );
	}

}