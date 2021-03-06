package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBNames;
import org.urm.db.engine.DBEngineData;
import org.urm.db.system.DBSystemData;
import org.urm.db.upgrade.DBUpgrade;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineCore;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.run.EngineMethod;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.ProductStorage;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;

public class EngineLoader {

	public Engine engine;
	private DataService data;
	public RunContext execrc;
	
	private EngineEntities entities;
	private EngineMatcher matcher;
	private DBConnection connection;
	private TransactionBase transaction;
	private EngineMethod method;
	private ActionBase action;

	private EngineLoaderCore ldc;
	private EngineLoaderSystems lds;
	private EngineLoaderProducts ldp;
	
	public EngineLoader( Engine engine , DataService data , ActionBase action ) {
		create( engine , data , action );
	}

	public EngineLoader( Engine engine , DataService data , EngineMethod method , ActionBase action ) {
		create( engine , data , action );
		this.method = method;
	}

	public EngineLoader( Engine engine , DataService data , TransactionBase transaction ) {
		create( engine , data , transaction.action );
		this.transaction = transaction;
	}

	private void create( Engine engine , DataService data , ActionBase action ) {
		this.engine = engine;
		this.data = data;
		this.action = action;
		this.execrc = engine.execrc;
		this.entities = data.getEntities();
		
		ldc = new EngineLoaderCore( this , data );
		lds = new EngineLoaderSystems( this , data );
		ldp = new EngineLoaderProducts( this , data );
	}
	
	public TransactionBase getTransaction() {
		return( transaction );
	}
	
	public EngineMethod getMethod() {
		return( method );
	}
	
	public AuthService getAuth() {
		return( engine.getAuth() );
	}
	
	public EngineEntities getEntities() {
		return( entities );
	}
	
	public EngineDB getDatabase() {
		return( data.getDatabase() );
	}
	
	public EngineSettings getSettings() {
		return( ldc.getSettings() );
	}

	public EngineResources getResources() {
		return( ldc.getResources() );
	}

	public EngineBuilders getBuilders() {
		return( ldc.getBuilders() );
	}

	public EngineDirectory getDirectory() {
		return( lds.getDirectory() );
	}

	public EngineMirrors getMirrors() {
		return( ldc.getMirrors() );
	}
	
	public EngineInfrastructure getInfrastructure() {
		return( ldc.getInfrastructure() );
	}

	public EngineBase getBase() {
		return( ldc.getBase() );
	}

	public EngineMonitoring getMonitoring() {
		return( ldc.getMonitoring() );
	}

	public EngineLifecycles getLifecycles() {
		return( ldc.getLifecycles() );
	}

	public EngineMatcher getMatcher() {
		return( matcher );
	}
	
	public DBConnection getConnection() throws Exception {
		if( connection == null ) {
			if( transaction != null )
				connection = transaction.getConnection();
			else
			if( method != null )
				connection = method.getMethodConnection( action );
			else {
				EngineDB db = data.getDatabase();
				connection = db.getConnection( action );
			}
		}
			
		return( connection );
	}
	
	public DBConnection startLoad() throws Exception {
		getConnection();
		matcher = new EngineMatcher( this );
		entities = data.getEntities();
		return( connection );
	}
	
	public void closeConnection( boolean commit ) throws Exception {
		if( commit ) {
			ldc.setData();
			lds.setData();
		}

		if( connection != null ) {
			if( transaction == null )
				connection.close( commit );
			else
				connection.save( commit );
			connection = null;
		}
	}

	public void saveConnection( boolean commit ) throws Exception {
		if( commit ) {
			ldc.setData();
			lds.setData();
		}

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
		AppProduct product = action.findProduct( productName );
		ProductStorage storageMeta = action.artefactory.getMetadataStorage( action , product );
		LocalFolder folder = storageMeta.getHomeFolder( action );
		return( folder );
	}

	public static boolean getVersionUpgradeState() {
		return( Common.getBooleanValue( System.getProperty( "autoupgrade" ) ) );
	}

	public static boolean getInitialUpdateState() {
		return( Common.getBooleanValue( System.getProperty( "dbupdate" ) ) );
	}

	public static int getOverridenDatabaseVersion() {
		String version = System.getProperty( "dbversion" );
		if( version == null || version.isEmpty() )
			return( -1 );
		return( Integer.parseInt( version ) );
	}

	public static void setInitialUpdateState() {
		System.setProperty( "dbupdate" , Common.getBooleanValue( true ) );
	}
	
