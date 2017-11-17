package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineSettings extends EngineObject {

	public EngineData data;
	public Engine engine;
	public RunContext execrc;
	public EngineContext context;

	private ObjectProperties execrcProperties;
	private ObjectProperties engineProperties;
	private ObjectProperties defaultProductProperties;
	private ObjectProperties defaultProductBuildProperties;
	private Map<DBEnumBuildModeType,ObjectProperties> mapBuildModeDefaults;
	
	public EngineSettings( EngineData data ) {
		super( null );
		this.data = data;
		this.engine = data.engine;
		this.execrc = engine.execrc;
		
		mapBuildModeDefaults = new HashMap<DBEnumBuildModeType,ObjectProperties>();
	}

	@Override
	public String getName() {
		return( "server-settings" );
	}
	
	public void load( String path , DBConnection c , boolean savedb ) throws Exception {
		setExecProperties();
		
		Document doc = ConfReader.readXmlFile( execrc , path );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + path , path );

		Node root = doc.getDocumentElement();
		
		loadEngineSettings( root , c , savedb );
		loadProductDefaults( root );
		
		context = new EngineContext( execrc , engineProperties.getProperties() );
		context.scatterProperties();
	}

	public void setData( ActionBase action , EngineSettings src ) throws Exception {
		execrcProperties = src.execrcProperties;
		engineProperties = src.engineProperties;
		defaultProductProperties = src.defaultProductProperties;
		defaultProductBuildProperties = src.defaultProductBuildProperties;
		mapBuildModeDefaults = src.mapBuildModeDefaults;
		
		context = new EngineContext( execrc , engineProperties.getProperties() );
		context.scatterProperties();
	}
	
	private void loadEngineSettings( Node root , DBConnection c , boolean savedb ) throws Exception {
		engineProperties = new ObjectProperties( "engine" , execrc );
		engineProperties.load( root , execrcProperties );
	}
	
	private void loadProductDefaults( Node root ) throws Exception {
		defaultProductProperties = new ObjectProperties( "product.primary" , execrc );
		
		Node node = ConfReader.xmlGetFirstChild( root , "defaults" );
		defaultProductProperties.load( node , engineProperties );
		
		Node build = ConfReader.xmlGetFirstChild( node , "build" );
		defaultProductBuildProperties = new ObjectProperties( "build.common " , execrc );
		defaultProductBuildProperties.load( build , defaultProductProperties );
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( build , "mode" );
		if( items != null ) {
			for( Node itemNode : items ) {
				String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
				DBEnumBuildModeType mode = DBEnumBuildModeType.valueOf( MODE.toUpperCase() );
				ObjectProperties properties = new ObjectProperties( "build." + MODE.toLowerCase() , execrc );
	
				properties.load( itemNode , defaultProductBuildProperties );
				mapBuildModeDefaults.put( mode , properties );
			}
		}
	}
	
	public EngineContext getServerContext() {
		return( context );
	}

	public PropertySet getExecProperties() {
		return( execrcProperties.getProperties() );
	}
	
	public PropertySet getDefaultProductProperties() {
		return( defaultProductProperties.getProperties() );
	}

	public PropertySet getDefaultProductBuildProperties() {
		return( defaultProductBuildProperties.getProperties() );
	}

	public PropertySet getDefaultProductBuildProperties( DBEnumBuildModeType mode ) {
		ObjectProperties properties = mapBuildModeDefaults.get( mode );
		return( properties.getProperties() );
	}
	
	public PropertySet[] getBuildModeDefaults() {
		return( mapBuildModeDefaults.values().toArray( new PropertySet[0] ) );
	}

	public EngineSettings copy() throws Exception {
		EngineSettings r = new EngineSettings( data );
		r.execrcProperties = execrcProperties;
		r.engineProperties = engineProperties.copy( execrcProperties );
		r.context = context.copy( r.engineProperties.getProperties() );
		
		r.defaultProductProperties = defaultProductProperties.copy( r.engineProperties );
		r.defaultProductBuildProperties = defaultProductBuildProperties.copy( r.defaultProductProperties );
		
		for( DBEnumBuildModeType mode : mapBuildModeDefaults.keySet() ) {
			ObjectProperties set = mapBuildModeDefaults.get( mode );
			ObjectProperties rs = set.copy( r.defaultProductBuildProperties );
			r.mapBuildModeDefaults.put( mode , rs );
		}
		
		return( r );
	}
	
	public void save( String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "server" );
		Element root = doc.getDocumentElement();

		// properties
		engineProperties.saveAsElements( doc , root , false );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , "defaults" );
		defaultProductProperties.saveAsElements( doc , modeDefaults , false );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , "build" );
		defaultProductBuildProperties.saveAsElements( doc , modeBuild , false );
		
		// product defaults
		for( DBEnumBuildModeType mode : mapBuildModeDefaults.keySet() ) {
			ObjectProperties set = mapBuildModeDefaults.get( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , "mode" );
			Common.xmlSetElementAttr( doc , modeElement , "name" , mode.toString().toLowerCase() );
			set.saveAsElements( doc , modeElement , false );
		}
		
		Common.xmlSaveDoc( doc , path );
	}

	public void setServerProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		engineProperties.updateProperties( transaction , props , false );
		engineProperties.resolveRawProperties();
	}

	public void setProductDefaultsProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		defaultProductProperties.updateProperties( transaction , props , true );
		defaultProductProperties.resolveRawProperties();
	}

	public void setProductBuildCommonDefaultsProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		defaultProductBuildProperties.updateProperties( transaction , props , true );
		defaultProductBuildProperties.resolveRawProperties();
	}
	
	public void setProductBuildModeDefaultsProperties( EngineTransaction transaction , DBEnumBuildModeType mode , PropertySet props ) throws Exception {
		ObjectProperties set = mapBuildModeDefaults.get( mode );
		if( set == null ) {
			set = new ObjectProperties( "build." + Common.getEnumLower( mode ) , execrc );
			set.create( defaultProductBuildProperties );
			mapBuildModeDefaults.put( mode , set );
		}
		
		set.updateProperties( transaction , props , true );
		set.resolveRawProperties();
	}

	public void setExecProperties() throws Exception {
		execrcProperties = new ObjectProperties( "execrc" , execrc );
		execrcProperties.create( null );
		PropertySet set = execrcProperties.getProperties();
		
		RunContext rc = execrc;
		set.setStringProperty( RunContext.PROPERTY_OS_TYPE , Common.getEnumLower( rc.osType ) );
		set.setPathProperty( RunContext.PROPERTY_INSTALL_PATH , rc.installPath , null );
		set.setPathProperty( RunContext.PROPERTY_WORK_PATH , rc.workPath , null );
		set.setPathProperty( RunContext.PROPERTY_USER_HOME , rc.userHome , null );
		set.setPathProperty( RunContext.PROPERTY_AUTH_PATH , rc.authPath , null );
		set.setStringProperty( RunContext.PROPERTY_HOSTNAME , rc.hostName );
		set.setPathProperty( RunContext.PROPERTY_SERVER_CONFPATH , rc.installPath + "/etc" , null );
		set.setPathProperty( RunContext.PROPERTY_SERVER_MASTERPATH , rc.installPath + "/master" , null );
		set.setPathProperty( RunContext.PROPERTY_SERVER_PRODUCTSPATH , rc.installPath + "/products" , null );
		
		execrcProperties.resolveRawProperties();
	}
}
