package org.urm.meta;

import java.util.HashMap;
import java.util.Map;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.TransactionBase;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.Product;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;

public class EngineData {

	public Engine engine;
	public RunContext execrc;
	
	private EngineDB db;
	private EngineSettings settings;
	private EngineRegistry registry;
	private EngineBase base;
	private EngineInfrastructure infra;
	private EngineReleaseLifecycles lifecycles;
	private EngineMonitoring mon;
	private EngineProducts products;

	private Map<String,Integer> mapSystemUnmatched;
	private Map<String,Integer> mapProductUnmatched;
	private Map<String,Integer> mapEnvUnmatched;
	
	public EngineData( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
		
		db = new EngineDB( this );
		
		mapSystemUnmatched = new HashMap<String,Integer>();
		mapProductUnmatched = new HashMap<String,Integer>();
		mapEnvUnmatched = new HashMap<String,Integer>();
		
		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
		mon = new EngineMonitoring( this ); 
		products = new EngineProducts( this );
	}

	public void init() throws Exception {
		db.init();
		EngineLoader loader = new EngineLoader( engine , this );
		loader.init();
	}

	public void loadProducts( ActionBase action ) throws Exception {
		EngineLoader loader = new EngineLoader( engine , this );
		loader.loadProducts( action );
	}

	public void unloadProducts() {
		products.clearProducts();
	}
	
	public void clearCoreWithSystems() throws Exception {
		settings.deleteObject();
		registry.deleteObject(); 
		base.deleteObject(); 
		infra.deleteObject(); 
		lifecycles.deleteObject(); 
		mon.deleteObject();
		
		settings = new EngineSettings( this );
		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
		mon = new EngineMonitoring( this ); 
	}
	
	public EngineDB getDatabase() {
		synchronized( engine ) {
			return( db );
		}
	}
	
	public EngineSettings getServerSettings() {
		synchronized( engine ) {
			return( settings );
		}
	}

	public EngineResources getResources() {
		synchronized( engine ) {
			return( registry.resources );
		}
	}
	
	public EngineRegistry getRegistry() {
		synchronized( engine ) {
			return( registry );
		}
	}

	public EngineInfrastructure getInfrastructure() {
		synchronized( engine ) {
			return( infra );
		}
	}

	public EngineReleaseLifecycles getReleaseLifecycles() {
		synchronized( engine ) {
			return( lifecycles );
		}
	}

	public EngineMonitoring getMonitoring() {
		synchronized( engine ) {
			return( mon );
		}
	}

	public EngineBase getServerBase() {
		synchronized( engine ) {
			return( base );
		}
	}

	public EngineProducts getProducts() {
		synchronized( engine ) {
			return( products );
		}
	}
	
	public void setResources( TransactionBase transaction , EngineResources resourcesNew ) throws Exception {
		registry.setResources( transaction , resourcesNew );
	}

	public void setBuilders( TransactionBase transaction , EngineBuilders buildersNew ) throws Exception {
		registry.setBuilders( transaction , buildersNew );
	}

	public void setDirectory( TransactionBase transaction , EngineDirectory directoryNew ) throws Exception {
		registry.setDirectory( transaction , directoryNew );
	}

	public void setMirrors( TransactionBase transaction , EngineMirrors mirrorsNew ) throws Exception {
		registry.setMirrors( transaction , mirrorsNew );
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
	
	public void setSystemUnmatched( AppSystem system ) throws Exception {
		mapSystemUnmatched.put( system.NAME , system.ID );
	}
	
	public void setProductUnmatched( Product product ) throws Exception {
		mapProductUnmatched.put( product.NAME , product.ID );
	}
	
	public void setEnvUnmatched( MetaEnv env ) throws Exception {
		mapEnvUnmatched.put( env.meta.name + "::" + env.ID , 0 );
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
	
}
