package org.urm.engine.status;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.engine.TransactionBase;
import org.urm.engine.events.EngineEvents;
import org.urm.engine.status.EngineStatus.StatusType;
import org.urm.engine.status.StatusData.OBJECT_STATE;
import org.urm.meta.EngineObject;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineStatusProduct extends EngineObject {

	EngineStatus engineStatus;
	EngineEvents events;
	AppProduct product;
	Meta meta;
	
	private Map<EngineObject,StatusSource> productSources;
	
	public EngineStatusProduct( EngineStatus engineStatus , AppProduct product , Meta meta ) {
		super( engineStatus );
		this.product = product;
		this.meta = meta;
		this.engineStatus = engineStatus;
		this.events = engineStatus.events;
		productSources = new HashMap<EngineObject,StatusSource>();
	}

	@Override
	public String getName() {
		return( "engine-status-" + meta.name );
	}

	public void start( ActionBase action ) {
		ProductEnvs envs = meta.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findMetaEnv( envName );
			startEnvironment( action , env );
		}
	}
	
	public void stop( ActionBase action ) {
		cleanProductSources();
	}
	
	public StatusSource getObjectSource( MetaEnv env ) {
		return( getProductSource( env ) );
	}

	public StatusSource getObjectSource( MetaEnvSegment sg ) {
		return( getProductSource( sg ) );
	}

	public StatusSource getObjectSource( MetaEnvServer server ) {
		return( getProductSource( server ) );
	}

	public StatusSource getObjectSource( MetaEnvServerNode node ) {
		return( getProductSource( node ) );
	}

	public void setSegmentStatus( ActionBase action , MetaEnvSegment sg , SegmentStatus status ) {
		StatusSource sgSource = getObjectSource( sg );
		if( sgSource != null )
			processSegment( action , sgSource , sg , status );
	}

	public void setSegmentItemsStatus( ActionBase action , MetaEnvSegment sg , SegmentStatus status ) {
		StatusSource sgSource = getObjectSource( sg );
		if( sgSource != null )
			processSegmentItems( action , sgSource , sg , status );
	}
	
	public void setServerStatus( ActionBase action , MetaEnvServer server , ServerStatus status ) {
		StatusSource serverSource = getObjectSource( server );
		if( serverSource != null )
			processServer( action , serverSource , server , status );
	}
	
	public void setServerItemsStatus( ActionBase action , MetaEnvServer server , ServerStatus status ) {
		StatusSource serverSource = getObjectSource( server );
		if( serverSource != null )
			processServerItems( action , serverSource , server , status );
	}
	
	public void setServerNodeStatus( ActionBase action , MetaEnvServerNode node , NodeStatus status ) {
		StatusSource nodeSource = getObjectSource( node );
		if( nodeSource != null )
			processServerNode( action , nodeSource , node , status );
	}
	
	public void setServerNodeItemsStatus( ActionBase action , MetaEnvServerNode node , NodeStatus status ) {
		StatusSource nodeSource = getObjectSource( node );
		if( nodeSource != null )
			processServerNodeItems( action , nodeSource , node , status );
	}
	
	public void modifyProduct( TransactionBase transaction , ProductMeta storageOld , ProductMeta storage ) throws Exception {
		ActionBase action = transaction.getAction();
		product = transaction.getProduct( product );
		meta = transaction.getMeta( product );
		
		ProductEnvs envs = meta.getEnviroments();
		ProductEnvs envsOld = storageOld.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv envNew = envs.findMetaEnv( envName );
			MetaEnv envOld = envsOld.findMetaEnv( envName );
			if( envOld != null )
				modifyEnvironment( action , envOld , envNew );
			else
				startEnvironment( action , envNew );
		}
		
		for( String envName : envsOld.getEnvNames() ) {
			MetaEnv envOld = envsOld.findMetaEnv( envName );
			MetaEnv envNew = envs.findMetaEnv( envName );
			if( envNew == null )
				stopEnvironment( action , envOld , true );
		}
	}

	private void stopEnvironment( ActionBase action , MetaEnv env , boolean delete ) {
		// stop childs
		for( MetaEnvSegment sg : env.getSegments() )
			stopSegment( action , sg , delete );
		
		if( delete )
			removeProductSource( env );
	}

	public void stopSegment( ActionBase action , MetaEnvSegment sg , boolean delete ) {
		// stop childs
		for( MetaEnvServer server : sg.getServers() )
			stopServer( action , server , delete );
		
		if( delete )
			removeProductSource( sg );
	}
	
	public void stopServer( ActionBase action , MetaEnvServer server , boolean delete ) {
		// stop childs
		for( MetaEnvServerNode node : server.getNodes() )
			stopNode( action , node , delete );
		
		if( delete )
			removeProductSource( server );
	}
	
	public void stopNode( ActionBase action , MetaEnvServerNode node , boolean delete ) {
		if( delete )
			removeProductSource( node );
	}

	public StatusSource getProductSource( EngineObject object ) {
		return( productSources.get( object ) );
	}

	private void modifyEnvironment( ActionBase action , MetaEnv envOld , MetaEnv envNew ) {
		replaceSource( StatusType.ENVIRONMENT , envOld , envNew );
		
		for( MetaEnvSegment sgNew : envNew.getSegments() ) {
			MetaEnvSegment sgOld = envOld.findSegment( sgNew.NAME );
			if( sgOld != null )
				modifySegment( action , sgOld , sgNew );
			else
				startSegment( action , sgNew );
		}
		for( MetaEnvSegment sgOld : envOld.getSegments() ) {
			MetaEnvSegment sgNew = envNew.findSegment( sgOld.NAME );
			if( sgNew == null )
				stopSegment( action , sgOld , true );
		}
	}
	
	private void modifySegment( ActionBase action , MetaEnvSegment sgOld , MetaEnvSegment sgNew ) {
		replaceSource( StatusType.SEGMENT , sgOld , sgNew );
		
		for( MetaEnvServer serverNew : sgNew.getServers() ) {
			MetaEnvServer serverOld = sgOld.findServer( serverNew.NAME );
			if( serverOld != null )
				modifyServer( action , serverOld , serverNew );
			else
				startServer( action , serverNew );
		}
		for( MetaEnvServer serverOld : sgOld.getServers() ) {
			MetaEnvServer serverNew = sgNew.findServer( serverOld.NAME );
			if( serverNew == null )
				stopServer( action , serverOld , true );
		}
	}
	
	private void modifyServer( ActionBase action , MetaEnvServer serverOld , MetaEnvServer serverNew ) {
		replaceSource( StatusType.SEGMENT , serverOld , serverNew );
		
		for( MetaEnvServerNode nodeNew : serverNew.getNodes() ) {
			MetaEnvServerNode nodeOld = serverOld.findNode( nodeNew.POS );
			if( nodeOld != null )
				replaceSource( StatusType.NODE , nodeOld , nodeNew );
			else
				startNode( action , nodeNew );
		}
		for( MetaEnvServerNode nodeOld : serverOld.getNodes() ) {
			MetaEnvServerNode nodeNew = serverNew.findNode( nodeOld.POS );
			if( nodeNew == null )
				stopNode( action , nodeOld , true );
		}
	}

	private void replaceSource( StatusType type , EngineObject objectOld , EngineObject objectNew ) {
		StatusSource source = productSources.get( objectOld );
		if( source == null )
			return;
		
		productSources.remove( objectOld );
		source.setObject( objectNew );
		productSources.put( objectNew , source );
	}

	private void startEnvironment( ActionBase action , MetaEnv env ) {
		startEnvironmentSource( action , env );
		
		// start childs
		for( MetaEnvSegment sg : env.getSegments() )
			startSegment( action , sg );
	}

	private void startEnvironmentSource( ActionBase action , MetaEnv env ) {
		createProductSource( StatusType.ENVIRONMENT , env , env.NAME , new EnvStatus( env ) );
	}
	
	private void startSegment( ActionBase action , MetaEnvSegment sg ) {
		startSegmentSource( action , sg );
		
		// start childs
		for( MetaEnvServer server : sg.getServers() )
			startServer( action , server );
	}

	private void startSegmentSource( ActionBase action , MetaEnvSegment sg ) {
		createProductSource( StatusType.SEGMENT , sg , sg.env.NAME + "-" + sg.NAME , new SegmentStatus( sg ) );
	}
	
	private void startServer( ActionBase action , MetaEnvServer server ) {
		startServerSource( action , server );
		
		// start childs
		for( MetaEnvServerNode node : server.getNodes() )
			startNode( action , node );
	}

	private void startServerSource( ActionBase action , MetaEnvServer server ) {
		createProductSource( StatusType.SERVER , server , server.sg.env.NAME + "-" + server.sg.NAME + "-" + server.NAME , new ServerStatus( server ) );
	}
	
	private void startNode( ActionBase action , MetaEnvServerNode node ) {
		startNodeSource( action , node );
	}
	
	private void startNodeSource( ActionBase action , MetaEnvServerNode node ) {
		createProductSource( StatusType.NODE , node , node.server.sg.env.NAME + "-" + node.server.sg.NAME + "-" + node.server.NAME + "-" + node.POS , new NodeStatus( node ) );
	}
	
	private StatusSource createProductSource( StatusType type , EngineObject object , String name , Status status ) {
		StatusSource source = new StatusSource( events , object , type , name , status );
		productSources.put( object , source );
		return( source );
	}

	private void cleanProductSources() {
		for( StatusSource source : productSources.values() )
			source.unsubscribeAll();
		productSources.clear();
	}
	
	private void removeProductSource( EngineObject object ) {
		StatusSource source = productSources.get( object );
		if( source == null )
			return;
		
		productSources.remove( object );
		source.unsubscribeAll();
	}

	private void processSegment( ActionBase action , StatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		sgSource.setPrimaryLog( status.getLog() );
		sgSource.finishUpdate();
	}
	
	private void processSegmentItems( ActionBase action , StatusSource sgSource , MetaEnvSegment sg , SegmentStatus status ) {
		sgSource.setExtraLog( EngineStatus.EXTRA_SEGMENT_ITEMS , status.getLog() );
		if( sgSource.setExtraState( EngineStatus.EXTRA_SEGMENT_ITEMS , status.itemState , status ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( action , env );
		}
	}
	
	private void processServer( ActionBase action , StatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		serverSource.setPrimaryLog( status.getLog() );
		if( serverSource.setState( status.itemState , status ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( action , sg );
		}
	}
	
	private void processServerItems( ActionBase action , StatusSource serverSource , MetaEnvServer server , ServerStatus status ) {
		serverSource.setExtraLog( EngineStatus.EXTRA_SERVER_ITEMS , status.getLog() );
		if( serverSource.setExtraState( EngineStatus.EXTRA_SERVER_ITEMS , status.itemState , status ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( action , sg );
		}
	}
	
	private void processServerNode( ActionBase action , StatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		nodeSource.setPrimaryLog( status.getLog() );
		if( nodeSource.setState( status.itemState , status ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( action , server );
		}
	}
	
	private void processServerNodeItems( ActionBase action , StatusSource nodeSource , MetaEnvServerNode node , NodeStatus status ) {
		nodeSource.setExtraLog( EngineStatus.EXTRA_NODE_ITEMS , status.getLog() );
		if( nodeSource.setExtraState( EngineStatus.EXTRA_NODE_ITEMS , status.itemState , status ) ) {
			MetaEnvServer server = node.server;
			recalculateServer( action , server );
		}
	}
	
	private void recalculateServer( ActionBase action , MetaEnvServer server ) {
		StatusSource serverSource = getObjectSource( server );
		if( serverSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvServerNode node : server.getNodes() ) {
			StatusSource nodeSource = getObjectSource( node );
			if( nodeSource != null )
				finalState = StatusData.addState( finalState , nodeSource.state.state );
		}
		
		if( serverSource.setFinalState( finalState ) ) {
			MetaEnvSegment sg = server.sg;
			recalculateSegment( action , sg );
		}
	}
	
	private void recalculateSegment( ActionBase action , MetaEnvSegment sg ) {
		StatusSource sgSource = getObjectSource( sg );
		if( sgSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvServer server : sg.getServers() ) {
			StatusSource serverSource = getObjectSource( server );
			if( serverSource != null )
				finalState = StatusData.addState( finalState , serverSource.state.state );
		}
		
		if( sgSource.setFinalState( finalState ) ) {
			MetaEnv env = sg.env;
			recalculateEnv( action , env );
		}
	}
	
	private void recalculateEnv( ActionBase action , MetaEnv env ) {
		StatusSource envSource = getObjectSource( env );
		if( envSource == null )
			return;

		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		for( MetaEnvSegment sg : env.getSegments() ) {
			StatusSource sgSource = getObjectSource( sg );
			if( sgSource != null )
				finalState = StatusData.addState( finalState , sgSource.state.state );
		}
		
		if( envSource.setFinalState( finalState ) )
			recalculateProduct( action );
	}

	private void recalculateProduct( ActionBase action ) {
		OBJECT_STATE finalState = OBJECT_STATE.STATE_NODATA;
		ProductEnvs envs = meta.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findMetaEnv( envName );
			StatusSource envSource = getObjectSource( env );
			if( envSource != null )
				finalState = StatusData.addState( finalState , envSource.state.state );
		}

		engineStatus.setProductStatus( action , product , finalState );
	}
	
}
