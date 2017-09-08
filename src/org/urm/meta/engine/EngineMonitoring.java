package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.status.EngineStatus;
import org.urm.engine.status.StatusSource;
import org.urm.engine.status.StatusData;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductCoreSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineMonitoring extends EngineObject {

	EngineLoader loader;
	Engine engine;
	EngineEvents events;

	Map<String,EngineMonitoringProduct> mapProduct;
	
	public Map<EngineObject,StatusSource> sourceMap;
	
	public PropertySet properties;
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String DIR_LOGS;
	public String RESOURCE_URL;

	EngineEventsApp eventsApp;
	
	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	public EngineMonitoring( EngineLoader loader ) {
		super( null );
		this.loader = loader; 
		this.engine = loader.engine;
		this.events = engine.getEvents();
		
		mapProduct = new HashMap<String,EngineMonitoringProduct>();
		sourceMap = new HashMap<EngineObject,StatusSource>();
	}

	@Override
	public String getName() {
		return( "server-monitoring" );
	}
	
	public void scatterProperties() throws Exception {
		EngineSettings settings = loader.getServerSettings();
		PropertySet src = settings.serverContext.properties;
		
		ENABLED = properties.getSystemBooleanProperty( PROPERTY_ENABLED , false , true );
		RESOURCE_URL = properties.getSystemUrlExprProperty( PROPERTY_RESOURCE_URL , getProductExpr( src , EngineContext.PROPERTY_MON_RESURL ) , true );
		DIR_RES = properties.getSystemPathExprProperty( PROPERTY_RESOURCE_PATH , engine.execrc , getProductExpr( src , EngineContext.PROPERTY_MON_RESPATH ) , true );
		DIR_DATA = properties.getSystemPathExprProperty( PROPERTY_DIR_DATA , engine.execrc , getProductExpr( src , EngineContext.PROPERTY_MON_DATAPATH ) , true );
		DIR_REPORTS = properties.getSystemPathExprProperty( PROPERTY_DIR_REPORTS , engine.execrc , getProductExpr( src , EngineContext.PROPERTY_MON_REPORTPATH ) , true );
		DIR_LOGS = properties.getSystemPathExprProperty( PROPERTY_DIR_LOGS , engine.execrc , getProductExpr( src , EngineContext.PROPERTY_MON_LOGPATH ) , true );
	}

	private String getProductExpr( PropertySet src , String prop ) {
		String value = src.getExpressionByProperty( prop );
		return( value + "/" + PropertySet.getRef( MetaProductCoreSettings.PROPERTY_PRODUCT_NAME ) );
	}
	
	public void load( String monFile , RunContext execrc ) throws Exception {
		EngineSettings settings = loader.getServerSettings();
		properties = new PropertySet( "defmon" , settings.serverContext.properties );
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		properties.loadFromNodeElements( root , false );
		scatterProperties();
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		properties.saveAsElements( doc , root , false );
		Common.xmlSaveDoc( doc , path );
	}

	public void start() {
		if( !ENABLED )
			return;
		
		sourceMap.clear();
		EngineEvents events = engine.getEvents();
		eventsApp = events.createApp( "monitoring" );
		
		EngineRegistry registry = loader.getRegistry();
		for( String systemName : registry.directory.getSystemNames() ) {
			System system = registry.directory.findSystem( systemName );
			startSystem( system );
		}
	}

	public void stop() {
		engine.info( "stop monitoring ..." );
		for( EngineMonitoringProduct mon : mapProduct.values() )
			mon.stop();

		mapProduct.clear();
		
		if( eventsApp != null ) {
			EngineEvents events = engine.getEvents();
			events.deleteApp( eventsApp );
		}
		
		for( StatusSource source : sourceMap.values() )
			source.clearState();
	}

	private void startSystem( System system ) {
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			startProduct( product );
		}
	}

	public StatusData getState( EngineEventsSubscription sub ) {
		StatusData state = ( StatusData )sub.getState();
		return( state );
	}

	public void setEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		properties.setOriginalSystemBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
	}

	public void setDefaultProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( props , true );
		scatterProperties();
	}
	
	public Product findProduct( String name ) {
		EngineRegistry registry = loader.getRegistry();
		return( registry.directory.findProduct( name ) );
	}

	public void modifyTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
	}

	public StatusSource findTargetSource( MetaMonitoringTarget target ) {
		MetaEnv env = target.meta.findEnv( target.ENV );
		if( env == null )
			return( null );
		MetaEnvSegment sg = env.findSegment( target.SG );
		if( sg == null)
			return( null );
		
		EngineStatus status = engine.getStatus();
		return( status.getObjectSource( sg ) );
	}

	public synchronized void createProduct( ActionBase action , Product product , ProductMeta storage ) {
	}
	
	public void modifyProduct( ActionBase action , ProductMeta storageOld , ProductMeta storageNew ) {
	}
	
	public synchronized void deleteProduct( ActionBase action , ProductMeta storage ) {
	}	
	
	public synchronized void startProduct( String product ) {
	}
	
	public synchronized void stopProduct( String product ) {
	}
	
	private synchronized void startProduct( Product product ) {
	}
	
}
