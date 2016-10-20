package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsSubscription;
import org.urm.engine.ServerTransaction;
import org.urm.meta.ServerLoader;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoring;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMonitoring extends ServerObject {

	ServerLoader loader;
	ServerEngine engine;
	ServerEvents events;

	Map<String,ServerMonitoringProduct> mapProduct;
	
	public static int MONITORING_SYSTEM = 1;
	public static int MONITORING_PRODUCT = 2;
	public static int MONITORING_ENVIRONMENT = 3;
	public static int MONITORING_DATACENTER = 4;
	public static int MONITORING_SERVER = 5;
	public static int MONITORING_NODE = 6;
	public Map<ServerObject,ServerMonitoringSource> sourceMap;
	
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String RESOURCE_URL;
	
	// properties
	public static String PROPERTY_ENABLED = "server.monitoring.enabled";
	public static String PROPERTY_DIR_DATA = "server.data.path";
	public static String PROPERTY_DIR_REPORTS = "server.reports.path";
	public static String PROPERTY_DIR_RES = "server.resources.path";
	public static String PROPERTY_RESOURCE_URL = "server.resources.url";
	
	public ServerMonitoring( ServerLoader loader ) {
		super( null );
		this.loader = loader; 
		this.engine = loader.engine;
		this.events = engine.getEvents();
		
		mapProduct = new HashMap<String,ServerMonitoringProduct>();
		sourceMap = new HashMap<ServerObject,ServerMonitoringSource>(); 
	}

	public void load( String monFile , RunContext execrc ) throws Exception {
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();

		String rootPath = Common.getPath( engine.execrc.installPath , "monitoring" );
		ENABLED = Common.getBooleanValue( ConfReader.getPropertyValue( root , PROPERTY_ENABLED , "no" ) );
		DIR_DATA = ConfReader.getPropertyValue( root , PROPERTY_DIR_DATA , Common.getPath( rootPath , "data" ) );
		DIR_REPORTS = ConfReader.getPropertyValue( root , PROPERTY_DIR_REPORTS , Common.getPath( rootPath , "reports" ) );
		DIR_RES = ConfReader.getPropertyValue( root , PROPERTY_DIR_RES , Common.getPath( rootPath , "res" ) );
		RESOURCE_URL = ConfReader.getPropertyValue( root , PROPERTY_RESOURCE_URL , "" );
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_DATA , DIR_DATA );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_REPORTS , DIR_REPORTS );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_DIR_RES , DIR_REPORTS );
		Common.xmlCreatePropertyElement( doc , root , PROPERTY_RESOURCE_URL , DIR_RES );
		
		Common.xmlSaveDoc( doc , path );
	}

	public void start() {
		ServerRegistry registry = loader.getRegistry();
		for( String systemName : registry.directory.getSystems() ) {
			ServerSystem system = registry.directory.findSystem( systemName );
			startSystem( system );
		}
	}

	public void stop() {
		for( ServerMonitoringProduct mon : mapProduct.values() )
			mon.stop();
		
		mapProduct.clear();
	}

	public void startSystem( ServerSystem system ) {
		ActionBase action = engine.serverAction;
		createSource( MONITORING_SYSTEM , system );
		action.trace( "monitoring started for system=" + system.NAME );
		
		// start products
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			startProduct( product );
		}
	}
	
	public void startProduct( ServerProduct product ) {
		ActionBase action = engine.serverAction;
		ServerProductMeta storage = loader.findProductStorage( product.NAME );
		if( storage == null || storage.loadFailed ) {
			action.trace( "ignore monitoring for non-healthy product=" + product.NAME );
			return;
		}
		
		MetaMonitoring meta = storage.getMonitoring();
		if( !meta.ENABLED ) {
			action.trace( "monitoring is turned off for product=" + product.NAME );
			return;
		}

		ServerMonitoringSource source = createSource( MONITORING_PRODUCT , product );
		ServerMonitoringProduct mon = new ServerMonitoringProduct( this , meta , source );
		mapProduct.put( product.NAME , mon );
		mon.start();
		action.trace( "monitoring started for product=" + product.NAME );
		
		// start childs
		for( String envName : storage.getEnvironments() ) {
			MetaEnv env = storage.findEnvironment( envName );
			startEnvironment( env );
		}
	}

	public void startEnvironment( MetaEnv env ) {
		createSource( MONITORING_ENVIRONMENT , env );
		
		// start childs
		for( MetaEnvDC dc : env.getOriginalDCList() )
			startDatacenter( dc );
	}

	public void startDatacenter( MetaEnvDC dc ) {
		createSource( MONITORING_DATACENTER , dc );
		
		// start childs
		for( MetaEnvServer server : dc.getOriginalServerList() )
			startServer( server );
	}
	
	public void startServer( MetaEnvServer server ) {
		createSource( MONITORING_SERVER , server );
		
		// start childs
		for( MetaEnvServerNode node : server.getNodes() )
			startNode( node );
	}
	
	public void startNode( MetaEnvServerNode node ) {
		createSource( MONITORING_NODE , node );
	}
	
	private ServerMonitoringSource createSource( int level , ServerObject object ) {
		String name = "o" + level + "." + object.objectId;
		ServerMonitoringSource source = new ServerMonitoringSource( this , object , level , name );
		sourceMap.put( object , source );
		return( source );
	}
	
	public void stopProduct( String productName ) {
		ServerMonitoringProduct mon = mapProduct.get( productName );
		if( mon == null )
			return;
		
		mon.stop();
		mapProduct.remove( productName );
	}

	public ServerMonitoringSource getObjectSource( ServerObject object ) {
		return( sourceMap.get( object ) );
	}

	public ServerMonitoringState getState( ServerEventsSubscription sub ) {
		ServerMonitoringState state = ( ServerMonitoringState )sub.getState();
		return( state );
	}

	public void setEnabled( ServerTransaction transaction , boolean enabled ) {
		ENABLED = enabled;
	}
	
}
