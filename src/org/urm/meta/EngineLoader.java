package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.engine.DBEngineBase;
import org.urm.db.engine.DBEngineBuilders;
import org.urm.db.engine.DBEngineData;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.engine.DBEngineInfrastructure;
import org.urm.db.engine.DBEngineLifecycles;
import org.urm.db.engine.DBEngineMirrors;
import org.urm.db.engine.DBEngineMonitoring;
import org.urm.db.engine.DBEngineResources;
import org.urm.db.engine.DBEngineSettings;
import org.urm.db.system.DBSystemData;
import org.urm.db.upgrade.DBUpgrade;
import org.urm.engine.Engine;
import org.urm.engine.TransactionBase;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineMirrors;
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
	public static String ELEMENT_DIRECTORY = "directory";
	
	public Engine engine;
	private EngineData data;
	public RunContext execrc;
	
	private EngineEntities entities;
	private EngineMatcher matcher;
	private DBConnection connection;
	private TransactionBase transaction;
	private ActionBase action;

	private EngineSettings settingsNew;
	private EngineResources resourcesNew;
	private EngineBuilders buildersNew;
	private EngineDirectory directoryNew;
	private EngineMirrors mirrorsNew;
	private EngineBase baseNew;
	private EngineInfrastructure infraNew;
	private EngineLifecycles lifecyclesNew;
	private EngineMonitoring monitoringNew;
	
	public EngineLoader( Engine engine , EngineData data , ActionBase action ) {
		this.engine = engine;
		this.data = data;
		this.action = action;
		this.execrc = engine.execrc;
		this.entities = data.getEntities();
	}

	public EngineLoader( Engine engine , EngineData data , TransactionBase transaction ) {
		this.engine = engine;
		this.data = data;
		this.transaction = transaction;
		this.action = transaction.action;
		this.execrc = engine.execrc;
		this.entities = data.getEntities();
	}

	public EngineEntities getEntities() {
		return( entities );
	}
	
	public EngineDB getDatabase() {
		return( data.getDatabase() );
	}
	
	public EngineSettings getSettings() {
		if( settingsNew != null )
			return( settingsNew );
		return( data.getEngineSettings() );
	}

	public EngineResources getResources() {
		if( resourcesNew != null )
			return( resourcesNew );
		return( data.getResources() );
	}

	public EngineBuilders getBuilders() {
		if( buildersNew != null )
			return( buildersNew );
		return( data.getBuilders() );
	}

	public EngineDirectory getDirectory() {
		if( directoryNew != null )
			return( directoryNew );
		return( data.getDirectory() );
	}

	public EngineMirrors getMirrors() {
		if( mirrorsNew != null )
			return( mirrorsNew );
		return( data.getMirrors() );
	}
	
	public EngineInfrastructure getInfrastructure() {
		if( infraNew != null )
			return( infraNew );
		return( data.getInfrastructure() );
	}

	public EngineMatcher getMatcher() {
		return( matcher );
	}
	
	public DBConnection getConnection() throws Exception {
		if( connection == null ) {
			if( transaction != null )
				connection = transaction.getConnection();
			else {
				EngineDB db = data.getDatabase();
				connection = db.getConnection( action );
			}
		}
			
		return( connection );
	}
	
	private void closeConnection( boolean commit ) throws Exception {
		if( commit )
			setData();

		if( connection != null ) {
			if( transaction == null )
				connection.close( commit );
			else
				connection.save( commit );
			connection = null;
		}
	}

	private void saveConnection( boolean commit ) throws Exception {
		if( commit )
			setData();

		if( connection != null )
			connection.save( commit );
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
		Meta meta = action.actionInit.findActiveProductMetadata( productName );
		if( meta == null ) {
			String path = Common.getPath( execrc.installPath , "products/" + productName );
			LocalFolder folder = new LocalFolder( path , execrc.isWindows() );
			return( folder );
		}
		
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

	private String getAuthFile() throws Exception {
		String path = Common.getPath( execrc.installPath , "etc" );
		String propertyFile = Common.getPath( path , "auth.xml" );
		return( propertyFile );
	}

	public static boolean getVersionUpgradeState() {
		return( Common.getBooleanValue( System.getProperty( "autoupgrade" ) ) );
	}

	public static boolean getInitialUpdateState() {
		return( Common.getBooleanValue( System.getProperty( "dbupdate" ) ) );
	}

	public static void setInitialUpdateState() {
		System.setProperty( "dbupdate" , Common.getBooleanValue( true ) );
	}
	
	public void initMeta() throws Exception {
		try {
			trace( "init, checking engine/database consistency ..." );
			trace( "load names ..." );
			DBNames.loaddb( this );
			
			boolean dbUpdate = getInitialUpdateState();
			if( dbUpdate ) {
				upgradeMeta();
				closeConnection( true );
			}
			else
				useMeta();
		}
		finally {
			closeConnection( false );
		}
	}
	
	private void upgradeMeta() throws Exception {
		trace( "upgrade meta ..." );
		DBEngineData.upgradeMeta( this );
		EngineCore core = data.getCore();
		core.upgradeMeta( this );
		saveConnection( true );
	}
	
	private void useMeta() throws Exception {
		trace( "load meta ..." );
		getConnection();
		int version = connection.getCurrentAppVersion();
		if( version != EngineDB.APP_VERSION ) {
			if( getVersionUpgradeState() )
				DBUpgrade.upgrade( this , EngineDB.APP_VERSION , version );
			else
				Common.exit2( _Error.InvalidVersion2 , "Mismatched engine/database, engine version=" + EngineDB.APP_VERSION + ", database version=" + version , "" + EngineDB.APP_VERSION , "" + version );
		}
		else
			trace( "using database version=" + version );
		
		DBEngineData.useMeta( this );
		EngineCore core = data.getCore();
		core.useMeta( this );
	}

	public void importRepo( MirrorRepository repo ) throws Exception {
		try {
			getConnection();
			matcher = new EngineMatcher( this );
			entities = data.getEntities();
			
			if( repo.isServer() ) {
				importEngine();
				saveConnection( true );
				loadProducts();
			}
			else {
				importProduct( repo.productId , true );
				closeConnection( true );
			}
		}
		catch( Throwable e ) {
			log( "import repository" , e );
			trace( "unable to import repository" );
			
			closeConnection( false );
			Common.exitUnexpected();
		}
	}
	
	public void exportRepo( MirrorRepository repo ) throws Exception {
		if( repo.isServer() )
			exportEngine();
		else {
			if( repo.productId == null )
				Common.exitUnexpected();
			exportProduct( repo.productId );
		}
	}
	
	public void initCore() throws Exception {
		boolean dbUpdate = getInitialUpdateState();
		if( dbUpdate )
			importCore( true );
		
		data.unloadAll();
		loadCore( false , true );
	}
	
	public void initAuth( EngineAuth auth ) throws Exception {
		boolean dbUpdate = getInitialUpdateState();
		if( dbUpdate )
			importAuth( auth );
		
		auth.unloadAll();
		loadAuth( auth , false );
	}
	
	public void trace( String s ) {
		action.trace( s );
	}

	public void log( String p , Throwable e ) {
		action.log( p ,  e );
	}
	
	private void importEngine() throws Exception {
		importCore( true );
		importAuth( engine.getAuth() );
	}
	
	private void importCore( boolean includingSystems ) throws Exception {
		trace( "cleanup engine data ..." );
		data.unloadAll();
		dropAuthData();
		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
	}

	private void importAuth( EngineAuth auth ) throws Exception {
		trace( "cleanup auth data ..." );
		auth.unloadAll();
		dropAuthData();
		loadAuth( auth , true );
	}

	private void exportEngine() throws Exception {
		exportCore( true );
		exportAuth( engine.getAuth() );
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

	private void exportAuth( EngineAuth auth ) throws Exception {
		trace( "export engine auth data ..." );
		exportxmlAuth( auth );
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
		try {
			getConnection();
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
				saveConnection( true );
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
					saveConnection( true );
					trace( "successfully completed import of engine directory data" );
				}
				else
					loaddbDirectory();
			}
			else {
				loaddbDirectory();
			}
			
			closeConnection( true );
		}
		catch( Throwable e ) {
			log( "init" , e );
			if( importxml )
				trace( "unable to import engine data" );
			else
				trace( "unable to load engine data" );
			
			closeConnection( false );
			Common.exitUnexpected();
		}
	}

	private void setData() {
		if( settingsNew != null ) {
			data.setSettings( settingsNew );
			settingsNew = null;
		}
		
		if( resourcesNew != null ) {
			data.setResources( resourcesNew );
			resourcesNew = null;
		}
		
		if( buildersNew != null ) {
			data.setBuilders( buildersNew );
			buildersNew = null;
		}
		
		if( directoryNew != null ) {
			data.setDirectory( directoryNew );
			directoryNew = null;
		}
		
		if( mirrorsNew != null ) {
			data.setMirrors( mirrorsNew );
			mirrorsNew = null;
		}
		
		if( baseNew != null ) {
			data.setBase( baseNew );
			baseNew = null;
		}
		
		if( infraNew != null ) {
			data.setInfrastructure( infraNew );
			infraNew = null;
		}
		
		if( lifecyclesNew != null ) {
			data.setLifecycles( lifecyclesNew );
			lifecyclesNew = null;
		}
		
		if( monitoringNew != null ) {
			data.setMonitoring( monitoringNew );
			monitoringNew = null;
		}
	}
	
	private void loadAuth( EngineAuth auth , boolean importxml ) throws Exception {
		try {
			getConnection();
			matcher = new EngineMatcher( this );
			entities = data.getEntities();

			// core
			if( importxml ) {
				int version = connection.getNextLocalVersion();
				trace( "create new engine auth version=" + version + " ..." );
				importxmlAuth( auth );
				saveConnection( true );
				trace( "successfully completed import of engine auth data" );
			}
			else {
				trace( "load engine auth data, version=" + connection.getLocalVersion() + " ..." );
				loaddbAuth( auth );
			}

			closeConnection( true );
		}
		catch( Throwable e ) {
			log( "init" , e );
			if( importxml )
				trace( "unable to import engine auth data" );
			else
				trace( "unable to load engine auth data" );
			
			closeConnection( false );
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
		
		settingsNew = new EngineSettings( engine );
		DBEngineSettings.importxml( this , settingsNew , root );
	}

	private void loaddbEngineSettings() throws Exception {
		trace( "load engine settings data ..." );
		settingsNew = new EngineSettings( engine );
		DBEngineSettings.loaddb( this , settingsNew );
	}

	private void importxmlBase() throws Exception {
		trace( "import engine base data ..." );
		String baseFile = getBaseFile();
		Document doc = ConfReader.readXmlFile( execrc , baseFile );
		Node root = doc.getDocumentElement();
		
		baseNew = new EngineBase( engine );
		DBEngineBase.importxml( this , baseNew , root );
	}

	private void loaddbBase() throws Exception {
		trace( "load engine base data ..." );
		baseNew = new EngineBase( engine );
		DBEngineBase.loaddb( this , baseNew );
	}

	private void importxmlInfrastructure() throws Exception {
		trace( "import engine infrastructure data ..." );
		String infraFile = getInfrastructureFile();
		Document doc = ConfReader.readXmlFile( execrc , infraFile );
		Node root = doc.getDocumentElement();
		
		infraNew = new EngineInfrastructure( engine );
		DBEngineInfrastructure.importxml( this , infraNew , root );
	}

	private void loaddbInfrastructure() throws Exception {
		trace( "load engine infrastructure data ..." );
		infraNew = new EngineInfrastructure( engine );
		DBEngineInfrastructure.loaddb( this , infraNew );
	}

	private void importxmlReleaseLifecycles() throws Exception {
		String lcFile = getReleaseLifecyclesFile();
		
		trace( "import engine lifecycles data from " + lcFile + " ..." );
		Document doc = ConfReader.readXmlFile( execrc , lcFile );
		Node root = doc.getDocumentElement();
		
		lifecyclesNew = new EngineLifecycles( engine );
		DBEngineLifecycles.importxml( this , lifecyclesNew , root );
	}

	private void loaddbReleaseLifecycles() throws Exception {
		trace( "load release lifecycles data ..." );
		lifecyclesNew = new EngineLifecycles( engine );
		DBEngineLifecycles.loaddb( this , lifecyclesNew );
	}

	private void importxmlMonitoring() throws Exception {
		trace( "import engine infrastructure data ..." );
		String monFile = getMonitoringFile();
		Document doc = ConfReader.readXmlFile( execrc , monFile );
		Node root = doc.getDocumentElement();
		
		monitoringNew = new EngineMonitoring( engine );
		DBEngineMonitoring.importxml( this , monitoringNew , root );
	}

	private void loaddbMonitoring() throws Exception {
		trace( "load engine monitoring data ..." );
		monitoringNew = new EngineMonitoring( engine );
		DBEngineMonitoring.loaddb( this , monitoringNew );
	}

	private void importxmlRegistry() throws Exception {
		trace( "import engine registry data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		Node node;
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_RESOURCES );
		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.importxml( this , resourcesNew , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_MIRRORS );
		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.importxml( this , mirrorsNew , node );
		node = ConfReader.xmlGetFirstChild( root , ELEMENT_BUILDERS );
		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.importxml( this , buildersNew , node );
	}

	private void loaddbRegistry() throws Exception {
		trace( "load engine registry data ..." );
		resourcesNew = new EngineResources( engine ); 
		DBEngineResources.loaddb( this , resourcesNew );
		mirrorsNew = new EngineMirrors( engine );
		DBEngineMirrors.loaddb( this , mirrorsNew );
		buildersNew = new EngineBuilders( engine );
		DBEngineBuilders.loaddb( this , buildersNew );
	}

	private void loaddbDirectory() throws Exception {
		trace( "load engine directory data ..." );
		directoryNew = new EngineDirectory( engine );
		DBEngineDirectory.loaddb( this , directoryNew );
	}
	
	private void importxmlDirectory() throws Exception {
		trace( "import engine directory data ..." );
		String registryFile = getRegistryFile();
		Document doc = ConfReader.readXmlFile( execrc , registryFile );
		Node root = doc.getDocumentElement();
		
		directoryNew = new EngineDirectory( engine );
		Node node = ConfReader.xmlGetFirstChild( root , ELEMENT_DIRECTORY );
		DBEngineDirectory.importxml( this , directoryNew , node );
	}
	
	public void loadProducts() throws Exception {
		try {
			getConnection();
			matcher = new EngineMatcher( this );
			entities = data.getEntities();

			trace( "load engine products data ..." );
			data.unloadProducts();
			EngineProducts products = data.getProducts();
			products.loadProducts( this );

			closeConnection( true );
		}
		catch( Throwable e ) {
			log( "init" , e );
			trace( "unable to load products data" );
			
			closeConnection( false );
			Common.exitUnexpected();
		}
	}

	private void importxmlAuth( EngineAuth auth ) throws Exception {
		trace( "import engine auth data ..." );
		String authFile = getAuthFile();
		Document doc = ConfReader.readXmlFile( execrc , authFile );
		Node root = doc.getDocumentElement();
		
		DBEngineAuth.importxml( this , auth , root );
	}

	private void loaddbAuth( EngineAuth auth ) throws Exception {
		trace( "load engine auth data ..." );
		DBEngineAuth.loaddb( this , auth );
	}

	private void exportxmlRegistry() throws Exception {
		trace( "export engine registry data ..." );
		String propertyFile = getRegistryFile();
		Document doc = Common.xmlCreateDoc( "registry" );
		Element root = doc.getDocumentElement();
		
		Element node;
		node = Common.xmlCreateElement( doc , root , ELEMENT_RESOURCES );
		DBEngineResources.exportxml( this , data.getResources() , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_MIRRORS );
		DBEngineMirrors.exportxml( this , data.getMirrors() , doc , node );
		node = Common.xmlCreateElement( doc , root , ELEMENT_BUILDERS );
		DBEngineBuilders.exportxml( this , data.getBuilders() , doc , node );
		
		EngineDirectory directory = data.getDirectory();
		node = Common.xmlCreateElement( doc , root , "directory" );
		DBEngineDirectory.exportxml( this , directory , doc , node );
		
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlBase() throws Exception {
		trace( "export engine base data ..." );
		String propertyFile = getBaseFile();
		EngineBase base = data.getEngineBase();
		Document doc = Common.xmlCreateDoc( "base" );
		Element root = doc.getDocumentElement();
		DBEngineBase.exportxml( this , base , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlInfrastructure() throws Exception {
		trace( "export engine infrastructure data ..." );
		String propertyFile = getInfrastructureFile();
		EngineInfrastructure infra = data.getInfrastructure();
		Document doc = Common.xmlCreateDoc( "infrastructure" );
		Element root = doc.getDocumentElement();
		DBEngineInfrastructure.exportxml( this , infra , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlReleaseLifecycles() throws Exception {
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
	
	private void exportxmlMonitoring() throws Exception {
		trace( "export engine monitoring data ..." );
		String propertyFile = getMonitoringFile();
		EngineMonitoring mon = data.getMonitoring();
		Document doc = Common.xmlCreateDoc( "monitoring" );
		Element root = doc.getDocumentElement();
		DBEngineMonitoring.savexml( this , mon , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}

	private void exportxmlSettings() throws Exception {
		trace( "export engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		String propertyFile = getEngineSettingsFile();
		Document doc = Common.xmlCreateDoc( "engine" );
		Element root = doc.getDocumentElement();
		DBEngineSettings.exportxml( this , settings , doc , root );
		Common.xmlSaveDoc( doc , propertyFile );
	}
	
	private void exportxmlAuth( EngineAuth auth ) throws Exception {
		trace( "export engine auth data ..." );
		String authFile = getAuthFile();
		Document doc = Common.xmlCreateDoc( "auth" );
		Element root = doc.getDocumentElement();
		DBEngineAuth.exportxml( this , auth , doc , root );
		Common.xmlSaveDoc( doc , authFile );
	}

	public void setSettings( EngineSettings settingsNew ) throws Exception {
		trace( "change engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		getConnection();
		settings.setData( action , settingsNew , connection.getCoreVersion() );
	}

	private void dropCoreData( boolean includingSystems ) throws Exception {
		try {
			getConnection();
			if( includingSystems ) {
				trace( "drop engine directory data in database ..." );
				DBSystemData.dropSystemData( this );
				trace( "successfully dropped engine directory data" );
			}
			
			trace( "drop engine core data in database ..." );
			int version = connection.getNextCoreVersion();
			DBEngineData.dropCoreData( this );
			closeConnection( true );
			trace( "successfully dropped engine core data, core version=" + version );
		}
		catch( Throwable e ) {
			log( "init" , e );
			closeConnection( false );
			trace( "unable to drop engine core data" );
			Common.exitUnexpected();
		}
	}

	private void dropAuthData() throws Exception {
		try {
			getConnection();
			
			trace( "drop auth data in database ..." );
			int version = connection.getNextLocalVersion();
			DBEngineData.dropAuthData( this );
			closeConnection( true );
			trace( "successfully dropped engine auth data, auth version=" + version );
		}
		catch( Throwable e ) {
			log( "init" , e );
			closeConnection( false );
			trace( "unable to drop engine auth data" );
			Common.exitUnexpected();
		}
	}

}
