package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;

public class EngineMonitoring extends EngineObject {

	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	private Engine engine;

	Map<String,MonitoringProduct> mapProduct;
	boolean running;
	public boolean ENABLED;
	
	public ObjectProperties properties;
	
	public EngineMonitoring( Engine engine ) {
		super( null );
		this.engine = engine;
		
		mapProduct = new HashMap<String,MonitoringProduct>();
		running = false;
	}

	@Override
	public String getName() {
		return( "server-monitoring" );
	}
	
	public void setProperties( ObjectProperties properties ) throws Exception {
		this.properties = properties;
		this.ENABLED = properties.getBooleanProperty( PROPERTY_ENABLED );
	}
	
	public void start( ActionBase action ) throws Exception {
		running = true;
		
		EngineDirectory directory = action.getServerDirectory();
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
		for( String productName : system.getProductNames() )
			createProduct( action , productName );
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
		ENABLED = properties.getBooleanProperty( PROPERTY_ENABLED );
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

	public synchronized void createProduct( ActionBase action , String productName ) throws Exception {
		Meta meta = action.getProductMetadata( productName );
		MetaMonitoring mon = meta.getMonitoring( action );
		AppProduct product = action.getProduct( meta.name );
		MonitoringProduct mp = new MonitoringProduct( this , product , mon );
		mapProduct.put( meta.name , mp );
		mp.start( action );
	}
	
	public synchronized void modifyProduct( ActionBase action , String productName ) throws Exception {
		deleteProduct( action , productName );
		createProduct( action , productName );
	}
	
	public synchronized void deleteProduct( ActionBase action , String productName ) throws Exception {
		MonitoringProduct mon = mapProduct.get( productName );
		if( mon != null ) {
			mon.stop( action );
			mapProduct.remove( productName );
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
	
	public boolean isRunning( AppProduct product ) {
		MonitoringProduct mon = mapProduct.get( product.NAME );
		return( mon != null && isRunning( product.system ) && product.OFFLINE == false && product.MONITORING_ENABLED );
	}
	
	public boolean isRunning( MetaEnv env ) {
		EngineDirectory directory = engine.serverAction.getServerDirectory();
		AppProduct product = directory.findProduct( env.meta.name );
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
