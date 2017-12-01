package org.urm.meta;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.EngineEntities;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.Product;
import org.urm.meta.product.Meta;

public class EngineData {

	public Engine engine;
	public RunContext execrc;
	
	private EngineDB db;
	private EngineCore core; 
	private EngineDirectory directory;
	private EngineMonitoring mon;
	private EngineProducts products;

	private Map<String,UnmatchedSystem> mapSystemUnmatched;
	private Map<String,Integer> mapProductUnmatched;
	private Map<String,Integer> mapEnvUnmatched;
	
	public EngineData( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
		
		db = new EngineDB( this );
		
		mapSystemUnmatched = new HashMap<String,UnmatchedSystem>();
		mapProductUnmatched = new HashMap<String,Integer>();
		mapEnvUnmatched = new HashMap<String,Integer>();

		core = new EngineCore( this );
		directory = new EngineDirectory( this );
		products = new EngineProducts( this );
		mon = new EngineMonitoring( this ); 
	}

	public void init() throws Exception {
		db.init();
	}

	public void unloadProducts() {
		mapProductUnmatched.clear();
		mapEnvUnmatched.clear();
		products.unloadProducts();
	}
	
	public void matchdoneSystem( EngineLoader loader , AppSystem system ) {
		if( system.MATCHED )
			mapSystemUnmatched.remove( system.NAME );
		else {
			UnmatchedSystem unmatched = new UnmatchedSystem( system );
			mapSystemUnmatched.put( system.NAME , unmatched );
			directory.unloadSystem( system );
		}
	}
	
	public void unloadDirectory() {
		unloadProducts();
		mapSystemUnmatched.clear();
		directory.unloadAll();
	}
	
	public void unloadAll() throws Exception {
		unloadDirectory();
		
		core.recreateAll();
		mon.deleteObject();
		
		mon = new EngineMonitoring( this ); 
	}
	
	public EngineCore getCore() {
		return( core );
	}
	
	public EngineDB getDatabase() {
		synchronized( engine ) {
			return( db );
		}
	}
	
	public EngineSettings getEngineSettings() {
		synchronized( engine ) {
			return( core.getSettings() );
		}
	}

	public EngineResources getResources() {
		synchronized( engine ) {
			EngineRegistry registry = core.getRegistry();
			return( registry.resources );
		}
	}
	
	public EngineBuilders getBuilders() {
		synchronized( engine ) {
			EngineRegistry registry = core.getRegistry();
			return( registry.builders );
		}
	}
	
	public EngineRegistry getRegistry() {
		synchronized( engine ) {
			EngineRegistry registry = core.getRegistry();
			return( registry );
		}
	}

	public EngineInfrastructure getInfrastructure() {
		synchronized( engine ) {
			return( core.getInfrastructure() );
		}
	}

	public EngineLifecycles getReleaseLifecycles() {
		synchronized( engine ) {
			return( core.getLifecycles() );
		}
	}

	public EngineMonitoring getMonitoring() {
		synchronized( engine ) {
			return( mon );
		}
	}

	public EngineEntities getEntities() {
		synchronized( engine ) {
			return( core.getEntities() );
		}
	}
	
	public EngineBase getEngineBase() {
		synchronized( engine ) {
			return( core.getBase() );
		}
	}

	public EngineDirectory getDirectory() {
		synchronized( engine ) {
			return( directory );
		}
	}
	
	public EngineMirrors getMirrors() {
		synchronized( engine ) {
			EngineRegistry registry = core.getRegistry();
			return( registry.mirrors );
		}
	}
	
	public EngineProducts getProducts() {
		synchronized( engine ) {
			return( products );
		}
	}
	
	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		EngineRegistry registry = core.getRegistry();
		registry.setResources( transaction , resourcesNew );
	}

	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		EngineRegistry registry = core.getRegistry();
		registry.setBuilders( transaction , buildersNew );
	}

	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		EngineRegistry registry = core.getRegistry();
		registry.setMirrors( transaction , mirrorsNew );
	}

	public void setDirectory( TransactionBase transaction , EngineDirectory directoryNew ) throws Exception {
		directory = directoryNew;
	}

	public void saveProductMetadata( EngineLoader loader , String productName ) throws Exception {
		products.saveProductMetadata( loader , productName );
	}
	
	public void setProductMetadata( TransactionBase transaction , ProductMeta storageNew ) throws Exception {
		products.setProductMetadata( transaction , storageNew );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		products.deleteProductMetadata( transaction , storage );
	}

	public Meta createProductMetadata( TransactionBase transaction , Product product ) throws Exception {
		ProductMeta storage = products.createProductMetadata( transaction , product );
		return( products.createSessionProductMetadata( transaction.action , storage ) );
	}

	public Meta findSessionProductMetadata( ActionBase action , String productName ) throws Exception {
		return( products.findSessionProductMetadata( action , productName ) );
	}
	
	public ProductMeta findProductStorage( String productName ) {
		return( products.findProductStorage( productName ) );
	}
	
	public Meta getSessionProductMetadata( ActionBase action , String productName , boolean primary ) throws Exception {
		return( products.getSessionProductMetadata( action , productName , primary ) );
	}

	public void releaseSessionProductMetadata( ActionBase action , Meta meta , boolean deleteMeta ) throws Exception {
		products.releaseSessionProductMetadata( action , meta , deleteMeta );
	}
	
	public void checkSystemNameBusy( String name ) throws Exception {
		if( mapSystemUnmatched.containsKey( name ) )
			Common.exit1( _Error.DuplicateSystemNameUnmatched1 , "System with name=" + name + " + already exists, unmatched" , name );
	}
	
	public void checkProductNameBusy( String name ) throws Exception {
		if( mapProductUnmatched.containsKey( name ) )
			Common.exit1( _Error.DuplicateProductNameUnmatched1 , "Product with name=" + name + " + already exists, unmatched" , name );
	}
	
	public void checkEnvNameBusy( String product , String name ) throws Exception {
		if( mapEnvUnmatched.containsKey( product + "::" + name ) )
			Common.exit2( _Error.DuplicateEnvNameUnmatched2 , "Environment with name=" + name + " + already exists in product=" + product + ", unmatched" , product , name );
	}

	public UnmatchedSystem[] getSystemsUnmatched() {
		List<UnmatchedSystem> list = new LinkedList<UnmatchedSystem>();
		for( String name : Common.getSortedKeys( mapSystemUnmatched ) ) {
			UnmatchedSystem system = mapSystemUnmatched.get( name );
			list.add( system );
		}
		
		return( list.toArray( new UnmatchedSystem[0] ) );
	}

}
