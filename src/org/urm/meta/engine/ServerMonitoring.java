package org.urm.meta.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionCore;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.EngineTransaction;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.status.ServerStatusSource;
import org.urm.engine.status.ServerStatusData;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaMonitoringTarget;
import org.urm.meta.product.MetaProductCoreSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ServerMonitoring extends EngineObject {

	EngineLoader loader;
	Engine engine;
	EngineEvents events;

	Map<String,ServerMonitoringProduct> mapProduct;
	
	public static int MONITORING_APP = 1;
	public static int MONITORING_SYSTEM = 2;
	public static int MONITORING_PRODUCT = 3;
	public static int MONITORING_ENVIRONMENT = 4;
	public static int MONITORING_SEGMENT = 5;
	public static int MONITORING_SERVER = 6;
	public static int MONITORING_NODE = 7;
	public Map<EngineObject,ServerStatusSource> sourceMap;
	
	public PropertySet properties;
	public boolean ENABLED;
	public String DIR_DATA;
	public String DIR_REPORTS;
	public String DIR_RES;
	public String DIR_LOGS;
	public String RESOURCE_URL;

	EngineEventsApp eventsApp;
	
	public static String EXTRA_SEGMENT_ITEMS = "sgitems";
	public static String EXTRA_SERVER_ITEMS = "serveritems";
	public static String EXTRA_NODE_ITEMS = "nodeitems";
	
	// properties
	public static String PROPERTY_ENABLED = "monitoring.enabled";
	public static String PROPERTY_RESOURCE_PATH = "default.resources.path";
	public static String PROPERTY_RESOURCE_URL = "default.resources.url";
	public static String PROPERTY_DIR_DATA = "default.data.path";
	public static String PROPERTY_DIR_REPORTS = "default.reports.path";
	public static String PROPERTY_DIR_LOGS = "default.logs.path";
	
	public ServerMonitoring( EngineLoader loader ) {
		super( null );
		this.loader = loader; 
		this.engine = loader.engine;
		this.events = engine.getEvents();
		
		mapProduct = new HashMap<String,ServerMonitoringProduct>();
		sourceMap = new HashMap<EngineObject,ServerStatusSource>();
	}

	@Override
	public String getName() {
		return( "server-monitoring" );
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
		return( value + "/" + PropertySet.getRef( MetaProductCoreSettings.PROPERTY_PRODUCT_NAME ) );
	}
	
	public void load( String monFile , RunContext execrc ) throws Exception {
		ServerSettings settings = loader.getServerSettings();
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
		
		ServerRegistry registry = loader.getRegistry();
		startApp( registry.directory );
		for( String systemName : registry.directory.getSystems() ) {
			ServerSystem system = registry.directory.findSystem( systemName );
			startSystem( system );
		}
	}

	public void stop() {
		engine.info( "stop monitoring ..." );
		for( ServerMonitoringProduct mon : mapProduct.values() )
			mon.stop();

		mapProduct.clear();
		
		if( eventsApp != null ) {
			EngineEvents events = engine.getEvents();
			events.deleteApp( eventsApp );
		}
		
		for( ServerStatusSource source : sourceMap.values() )
			source.clearState();
	}

	public void startApp( ServerDirectory directory ) {
		createSource( MONITORING_APP , directory );
		engine.trace( "monitoring started for applications" );
	}
	
	public void startSystem( ServerSystem system ) {
		createSource( MONITORING_SYSTEM , system );
		engine.trace( "monitoring started for system=" + system.NAME );
		
		// start products
		for( String productName : system.getProductNames() ) {
			ServerProduct product = system.findProduct( productName );
			startProduct( product );
		}
	}
	
	public void startProduct( ServerProduct product ) {
		ProductMeta storage = loader.findProductStorage( product.NAME );
		if( storage == null || storage.loadFailed ) {
			engine.trace( "ignore monitoring for non-healthy product=" + product.NAME );
			return;
		}
		
		MetaMonitoring meta = storage.getMonitoring();
		if( !meta.ENABLED ) {
			engine.trace( "monitoring is turned off for product=" + product.NAME );
			return;
		}

		// start childs
		for( String envName : storage.getEnvironmentNames() ) {
			MetaEnv env = storage.findEnvironment( envName );
			startEnvironment( env );
		}
		
		ServerStatusSource source = createSource( MONITORING_PRODUCT , product );
		ServerMonitoringProduct mon = new ServerMonitoringProduct( this , product.NAME , source , eventsApp );
		mapProduct.put( product.NAME , mon );
		mon.start();
		engine.trace( "monitoring started for product=" + product.NAME );
	}

	public void startEnvironment( MetaEnv env ) {
		createSource( MONITORING_ENVIRONMENT , env );
		
		// start childs
		for( MetaEnvSegment sg : env.getSegments() )
			startSegment( sg );
	}

	public void startSegment( MetaEnvSegment sg ) {
		createSource( MONITORING_SEGMENT , sg );
		
		// start childs
		for( MetaEnvServer server : sg.getServers() )
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
	
	private ServerStatusSource createSource( int level , EngineObject object ) {
		String name = "o" + level + "." + object.objectId;
		ServerStatusSource source = new ServerStatusSource( events , object , level , name );
		sourceMap.put( object , source );
		return( source );
	}
	
	private void removeSource( int level , EngineObject object ) {
		ServerStatusSource source = sourceMap.get( object );
		if( source == null )
			return;
		
		source.unsubscribeAll();
		sourceMap.remove( object );
	}

	private void replaceSource( int level , EngineObject objectOld , EngineObject objectNew ) {
		ServerStatusSource source = sourceMap.get( objectOld );
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
		ProductMeta storage = loader.findProductStorage( productName );
		
		// stop childs
		for( String envName : storage.getEnvironmentNames() ) {
			MetaEnv env = storage.findEnvironment( envName );
			stopEnvironment( env , delete );
		}
		
		if( delete )
			removeSource( MONITORING_PRODUCT , product );
	}

	public void stopEnvironment( MetaEnv env , boolean delete ) {
		// stop childs
		for( MetaEnvSegment sg : env.getSegments() )
			stopSegment( sg , delete );
		
		if( delete )
			removeSource( MONITORING_ENVIRONMENT , env );
	}

	public void stopSegment( MetaEnvSegment sg , boolean delete ) {
		// stop childs
		for( MetaEnvServer server : sg.getServers() )
			stopServer( server , delete );
		
		if( delete )
			removeSource( MONITORING_SEGMENT , sg );
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

	public ServerStatusSource getAppSource() {
		ServerRegistry registry = loader.getRegistry();
		return( getObjectSource( registry.directory ) );
	}
	
	public ServerStatusSource getObjectSource( EngineObject object ) {
		return( sourceMap.get( object ) );
	}

	public ServerStatusData getState( EngineEventsSubscription sub ) {
		ServerStatusData state = ( ServerStatusData )sub.getState();
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
	
	public ServerProduct findProduct( String name ) {
		ServerRegistry registry = loader.getRegistry();
		return( registry.directory.findProduct( name ) );
	}

	public EngineEventsSubscription subscribe( EngineEventsApp app , EngineEventsListener listener , EngineObject object ) {
		ServerStatusSource source = getObjectSource( object );
		if( source == null )
			return( null );
		
		return( app.subscribe( source , listener ) );
	}

	public void createProduct( ProductMeta storage ) {
		ServerRegistry registry = loader.getRegistry();
		ServerProduct product = registry.directory.findProduct( storage.name );
		startProduct( product );
	}
	
	public void deleteProduct( ProductMeta storage ) {
		stopProduct( storage.name , true );
	}
	
	public void modifyProduct( ProductMeta storageOld , ProductMeta storage ) {
		for( String envName : storage.getEnvironmentNames() ) {
			MetaEnv envNew = storage.findEnvironment( envName );
			MetaEnv envOld = storageOld.findEnvironment( envName );
			if( envOld != null )
				modifyEnvironment( envOld , envNew );
			else
				startEnvironment( envNew );
		}
		for( String envName : storageOld.getEnvironmentNames() ) {
			MetaEnv envOld = storageOld.findEnvironment( envName );
			MetaEnv envNew = storage.findEnvironment( envName );
			if( envNew == null )
				stopEnvironment( envOld , true );
		}
	}

	private void modifyEnvironment( MetaEnv envOld , MetaEnv envNew ) {
		replaceSource( MONITORING_ENVIRONMENT , envOld , envNew );
		
		for( MetaEnvSegment sgNew : envNew.getSegments() ) {
			MetaEnvSegment sgOld = envOld.findSegment( sgNew.NAME );
			if( sgOld != null )
				modifySegment( sgOld , sgNew );
			else
				startSegment( sgNew );
		}
		for( MetaEnvSegment sgOld : envOld.getSegments() ) {
			MetaEnvSegment sgNew = envNew.findSegment( sgOld.NAME );
			if( sgNew == null )
				stopSegment( sgOld , true );
		}
	}
	
	private void modifySegment( MetaEnvSegment sgOld , MetaEnvSegment sgNew ) {
		replaceSource( MONITORING_SEGMENT , sgOld , sgNew );
		
		for( MetaEnvServer serverNew : sgNew.getServers() ) {
			MetaEnvServer serverOld = sgOld.findServer( serverNew.NAME );
			if( serverOld != null )
				modifyServer( serverOld , serverNew );
			else
				startServer( serverNew );
		}
		for( MetaEnvServer serverOld : sgOld.getServers() ) {
			MetaEnvServer serverNew = sgNew.findServer( serverOld.NAME );
			if( serverNew == null )
				stopServer( serverOld , true );
		}
	}
	
	private void modifyServer( MetaEnvServer serverOld , MetaEnvServer serverNew ) {
		replaceSource( MONITORING_SEGMENT , serverOld , serverNew );
		
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

	public void deleteTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
		target.monitoring.deleteTarget( transaction , target );
	}
	
	public void modifyTarget( EngineTransaction transaction , MetaMonitoringTarget target ) throws Exception {
	}

	public ServerStatusSource findTargetSource( MetaMonitoringTarget target ) {
		MetaEnv env = target.meta.findEnv( target.ENV );
		if( env == null )
			return( null );
		MetaEnvSegment sg = env.findSegment( target.SG );
		if( sg == null)
			return( null );
		return( getObjectSource( sg ) );
	}
	
}
