package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.RunContext;
import org.urm.db.EngineDB;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.engine.Engine;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaMonitoring;
import org.urm.meta.product.MetaProductPolicy;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.MetaProductVersion;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaUnits;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderProducts {

	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_SETTINGS = "product";
	public static String XML_ROOT_POLICY = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DATABASE = "database";
	public static String XML_ROOT_SOURCES = "sources";
	public static String XML_ROOT_MONITORING = "monitoring";
	public static String XML_ROOT_ENV = "environment";
	public static String XML_ROOT_DESIGN = "design";

	private EngineLoader loader;
	private EngineData data;
	public RunContext execrc;
	public Engine engine;

	public EngineLoaderProducts( EngineLoader loader , EngineData data ) {
		this.loader = loader;
		this.data = data;
		this.execrc = loader.execrc;
		this.engine = loader.engine;
	}
	
	private boolean addProduct( ProductMeta set ) {
		EngineDirectory directory = loader.getDirectory();
		AppProduct product = directory.findProduct( set.name );
		
		if( !DBEngineDirectory.matchProduct( loader , directory , product , set , false ) ) {
			loader.trace( "match failed for product=" + product.NAME );
			set.meta.deleteObject();
			set.deleteObject();
			return( false );
		}
		
		EngineProducts products = data.getProducts();
		products.addProduct( set );
		return( true );
	}

	private ProductMeta loadProduct( String name , boolean importxml ) {
		EngineProducts products = data.getProducts();
		ProductMeta set = products.createPrimaryMeta( name );
		
		ActionBase action = loader.getAction();
		try {
			MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
			LocalFolder folder = storageMeta.getMetaFolder( action );
			if( folder.checkExists( action ) )
				loadAll( set , storageMeta , importxml );
			else
				Common.exitUnexpected();
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + name );
			set.meta.deleteObject();
			set.deleteObject();
			return( null );
		}
		
		return( set );
	}
	
	public void importProduct( String productName , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		engine.trace( "reload settings, product=" + productName + " ..." );
		
		EngineDB db = loader.getDatabase();
		db.clearProduct( productName );
		
		ProductMeta storageNew = loadProduct( productName , true );
		if( storageNew == null )
			return;

		synchronized( products ) {
			ProductMeta storage = products.findProductStorage( productName );
			if( storage != null )
				products.unloadProduct( storage );
			
			if( !addProduct( storageNew ) )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + productName , productName );
		}
	}
	
	public void saveProductMetadata( String productName ) throws Exception {
		ActionBase action = loader.getAction();
		EngineProducts products = data.getProducts();
		ProductMeta storage = products.findProductStorage( productName );
		if( storage == null )
			action.exitUnexpectedState();

		saveAll( storage );
	}
	
	public void loadProducts() {
		data.unloadProducts();
		EngineDirectory directory = loader.getDirectory();
		for( String name : directory.getAllProductNames( null ) ) {
			ProductMeta storage = loadProduct( name , false );
			if( storage != null )
				addProduct( storage );
		}
	}

	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product ) throws Exception {
		loader.log( msg ,  e );
		Common.exit1( error , msg , product );
	}
	
	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product , String item ) throws Exception {
		loader.log( msg ,  e );
		Common.exit2( error , msg , product , item );
	}
	
	public void loadAll( ProductMeta set , MetadataStorage storageMeta , boolean importxml ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			loadVersion( set , storageMeta );
			loadSettings( set , storageMeta );
			loadPolicy( set , storageMeta );
			loadUnits( set , storageMeta );
			loadDatabase( set , storageMeta );
			loadSources( set , storageMeta );
			loadDocs( set , storageMeta );
			loadDistr( set , storageMeta );
			loadMonitoring( set , storageMeta );
		
			for( String envFile : storageMeta.getEnvFiles( action ) )
				loadEnvData( set , storageMeta , envFile );
			for( String designFile : storageMeta.getDesignFiles( action ) )
				loadDesignData( set , storageMeta , designFile );
			
			loadReleases( set , importxml );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadVersion( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductVersion version = new MetaProductVersion( set , set.meta );
		set.setVersion( version );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getVersionConfFile( action );
			action.debug( "read product version file " + file + "..." );
			//Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			//Node root = doc.getDocumentElement();
			//version.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to load version metadata, product=" + set.name , set.name );
		}
	}

	public void loadSettings( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		EngineDirectory directory = loader.getDirectory();
		AppProduct product = directory.getProduct( set.name );
		
		MetaProductSettings settings = new MetaProductSettings( set , set.meta );
		set.setSettings( settings );

		ActionBase action = loader.getAction();
		try {
			ProductContext productContext = new ProductContext( set.meta , product );
			productContext.create( action , set.getVersion() );
			
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read product definition file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			settings.load( action , productContext , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSettings1 , e , "unable to load settings metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadPolicy( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductPolicy policy = new MetaProductPolicy( set , set.meta );
		set.setPolicy( policy );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getPolicyConfFile( action );
			action.debug( "read product policy file " + file + "..." );
			Document doc = ConfReader.readXmlFile( action.session.execrc , file );
			Node root = doc.getDocumentElement();
			policy.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductVersion1 , e , "unable to load version metadata, product=" + set.name , set.name );
		}
	}

	public void loadUnits( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		
		MetaUnits units = new MetaUnits( set , settings , set.meta );
		set.setUnits( units );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			Node node = ConfReader.xmlGetFirstChild( root , "units" );
			units.load( action , node );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductUnits1 , e , "unable to load units metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadDatabase( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		
		MetaDatabase database = new MetaDatabase( set , settings , set.meta );
		set.setDatabase( database );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDatabaseConfFile( action );
			action.debug( "read database definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			database.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDatabase1 , e , "unable to load database metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadSources( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaSource sources = new MetaSource( set , settings , set.meta );
		set.setSources( sources );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getSourcesConfFile( action );
			action.debug( "read source definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			sources.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductSources1 , e , "unable to load source metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadDocs( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaDocs docs = new MetaDocs( set , settings , set.meta );
		set.setDocs( docs );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getCoreConfFile( action );
			action.debug( "read units definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			Node node = ConfReader.xmlGetFirstChild( root , "documentation" );
			docs.load( action , node );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDocs1 , e , "unable to load documentation metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadDistr( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaDatabase db = set.getDatabase();
		MetaDocs docs = set.getDocs();
		MetaDistr distr = new MetaDistr( set , settings , set.meta );
		set.setDistr( distr );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getDistrConfFile( action );
			action.debug( "read distributive definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			distr.load( action , db , docs , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDistr1 , e , "unable to load distributive metadata, product=" + set.name , set.name );
		}
	}

	public void loadMonitoring( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaMonitoring mon = new MetaMonitoring( set , settings , set.meta );
		set.setMonitoring( mon );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getMonitoringConfFile( action );
			action.debug( "read monitoring definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			mon.load( action , root );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to load monitoring metadata, product=" + set.name , set.name );
		}
	}
	
	public void loadEnvData( ProductMeta set , MetadataStorage storageMeta , String envName ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaEnv env = new MetaEnv( set , settings , set.meta );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getEnvConfFile( action , envName );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			env.load( action , root );
			set.addEnv( env );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.name + ", env=" + envName , set.name , envName );
		}
	}
	
	public void loadDesignData( ProductMeta set , MetadataStorage storageMeta , String diagramName ) throws Exception {
		MetaDesignDiagram diagram = new MetaDesignDiagram( set , set.meta );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String filePath = storageMeta.getDesignFile( action , diagramName );
			action.debug( "read design definition file " + filePath + "..." );
			Document doc = action.readXmlFile( filePath );
			Node root = doc.getDocumentElement();
			diagram.load( action , root );
			set.addDiagram( diagram );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDiagram2 , e , "unable to load design metadata, product=" + set.name + ", diagram=" + diagramName , set.name , diagramName );
		}
	}

	public void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			DistRepository repo = DistRepository.loadDistRepository( action , set.meta , importxml );
			set.setReleases( repo );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.name , set.name );
		}
	}
	
	public void saveAll( ProductMeta set ) throws Exception {
		ActionBase action = loader.getAction();
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , set.meta );
		
		saveVersion( set , ms );
		saveProduct( set , ms );
		saveDatabase( set , ms );
		saveSources( set , ms );
		saveDistr( set , ms );
		saveMonitoring( set , ms );
		
		for( String envName : set.getEnvironmentNames() ) {
			MetaEnv env = set.findEnvironment( envName );
			saveEnvData( set , ms , env );
		}
		for( String diagramName : set.getDiagramNames() ) {
			MetaDesignDiagram diagram = set.findDiagram( diagramName );
			saveDesignData( set , ms , diagram );
		}
	}
	
	public void saveVersion( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_VERSION );
		//MetaProductVersion version = set.getVersion();
		//version.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveVersionConfFile( action , doc );
	}
	
	public void saveProduct( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_SETTINGS );
		Element root = doc.getDocumentElement();
		MetaProductSettings settings = set.getSettings();
		settings.save( action , doc , root );
		
		Element node = Common.xmlCreateElement( doc , root , "units" );
		MetaUnits units = set.getUnits();
		units.save( action , doc , node );
		
		node = Common.xmlCreateElement( doc , root , "documentation" );
		MetaDocs docs = set.getDocs();
		docs.save( action , doc , node );
		
		storageMeta.saveCoreConfFile( action , doc );
	}
	
	public void saveDatabase( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DATABASE );
		MetaDatabase database = set.getDatabase();
		database.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDatabaseConfFile( action , doc );
	}
	
	public void saveDistr( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DISTR );
		MetaDistr distr = set.getDistr();
		distr.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveDistrConfFile( action , doc );
	}
	
	public void saveSources( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_SOURCES );
		MetaSource sources = set.getSources();
		sources.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveSourcesConfFile( action , doc );
	}
	
	public void saveMonitoring( ProductMeta set , MetadataStorage storageMeta ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		MetaMonitoring mon = set.getMonitoring();
		mon.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveMonitoringConfFile( action , doc );
	}
	
	public void saveEnvData( ProductMeta set , MetadataStorage storageMeta , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		String envFile = env.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
	public void saveDesignData( ProductMeta set , MetadataStorage storageMeta , MetaDesignDiagram diagram ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DESIGN );
		diagram.save( action , doc , doc.getDocumentElement() );
		String diagramFile = diagram.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , diagramFile );
	}
	
}
