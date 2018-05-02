package org.urm.engine.data;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.transaction.EngineTransaction;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.MonitoringProduct;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.env.MetaMonitoringTarget;
import org.urm.meta.loader.EngineObject;

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
	
	public ObjectProperties ops;
	
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
		this.ops = properties;
		this.ENABLED = properties.getBooleanProperty( PROPERTY_ENABLED );
	}
	
	public void start( ActionBase action ) throws Exception {
		running = true;
		
		EngineDirectory directory = action.getEngineDirectory();
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
		ops.setBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
		
		if( enabled )
			startAll( transaction.getAction() );
		else
			stopAll( transaction.getAction() );
	}

	public ObjectProperties getProperties() {
		return( ops );
	}
	
	public void modifyTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
		EngineDirectory directory = transaction.getDirectory();
		AppProduct product = directory.getProduct( target.envs.meta.name );
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
		EngineProduct ep = product.getEngineProduct();
		MonitoringProduct monp = new MonitoringProduct( this , ep );
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
	
	public synchronized void stopProduct( ActionBase action , AppProduct product ) throws Exception {
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
		EngineDirectory directory = engine.serverAction.getEngineDirectory();
		AppProduct product = directory.findProduct( env.meta.name );
		return( product != null && isRunning( product ) && env.OFFLINE == false );
	}

	public boolean isRunning( MetaEnvSegment sg ) {
		EngineDirectory directory = engine.serverAction.getEngineDirectory();
		AppProduct product = directory.findProduct( sg.meta.name );
		if( product == null )
			return( false );
		
		MonitoringProduct mon = getProduct( product );
		if( mon == null )
			return( false );
		
		MetaMonitoring metamon = sg.meta.getMonitoring();
		MetaMonitoringTarget target = metamon.findTarget( sg );
		return( target != null && isRunning( sg.env ) && sg.OFFLINE == false && ( target.MAJOR_ENABLED || target.MINOR_ENABLED ) );
	}	
	
	public boolean isRunning( MetaEnvServer server ) {
		return( isRunning( server.sg ) && server.OFFLINE == false );
	}

}
