package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.db.core.DBSettings;
import org.urm.db.core.DBVersions;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineData;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineMonitoring extends EngineObject {

	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	EngineData data;
	Engine engine;
	EngineEvents events;

	Map<String,MonitoringProduct> mapProduct;
	boolean running;
	public boolean ENABLED;
	
	public ObjectProperties properties;
	
	public EngineMonitoring( EngineData data ) {
		super( null );
		this.data = data; 
		this.engine = data.engine;
		this.events = engine.getEvents();
		
		mapProduct = new HashMap<String,MonitoringProduct>();
		running = false;
	}

	@Override
	public String getName() {
		return( "server-monitoring" );
	}
	
	public void loadxml( EngineLoader loader , Node root ) throws Exception {
		EngineSettings settings = data.getEngineSettings();
		EngineEntities entities = data.getEntities();
		properties = entities.createEngineMonitoringProps( settings.getEngineProperties() );
		DBSettings.importxml( loader , root , properties , true );
		scatterProperties();
	}
	
	public void savexml( EngineLoader loader , Document doc , Element root ) throws Exception {
		DBSettings.exportxml( loader , doc , root , properties , true );
	}

	public void loaddb( EngineLoader loader ) throws Exception {
		EngineSettings settings = data.getEngineSettings();
		EngineEntities entities = data.getEntities();
		properties = entities.createEngineMonitoringProps( settings.getEngineProperties() );
		DBSettings.loaddbValues( loader , DBVersions.CORE_ID , properties , true );
	}
	
	private void scatterProperties() throws Exception {
		ENABLED = properties.getBooleanProperty( PROPERTY_ENABLED );
	}
	
	public void start( ActionBase action ) throws Exception {
		running = true;
		
		EngineDirectory directory = data.getDirectory();
		for( String systemName : directory.getSystemNames() ) {
			AppSystem system = directory.findSystem( systemName );
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
	
	private void createSystem( ActionBase action , AppSystem system ) throws Exception {
		for( String productName : system.getProductNames() ) {
			ProductMeta storage = data.findProductStorage( productName );
			createProduct( action , storage );
		}
	}

	public void setEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		properties.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
		
		if( enabled )
			startAll( transaction.getAction() );
		else
			stopAll( transaction.getAction() );
	}

	public void setDefaultProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( transaction , props , true );
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

	public boolean isRunning( AppSystem system ) {
		return( isEnabled() && system.OFFLINE == false );
	}
	
	public boolean isRunning( Product product ) {
		MonitoringProduct mon = mapProduct.get( product.NAME );
		return( mon != null && isRunning( product.system ) && product.OFFLINE == false && product.MONITORING_ENABLED );
	}
	
	public boolean isRunning( MetaEnv env ) {
		EngineDirectory directory = data.getDirectory();
		Product product = directory.findProduct( env.meta.name );
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
