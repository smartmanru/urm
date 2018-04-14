package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.RunContext;
import org.urm.db.EngineDB;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineCore;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class DataService {

	public Engine engine;
	public RunContext execrc;
	
	private EngineDB db;
	private EngineProducts products;

	private EngineCore core; 
	private EngineDirectory directory;
	private EngineMonitoring monitoring;
	
	public DataService( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
		
		db = new EngineDB( engine );
		core = new EngineCore( engine );
		products = new EngineProducts( engine , this ); 
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
			return( core.getResources() );
		}
	}
	
	public EngineBuilders getBuilders() {
		synchronized( engine ) {
			return( core.getBuilders() );
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
			return( core.getMirrors() );
		}
	}
	
	public EngineProducts getProducts() {
		synchronized( engine ) {
			return( products );
		}
	}

	public void updateEntity( PropertyEntity entity ) {
		core.updateEntity( entity );
	}
	
	public void setSettings( EngineSettings settingsNew ) {
		EngineSettings settingsOld = core.getSettings();
		core.setSettings( settingsNew );
		
		if( settingsOld != null ) {
			ObjectProperties opsOld = settingsOld.getEngineProperties();
			ObjectProperties opsNew = settingsNew.getEngineProperties();
			opsOld.replaceChildsParent( opsNew );
		}
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
		core.setResources( resourcesNew );
	}

	public void setBuilders( EngineBuilders buildersNew ) {
		core.setBuilders( buildersNew );
	}

	public void setMirrors( EngineMirrors mirrorsNew ) {
		core.setMirrors( mirrorsNew );
	}

	public void setDirectory( EngineDirectory directoryNew ) {
		EngineDirectory directoryOld = directory;
		directory = directoryNew;

		if( directoryOld != null ) {
			for( AppSystem systemOld : directoryOld.getSystems() ) {
				AppSystem systemNew = directory.findSystem( systemOld );
				if( systemNew != null ) {
					ObjectProperties opsOld = systemOld.getParameters();
					ObjectProperties opsNew = systemNew.getParameters();
					opsOld.replaceChildsParent( opsNew );
				}
			}
		}
	}

	public void setMonitoring( EngineMonitoring monitoringNew ) {
		monitoring = monitoringNew;
	}

	public void setProductMetadata( TransactionBase transaction , ProductMeta storageNew ) throws Exception {
		products.setProductMetadata( storageNew );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		products.deleteProductMetadata( transaction , storage );
	}

	public ProductMeta findProductStorage( String productName ) {
		return( products.findProductStorage( productName ) );
	}
	
	public Meta createSessionProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		return( products.createSessionProductMetadata( transaction.action , storage ) );
	}
	
	public Meta findSessionProductMetadata( ActionBase action , String productName ) {
		return( products.findSessionProductMetadata( action , productName ) );
	}
	
	public Meta getSessionProductMetadata( ActionBase action , String productName , boolean primary ) throws Exception {
		return( products.getSessionProductMetadata( action , productName , primary ) );
	}

	public Meta getSessionProductMetadata( ActionBase action , int metaId , boolean primary ) throws Exception {
		return( products.getSessionProductMetadata( action , metaId , primary ) );
	}

	public void releaseSessionProductMetadata( ActionBase action , Meta meta ) throws Exception {
		products.releaseSessionProductMetadata( action , meta );
	}
	
}
