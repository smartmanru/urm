package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.RunContext;
import org.urm.db.EngineDB;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.EngineEntities;
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
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;

public class EngineData {

	public Engine engine;
	public RunContext execrc;
	
	private EngineDB db;
	private EngineProducts products;

	private EngineCore core; 
	private EngineDirectory directory;
	private EngineMonitoring monitoring;
	
	public EngineData( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
		
		db = new EngineDB( engine );
		core = new EngineCore( engine );
		products = new EngineProducts( engine ); 
	}

	public void init() throws Exception {
		db.init();
	}

	public void unloadProducts() {
		products.unloadProducts();
	}
	
	public void unloadDirectory() {
		if( directory != null ) {
			directory.removeAll();
			directory.deleteObject();
			directory = null;
		}
		products.unloadProducts();
	}
	
	public void unloadAll() throws Exception {
		unloadDirectory();
		
		core.unloadAll();
		if( monitoring != null ) {
			monitoring.deleteObject();
			monitoring = null;
		}
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
			return( monitoring );
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
	
	public void setSettings( EngineSettings settingsNew ) {
		core.setSettings( settingsNew );
	}

	public void setBase( EngineBase baseNew ) {
		core.setBase( baseNew );
	}

	public void setInfrastructure( EngineInfrastructure infraNew ) {
		core.setInfrastructure( infraNew );
	}

	public void setLifecycles( EngineLifecycles lifecyclesNew ) {
		core.setLifecycles( lifecyclesNew );
	}

	public void setResources( EngineResources resourcesNew ) {
		EngineRegistry registry = core.getRegistry();
		registry.setResources( resourcesNew );
	}

	public void setBuilders( EngineBuilders buildersNew ) {
		EngineRegistry registry = core.getRegistry();
		registry.setBuilders( buildersNew );
	}

	public void setMirrors( EngineMirrors mirrorsNew ) {
		EngineRegistry registry = core.getRegistry();
		registry.setMirrors( mirrorsNew );
	}

	public void setDirectory( EngineDirectory directoryNew ) {
		directory = directoryNew;
	}

	public void setMonitoring( EngineMonitoring monitoringNew ) {
		monitoring = monitoringNew;
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

	public Meta createProductMetadata( TransactionBase transaction , EngineSettings settings , AppProduct product ) throws Exception {
		ProductMeta storage = products.createProductMetadata( transaction , settings , product );
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
	
}
