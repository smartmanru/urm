package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.ServerEngine;
import org.urm.engine.ServerEvents;
import org.urm.engine.ServerEventsApp;
import org.urm.engine.ServerEventsListener;
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
import org.urm.meta.product.MetaProductSettings;
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
	
	public PropertySet properties;
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String DIR_LOGS;
	public String RESOURCE_URL;

	ServerEventsApp eventsApp;
	public static int EVENT_FINALSTATE = 101;
	
	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	public ServerMonitoring( ServerLoader loader ) {
		super( null );
		this.loader = loader; 
		this.engine = loader.engine;
		this.events = engine.getEvents();
		
		mapProduct = new HashMap<String,ServerMonitoringProduct>();
		sourceMap = new HashMap<ServerObject,ServerMonitoringSource>();
	}

	public void scatterProperties() throws Exception {
		ServerSettings settings = loader.getServerSettings();
		PropertySet src = settings.serverContext.properties;
		
		ENABLED = properties.getSystemBooleanProperty( PROPERTY_ENABLED , false , true );
		RESOURCE_URL = properties.getSystemUrlExprProperty( PROPERTY_RESOURCE_URL , getProductExpr( src , ServerContext.PROPERTY_MON_RESURL ) , true );
		DIR_RES = properties.getSystemPathExprProperty( PROPERTY_RESOURCE_PATH , engine.execrc , getProductExpr( src , ServerContext.PROPERTY_MON_RESPATH ) , true );
		DIR_DATA = properties.getSystemPathExprProperty( PROPERTY_DIR_DATA , engine.execrc , getProductExpr( src , ServerContext.PROPERTY_MON_DATAPATH ) , true );
		DIR_REPORTS = properties.getSystemPathExprProperty( PROPERTY_DIR_REPORTS , engine.execrc , getProductExpr( src , ServerContext.PROPERTY_MON_REPORTPATH ) , true );
		DIR_LOGS = properties.getSystemPathExprProperty( PROPERTY_DIR_LOGS , engine.execrc , getProductExpr( src , ServerContext.PROPERTY_MON_LOGPATH ) , true );
	}

	private String getProductExpr( PropertySet src , String prop ) {
		String value = src.getExpressionByProperty( prop );
		return( value + "/" + PropertySet.getRef( MetaProductSettings.PROPERTY_PRODUCT_NAME ) );
	}
	
	public void load( String monFile , RunContext execrc ) throws Exception {
		ServerSettings settings = loader.getServerSettings();
		properties = new PropertySet( "defmon" , settings.serverContext.properties );
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		properties.loadFromNodeElements( root );
		scatterProperties();
	}
	
	public void save( ActionCore action , String path , RunContext execrc ) throws Exception {
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		properties.saveAsElements( doc , root );
		Common.xmlSaveDoc( doc , path );
	}

	public void start() {
		if( !ENABLED )
			return;
		
		ServerEvents events = engine.getEvents();
		eventsApp = events.createApp( "monitoring" );
		
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
		
		if( eventsApp != null ) {
			ServerEvents events = engine.getEvents();
			events.deleteApp( eventsApp );
		}
		
		for( ServerMonitoringSource source : sourceMap.values() )
			source.clearState();
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

		// start childs
		for( String envName : storage.getEnvironments() ) {
			MetaEnv env = storage.findEnvironment( envName );
			startEnvironment( env );
		}
		
		ServerMonitoringSource source = createSource( MONITORING_PRODUCT , product );
		ServerMonitoringProduct mon = new ServerMonitoringProduct( this , product.NAME , source , eventsApp );
		mapProduct.put( product.NAME , mon );
		mon.start();
		action.trace( "monitoring started for product=" + product.NAME );
	}

	public void startEnvironment( MetaEnv env ) {
		createSource( MONITORING_ENVIRONMENT , env );
		
		// start childs
		for( MetaEnvDC dc : env.getDatacenters() )
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
	
	private void removeSource( int level , ServerObject object ) {
		ServerMonitoringSource source = sourceMap.get( object );
		if( source == null )
			return;
		
		source.unsubscribeAll();
		sourceMap.remove( object );
	}

	private void replaceSource( int level , ServerObject objectOld , ServerObject objectNew ) {
		ServerMonitoringSource source = sourceMap.get( objectOld );
		if( source == null )
			return;
		
		sourceMap.remove( objectOld );
		source.setObject( objectNew );
		sourceMap.put( objectNew , source );
	}

	public void stopProduct( String productName ) {
		stopProduct( productName , false );
	}
	
	public void stopProduct( String productName , boolean delete ) {
		ServerMonitoringProduct mon = mapProduct.get( productName );
		if( mon == null )
			return;
		
		mon.stop();
		mapProduct.remove( productName );

		ServerRegistry registry = loader.getRegistry();
		ServerProduct product = registry.directory.findProduct( productName );
		ServerProductMeta storage = loader.findProductStorage( productName );
		
		// stop childs
		for( String envName : storage.getEnvironments() ) {
			MetaEnv env = storage.findEnvironment( envName );
			stopEnvironment( env , delete );
		}
		
		if( delete )
			removeSource( MONITORING_PRODUCT , product );
	}

	public void stopEnvironment( MetaEnv env , boolean delete ) {
		// stop childs
		for( MetaEnvDC dc : env.getDatacenters() )
			stopDatacenter( dc , delete );
		
		if( delete )
			removeSource( MONITORING_ENVIRONMENT , env );
	}

	public void stopDatacenter( MetaEnvDC dc , boolean delete ) {
		// stop childs
		for( MetaEnvServer server : dc.getOriginalServerList() )
			stopServer( server , delete );
		
		if( delete )
			removeSource( MONITORING_DATACENTER , dc );
	}
	
	public void stopServer( MetaEnvServer server , boolean delete ) {
		// stop childs
		for( MetaEnvServerNode node : server.getNodes() )
			stopNode( node , delete );
		
		if( delete )
			removeSource( MONITORING_SERVER , server );
	}
	
	public void stopNode( MetaEnvServerNode node , boolean delete ) {
		if( delete )
			removeSource( MONITORING_NODE , node );
	}
	
	public ServerMonitoringSource getObjectSource( ServerObject object ) {
		return( sourceMap.get( object ) );
	}

	public ServerMonitoringState getState( ServerEventsSubscription sub ) {
		ServerMonitoringState state = ( ServerMonitoringState )sub.getState();
		return( state );
	}

	public void setEnabled( ServerTransaction transaction , boolean enabled ) throws Exception {
		properties.setOriginalSystemBooleanProperty( PROPERTY_ENABLED , enabled );
		ENABLED = enabled;
	}

	public void setDefaultProperties( ServerTransaction transaction , PropertySet props ) throws Exception {
		properties.updateProperties( props );
		scatterProperties();
	}
	
	public ServerProduct findProduct( String name ) {
		ServerRegistry registry = loader.getRegistry();
		return( registry.directory.findProduct( name ) );
	}

	public ServerEventsSubscription subscribe( ServerEventsApp app , ServerEventsListener listener , ServerObject object ) {
		ServerMonitoringSource source = getObjectSource( object );
		if( source == null )
			return( null );
		
		return( app.subscribe( source , listener ) );
	}

	public void createProduct( ServerProductMeta storage ) {
		ServerRegistry registry = loader.getRegistry();
		ServerProduct product = registry.directory.findProduct( storage.name );
		startProduct( product );
	}
	
	public void deleteProduct( ServerProductMeta storage ) {
		stopProduct( storage.name , true );
	}
	
	public void modifyProduct( ServerProductMeta storageOld , ServerProductMeta storage ) {
		for( String envName : storage.getEnvironments() ) {
			MetaEnv envNew = storage.findEnvironment( envName );
			MetaEnv envOld = storageOld.findEnvironment( envName );
			if( envOld != null )
				modifyEnvironment( envOld , envNew );
			else
				startEnvironment( envNew );
		}
		for( String envName : storageOld.getEnvironments() ) {
			MetaEnv envOld = storageOld.findEnvironment( envName );
			MetaEnv envNew = storage.findEnvironment( envName );
			if( envNew == null )
				stopEnvironment( envOld , true );
		}
	}

	private void modifyEnvironment( MetaEnv envOld , MetaEnv envNew ) {
		replaceSource( MONITORING_ENVIRONMENT , envOld , envNew );
		
		for( MetaEnvDC dcNew : envNew.getDatacenters() ) {
			MetaEnvDC dcOld = envOld.findDC( dcNew.NAME );
			if( dcOld != null )
				modifyDatacenter( dcOld , dcNew );
			else
				startDatacenter( dcNew );
		}
		for( MetaEnvDC dcOld : envOld.getDatacenters() ) {
			MetaEnvDC dcNew = envNew.findDC( dcOld.NAME );
			if( dcNew == null )
				stopDatacenter( dcOld , true );
		}
	}
	
	private void modifyDatacenter( MetaEnvDC dcOld , MetaEnvDC dcNew ) {
		replaceSource( MONITORING_DATACENTER , dcOld , dcNew );
		
		for( MetaEnvServer serverNew : dcNew.getServers() ) {
			MetaEnvServer serverOld = dcOld.findServer( serverNew.NAME );
			if( serverOld != null )
				modifyServer( serverOld , serverNew );
			else
				startServer( serverNew );
		}
		for( MetaEnvServer serverOld : dcOld.getServers() ) {
			MetaEnvServer serverNew = dcNew.findServer( serverOld.NAME );
			if( serverNew == null )
				stopServer( serverOld , true );
		}
	}
	
	private void modifyServer( MetaEnvServer serverOld , MetaEnvServer serverNew ) {
		replaceSource( MONITORING_DATACENTER , serverOld , serverNew );
		
		for( MetaEnvServerNode nodeNew : serverNew.getNodes() ) {
			MetaEnvServerNode nodeOld = serverOld.findNode( nodeNew.POS );
			if( nodeOld != null )
				replaceSource( MONITORING_NODE , nodeOld , nodeNew );
			else
				startNode( nodeNew );
		}
		for( MetaEnvServerNode nodeOld : serverOld.getNodes() ) {
			MetaEnvServerNode nodeNew = serverNew.findNode( nodeOld.POS );
			if( nodeNew == null )
				stopNode( nodeOld , true );
		}
	}
	
}
