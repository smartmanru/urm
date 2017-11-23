package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.core.DBNames;
import org.urm.db.core.DBVersions;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineSettings;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EngineEntities;
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
	
	private EngineEntities entities;
	private EngineMatcher matcher;
	private DBConnection connection;
	private ActionBase action;

	public EngineLoader( Engine engine , EngineData data , ActionBase action ) {
		this.engine = engine;
		this.data = data;
		this.execrc = engine.execrc;
		this.entities = data.getEntities();
		this.action = action;
	}

	public EngineData getData() {
		return( data );
	}
	
	public EngineEntities getEntities() {
		return( entities );
	}
	
	public EngineMatcher getMatcher() {
		return( matcher );
	}
	
	public DBConnection getConnection() {
		return( connection );
	}
	
	public ActionBase getAction() {
		return( action );
	}
	
	public void initData() throws Exception {
		try {
			engine.trace( "init, checking client/server consistency ..." );
			EngineDB db = data.getDatabase();
			connection = db.getConnection( null );
			
			DBNames.loaddb( this );
			
			boolean dbUpdate = Common.getBooleanValue( System.getProperty( "dbupdate" ) );
			if( dbUpdate )
				upgradeData();
			else
				useData();
		}
		finally {
			if( connection != null )
				connection.close( true );
			connection = null;
		}
	}
	
	public void initData( DBConnection connection ) throws Exception {
	}
	
	private void upgradeData() throws Exception {
		DBCoreData.upgradeData( this );
		EngineCore core = data.getCore();
		core.upgradeData( this );
	}
	
	private void useData() throws Exception {
		int version = DBVersions.getCurrentAppVersion( connection );
		if( version != EngineDB.APP_VERSION )
			Common.exit2( _Error.InvalidVersion2 , "Mismatched client/database, client version=" + EngineDB.APP_VERSION + ", database version=" + version , "" + EngineDB.APP_VERSION , "" + version );
		
		DBCoreData.useData( this );
		EngineCore core = data.getCore();
		core.useData( this );
	}
	
	public void importRepo( MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			importCore( true );
		else
			importProduct( repo.PRODUCT , true );
	}
	
	public void exportRepo( MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			exportCore( true );
		else
			exportProduct( repo.PRODUCT );
	}
	
	public void loadCore() throws Exception {
		loadCore( false , true );
	}
	
	public void trace( String s ) {
		action.trace( s );
	}

	public void log( String p , Throwable e ) {
		action.log( p ,  e );
	}
	
	private void importCore( boolean includingSystems ) throws Exception {
		trace( "import server core settings ..." );
		
		if( includingSystems )
			data.unloadAll();
		else
			Common.exitUnexpected();

		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
	}

	private void exportCore( boolean includingSystems ) throws Exception {
		trace( "export server core settings ..." );
		exportxmlSettings();
		exportxmlBase();
		saveInfrastructure();
		saveReleaseLifecycles();
		saveMonitoring();
		saveRegistry();
	}

	private void exportProduct( String productName ) throws Exception {
		trace( "export server core settings ..." );
		data.saveProductMetadata( this , productName );
	}

	public void importProduct( String product , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		products.importProduct( this , product , includingEnvironments );
	}
	
	private void loadCore( boolean importxml , boolean withSystems ) throws Exception {
		data.unloadAll();
		
		EngineDB db = data.getDatabase();
		try {
			connection = db.getConnection( action );
			matcher = new EngineMatcher( this );
			entities = data.getEntities();

			// core
			if( importxml ) {
				connection.setNextCoreVersion();
				importxmlSettings();
				importxmlBase();
				loadInfrastructure();
				loadReleaseLifecycles();
				importxmlMonitoring();
				loadRegistry();
			}
			else {
				loaddbSettings();
				loaddbBase();
				loadInfrastructure();
				loadReleaseLifecycles();
				loaddbMonitoring();
				loadRegistry();
			}
				
			connection.save( true );

			// systems
			if( importxml ) {
				if( withSystems )
					importxmlDirectory();
				else
					loaddbDirectory();
			}
			else {
				loaddbDirectory();
			}
			
			connection.save( true );
			
			if( importxml ) {
				trace( "successfully saved server metadata version=" + connection.getNextCoreVersion() );
				connection.close( true );
				connection = null;
			}
		}
		catch( Throwable e ) {
			log( "init" , e );
			if( connection != null && importxml ) {
				trace( "unable to save server metadata version=" + connection.getNextCoreVersion() );
				connection.close( false );
				connection = null;
			}
			Common.exitUnexpected();
		}
	}
	
	public LocalFolder getServerHomeFolder() throws Exception {
		LocalFolder folder = action.getLocalFolder( execrc.installPath );
		return( folder );
	}

	public LocalFolder getServerSettingsFolder() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		LocalFolder folder = action.getLocalFolder( path );
		return( folder );
	}

	public LocalFolder getProductHomeFolder( String productName ) throws Exception {
		Meta meta = action.actionInit.getActiveProductMetadata( productName );
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

	private void loaddbMonitoring() throws Exception {
		EngineMonitoring mon = data.getMonitoring();
		mon.loaddb( this );
	}

	private void importxmlMonitoring() throws Exception {
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		EngineMonitoring mon = data.getMonitoring();
		mon.loadxml( this , root );
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
		registry.loadxml( this , root );
	}

	private void loaddbDirectory() throws Exception {
		EngineDirectory directory = data.getDirectory();
		DBEngineDirectory.loaddb( this , directory );
		
		for( AppSystem system : directory.getSystems() )
			data.matchdoneSystem( this , system );
	}
	
	private void importxmlDirectory() throws Exception {
		String registryFile = getServerRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		EngineDirectory directory = data.getDirectory();
		Node node = ConfReader.xmlGetFirstChild( root , "directory" );
		DBEngineDirectory.importxml( this , directory , node );
		
		// match systems to core
		for( AppSystem system : directory.getSystems() )
			data.matchdoneSystem( this , system );
	}
	
	private void importxmlBase() throws Exception {
		String baseFile = getBaseFile();
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		EngineBase base = data.getServerBase();
		DBEngineBase.importxml( this , base , root );
	}

	private void importxmlSettings() throws Exception {
		String propertyFile = getServerSettingsFile();
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + propertyFile , propertyFile );
		
		Node root = doc.getDocumentElement();
		
		EngineSettings settings = data.getEngineSettings();
		DBEngineSettings.importxml( this , settings , root );
	}

	private void loaddbSettings() throws Exception {
		EngineSettings settings = data.getEngineSettings();
		DBEngineSettings.loaddb( this , settings );
	}

	private void loaddbBase() throws Exception {
		EngineBase base = data.getServerBase();
		DBEngineBase.loaddb( this , base );
	}

	public void loadProducts() throws Exception {
		data.unloadProducts();
		EngineProducts products = data.getProducts();
		products.loadProducts( this );
	}

	public void saveRegistry() throws Exception {
		String propertyFile = getServerRegistryFile();
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		EngineRegistry registry = data.getRegistry();
		registry.savexml( this , doc , root );
		
		EngineDirectory directory = data.getDirectory();
		Element node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.exportxml( this , directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitBase() throws Exception {
	}
	
	public void exportxmlBase() throws Exception {
		String propertyFile = getBaseFile();
		EngineBase base = data.getServerBase();
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		DBEngineBase.exportxml( this , base , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void saveInfrastructure() throws Exception {
		String propertyFile = getInfrastructureFile();
		EngineInfrastructure infra = data.getInfrastructure();
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		infra.save( this , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void saveReleaseLifecycles() throws Exception {
		String propertyFile = getReleaseLifecyclesFile();
		EngineReleaseLifecycles lifecycles = data.getReleaseLifecycles();
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		lifecycles.save( this , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void saveMonitoring() throws Exception {
		String propertyFile = getMonitoringFile();
		EngineMonitoring mon = data.getMonitoring();
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		mon.savexml( this , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitSettings() throws Exception {
	}	
	
	public void exportxmlSettings() throws Exception {
		EngineSettings settings = data.getEngineSettings();
		String propertyFile = getServerSettingsFile();
		Document doc = Common.xmlCreateDoc( "server" );
		Element root = doc.getDocumentElement();
		DBEngineSettings.exportxml( this , settings , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	public void setSettings( EngineSettings settingsNew ) throws Exception {
		EngineSettings settings = data.getEngineSettings();
		settings.setData( action , settingsNew , connection.getCoreVersion() );
		commitSettings();
	}

	private void dropCoreData( boolean includingSystems ) throws Exception {
		EngineDB db = data.getDatabase();
		try {
			connection = db.getConnection( action );
			if( includingSystems )
				DBSystemData.dropSystemData( this );
			DBCoreData.dropCoreData( this );
			int CV = connection.getCurrentCoreVersion();
			connection.close( true );
			connection = null;
			trace( "successfully deleted current server metadata, version=" + CV );
		}
		catch( Throwable e ) {
			log( "init" , e );
			int CV = connection.getCurrentCoreVersion();
			connection.close( false );
			connection = null;
			trace( "unable to delete current server metadata, version=" + CV );
			Common.exitUnexpected();
		}
	}
	
}
