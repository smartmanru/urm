package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductCoreSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineMonitoring extends EngineObject {

	EngineLoader loader;
	Engine engine;
	EngineEvents events;

	Map<String,MonitoringProduct> mapProduct;
	boolean running;
	
	public PropertySet properties;
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String DIR_LOGS;
	public String RESOURCE_URL;

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
		
		mapProduct = new HashMap<String,MonitoringProduct>();
		running = false;
	}

	@Override
	public String getName() {
		return( "server-monitoring" );
	}
	
	public void scatterProperties() throws Exception {
		EngineSettings settings = loader.getServerSettings();
		PropertySet src = settings.context.properties;
		
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
		properties = new PropertySet( "defmon" , settings.context.properties );
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

	public void start( ActionBase action ) throws Exception {
		running = true;
		
		EngineRegistry registry = loader.getRegistry();
		for( String systemName : registry.directory.getSystemNames() ) {
			System system = registry.directory.findSystem( systemName );
			createSystem( action , system );
		}
	}
	
	public synchronized void stop( ActionBase action ) throws Exception {
		engine.info( "stop monitoring ..." );
		running = false;
		stopAll( action );
		mapProduct.clear();
	}

	private void startAll( ActionBase action ) throws Exception {
		for( MonitoringProduct mon : mapProduct.values() )
			mon.start( action );
	}
	
	private void stopAll( ActionBase action ) throws Exception {
		for( MonitoringProduct mon : mapProduct.values() )
			mon.stop( action );
	}
	
	private void createSystem( ActionBase action , System system ) throws Exception {
		for( String productName : system.getProductNames() ) {
			ProductMeta storage = loader.findProductStorage( productName );
			createProduct( action , storage );
		}
	}

	public void setEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		properties.setOriginalSystemBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
		
		if( enabled )
			startAll( transaction.getAction() );
		else
			stopAll( transaction.getAction() );
	}

	public void setDefaultProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( props , true );
		scatterProperties();
	}

	public void setProductMonitoringProperties( EngineTransaction transaction , Meta meta , PropertySet props ) throws Exception {
		MonitoringProduct mon = mapProduct.get( meta.name );
		if( mon == null )
			return;
		
		ActionBase action = transaction.getAction();
		mon.stop( action );
		MetaMonitoring metaMon = meta.getMonitoring( action );
		metaMon.setProductProperties( transaction , props );
		mon.start( action );
	}
	
	public void modifyTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
	}

	public synchronized void createProduct( ActionBase action , ProductMeta storage ) throws Exception {
		MetaMonitoring meta = storage.getMonitoring();
		Product product = action.getProduct( storage.name );
		MonitoringProduct mon = new MonitoringProduct( this , product , meta );
		mapProduct.put( storage.name , mon );
		mon.start( action );
	}
	
	public synchronized void modifyProduct( ActionBase action , ProductMeta storageOld , ProductMeta storageNew ) throws Exception {
		deleteProduct( action , storageOld );
		createProduct( action , storageNew );
	}
	
	public synchronized void deleteProduct( ActionBase action , ProductMeta storage ) throws Exception {
		MonitoringProduct mon = mapProduct.get( storage.name );
		if( mon != null ) {
			mon.stop( action );
			mapProduct.remove( storage.name );
		}
	}	
	
	public synchronized void startProduct( ActionBase action , String product ) throws Exception {
		MonitoringProduct mon = mapProduct.get( product );
		if( mon != null )
			mon.start( action );
	}
	
	public synchronized void stopProduct( ActionBase action , String product ) throws Exception {
		MonitoringProduct mon = mapProduct.get( product );
		if( mon != null )
			mon.stop( action );
	}

	public boolean isEnabled() {
		if( running && ENABLED )
			return( true );
		return( false );
	}

	public boolean isRunning( System system ) {
		return( isEnabled() && system.OFFLINE == false );
	}
	
	public boolean isRunning( Product product ) {
		MonitoringProduct mon = mapProduct.get( product.NAME );
		return( mon != null && isRunning( product.system ) && product.OFFLINE == false && product.MONITORING_ENABLED );
	}
	
	public boolean isRunning( MetaEnv env ) {
		EngineRegistry registry = loader.getRegistry();
		Product product = registry.directory.findProduct( env.meta.name );
		return( product != null && isRunning( product ) && env.OFFLINE == false );
	}

	public boolean isRunning( MetaEnvSegment sg ) {
		MonitoringProduct mon = mapProduct.get( sg.meta.name );
		MetaMonitoringTarget target = mon.meta.findMonitoringTarget( sg );
		return( target != null && isRunning( sg.env ) && sg.OFFLINE == false && ( target.enabledMajor || target.enabledMinor ) );
	}	
	
	public boolean isRunning( MetaEnvServer server ) {
		return( isRunning( server.sg ) && server.OFFLINE == false );
	}

}
