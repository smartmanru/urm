package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.TransactionBase;
import org.urm.engine.action.ActionInit;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine._Error;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoader {

	public Engine engine;
	public EngineData data;
	public RunContext execrc;
	
	private EngineMatcher matcher;

	public EngineLoader( Engine engine , EngineData data ) {
		this.engine = engine;
		this.data = data;
		this.execrc = engine.execrc;
	}

	public void loadCore() throws Exception {
		loadCore( false , true );
	}
	
	private void loadCore( boolean savedb , boolean withSystems ) throws Exception {
		data.unloadAll();
		
		EngineDB db = data.getDatabase();
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			matcher = new EngineMatcher( this , connection ); 
			
			if( savedb )
				connection.setNextCoreVersion();
			
			loadSettings( connection , savedb );
			loadBase();
			loadInfrastructure();
			loadReleaseLifecycles();
			loadMonitoring( connection , savedb );
			loadRegistry();
			connection.save( true );

			loadDirectory( connection , savedb , withSystems );
			connection.save( true );
			
			if( savedb ) {
				engine.trace( "successfully saved server metadata version=" + connection.getNextCoreVersion() );
				connection.close( true );
			}
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			if( connection != null && savedb ) {
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

	private void loadMonitoring( DBConnection c , boolean importxml ) throws Exception {
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		EngineMonitoring mon = data.getMonitoring();
		if( importxml )
			mon.loadxml( root , c );
		else
			mon.loaddb( c );
	}

	private String getServerRegistryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	private void loadRegistry() throws Exception {
		String registryFile = getServerRegistryFile();
		EngineRegistry registry = data.getRegistry();
		
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		registry.loadxml( matcher , root );
	}

	private void loadDirectory( DBConnection c , boolean importxml , boolean withSystems ) throws Exception {
		String registryFile = getServerRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		EngineDirectory directory = data.getDirectory();
		if( importxml && withSystems ) {
			Node node = ConfReader.xmlGetFirstChild( root , "directory" );
			directory.loadxml( matcher , node , c );
		}
		else
			directory.loaddb( matcher , c );
		
		for( AppSystem system : directory.getSystems() )
			data.matchSystem( system );
	}
	
	private String getServerSettingsFile() {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	private void loadSettings( DBConnection c , boolean importxml ) throws Exception {
		String propertyFile = getServerSettingsFile();
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + propertyFile , propertyFile );
		
		Node root = doc.getDocumentElement();
		
		EngineSettings settings = data.getServerSettings();
		if( importxml )
			settings.loadxml( root , c );
		else
			settings.loaddb( c );
	}

	public void loadProducts( ActionBase action ) throws Exception {
		data.unloadProducts();
		EngineProducts products = data.getProducts();
		products.loadProducts( action );
	}

	public void saveRegistry( TransactionBase transaction ) throws Exception {
		String propertyFile = getServerRegistryFile();
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		EngineRegistry registry = data.getRegistry();
		registry.savexml( transaction.getAction() , doc , root , execrc );
		
		EngineDirectory directory = data.getDirectory();
		Element node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.savexml( directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
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
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		mon.savexml( transaction , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void setServerSettings( TransactionBase transaction , EngineSettings settingsNew ) throws Exception {
		String propertyFile = getServerSettingsFile();
		Document doc = Common.xmlCreateDoc( "server" );
		Element root = doc.getDocumentElement();
		settingsNew.savexml( transaction , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
		
		EngineSettings settings = data.getServerSettings();
		settings.setData( transaction.getAction() , settingsNew , transaction.connection.getCoreVersion() );
	}

	public void importProduct( ActionBase action , String product , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		products.importProduct( action , product , includingEnvironments );
	}
	
	public void importCore( boolean includingSystems ) throws Exception {
		engine.trace( "reload server core settings ..." );
		
		if( includingSystems )
			data.unloadAll();
		else
			Common.exitUnexpected();

		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
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
