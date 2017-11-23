package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.EngineTransaction;
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
import org.urm.meta.engine.MirrorRepository;
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

	public void importRepo( EngineTransaction transaction , MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			importCore( transaction , true );
		else
			importProduct( transaction , repo.PRODUCT , true );
	}
	
	public void exportRepo( EngineTransaction transaction , MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			exportCore( transaction , true );
		else
			exportProduct( transaction , repo.PRODUCT );
	}
	
	public void loadCore() throws Exception {
		loadCore( false , true );
	}
	
	private void importCore( EngineTransaction transaction , boolean includingSystems ) throws Exception {
		engine.trace( "import server core settings ..." );
		
		if( includingSystems )
			data.unloadAll();
		else
			Common.exitUnexpected();

		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
	}

	private void exportCore( TransactionBase transaction , boolean includingSystems ) throws Exception {
		engine.trace( "export server core settings ..." );
		exportxmlSettings( transaction );
		exportxmlBase( transaction );
		saveInfrastructure( transaction );
		saveReleaseLifecycles( transaction );
		saveMonitoring( transaction );
		saveRegistry( transaction );
	}

	private void exportProduct( TransactionBase transaction , String productName ) throws Exception {
		engine.trace( "export server core settings ..." );
		data.saveProductMetadata( transaction.action , productName );
	}

	public void importProduct( EngineTransaction transaction , String product , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		products.importProduct( transaction.action , product , includingEnvironments );
	}
	
	private void loadCore( boolean importxml , boolean withSystems ) throws Exception {
		data.unloadAll();
		
		EngineDB db = data.getDatabase();
		DBConnection connection = null;
		try {
			connection = db.getConnection( engine.serverAction );
			matcher = new EngineMatcher( this , connection ); 

			// core
			if( importxml ) {
				connection.setNextCoreVersion();
				importxmlSettings( connection );
				importxmlBase( connection );
				loadInfrastructure();
				loadReleaseLifecycles();
				importxmlMonitoring( connection );
				loadRegistry();
			}
			else {
				loaddbSettings( connection );
				loaddbBase( connection );
				loadInfrastructure();
				loadReleaseLifecycles();
				loaddbMonitoring( connection );
				loadRegistry();
			}
				
			connection.save( true );

			// systems
			if( importxml ) {
				if( withSystems )
					importxmlDirectory( connection );
				else
					loaddbDirectory( connection );
			}
			else {
				loaddbDirectory( connection );
			}
			
			connection.save( true );
			
			if( importxml ) {
				engine.trace( "successfully saved server metadata version=" + connection.getNextCoreVersion() );
				connection.close( true );
			}
		}
		catch( Throwable e ) {
			engine.log( "init" , e );
			if( connection != null && importxml ) {
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

	private String getServerSettingsFile() {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "server.xml" );
		return( propertyFile );
	}
	
	private String getBaseFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "base.xml" );
		return( propertyFile );
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

	private void loaddbMonitoring( DBConnection c ) throws Exception {
		EngineMonitoring mon = data.getMonitoring();
		mon.loaddb( c );
	}

	private void importxmlMonitoring( DBConnection c ) throws Exception {
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		EngineMonitoring mon = data.getMonitoring();
		mon.loadxml( root , c );
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

	private void loaddbDirectory( DBConnection c ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		DBEngineDirectory.loaddb( directory , matcher , c );
		
		for( AppSystem system : directory.getSystems() )
			data.matchdoneSystem( system );
	}
	
	private void importxmlDirectory( DBConnection c ) throws Exception {
		String registryFile = getServerRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		EngineDirectory directory = data.getDirectory();
		Node node = ConfReader.xmlGetFirstChild( root , "directory" );
		DBEngineDirectory.importxml( directory , matcher , node , c );
		
		// match systems to core
		for( AppSystem system : directory.getSystems() )
			data.matchdoneSystem( system );
	}
	
	private void importxmlBase( DBConnection c ) throws Exception {
		String baseFile = getBaseFile();
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		EngineBase base = data.getServerBase();
		DBEngineBase.importxml( base , root , c );
	}

	private void importxmlSettings( DBConnection c ) throws Exception {
		String propertyFile = getServerSettingsFile();
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + propertyFile , propertyFile );
		
		Node root = doc.getDocumentElement();
		
		EngineSettings settings = data.getServerSettings();
		settings.importxml( root , c );
	}

	private void loaddbSettings( DBConnection c ) throws Exception {
		EngineSettings settings = data.getServerSettings();
		settings.loaddb( c );
	}

	private void loaddbBase( DBConnection c ) throws Exception {
		EngineBase base = data.getServerBase();
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
		DBEngineDirectory.exportxml( transaction.getAction() , directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitBase( TransactionBase transaction ) throws Exception {
	}
	
	public void exportxmlBase( TransactionBase transaction ) throws Exception {
		String propertyFile = getBaseFile();
		EngineBase base = data.getServerBase();
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		DBEngineBase.exportxml( transaction.getAction() , base , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
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

	public void commitSettings( TransactionBase transaction ) throws Exception {
	}	
	
	public void exportxmlSettings( TransactionBase transaction ) throws Exception {
		EngineSettings settings = data.getServerSettings();
		String propertyFile = getServerSettingsFile();
		Document doc = Common.xmlCreateDoc( "server" );
		Element root = doc.getDocumentElement();
		settings.savexml( transaction , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	public void setSettings( TransactionBase transaction , EngineSettings settingsNew ) throws Exception {
		EngineSettings settings = data.getServerSettings();
		settings.setData( transaction.getAction() , settingsNew , transaction.connection.getCoreVersion() );
		commitSettings( transaction );
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
