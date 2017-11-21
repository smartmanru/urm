package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.EntityVar;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineCore;
import org.urm.meta.EngineObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineSettings extends EngineObject {

	public EngineCore core;
	public Engine engine;
	public RunContext execrc;
	public EngineContext context;
	public int version;

	private ObjectProperties execrcProperties;
	private ObjectProperties engineProperties;
	private ObjectProperties defaultProductProperties;
	private ObjectProperties defaultProductBuildProperties;
	private Map<DBEnumBuildModeType,ObjectProperties> mapBuildModeDefaults;
	
	public EngineSettings( EngineCore core ) {
		super( null );
		this.core = core;
		this.engine = core.engine;
		this.execrc = engine.execrc;
		
		mapBuildModeDefaults = new HashMap<DBEnumBuildModeType,ObjectProperties>();
	}

	@Override
	public String getName() {
		return( "server-settings" );
	}
	
	public void loadxml( Node root , DBConnection c ) throws Exception {
		this.version = c.getNextCoreVersion();
		setExecProperties();
		
		importEngineSettings( root , c );
		importProductDefaults( root , c );
		
		context = new EngineContext( execrc , engineProperties.getProperties() );
		context.scatterProperties();
	}

	public void loaddb( DBConnection c ) throws Exception {
		this.version = c.getCurrentCoreVersion();
		setExecProperties();
		
		loaddbEngineSettings( c );
		loaddbProductDefaults( c );
		
		context = new EngineContext( execrc , engineProperties.getProperties() );
		context.scatterProperties();
	}
	
	public void setData( ActionBase action , EngineSettings src , int version ) throws Exception {
		this.version = version;
		
		execrcProperties = src.execrcProperties;
		engineProperties = src.engineProperties;
		defaultProductProperties = src.defaultProductProperties;
		defaultProductBuildProperties = src.defaultProductBuildProperties;
		mapBuildModeDefaults = src.mapBuildModeDefaults;
		
		context = new EngineContext( execrc , engineProperties.getProperties() );
		context.scatterProperties();
	}
	
	private void importEngineSettings( Node root , DBConnection c ) throws Exception {
		EngineEntities entities = core.getEntities();
		engineProperties = entities.createEngineProps( execrcProperties );
		DBSettings.loadxml( root , engineProperties , true , true );
	}
	
	private void loaddbEngineSettings( DBConnection c ) throws Exception {
		EngineEntities entities = core.getEntities();
		engineProperties = entities.createEngineProps( execrcProperties );
		DBSettings.loaddb( c , DBVersions.CORE_ID , engineProperties );
	}
	
	private void importProductDefaults( Node root , DBConnection c ) throws Exception {
		EngineEntities entities = core.getEntities();
		
		defaultProductProperties = entities.createDefaultProductProps( engineProperties );
		Node node = ConfReader.xmlGetFirstChild( root , "defaults" );
		DBSettings.loadxml( node , defaultProductProperties , true , true );
		
		defaultProductBuildProperties = entities.createDefaultBuildCommonProps( defaultProductBuildProperties );
		Node build = ConfReader.xmlGetFirstChild( node , "build" );
		DBSettings.loadxml( build , defaultProductBuildProperties , true , true );
		
		// for build modes
		Node[] items = ConfReader.xmlGetChildren( build , "mode" );
		if( items != null ) {
			for( Node itemNode : items ) {
				String MODE = ConfReader.getRequiredAttrValue( itemNode , "name" );
				DBEnumBuildModeType mode = DBEnumBuildModeType.valueOf( MODE.toUpperCase() );
				ObjectProperties properties = entities.createDefaultBuildModeProps( defaultProductBuildProperties , mode );
	
				DBSettings.loadxml( itemNode , properties , false , true );
				mapBuildModeDefaults.put( mode , properties );
			}
		}
	}
	
	private void loaddbProductDefaults( DBConnection c ) throws Exception {
		EngineEntities entities = core.getEntities();
		
		defaultProductProperties = entities.createDefaultProductProps( engineProperties );
		DBSettings.loaddb( c , DBVersions.CORE_ID , defaultProductProperties );
		
		defaultProductBuildProperties = entities.createDefaultBuildCommonProps( defaultProductBuildProperties );
		DBSettings.loaddb( c , DBVersions.CORE_ID , defaultProductBuildProperties );
		
		// for build modes
		for( DBEnumBuildModeType mode : DBEnumBuildModeType.values() ) {
			if( mode == DBEnumBuildModeType.UNKNOWN )
				continue;
			
			ObjectProperties properties = entities.createDefaultBuildModeProps( defaultProductBuildProperties , mode );
			DBSettings.loaddb( c , DBVersions.CORE_ID , properties );
			mapBuildModeDefaults.put( mode , properties );
		}
	}
	
	public int getVersion() {
		return( version );
	}
	
	public EngineContext getServerContext() {
		return( context );
	}

	public PropertySet getExecProperties() {
		return( execrcProperties.getProperties() );
	}
	
	public ObjectProperties getEngineProperties() {
		return( engineProperties );
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
		EngineSettings r = new EngineSettings( core );
		r.version = version;
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
		DBSettings.savexml( doc , root , engineProperties , true , true );

		// defaults
		Element modeDefaults = Common.xmlCreateElement( doc , root , "defaults" );
		DBSettings.savexml( doc , modeDefaults , defaultProductProperties , false , true );
		Element modeBuild = Common.xmlCreateElement( doc , modeDefaults , "build" );
		DBSettings.savexml( doc , modeBuild , defaultProductBuildProperties , false , true );
		
		// product defaults
		for( DBEnumBuildModeType mode : mapBuildModeDefaults.keySet() ) {
			ObjectProperties set = mapBuildModeDefaults.get( mode );
			Element modeElement = Common.xmlCreateElement( doc , modeBuild , "mode" );
			Common.xmlSetElementAttr( doc , modeElement , "name" , mode.toString().toLowerCase() );
			DBSettings.savexml( doc , modeElement , set , false , true );
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
			EngineEntities entities = core.getEntities();
			set = entities.createDefaultBuildModeProps( defaultProductBuildProperties , mode );
			mapBuildModeDefaults.put( mode , set );
		}
		
		set.updateProperties( transaction , props , true );
		set.resolveRawProperties();
	}

	public void setExecProperties() throws Exception {
		EngineEntities entities = core.getEntities();
		execrcProperties = entities.createRunContextProps();
		
		RunContext rc = execrc;
		execrcProperties.setStringProperty( RunContext.PROPERTY_HOSTNAME , rc.hostName );
		execrcProperties.setPathProperty( RunContext.PROPERTY_USER_HOME , rc.userHome );
		execrcProperties.setStringProperty( RunContext.PROPERTY_OS_TYPE , Common.getEnumLower( rc.osType ) );
		execrcProperties.setPathProperty( RunContext.PROPERTY_INSTALL_PATH , rc.installPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_WORK_PATH , rc.workPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_AUTH_PATH , rc.authPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_DB_PATH , rc.dbPath );
		execrcProperties.setPathProperty( RunContext.PROPERTY_SERVER_CONFPATH , Common.getPath( rc.installPath , "etc" ) );
		execrcProperties.setPathProperty( RunContext.PROPERTY_SERVER_PRODUCTSPATH , Common.getPath( rc.installPath , "products" ) );

		// get custom properties from environment variables if any
		ObjectMeta meta = execrcProperties.getMeta();
		for( PropertyEntity entity : meta.getEntities() ) {
			if( !entity.custom ) 
				continue;
			
			for( EntityVar var : entity.getVars() ) {
				String value = RunContext.getProperty( var.NAME );
				execrcProperties.setStringProperty( var.NAME , value );
			}
		}
		
		execrcProperties.resolveRawProperties();
	}
}
