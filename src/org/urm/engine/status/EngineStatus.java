package org.urm.engine.status;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.Engine;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.Product;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class EngineStatus extends EngineObject {

	Engine engine;
	EngineEvents events;

	public enum StatusType {
		APP ,
		SYSTEM ,
		PRODUCT ,
		ENVIRONMENT ,
		SEGMENT ,
		SERVER ,
		NODE
	};
	
	public static String EXTRA_SEGMENT_ITEMS = "sgitems";
	public static String EXTRA_SERVER_ITEMS = "serveritems";
	public static String EXTRA_NODE_ITEMS = "nodeitems";
	
	private Map<String,StatusSource> globalSources;
	private Map<String,EngineStatusProduct> products;
	
	public EngineStatus( Engine engine ) {
		super( null );
		this.engine = engine;
		this.events = engine.getEvents();
		
		globalSources = new HashMap<String,StatusSource>();
		products = new HashMap<String,EngineStatusProduct>(); 
	}

	@Override
	public String getName() {
		return( "engine-status" );
	}

	public void init() throws Exception {
	}
	
	public synchronized void start( ActionBase action , EngineData data ) {
		action.trace( "start status tracking ..." );
		startApp( action , data );
	}
	
	public synchronized void stop( ActionBase action ) throws Exception {
		action.trace( "stop status tracking ..." );
		for( EngineStatusProduct status : products.values() )
			status.stop( action );
		products.clear();
		
		for( StatusSource source : globalSources.values() )
			source.unsubscribeAll();
		globalSources.clear();
	}
	
	public synchronized StatusSource getAppSource() {
		return( getGlobalSource( StatusType.APP , "app" ) );
	}
	
	public synchronized StatusSource getObjectSource( EngineObject object ) {
		if( object instanceof EngineDirectory )
			return( getAppSource() );
			
		if( object instanceof AppSystem ) {
			AppSystem system = ( AppSystem )object; 
			return( getGlobalSource( StatusType.SYSTEM , system.NAME ) );
		}
		
		if( object instanceof Product ) {
			Product product = ( Product )object; 
			return( getGlobalSource( StatusType.PRODUCT , product.NAME ) );
		}
		
		if( object instanceof MetaEnv ) {
			MetaEnv env = ( MetaEnv )object; 
			return( getProductSource( env.meta , object ) );
		}
		
		if( object instanceof MetaEnvSegment ) {
			MetaEnvSegment sg = ( MetaEnvSegment )object; 
			return( getProductSource( sg.meta , object ) );
		}
		
		if( object instanceof MetaEnvServer ) {
			MetaEnvServer server = ( MetaEnvServer )object; 
			return( getProductSource( server.meta , object ) );
		}
		
		if( object instanceof MetaEnvServerNode ) {
			MetaEnvServerNode node = ( MetaEnvServerNode )object; 
			return( getProductSource( node.meta , object ) );
		}
		
		return( null );
	}

	public synchronized StatusSource getProductSource( Meta meta , EngineObject object ) {
		EngineStatusProduct productStatus = products.get( meta.name );
		if( productStatus == null )
			return( null );
		
		return( productStatus.getProductSource( object ) );
	}
	
	public synchronized StatusSource getObjectSource( Product product ) {
		return( getGlobalSource( StatusType.PRODUCT , product.NAME ) );
	}

	public synchronized StatusSource getObjectSource( Meta meta ) {
		return( getGlobalSource( StatusType.PRODUCT , meta.name ) );
	}

	public synchronized boolean isValidProduct( String name ) {
		if( globalSources.get( name ) != null )
			return( true );
		return( false );
	}

	public synchronized void setProductStatus( ActionBase action , Product product , OBJECT_STATE state ) {
		StatusSource productSource = getObjectSource( product );
		if( productSource != null && productSource.setFinalState( state ) ) {
			AppSystem system = product.system;
			recalculateSystem( system );
		}
	}

	public synchronized void setSegmentStatus( ActionBase action , MetaEnvSegment sg , SegmentStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( sg.meta );
		if( productStatus != null )
			productStatus.setSegmentStatus( action , sg , status );
	}
	
	public synchronized void setSegmentItemsStatus( ActionBase action , MetaEnvSegment sg , SegmentStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( sg.meta );
		if( productStatus != null )
			productStatus.setSegmentItemsStatus( action , sg , status );
	}
	
	public synchronized void setServerStatus( ActionBase action , MetaEnvServer server , ServerStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( server.meta );
		if( productStatus != null )
			productStatus.setServerStatus( action , server , status );
	}
	
	public synchronized void setServerItemsStatus( ActionBase action , MetaEnvServer server , ServerStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( server.meta );
		if( productStatus != null )
			productStatus.setServerItemsStatus( action , server , status );
	}
	
	public synchronized void setServerNodeStatus( ActionBase action , MetaEnvServerNode node , NodeStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( node.meta );
		if( productStatus != null )
			productStatus.setServerNodeStatus( action , node , status );
	}
	
	public synchronized void setServerNodeItemsStatus( ActionBase action , MetaEnvServerNode node , NodeStatus status ) {
		EngineStatusProduct productStatus = getProductStatus( node.meta );
		if( productStatus != null )
			productStatus.setServerNodeItemsStatus( action , node , status );
	}
	
	public synchronized EngineEventsSubscription subscribe( EngineEventsApp app , EngineEventsListener listener , EngineObject object ) {
		StatusSource source = getObjectSource( object );
		if( source == null )
			return( null );
		
		return( app.subscribe( source , listener ) );
	}

	public synchronized void createProduct( ActionBase action , Product product , ProductMeta storage ) {
		startProduct( action , product , storage );
	}

	public void modifyProduct( ActionBase action , ProductMeta storageOld , ProductMeta storageNew ) {
		EngineStatusProduct productStatus = products.get( storageOld.name );
		if( productStatus == null )
			return;
		
		productStatus.modifyProduct( action , storageOld , storageNew );
	}
	
	public synchronized void deleteProduct( ActionBase action , ProductMeta storage ) {
		EngineStatusProduct productStatus = products.get( storage.name );
		if( productStatus == null )
			return;
		
		productStatus.stop( action );
		products.remove( storage.name );
		
		deleteGlobalSource( StatusType.PRODUCT , storage.name );
	}

	private EngineStatusProduct getProductStatus( Meta meta ) {
		return( products.get( meta.name ) );
	}
	
	private StatusSource getGlobalSource( StatusType type , String name ) {
		return( globalSources.get( type.name() + "-" + name ) );
	}

	private void startApp( ActionBase action , EngineData data ) {
		EngineRegistry registry = data.getRegistry();
		EngineDirectory directory = registry.directory;
		
		action.trace( "start status tracking for applications ..." );
		createGlobalSource( StatusType.APP , directory , "app" , new AppStatus() );
		
		for( String systemName : directory.getSystemNames() ) {
			AppSystem system = directory.findSystem( systemName );
			startSystem( action , data , system );
		}
	}
	
	private void startSystem( ActionBase action , EngineData data , AppSystem system ) {
		action.trace( "start status tracking for system=" + system.NAME + " ..." );
		createGlobalSource( StatusType.SYSTEM , system , system.NAME , new SystemStatus( system ) );
		
		// start products
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			ProductMeta storage = data.findProductStorage( product.NAME );
			startProduct( action , product , storage );
		}
	}
	
	private void startProduct( ActionBase action , Product product , ProductMeta storage ) {
		if( storage == null || storage.loadFailed ) {
			action.trace( "ignore status for non-healthy product=" + product.NAME );
			return;
		}
		
		// start product
		action.trace( "start status tracking for product=" + product.NAME + " ..." );
		createGlobalSource( StatusType.PRODUCT , product , product.NAME , new ProductStatus( product ) );
		
		Meta meta = storage.meta;
		EngineStatusProduct productStatus = new EngineStatusProduct( this , product , meta );
		products.put( meta.name , productStatus );
		
		productStatus.start( action );
	}

	private StatusSource createGlobalSource( StatusType type , EngineObject object , String name , Status status ) {
		String sourceName = type.name() + "-" + name;
		StatusSource source = new StatusSource( events , object , type , sourceName , status );
		globalSources.put( sourceName , source );
		return( source );
	}
	
	private void deleteGlobalSource( StatusType type , String name ) {
		String sourceName = type.name() + "-" + name;
		StatusSource source = globalSources.get( sourceName );
		if( source == null )
			return;
		
		globalSources.remove( sourceName );
		source.unsubscribeAll();
	}
	
	private void recalculateSystem( AppSystem system ) {
		StatusSource systemSource = getObjectSource( system );
		if( systemSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( String productName : system.getProductNames() ) {
			Product product = system.findProduct( productName );
			StatusSource productSource = getObjectSource( product );
			if( productSource != null )
				finalState = StatusData.addState( finalState , productSource.state.state );
		}
		
		if( systemSource.setFinalState( finalState ) )
			recalculateApp( system.directory );
	}

	private void recalculateApp( EngineDirectory directory ) {
		StatusSource appSource = getAppSource();
		if( appSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( String systemName : directory.getSystemNames() ) {
			AppSystem system = directory.findSystem( systemName );
			StatusSource systemSource = getObjectSource( system );
			if( systemSource != null )
				finalState = StatusData.addState( finalState , systemSource.state.state );
		}
		
		appSource.setFinalState( finalState );
	}

	public void updateRunTime( ActionBase action , MetaEnv env ) {
		EngineStatusProduct productStatus = products.get( env.meta.name );
		if( productStatus == null )
			return;

		StatusSource source = productStatus.getObjectSource( env );
		if( source != null ) {
			source.updateRunTime();
			
			Product product = productStatus.product;
			source = getGlobalSource( StatusType.PRODUCT , product.NAME );
			if( source != null ) {
				source.updateRunTime();
				source = getGlobalSource( StatusType.PRODUCT , product.system.NAME );
				if( source != null ) {
					source.updateRunTime();
					source = getAppSource();
					if( source != null )
						source.updateRunTime();
				}
			}
		}
	}

	public void updateRunTime( ActionBase action , MetaEnvSegment sg ) {
		EngineStatusProduct productStatus = products.get( sg.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( sg );
		if( source != null )
			source.updateRunTime();
	}
	
	public void updateRunTime( ActionBase action , MetaEnvServer server ) {
		EngineStatusProduct productStatus = products.get( server.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( server );
		if( source != null )
			source.updateRunTime();
	}
	
	public void updateRunTime( ActionBase action , MetaEnvServerNode node ) {
		EngineStatusProduct productStatus = products.get( node.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( node );
		if( source != null )
			source.updateRunTime();
	}
	
	public void finishUpdate( ActionBase action , MetaEnv env ) {
		EngineStatusProduct productStatus = products.get( env.meta.name );
		if( productStatus == null )
			return;

		StatusSource source = productStatus.getObjectSource( env );
		if( source != null ) {
			source.finishUpdate();
			
			Product product = productStatus.product;
			source = getGlobalSource( StatusType.PRODUCT , product.NAME );
			if( source != null ) {
				source.finishUpdate();
				source = getGlobalSource( StatusType.PRODUCT , product.system.NAME );
				if( source != null ) {
					source.finishUpdate();
					source = getAppSource();
					if( source != null )
						source.finishUpdate();
				}
			}
		}
	}

	public void finishUpdate( ActionBase action , MetaEnvSegment sg ) {
		EngineStatusProduct productStatus = products.get( sg.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( sg );
		if( source != null )
			source.finishUpdate();
	}
	
	public void finishUpdate( ActionBase action , MetaEnvServer server ) {
		EngineStatusProduct productStatus = products.get( server.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( server );
		if( source != null )
			source.finishUpdate();
	}
	
	public void finishUpdate( ActionBase action , MetaEnvServerNode node ) {
		EngineStatusProduct productStatus = products.get( node.meta.name );
		if( productStatus == null )
			return;
		
		StatusSource source = productStatus.getObjectSource( node );
		if( source != null )
			source.finishUpdate();
	}
	
}
