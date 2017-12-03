package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.core.DBCoreData;
import org.urm.db.core.DBNames;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineResources;
import org.urm.db.engine.DBEngineSettings;
import org.urm.db.system.DBSystemData;
import org.urm.engine.Engine;
import org.urm.engine.EngineDB;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineRegistry;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine._Error;
import org.urm.meta.product.Meta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoader {

	public static String ELEMENT_RESOURCES = "resources";
	public static String ELEMENT_MIRRORS = "mirror";
	public static String ELEMENT_BUILDERS = "build";
	
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
	
	public LocalFolder getEngineHomeFolder() throws Exception {
		LocalFolder folder = action.getLocalFolder( execrc.installPath );
		return( folder );
	}

	public LocalFolder getEngineSettingsFolder() throws Exception {
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

	private String getEngineSettingsFile() {
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

	private String getMonitoringFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "monitoring.xml" );
		return( propertyFile );
	}

	private String getRegistryFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "registry.xml" );
		return( propertyFile );
	}

	public void initData() throws Exception {
		try {
			trace( "init, checking engine/database consistency ..." );
			EngineDB db = data.getDatabase();
			connection = db.getConnection( null );
			
			trace( "load names ..." );
			DBNames.loaddb( this );
			
			boolean dbUpdate = Common.getBooleanValue( System.getProperty( "dbupdate" ) );
			if( dbUpdate ) {
				upgradeData();
				connection.close( true );
				connection = null;
				importCore( true );
				data.unloadAll();
			}
			else
				useData();
		}
		finally {
			if( connection != null )
				connection.close( false );
			connection = null;
		}
	}
	
	private void upgradeData() throws Exception {
		trace( "upgrade meta ..." );
		DBCoreData.upgradeData( this );
		EngineCore core = data.getCore();
		core.upgradeData( this );
		connection.save( true );
	}
	
	private void useData() throws Exception {
		trace( "load meta ..." );
		int version = connection.getCurrentAppVersion();
		if( version != EngineDB.APP_VERSION )
			Common.exit2( _Error.InvalidVersion2 , "Mismatched engine/database, engine version=" + EngineDB.APP_VERSION + ", database version=" + version , "" + EngineDB.APP_VERSION , "" + version );
		
		DBCoreData.useData( this );
		EngineCore core = data.getCore();
		core.useData( this );
	}
	
	public void importRepo( MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			importCore( true );
		else
			importProduct( repo.productId , true );
	}
	
	public void exportRepo( MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			exportCore( true );
		else
			exportProduct( repo.productId );
	}
	
	public void initCore() throws Exception {
		loadCore( false , true );
	}
	
	public void trace( String s ) {
		action.trace( s );
	}

	public void log( String p , Throwable e ) {
		action.log( p ,  e );
	}
	
	private void importCore( boolean includingSystems ) throws Exception {
		trace( "cleanup engine data ..." );
		data.unloadAll();
		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
	}

	private void exportCore( boolean includingSystems ) throws Exception {
		trace( "export engine core data ..." );
		exportxmlSettings();
		exportxmlBase();
		exportxmlReleaseLifecycles();
		exportxmlMonitoring();
		exportxmlRegistry();
		exportxmlInfrastructure();
	}

	private void exportProduct( Integer productId ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		AppProduct product = directory.getProduct( productId );
		
		trace( "export engine product=" + product.NAME + " data ..." );
		data.saveProductMetadata( this , product.NAME );
	}

	public void importProduct( Integer productId , boolean includingEnvironments ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		AppProduct product = directory.getProduct( productId );
		
		trace( "import engine product=" + product.NAME + " data ..." );
		EngineProducts products = data.getProducts();
		products.importProduct( this , product.NAME , includingEnvironments );
	}
	
	private void loadCore( boolean importxml , boolean withSystems ) throws Exception {
		EngineDB db = data.getDatabase();
		try {
			connection = db.getConnection( action );
			matcher = new EngineMatcher( this );
			entities = data.getEntities();

			// core
			if( importxml ) {
				int version = connection.getNextCoreVersion();
				trace( "create new engine core version=" + version + " ..." );
				importxmlEngineSettings();
				importxmlBase();
				importxmlReleaseLifecycles();
				importxmlMonitoring();
				importxmlRegistry();
				importxmlInfrastructure();
				connection.save( true );
				trace( "successfully completed import of engine core data" );
			}
			else {
				trace( "load engine core data, version=" + connection.getCoreVersion() + " ..." );
				loaddbEngineSettings();
				loaddbBase();
				loaddbReleaseLifecycles();
				loaddbMonitoring();
				loaddbRegistry();
				loaddbInfrastructure();
			}
				
			// systems
			if( importxml ) {
				if( withSystems ) {
					importxmlDirectory();
					connection.save( true );
					trace( "successfully completed import of engine directory data" );
				}
				else
					loaddbDirectory();
			}
			else {
				loaddbDirectory();
			}
			
			connection = null;
		}
		catch( Throwable e ) {
			log( "init" , e );
			if( importxml )
				trace( "unable to import engine data" );
			else
				trace( "unable to load engine data" );
			
			connection.close( false );
			connection = null;
			Common.exitUnexpected();
		}
	}
	
	private void importxmlEngineSettings() throws Exception {
		trace( "import engine settings data ..." );
		String propertyFile = getEngineSettingsFile();
		Document doc = ConfReader.readXmlFile( execrc , propertyFile );
		if( doc == null )
			Common.exit1( _Error.UnableReadEnginePropertyFile1 , "unable to read engine property file " + propertyFile , propertyFile );
		
		Node root = doc.getDocumentElement();
		
		EngineSettings settings = data.getEngineSettings();
		DBEngineSettings.importxml( this , settings , root );
	}

	private void loaddbEngineSettings() throws Exception {
		trace( "load engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		DBEngineSettings.loaddb( this , settings );
	}

	private void importxmlBase() throws Exception {
		trace( "import engine base data ..." );
		String baseFile = getBaseFile();
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		EngineBase base = data.getEngineBase();
		DBEngineBase.importxml( this , base , root );
	}

	private void loaddbBase() throws Exception {
		trace( "load engine base data ..." );
		EngineBase base = data.getEngineBase();
		DBEngineBase.loaddb( this , base );
	}

	private void importxmlInfrastructure() throws Exception {
		trace( "import engine infrastructure data ..." );
		String infraFile = getInfrastructureFile();
		Document doc = ConfReader.readXmlFile( execrc , infraFile );
		Node root = doc.getDocumentElement();
		
		EngineInfrastructure infra = data.getInfrastructure();
		DBEngineInfrastructure.importxml( this , infra , root );
	}

	private void loaddbInfrastructure() throws Exception {
		trace( "load engine infrastructure data ..." );
		EngineInfrastructure infra = data.getInfrastructure();
		DBEngineInfrastructure.loaddb( this , infra );
	}

	private void importxmlReleaseLifecycles() throws Exception {
		trace( "import engine lifecycles data ..." );
		String lcFile = getReleaseLifecyclesFile();
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		EngineLifecycles lifecycles = data.getReleaseLifecycles();
		DBEngineLifecycles.importxml( this , lifecycles , root );
	}

	private void loaddbReleaseLifecycles() throws Exception {
		trace( "load release lifecycles data ..." );
		EngineLifecycles lifecycles = data.getReleaseLifecycles();
		DBEngineLifecycles.loaddb( this , lifecycles );
	}

	private void importxmlMonitoring() throws Exception {
		trace( "import engine infrastructure data ..." );
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		EngineMonitoring mon = data.getMonitoring();
		mon.importxml( this , root );
	}

	private void loaddbMonitoring() throws Exception {
		trace( "load engine monitoring data ..." );
		EngineMonitoring mon = data.getMonitoring();
		mon.loaddb( this );
	}

	private void importxmlRegistry() throws Exception {
		trace( "import engine registry data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		EngineRegistry registry = data.getRegistry();
		Node node;
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_RESOURCES );
		DBEngineResources.importxml( this , registry.resources , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_MIRRORS );
		DBEngineMirrors.importxml( this , registry.mirrors , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILDERS );
		DBEngineBuilders.importxml( this , registry.builders , node );
	}

	private void loaddbRegistry() throws Exception {
		trace( "load engine registry data ..." );
		EngineRegistry registry = data.getRegistry();
		DBEngineResources.loaddb( this , registry.resources );
		DBEngineMirrors.loaddb( this , registry.mirrors );
		DBEngineBuilders.loaddb( this , registry.builders );
	}

	private void loaddbDirectory() throws Exception {
		trace( "load engine directory data ..." );
		EngineDirectory directory = data.getDirectory();
		DBEngineDirectory.loaddb( this , directory );
	}
	
	private void importxmlDirectory() throws Exception {
		trace( "import engine directory data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		EngineDirectory directory = data.getDirectory();
		Node node = ConfReader.xmlGetFirstChild( root , "directory" );
		DBEngineDirectory.importxml( this , directory , node );
	}
	
	public void loadProducts() throws Exception {
		trace( "load engine products data ..." );
		data.unloadProducts();
		EngineProducts products = data.getProducts();
		products.loadProducts( this );
	}

	public void commitRegistry() throws Exception {
		exportxmlRegistry();
	}
	
	public void exportxmlRegistry() throws Exception {
		trace( "export engine registry data ..." );
		String propertyFile = getRegistryFile();
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		EngineRegistry registry = data.getRegistry();
		Element node;
		node = Common.xmlCreateElement( doc , root , ELEMENT_RESOURCES );
		DBEngineResources.exportxml( this , registry.resources , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_MIRRORS );
		DBEngineMirrors.exportxml( this , registry.mirrors , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_BUILDERS );
		DBEngineBuilders.exportxml( this , registry.builders , doc , node );
		
		EngineDirectory directory = data.getDirectory();
		node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.exportxml( this , directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitBase() throws Exception {
	}
	
	public void exportxmlBase() throws Exception {
		trace( "export engine base data ..." );
		String propertyFile = getBaseFile();
		EngineBase base = data.getEngineBase();
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		DBEngineBase.exportxml( this , base , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void exportxmlInfrastructure() throws Exception {
		commitInfrastructure();
	}
	
	public void commitInfrastructure() throws Exception {
		trace( "export engine infrastructure data ..." );
		String propertyFile = getInfrastructureFile();
		EngineInfrastructure infra = data.getInfrastructure();
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		DBEngineInfrastructure.exportxml( this , infra , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitReleaseLifecycles() throws Exception {
		exportxmlReleaseLifecycles();
	}
	
	public void exportxmlReleaseLifecycles() throws Exception {
		trace( "export engine lifecycles data ..." );
		String propertyFile = getReleaseLifecyclesFile();
		EngineLifecycles lifecycles = data.getReleaseLifecycles();
		Document doc = Common.xmlCreateDoc( "lifecycles" );
		Element root = doc.getDocumentElement();
		DBEngineLifecycles.exportxml( this , lifecycles , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	public void commitMonitoring() throws Exception {
		exportxmlMonitoring();
	}
	
	public void exportxmlMonitoring() throws Exception {
		trace( "export engine monitoring data ..." );
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
		trace( "export engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		String propertyFile = getEngineSettingsFile();
		Document doc = Common.xmlCreateDoc( "engine" );
		Element root = doc.getDocumentElement();
		DBEngineSettings.exportxml( this , settings , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	public void setSettings( EngineSettings settingsNew ) throws Exception {
		trace( "change engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		settings.setData( action , settingsNew , connection.getCoreVersion() );
		commitSettings();
	}

	private void dropCoreData( boolean includingSystems ) throws Exception {
		EngineDB db = data.getDatabase();
		try {
			connection = db.getConnection( action );
			if( includingSystems ) {
				trace( "drop engine directory data in database ..." );
				DBSystemData.dropSystemData( this );
				trace( "successfully dropped engine directory data" );
			}
			
			trace( "drop engine core data in database ..." );
			int version = connection.getNextCoreVersion();
			DBCoreData.dropCoreData( this );
			connection.close( true );
			connection = null;
			trace( "successfully dropped engine core data, core version=" + version );
		}
		catch( Throwable e ) {
			log( "init" , e );
			connection.close( false );
			connection = null;
			trace( "unable to drop engine data" );
			Common.exitUnexpected();
		}
	}

}
