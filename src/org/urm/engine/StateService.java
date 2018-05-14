package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.events.EngineEventsApp;
import org.urm.engine.events.EngineEventsListener;
import org.urm.engine.events.EngineEventsSubscription;
import org.urm.engine.status.AppStatus;
import org.urm.engine.status.EngineStatusProduct;
import org.urm.engine.status.NodeStatus;
import org.urm.engine.status.ProductStatus;
import org.urm.engine.status.SegmentStatus;
import org.urm.engine.status.ServerStatus;
import org.urm.engine.status.Status;
import org.urm.engine.status.StatusData;
import org.urm.engine.status.StatusSource;
import org.urm.engine.status.SystemStatus;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppSystem;

public class StateService extends EngineObject {

	Engine engine;
	public EventService events;

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
	
	public StateService( Engine engine ) {
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
	
	public synchronized void start( ActionBase action , DataService data ) {
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
		
		if( object instanceof AppProduct ) {
			AppProduct product = ( AppProduct )object; 
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
	
	public synchronized StatusSource getObjectSource( AppProduct product ) {
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

	public synchronized void setProductStatus( ActionBase action , AppProduct product , OBJECT_STATE state ) {
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

	public synchronized void createProduct( TransactionBase transaction , AppProduct product ) {
		startProduct( transaction.getAction() , product );
	}

	public void modifyProduct( TransactionBase transaction , ProductMeta storageOld , ProductMeta storageNew ) throws Exception {
		EngineStatusProduct productStatus = products.get( storageOld.NAME );
		if( productStatus == null )
			return;
		
		productStatus.modifyProduct( transaction , storageOld , storageNew );
	}
	
	public synchronized void deleteProduct( TransactionBase transaction , AppProduct product ) {
		EngineStatusProduct productStatus = products.get( product.NAME );
		if( productStatus == null )
			return;
		
		productStatus.stop( transaction.getAction() );
		products.remove( product.NAME );
		
		deleteGlobalSource( StatusType.PRODUCT , product.NAME );
	}

	private EngineStatusProduct getProductStatus( Meta meta ) {
		return( products.get( meta.name ) );
	}
	
	private StatusSource getGlobalSource( StatusType type , String name ) {
		return( globalSources.get( type.name() + "-" + name ) );
	}

	private void startApp( ActionBase action , DataService data ) {
		EngineDirectory directory = data.getDirectory();
		
		action.trace( "start status tracking for applications ..." );
		createGlobalSource( StatusType.APP , directory , "app" , new AppStatus() );
		
		for( String systemName : directory.getSystemNames() ) {
			AppSystem system = directory.findSystem( systemName );
			startSystem( action , data , system );
		}
	}
	
	private void startSystem( ActionBase action , DataService data , AppSystem system ) {
		action.trace( "start status tracking for system=" + system.NAME + " ..." );
		createGlobalSource( StatusType.SYSTEM , system , system.NAME , new SystemStatus( system ) );
		
		// start products
		for( String productName : system.getProductNames() ) {
			AppProduct product = system.findProduct( productName );
			startProduct( action , product );
		}
	}
	
	private void startProduct( ActionBase action , AppProduct product ) {
		if( product.isOffline() ) {
			action.trace( "ignore status for offline product=" + product.NAME );
			return;
		}
		
		// start product
		action.trace( "start status tracking for product=" + product.NAME + " ..." );
		createGlobalSource( StatusType.PRODUCT , product , product.NAME , new ProductStatus( product ) );
		
		EngineStatusProduct productStatus = new EngineStatusProduct( this , product );
		products.put( product.NAME , productStatus );
		
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
			AppProduct product = system.findProduct( productName );
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
			
			AppProduct product = productStatus.product;
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
			
			AppProduct product = productStatus.product;
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
