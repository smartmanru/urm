package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertySet;
import org.urm.meta.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductCoreSettings;

public class EngineMonitoring extends EngineObject {

	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	private Engine engine;

	Map<Integer,MonitoringProduct> mapProduct;
	boolean running;
	public boolean ENABLED;
	
	public ObjectProperties properties;
	
	public EngineMonitoring( Engine engine ) {
		super( null );
		this.engine = engine;
		
		mapProduct = new HashMap<Integer,MonitoringProduct>();
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
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			createProduct( action , product );
		}
	}

	private void addProduct( AppProduct product , MonitoringProduct monp ) {
		mapProduct.put( product.ID , monp );
	}
	
	private void removeProduct( AppProduct product ) {
		mapProduct.remove( product.ID );
	}
	
	private MonitoringProduct getProduct( AppProduct product ) {
		return( mapProduct.get( product.ID ) );
	}
	
	public void setEnabled( EngineTransaction transaction , boolean enabled ) throws Exception {
		properties.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
		
		if( enabled )
			startAll( transaction.getAction() );
		else
			stopAll( transaction.getAction() );
	}

	public ObjectProperties getProperties() {
		return( properties );
	}
	
	public void modifyProperties( EngineTransaction transaction , PropertySet props ) throws Exception {
		stopAll( transaction.getAction() );
		properties.updateProperties( transaction , props , true );
		ENABLED = properties.getBooleanProperty( PROPERTY_ENABLED );
		startAll( transaction.getAction() );
	}

	public void setProductMonitoringProperties( EngineTransaction transaction , Meta meta , PropertySet props ) throws Exception {
		EngineDirectory directory = transaction.getDirectory();
		AppProduct product = directory.getProduct( meta.name );
		ActionBase action = transaction.getAction();
		
		stopProduct( action , product );
		MetaProductCoreSettings core = meta.getProductCoreSettings();
		core.setMonitoringProperties( transaction , props );
	}
	
	public void modifyTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
		EngineDirectory directory = transaction.getDirectory();
		AppProduct product = directory.getProduct( target.meta.name );
		ActionBase action = transaction.getAction();
		stopProduct( action , product );
	}

	public synchronized void transactionCommitCreateProduct( TransactionBase transaction , AppProduct product ) throws Exception {
		createProduct( transaction.getAction() , product );
	}
	
	public synchronized void transactionCommitModifyProduct( TransactionBase transaction , AppProduct product ) throws Exception {
		modifyProduct( transaction.getAction() , product );
	}
	
	public synchronized void transactionCommitDeleteProduct( TransactionBase transaction , AppProduct product ) throws Exception {
		deleteProduct( transaction.getAction() , product );
	}
	
	private synchronized void createProduct( ActionBase action , AppProduct product ) throws Exception {
		Meta meta = product.getMeta( action );
		MetaMonitoring mon = meta.getMonitoring();
		MonitoringProduct monp = new MonitoringProduct( this , product , mon );
		addProduct( product , monp );
		startProduct( action , product );
	}
	
	private synchronized void modifyProduct( ActionBase action , AppProduct product ) throws Exception {
		deleteProduct( action , product );
		createProduct( action , product );
	}
	
	private synchronized void deleteProduct( ActionBase action , AppProduct product ) throws Exception {
		stopProduct( action , product );
		removeProduct( product );
	}	

	public synchronized void setProductEnabled( ActionBase action , AppProduct product ) throws Exception {
		startProduct( action , product );
	}
	
	public synchronized void setProductDisabled( ActionBase action , AppProduct product ) throws Exception {
		stopProduct( action , product );
	}
	
	private synchronized void startProduct( ActionBase action , AppProduct product ) throws Exception {
		if( !ENABLED )
			return;
		
		MonitoringProduct mon = getProduct( product );
		if( mon != null )
			mon.start( action );
	}
	
	private synchronized void stopProduct( ActionBase action , AppProduct product ) throws Exception {
		MonitoringProduct mon = getProduct( product );
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
		MonitoringProduct mon = getProduct( product );
		return( mon != null && isRunning( product.system ) && product.OFFLINE == false && product.MONITORING_ENABLED );
	}
	
	public boolean isRunning( MetaEnv env ) {
		EngineDirectory directory = engine.serverAction.getServerDirectory();
		AppProduct product = directory.findProduct( env.meta.name );
		return( product != null && isRunning( product ) && env.OFFLINE == false );
	}

	public boolean isRunning( MetaEnvSegment sg ) {
		EngineDirectory directory = engine.serverAction.getServerDirectory();
		AppProduct product = directory.findProduct( sg.meta.name );
		if( product == null )
			return( false );
		
		MonitoringProduct mon = getProduct( product );
		if( mon == null )
			return( false );
		
		MetaMonitoringTarget target = mon.meta.findMonitoringTarget( sg );
		return( target != null && isRunning( sg.env ) && sg.OFFLINE == false && ( target.enabledMajor || target.enabledMinor ) );
	}	
	
	public boolean isRunning( MetaEnvServer server ) {
		return( isRunning( server.sg ) && server.OFFLINE == false );
	}

}