	public void initMeta() throws Exception {
		try {
			boolean dbUpdate = getInitialUpdateState();
			if( dbUpdate )
				recreateDatabase();
			
			trace( "init, checking engine/database consistency ..." );
			trace( "load names ..." );
			DBNames.loaddb( this );
			
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

	private void recreateDatabase() throws Exception {
		trace( "recreate database ..." );
		DBUpgrade.applyScripts( this , "database/create" );
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
		int version = getOverridenDatabaseVersion();
		if( version < 0 )
			version = connection.getCurrentAppVersion();
		
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
			startLoad();
			
			if( repo.isServer() ) {
				importEngine();
				saveConnection( true );
				loadProducts( true );
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
		if( repo.isServer() ) {
			ldc.exportxmlEngine();
			lds.exportxmlDirectory();
		}
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
	
	public void initAuth( AuthService auth ) throws Exception {
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

	public ProductMeta createProduct( AppProduct product , boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		trace( "create engine product=" + product.NAME + " data ..." );
		return( ldp.createProductMetadata( product , forceClearMeta , forceClearDist ) );
	}
	
	public ProductMeta createProductRevision( AppProduct product , String name , boolean forceClearMeta ) throws Exception {
		trace( "create engine product=" + product.NAME + " revision=" + name + " ..." );
		return( ldp.createProductRevision( product , name , forceClearMeta ) );
	}
	
	public ProductMeta copyProductRevision( AppProduct product , String name , ProductMeta src ) throws Exception {
		trace( "copy engine product=" + product.NAME + " revision=" + src.NAME + " to revision=" + name + " ..." );
		return( ldp.copyProductRevision( product , name , src ) );
	}
	
	private void exportProduct( Integer productId ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		AppProduct product = directory.getProduct( productId );
		
		trace( "export engine product=" + product.NAME + " data ..." );
		ldp.exportProductMetadata( product );
	}

	public void importProduct( Integer productId , boolean includingEnvironments ) throws Exception {
		EngineDirectory directory = data.getDirectory();
		AppProduct product = directory.getProduct( productId );
		
		if( !transaction.requestImportProduct( product ) )
			Common.exitUnexpected();
		
		trace( "import engine product=" + product.NAME + " data ..." );
		ldp.importProduct( product , EngineProductRevisions.REVISION_INITIAL , includingEnvironments );
	}
	
	public void loadProducts( boolean update ) throws Exception {
		try {
			startLoad();

			trace( "load engine products data ..." );
			ldp.loadProducts( update );

			closeConnection( true );
		}
		catch( Throwable e ) {
			log( "init" , e );
			trace( "unable to load products data" );
			
			closeConnection( false );
			Common.exitUnexpected();
		}
	}

	public void setSettings( EngineSettings settingsNew ) throws Exception {
		trace( "change engine settings data ..." );
		EngineSettings settings = data.getEngineSettings();
		getConnection();
		settings.setData( action , settingsNew , connection.getCoreVersion() );
	}

	public void loadCore( boolean importxml , boolean withSystems ) throws Exception {
		try {
			startLoad();

			// core
			if( importxml )
				ldc.importxmlCore();
			else
				ldc.loaddbCore();
				
			// systems
			if( importxml ) {
				if( withSystems ) {
					lds.importxmlDirectory();
					saveConnection( true );
					trace( "successfully completed import of engine directory data" );
				}
				else
					lds.loaddbDirectory();
			}
			else
				lds.loaddbDirectory();
			
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

	public void loadAuth( AuthService auth , boolean importxml ) throws Exception {
		try {
			startLoad();

			// core
			if( importxml ) {
				int version = connection.getNextLocalVersion();
				trace( "create new engine auth version=" + version + " ..." );
				ldc.importxmlAuth( auth );
				saveConnection( true );
				trace( "successfully completed import of engine auth data" );
			}
			else {
				trace( "load engine auth data, version=" + connection.getLocalVersion() + " ..." );
				ldc.loaddbAuth( auth );
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

	public void importAuth( AuthService auth ) throws Exception {
		trace( "cleanup auth data ..." );
		auth.unloadAll();
		dropAuthData();
		loadAuth( auth , true );
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

	public void dropAuthData() throws Exception {
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

	public void importEngine() throws Exception {
		importCore( true );
		importAuth( engine.getAuth() );
	}
	
	public void importCore( boolean includingSystems ) throws Exception {
		trace( "cleanup engine data ..." );
		data.unloadAll();
		dropAuthData();
		dropCoreData( includingSystems );
		loadCore( true , includingSystems );
	}

	public void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product ) throws Exception {
		log( msg ,  e );
		Common.exit1( error , msg , product );
	}
	
	public void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product , String item ) throws Exception {
		log( msg ,  e );
		Common.exit2( error , msg , product , item );
	}
	
}
