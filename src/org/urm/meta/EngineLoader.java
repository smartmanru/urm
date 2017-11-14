package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.DBVersions;
import org.urm.db.core.DBCoreData;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
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

public class EngineLoader {

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

	public int CV;
	
	public EngineLoader( Engine engine ) {
		this.engine = engine;
		this.execrc = engine.execrc;
		
		db = new EngineDB( this );
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
		init( false , true );
	}
	
	private void init( boolean savedb , boolean withSystems ) throws Exception {
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			CV = DBVersions.getCurrentCoreVersion( connection );
			
			if( savedb ) {
				CV = CV + 1;
				DBVersions.setNextCoreVersion( connection , CV );
			}
			
			loadServerSettings( connection , savedb );
			loadRegistry( connection , savedb , withSystems );
			loadBase();
			loadInfrastructure();
			loadReleaseLifecycles();
			loadMonitoring();
			
			if( savedb ) {
				connection.close( true );
				engine.trace( "successfully saved server metadata version=" + CV );
			}
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			if( savedb ) {
				connection.close( false );
				engine.trace( "unable to save server metadata version=" + CV );
			}
			Common.exitUnexpected();
		}
	}
	
	public LocalFolder getServerHomeFolder( ActionInit action ) throws Exception {
		LocalFolder folder = action.getLocalFolder( execrc.installPath );
		return( folder );
	}

	public LocalFolder getServerSettingsFolder( ActionInit action ) throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		LocalFolder folder = action.getLocalFolder( path );
		return( folder );
	}

	public LocalFolder getProductHomeFolder( ActionInit action , String productName ) throws Exception {
		return( products.getProductHomeFolder( action , productName ) );
	}

	private String getBaseFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "base.xml" );
		return( propertyFile );
	}

	private void loadBase() throws Exception {
		String baseFile = getBaseFile();
		base.load( baseFile , execrc );
	}

	private String getInfrastructureFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "networks.xml" );
		return( propertyFile );
	}

	private String getReleaseLifecyclesFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "lifecycles.xml" );
		return( propertyFile );
	}

	private void loadInfrastructure() throws Exception {
		String infraFile = getInfrastructureFile();
		infra.load( infraFile , execrc );
	}

	private void loadReleaseLifecycles() throws Exception {
		String lcFile = getReleaseLifecyclesFile();
		lifecycles.load( lcFile , execrc );
	}

	private String getMonitoringFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private void loadMonitoring() throws Exception {
		String monFile = getMonitoringFile();
		mon.load( monFile , execrc );
	}

	private String getServerRegistryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	private void loadRegistry( DBConnection c , boolean savedb , boolean withSystems ) throws Exception {
		String registryFile = getServerRegistryFile();
		registry.load( registryFile , c , savedb , withSystems );
	}

	private String getServerSettingsFile() {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	public void loadServerSettings( DBConnection c , boolean savedb ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settings.load( propertyFile , c , savedb );
	}

	public void loadProducts( ActionBase action ) throws Exception {
		products.loadProducts( action );
	}

	public void clearProducts() {
		products.clearProducts();
	}
	
	public boolean isProductBroken( String productName ) {
		return( products.isProductBroken( productName ) );
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
	
	public void saveRegistry( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		registry.save( transaction.getAction() , propertyFile , execrc );
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

	public void saveBase( TransactionBase transaction ) throws Exception {
		String propertyFile = getBaseFile();
		base.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveInfrastructure( TransactionBase transaction ) throws Exception {
		String propertyFile = getInfrastructureFile();
		infra.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveReleaseLifecycles( TransactionBase transaction ) throws Exception {
		String propertyFile = getReleaseLifecyclesFile();
		lifecycles.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveMonitoring( TransactionBase transaction ) throws Exception {
		String propertyFile = getMonitoringFile();
		mon.save( transaction.getAction() , propertyFile , execrc );
	}

	public EngineSettings getServerSettings() {
		synchronized( engine ) {
			return( settings );
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

	public EngineDB getDatabase() {
		synchronized( engine ) {
			return( db );
		}
	}
	
	public void setServerSettings( TransactionBase transaction , EngineSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , execrc );
		settings = settingsNew;
	}

	public void rereadEngineMirror( boolean includingSystems ) throws Exception {
		reloadCore( includingSystems );
	}

	public void rereadProductMirror( ActionBase action , String product , boolean includingEnvironments ) throws Exception {
		products.rereadProductMirror( action , product , includingEnvironments );
	}
	
	private void reloadCore( boolean includingSystems ) throws Exception {
		engine.trace( "reload server core settings ..." );
		
		db.init();
		
		clearCore( includingSystems );

		registry = new EngineRegistry( this ); 
		base = new EngineBase( this ); 
		settings = new EngineSettings( this );
		infra = new EngineInfrastructure( this ); 
		lifecycles = new EngineReleaseLifecycles( this ); 
		mon = new EngineMonitoring( this );
		
		init( true , includingSystems );
	}

	private void clearCore( boolean includingSystems ) throws Exception {
		registry.deleteObject(); 
		base.deleteObject(); 
		settings.deleteObject();
		infra.deleteObject(); 
		lifecycles.deleteObject(); 
		mon.deleteObject();
		
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			DBSystemData.dropSystemData( connection );
			DBCoreData.dropCoreData( connection );
			connection.close( true );
			engine.trace( "successfully deleted current server metadata, version=" + CV );
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			connection.close( false );
			engine.trace( "unable to delete current server metadata, version=" + CV );
			Common.exitUnexpected();
		}
	}
	
	public void setProductMetadata( TransactionBase transaction , ProductMeta storageNew ) throws Exception {
		products.setProductMetadata( transaction , storageNew );
	}
	
	public void deleteProductMetadata( TransactionBase transaction , ProductMeta storage ) throws Exception {
		products.deleteProductMetadata( transaction , storage );
	}

	public Meta createProductMetadata( TransactionBase transaction , EngineDirectory directory , Product product ) throws Exception {
		ProductMeta storage = products.createProductMetadata( transaction , directory , product );
		return( products.createSessionProductMetadata( transaction.action , storage ) );
	}

}
