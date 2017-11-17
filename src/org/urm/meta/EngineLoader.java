package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.Meta;

public class EngineLoader {

	public Engine engine;
	public EngineData data;
	public RunContext execrc;
	
	private EngineMatcher matcher;

	public EngineLoader( Engine engine , EngineData data ) {
		this.engine = engine;
		this.data = data;
		this.execrc = engine.execrc;
		
		matcher = new EngineMatcher( this ); 
	}

	public void init() throws Exception {
		init( false , true );
	}
	
	private void init( boolean savedb , boolean withSystems ) throws Exception {
		EngineDB db = data.getDatabase();
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			
			if( savedb )
				connection.setNextCoreVersion();
			
			loadServerSettings( connection , savedb );
			loadBase();
			loadInfrastructure();
			loadReleaseLifecycles();
			loadMonitoring();
			
			loadRegistry( connection , savedb , withSystems );

			if( savedb ) {
				engine.trace( "successfully saved server metadata version=" + connection.getNextCoreVersion() );
				connection.close( true );
			}
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			if( savedb ) {
				engine.trace( "unable to save server metadata version=" + connection.getNextCoreVersion() );
				connection.close( false );
			}
			Common.exitUnexpected();
		}
	}
	
	public EngineMatcher getMatcher() {
		synchronized( engine ) {
			return( matcher );
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
		Meta meta = action.getActiveProductMetadata( productName );
		MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , meta );
		LocalFolder folder = storageMeta.getHomeFolder( action );
		return( folder );
	}

	private String getBaseFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "base.xml" );
		return( propertyFile );
	}

	private void loadBase() throws Exception {
		String baseFile = getBaseFile();
		EngineBase base = data.getServerBase();
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
		EngineInfrastructure infra = data.getInfrastructure();
		infra.load( infraFile , execrc );
	}

	private void loadReleaseLifecycles() throws Exception {
		String lcFile = getReleaseLifecyclesFile();
		EngineReleaseLifecycles lifecycles = data.getReleaseLifecycles();
		lifecycles.load( lcFile , execrc );
	}

	private String getMonitoringFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private void loadMonitoring() throws Exception {
		String monFile = getMonitoringFile();
		EngineMonitoring mon = data.getMonitoring();
		mon.load( monFile , execrc );
	}

	private String getServerRegistryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	private void loadRegistry( DBConnection c , boolean savedb , boolean withSystems ) throws Exception {
		String registryFile = getServerRegistryFile();
		EngineRegistry registry = data.getRegistry();
		registry.loadmixed( registryFile , c , savedb , withSystems );
	}

	private String getServerSettingsFile() {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	public void loadServerSettings( DBConnection c , boolean savedb ) throws Exception {
		String propertyFile = getServerSettingsFile();
		EngineSettings settings = data.getServerSettings();
		settings.load( propertyFile , c , savedb );
	}

	public void loadProducts( ActionBase action ) throws Exception {
		EngineProducts products = data.getProducts();
		products.loadProducts( action );
	}

	public boolean isProductBroken( String productName ) {
		EngineProducts products = data.getProducts();
		return( products.isProductBroken( productName ) );
	}

	public void saveRegistry( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		EngineRegistry registry = data.getRegistry();
		registry.savexml( transaction.getAction() , propertyFile , execrc );
	}
	
	public void saveBase( TransactionBase transaction ) throws Exception {
		String propertyFile = getBaseFile();
		EngineBase base = data.getServerBase();
		base.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveInfrastructure( TransactionBase transaction ) throws Exception {
		String propertyFile = getInfrastructureFile();
		EngineInfrastructure infra = data.getInfrastructure();
		infra.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveReleaseLifecycles( TransactionBase transaction ) throws Exception {
		String propertyFile = getReleaseLifecyclesFile();
		EngineReleaseLifecycles lifecycles = data.getReleaseLifecycles();
		lifecycles.save( transaction.getAction() , propertyFile , execrc );
	}

	public void saveMonitoring( TransactionBase transaction ) throws Exception {
		String propertyFile = getMonitoringFile();
		EngineMonitoring mon = data.getMonitoring();
		mon.save( transaction.getAction() , propertyFile , execrc );
	}

	public void setServerSettings( TransactionBase transaction , EngineSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		settingsNew.save( propertyFile , execrc );
		EngineSettings settings = data.getServerSettings();
		settings.setData( transaction.getAction() , settingsNew );
	}

	public void rereadEngineMirror( boolean includingSystems ) throws Exception {
		reloadCore( includingSystems );
	}

	public void rereadProductMirror( ActionBase action , String product , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		products.rereadProductMirror( action , product , includingEnvironments );
	}
	
	private void reloadCore( boolean includingSystems ) throws Exception {
		engine.trace( "reload server core settings ..." );
		
		if( includingSystems )
			data.clearCoreWithSystems();
		else
			Common.exitUnexpected();

		dropCoreData( includingSystems );
		init( true , includingSystems );
	}

	private void dropCoreData( boolean includingSystems ) throws Exception {
		EngineDB db = data.getDatabase();
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			if( includingSystems )
				DBSystemData.dropSystemData( connection );
			DBCoreData.dropCoreData( connection );
			int CV = connection.getCurrentCoreVersion();
			connection.close( true );
			engine.trace( "successfully deleted current server metadata, version=" + CV );
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			int CV = connection.getCurrentCoreVersion();
			connection.close( false );
			engine.trace( "unable to delete current server metadata, version=" + CV );
			Common.exitUnexpected();
		}
	}
	
}
