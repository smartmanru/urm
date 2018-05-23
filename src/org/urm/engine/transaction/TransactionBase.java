package org.urm.engine.transaction;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.DBEnumCompItemType;
import org.urm.db.core.DBEnums.DBEnumParamEntityType;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.DataService;
import org.urm.engine._Error;
import org.urm.engine.AuthService.SpecialRights;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineBase;
import org.urm.engine.data.EngineBuilders;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineInfrastructure;
import org.urm.engine.data.EngineLifecycles;
import org.urm.engine.data.EngineMirrors;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.data.EngineResources;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.data.EngineEntities;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.engine.security.AuthResource;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.Network;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvDeployGroup;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerDeployment;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.MetaEnvStartGroup;
import org.urm.meta.env.MetaEnvStartInfo;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.loader.EngineObject;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaDocs;
import org.urm.meta.product.MetaProductDoc;
import org.urm.meta.product.MetaProductUnit;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaUnits;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;
import org.urm.meta.system.AppSystem;
import org.urm.meta.system.AppProductMonitoring;
import org.urm.meta.system.AppProductMonitoringItem;
import org.urm.meta.system.AppProductMonitoringTarget;
import org.urm.meta.system.ProductDump;
import org.urm.meta.system.ProductDumpMask;

public class TransactionBase extends EngineObject {

	public Engine engine;
	public ActionInit action;
	public RunError error;
	private DataService data;
	
	private DBConnection connection;
	private boolean USEDATABASE;
	private boolean IMPORT;
	private boolean CHANGEDATABASECORE;
	private boolean CHANGEDATABASEAUTH;
	
	// changed without copy
	protected AuthService authChange;
	protected EngineInfrastructure infraChange;
	protected EngineBase baseChange;
	protected EngineLifecycles lifecyclesChange;
	
	// changed as full copy
	protected EngineSettings settingsNew;
	protected EngineResources resourcesNew;
	protected EngineBuilders buildersNew;
	protected EngineDirectory directoryNew;
	protected EngineMirrors mirrorsNew;
	
	protected PropertyEntity entityNew;

	private EngineSettings settingsOld;
	private EngineResources resourcesOld;
	private EngineBuilders buildersOld;
	private EngineDirectory directoryOld;
	private EngineMirrors mirrorsOld;
	
	private Map<String,TransactionProduct> productChanges;
	
	public TransactionBase( Engine engine , DataService data , ActionInit action ) {
		super( null );
		this.engine = engine;
		this.data = data;
		this.action = action;
		
		USEDATABASE = false;
		IMPORT = false;
		CHANGEDATABASECORE = false;
		CHANGEDATABASEAUTH = false;
		
		productChanges = new HashMap<String,TransactionProduct>();
		engine.trace( "transaction created id=" + objectId );
	}
	
	@Override
	public String getName() {
		return( "server-transaction" );
	}
	
	public DBConnection getConnection() {
		return( connection );
	}
	
	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( this ) )
				return( false );
		
			action.setTransaction( this );
			return( true );
		}
	}

	public boolean commitTransaction() {
		synchronized( engine ) {
			if( !continueTransaction() )
				return( false );

			boolean res = true;
			if( res )
				res = commitTransactionProducts();
			
			if( res ) {
				if( connection != null ) {
					connection.close( true );
					connection = null;
				}
				
				if( !engine.commitTransaction( this ) )
					return( false );
				
				action.clearTransaction();
				
				if( entityNew != null ) {
					data.updateEntity( entityNew );
				}
				
				if( settingsNew != null ) {
					data.setSettings( settingsNew );
					settingsOld.deleteObject();
					settingsOld = null;
				}
				
				if( resourcesNew != null ) {
					data.setResources( resourcesNew );
					resourcesOld.deleteObject();
					resourcesOld = null;
				}
				
				if( buildersNew != null ) {
					data.setBuilders( buildersNew );
					buildersOld.deleteObject();
					buildersOld = null;
				}
				
				if( directoryNew != null ) {
					data.setDirectory( directoryNew );
					directoryOld.deleteObject();
					directoryOld = null;
				}
				
				if( mirrorsNew != null ) {
					data.setMirrors( mirrorsNew );
					if( mirrorsOld != null ) {
						engine.trace( "remove old mirrors object, id=" + mirrorsOld.objectId );
						mirrorsOld.deleteObject();
						mirrorsOld = null;
					}
				}
				
				return( true );
			}

			abortTransaction( true );
			return( false );
		}
	}
	
	public void abortTransaction( boolean save ) {
		synchronized( engine ) {
			if( !continueTransaction() )
				return;
			
			try {
				if( connection != null ) {
					connection.close( false );
					connection = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to rollback database changes" );
			}
			
			authChange = null;
			infraChange = null;
			lifecyclesChange = null;
			baseChange = null;
			
			if( settingsNew != null ) {
				settingsNew.deleteObject();
				settingsNew = null;
			}
			
			if( resourcesNew != null ) {
				resourcesNew.deleteObject();
				resourcesNew = null;
			}
			
			if( buildersNew != null ) {
				buildersNew.deleteObject();
				buildersNew = null;
			}
			
			if( directoryNew != null ) {
				directoryNew.deleteObject();
				directoryNew = null;
			}
			
			if( mirrorsNew != null ) {
				engine.trace( "remove new mirrors object, id=" + mirrorsNew.objectId );
				mirrorsNew.deleteObject();
				mirrorsNew = null;
			}

			abortProducts( save );
			
			engine.abortTransaction( this );
		}
		
		action.clearTransaction();
	}

	private void abortProducts( boolean save ) {
		try {
			for( TransactionProduct meta : productChanges.values() )
				meta.abortTransaction( save );
			productChanges.clear();
		}
		catch( Throwable e ) {
			handle( e , "unable to restore metadata" );
		}
	}
	
	public ActionInit getAction() {
		return( action );
	}
	
	public boolean continueTransaction() {
		if( engine.getTransaction() != this )
			return( false );
		
		return( true );
	}

	public void exitUnexpectedState() throws Exception {
		action.exitUnexpectedState();
	}
	
	public void exit( int errorCode , String msg , String params[] ) throws Exception {
		action.exit( errorCode , msg , params );
	}

	public void exit0( int errorCode , String msg ) throws Exception {
		action.exit( errorCode , msg , null );
	}

	public void exit1( int errorCode , String msg , String param1 ) throws Exception {
		action.exit( errorCode , msg , new String[] { param1 } );
	}

	public void exit2( int errorCode , String msg , String param1 , String param2 ) throws Exception {
		action.exit( errorCode , msg , new String[] { param1 , param2 } );
	}

	public void exit3( int errorCode , String msg , String param1 , String param2 , String param3 ) throws Exception {
		action.exit( errorCode , msg , new String[] { param1 , param2 , param3 } );
	}

	public void exit4( int errorCode , String msg , String param1 , String param2 , String param3 , String param4 ) throws Exception {
		action.exit( errorCode , msg , new String[] { param1 , param2 , param3 , param4 } );
	}

	public void fail( int errorCode , String msg , String params[] ) {
		error( msg );
		error = new RunError( errorCode , msg , params );
	}

	public void fail0( int errorCode , String msg ) {
		fail( errorCode , msg , null );
	}

	public void fail1( int errorCode , String msg , String param1 ) {
		fail( errorCode , msg , new String[] { param1 } );
	}

	public void fail2( int errorCode , String msg , String param1 , String param2 ) {
		fail( errorCode , msg , new String[] { param1 , param2 } );
	}

	public void fail3( int errorCode , String msg , String param1 , String param2 , String param3 ) {
		fail( errorCode , msg , new String[] { param1 , param2 , param3 } );
	}

	public void fail4( int errorCode , String msg , String param1 , String param2 , String param3 , String param4 ) {
		fail( errorCode , msg , new String[] { param1 , param2 , param3 , param4 } );
	}

	public void handle( Throwable e , String s ) {
		if( action != null )
			action.log( s , e );
		if( e.getClass() == RunError.class )
			error = ( RunError )e;
		else
			error = new RunError( 0 , s , new String[0] );
	}
	
	public void handle0( Throwable e , int errorCode , String msg ) throws RunError {
		error = new RunError( e , errorCode , msg , null );
		throw error;
	}
	
	public void handle1( Throwable e , int errorCode , String msg , String param1 ) throws RunError {
		error = new RunError( e , errorCode , msg , new String[] { param1 } );
		throw error;
	}
	
	public void handle2( Throwable e , int errorCode , String msg , String param1 , String param2 ) throws RunError {
		error = new RunError( e , errorCode , msg , new String[] { param1 , param2 } );
		throw error;
	}
	
	public void handle3( Throwable e , int errorCode , String msg , String param1 , String param2 , String param3 ) throws RunError {
		error = new RunError( errorCode , msg , new String[] { param1 , param2 , param3 } );
		throw error;
	}
	
	public void handle4( Throwable e , int errorCode , String msg , String param1 , String param2 , String param3 , String param4 ) throws RunError {
		error = new RunError( errorCode , msg , new String[] { param1 , param2 , param3 , param4 } );
		throw error;
	}
	
	public RunError getError() {
		return( error );
	}
	
	public void info( String s ) {
		if( action != null )
			action.info( s );
		else
			engine.info( s );
	}
	
	public void debug( String s ) {
		if( action != null )
			action.debug( s );
		else
			engine.debug( s );
	}
	
	public void error( String s ) {
		if( action != null )
			action.error( s );
		else
			engine.error( s );
	}
	
	public void trace( String s ) {
		action.trace( s );
	}
	
	public void useDatabase() throws Exception {
		if( USEDATABASE )
			return;
		
		USEDATABASE = true;
		EngineDB db = data.getDatabase();
		connection = db.getConnection( action );
	}
	
	private void changeDatabaseCore() throws Exception {
		useDatabase();
		if( CHANGEDATABASECORE )
			return;
		
		CHANGEDATABASECORE = true;
		int version = connection.getNextCoreVersion();
		trace( "core update, new version=" + version );
	}

	private void changeDatabaseAuth() throws Exception {
		useDatabase();
		if( CHANGEDATABASEAUTH )
			return;
		
		CHANGEDATABASEAUTH = true;
		int version = connection.getNextLocalVersion();
		trace( "auth update, new version=" + version );
	}
	
	public boolean startImport( AppProduct product ) throws Exception {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( IMPORT )
					return( true );

				if( product == null ) {
					if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
						return( false );
				}
				else {
					if( !checkSecurityProductChange( product , null , SecurityAction.ACTION_ADMIN ) )
						return( false );
				}
					
				
				useDatabase();
				IMPORT = true;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to start import" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean changeAuth( AuthService sourceAuth ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( authChange != null )
					return( true );

				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				changeDatabaseAuth();
				authChange = sourceAuth;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change infrastructure" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineInfrastructure changeInfrastructure( Network network ) throws Exception {
		if( authChange == null ) {
			if( !changeInfrastructure( getInfrastructure() , network ) )
				exitUnexpectedState();
		}
		return( infraChange );
	}
	
	public boolean changeInfrastructure( EngineInfrastructure sourceInfrastructure , Network network ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( infraChange != null )
					return( true );

				if( network == null ) {
					if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
						return( false );
				}
				else {
					if( !checkSecurityInfrastructureChange( network ) )
						return( false );
				}
				
				changeDatabaseCore();
				infraChange = sourceInfrastructure;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change infrastructure" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineLifecycles changeReleaseLifecycles() throws Exception {
		if( lifecyclesChange == null ) {
			if( !changeReleaseLifecycles( getLifecycles() ) )
				exitUnexpectedState();
		}
		return( lifecyclesChange );
	}
	
	public boolean changeReleaseLifecycles( EngineLifecycles sourceLifecycles ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( lifecyclesChange != null )
					return( true );

				if( !checkSecurityServerChange( SecurityAction.ACTION_RELEASE ) )
					return( false );
				
				changeDatabaseCore();
				lifecyclesChange = sourceLifecycles;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change release lifecycles" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineBase changeBase( SpecialRights sr ) throws Exception {
		if( baseChange == null ) {
			if( !changeBase( getEngineBase() , sr ) )
				exitUnexpectedState();
		}
		return( baseChange );
	}
	
	public boolean changeBase( EngineBase sourceBase , SpecialRights sr ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( baseChange != null )
					return( true );
				
				if( !checkSecuritySpecial( sr ) )
					return( false );
				
				changeDatabaseCore();
				baseChange = sourceBase;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change base" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineResources changeResources() throws Exception {
		if( resourcesNew == null ) {
			if( !changeResources( getResources() ) )
				exitUnexpectedState();
		}
		return( resourcesNew );
	}
	
	public boolean changeResources( EngineResources sourceResources ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( resourcesNew != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change resources, server is stopped" );
				else {
					resourcesOld = action.getActiveResources();
					if( sourceResources == resourcesOld ) {
						changeDatabaseCore();
						resourcesNew = sourceResources.copy();
						if( resourcesNew != null ) {
							trace( "transaction resources: source=" + sourceResources.objectId + ", copy=" + resourcesNew.objectId );
							return( true );
						}
					}
					else
						error( "unable to change old resources, id=" + sourceResources.objectId );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change resources" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineBuilders changeBuilders() throws Exception {
		if( buildersNew == null ) {
			if( !changeBuilders( getBuilders() ) )
				exitUnexpectedState();
		}
		return( buildersNew );
	}
	
	public boolean changeBuilders( EngineBuilders sourceBuilders ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( buildersNew != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_RELEASE ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change builders, server is stopped" );
				else {
					buildersOld = action.getActiveBuilders();
					if( sourceBuilders == buildersOld ) {
						changeDatabaseCore();
						buildersNew = sourceBuilders.copy();
						if( buildersNew != null ) {
							trace( "transaction builders: source=" + sourceBuilders.objectId + ", copy=" + buildersNew.objectId );
							return( true );
						}
					}
					else
						error( "unable to change old builders, id=" + sourceBuilders.objectId ); 
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change builders" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineDirectory changeDirectory( boolean critical ) throws Exception {
		if( directoryNew == null ) {
			if( !changeDirectory( getDirectory() , critical ) )
				exitUnexpectedState();
		}
		return( directoryNew );
	}
	
	public boolean changeDirectory( EngineDirectory sourceDirectory , boolean critical ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( directoryNew != null )
					return( true );
				
				SecurityAction mode = ( critical )? SecurityAction.ACTION_ADMIN : SecurityAction.ACTION_CONFIGURE;
				if( !checkSecurityServerChange( mode ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change directory, server is stopped" );
				else {
					directoryOld = action.getActiveDirectory();
					if( sourceDirectory == directoryOld ) {
						useDatabase();
						directoryNew = sourceDirectory.copy( this );
						if( directoryNew != null ) {
							trace( "transaction directory: source=" + sourceDirectory.objectId + ", copy=" + directoryNew.objectId );
							return( true );
						}
					}
					else
						error( "unable to change old directory, id=" + sourceDirectory.objectId );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change directory" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineMirrors changeMirrors() throws Exception {
		if( mirrorsNew == null ) {
			if( !changeMirrors( getMirrors() ) )
				exitUnexpectedState();
		}
		return( mirrorsNew );
	}
	
	public void setMirrors( EngineMirrors mirrors ) {
		if( mirrorsNew != null )
			mirrorsNew.deleteObject();
		mirrorsNew = mirrors;
	}
	
	public boolean changeMirrors( EngineMirrors sourceMirrors ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( mirrorsNew != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				mirrorsOld = action.getActiveMirrors();
				if( sourceMirrors == mirrorsOld ) {
					changeDatabaseCore();
					mirrorsNew = sourceMirrors.copy();
					if( mirrorsNew != null ) {
						trace( "transaction mirrors: source=" + sourceMirrors.objectId + ", copy=" + mirrorsNew.objectId );
						return( true );
					}
				}
				else
					error( "unable to change old mirrors, id=" + sourceMirrors.objectId );
			}
			catch( Throwable e ) {
				handle( e , "unable to change directory" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public EngineMonitoring changeMonitoring() throws Exception {
		EngineMonitoring monitoring = getMonitoring();
		if( !USEDATABASE ) {
			if( !changeMonitoring( monitoring ) )
				exitUnexpectedState();
		}
		return( monitoring );
	}
	
	public boolean changeMonitoring( EngineMonitoring sourceMonitoring ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_MONITOR ) )
					return( false );
				
				useDatabase();
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change settings" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public EngineSettings changeSettings() throws Exception {
		if( settingsNew == null ) {
			if( !changeSettings( getSettings() ) )
				exitUnexpectedState();
		}
		return( settingsNew );
	}
	
	public boolean changeSettings( EngineSettings sourceSettings ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( settingsNew != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				settingsOld = action.getActiveServerSettings();
				if( sourceSettings == settingsOld ) {
					changeDatabaseCore();
					settingsNew = sourceSettings.copy();
					trace( "transaction server settings: source=" + sourceSettings.objectId + ", copy=" + settingsNew.objectId );
					if( settingsNew != null )
						return( true );
				}
				else
					error( "unable to change old settings, id=" + sourceSettings.objectId );
			}
			catch( Throwable e ) {
				handle( e , "unable to change settings" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	public TransactionProduct findProductTransaction( String productName ) {
		return( productChanges.get( productName ) );
	}

	public void createProduct( AppProduct product ) throws Exception {
		EngineProduct ep = product.getEngineProduct();
		TransactionProduct tm = createProductTransaction( product , ep );
		tm.createProduct();
	}
	
	public Meta createProductMetadata( AppProduct product , ProductMeta storage ) throws Exception {
		EngineProduct ep = storage.getEngineProduct();
		TransactionProduct tm = createProductTransaction( product , ep );
		return( tm.createProductMetadata( storage ) );
	}

	private TransactionProduct createProductTransaction( AppProduct product , EngineProduct ep ) {
		TransactionProduct tm = findProductTransaction( ep.productName );
		if( tm == null ) {
			tm = new TransactionProduct( this , product , ep );
			productChanges.put( tm.ep.productName , tm );
		}
		return( tm );
	}
	
	public boolean requestImportProduct( AppProduct product ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				if( !checkSecurityProductChange( product , null , SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				TransactionProduct tm = createProductTransaction( product , product.findEngineProduct() );
				if( tm.importProduct() ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestImportEnv( MetaEnv env ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				EngineProduct ep = env.getEngineProduct();
				if( !checkSecurityProductChange( ep.getProduct() , env , SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				TransactionProduct tm = createProductTransaction( ep.getProduct() , ep );
				if( tm.importEnv( env ) )
					return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestRecreateMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				EngineProduct ep = meta.getEngineProduct();
				if( !checkSecurityProductChange( ep.getProduct() , null , SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				TransactionProduct tm = createProductTransaction( ep.getProduct() , ep );
				if( !tm.recreateMetadata( meta ) ) {
					useDatabase();
					return( true );
				}
				
			}
			catch( Throwable e ) {
				handle( e , "unable to recreate metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestDeleteMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				EngineProduct ep = meta.getEngineProduct();
				TransactionProduct tm = createProductTransaction( ep.getProduct() , ep );
				
				if( tm.deleteMetadata( meta ) ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to delete metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestDeleteProduct( AppProduct product ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				if( !checkSecurityProductChange( product , null , SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				EngineProduct ep = product.getEngineProduct();
				TransactionProduct tm = createProductTransaction( product , ep );
				
				if( tm.deleteProduct() ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestChangeProduct( AppProduct product ) throws Exception {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				if( !checkSecurityProductChange( product , null , SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				EngineProduct ep = product.getEngineProduct();
				TransactionProduct tm = createProductTransaction( product , ep );
				if( tm.changeProduct() ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change product" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestChangeMetadata( Meta meta , boolean draft ) {
		synchronized( engine ) {
			try {
				if( draft != meta.isDraft() )
					Common.exitUnexpected();
					
				if( !continueTransaction() )
					return( false );

				EngineProduct ep = meta.getEngineProduct();
				if( !checkSecurityProductChange( ep.getProduct() , null , SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				TransactionProduct tm = createProductTransaction( ep.getProduct() , ep ); 
				if( tm.changeMetadata( meta ) ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean requestChangeEnv( MetaEnv env ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				EngineProduct ep = env.getEngineProduct();
				if( !checkSecurityProductChange( ep.getProduct() , env , SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				TransactionProduct tm = createProductTransaction( ep.getProduct() , ep );
				if( tm.checkChangeEnv( env ) )
					return( true );
				
				if( tm.changeEnv( env ) ) {
					useDatabase();
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to change environment" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	private boolean commitTransactionProducts() {
		if( !continueTransaction() )
			return( false );

		try {
			boolean failed = false;
			for( TransactionProduct tm : productChanges.values() ) {
				if( !tm.commitTransaction() ) {
					failed = true;
					break;
				}
			}

			if( !failed )
				return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save metadata" );
		}

		abortTransaction( true );
		return( false );
	}
	
	protected void checkTransaction() throws Exception {
		if( !continueTransaction() )
			exit( _Error.TransactionAborted0 , "Transaction is aborted" , null );
	}

	protected void checkTransactionImport() throws Exception {
		checkTransaction();
		if( !IMPORT )
			exit( _Error.TransactionMissingImportChanges0 , "Missing import changes" , null );
	}

	protected void checkTransactionAuth() throws Exception {
		checkTransaction();
		if( authChange == null )
			exit( _Error.TransactionMissingAuthChanges0 , "Missing auth changes" , null );
	}

	protected void checkTransactionInfrastructure() throws Exception {
		checkTransaction();
		if( infraChange == null )
			exit( _Error.TransactionMissingInfrastructureChanges0 , "Missing infrastructure changes" , null );
	}

	protected void checkTransactionReleaseLifecycles() throws Exception {
		checkTransaction();
		if( lifecyclesChange == null )
			exit( _Error.TransactionMissingReleaseLifecyclesChanges0 , "Missing release lifecycles changes" , null );
	}

	protected void checkTransactionBase() throws Exception {
		checkTransaction();
		if( baseChange == null )
			exit( _Error.TransactionMissingBaseChanges0 , "Missing base changes" , null );
	}

	protected void checkTransactionResources( EngineResources sourceResources ) throws Exception {
		checkTransaction();
		if( resourcesNew == null || resourcesNew != sourceResources )
			exit( _Error.TransactionMissingResourceChanges0 , "Missing resources changes" , null );
	}

	protected void checkTransactionBuilders( EngineBuilders builders ) throws Exception {
		checkTransaction();
		if( buildersNew == null || buildersNew != builders )
			exit( _Error.TransactionMissingBuildersChanges0 , "Missing builders changes" , null );
	}

	protected void checkTransactionDirectory( EngineDirectory directory ) throws Exception {
		checkTransaction();
		if( directoryNew == null || directoryNew != directory )
			exit( _Error.TransactionMissingDirectoryChanges0 , "Missing directory changes" , null );
	}

	protected void checkTransactionMirrors( EngineMirrors sourceMirrors ) throws Exception {
		checkTransaction();
		changeDatabaseCore();
		if( sourceMirrors == null || mirrorsNew != sourceMirrors )
			exit( _Error.TransactionMissingMirrorsChanges0 , "Missing mirrors changes" , null );
	}

	protected void checkTransactionSettings() throws Exception {
		checkTransaction();
		if( settingsNew == null )
			exit( _Error.TransactionMissingSettingsChanges0 , "Missing settings changes" , null );
	}

	protected void checkTransactionMonitoring() throws Exception {
		checkTransaction();
	}

	protected void checkTransactionMetadata( ProductMeta sourceMeta ) throws Exception {
		checkTransaction();
		TransactionProduct tm = findProductTransaction( sourceMeta.ep.productName );
		if( tm == null || !tm.checkTransactionMetadata( sourceMeta ) )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
	}

	protected void checkTransactionEnv( MetaEnv env ) throws Exception {
		checkTransaction();
		TransactionProduct tm = findProductTransaction( env.meta.name );
		if( tm == null || !tm.checkTransactionEnv( env ) )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
	}

	protected void checkTransactionProduct( AppProduct product ) throws Exception {
		checkTransaction();
		TransactionProduct tm = findProductTransaction( product.NAME );
		if( tm == null || !tm.checkTransactionProduct( product ) )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing product changes" , null );
	}

	protected void checkTransactionCustomProperty( ObjectProperties ops , boolean secured ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		
		AuthService auth = getAuth();
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.RC_CUSTOM || 
			entity.PARAMENTITY_TYPE == DBEnumParamEntityType.ENGINE_CUSTOM ) {
			checkTransactionSettings();
			if( secured )
				auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.BASEITEM_CUSTOM ) {
			checkTransactionBase();
			if( secured )
				auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.SYSTEM_CUSTOM ) {
			checkTransactionDirectory( directoryNew );
			if( secured )
				auth.verifyAccessServerAction( action , SecurityAction.ACTION_SECURED , false );
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.PRODUCT_CUSTOM ) {
			Meta productMeta = getTransactionMetadata( ops.ownerId );
			if( productMeta == null )
				exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
			if( secured )
				auth.verifyAccessProductAction( action , SecurityAction.ACTION_SECURED , productMeta , false );
		}
		else
			exitUnexpectedState();
	}

	public EngineProducts getProducts() {
		return( data.getProducts() );
	}
	
	public EngineEntities getEntities() {
		return( data.getEntities() );
	}
	
	public EngineResources getTransactionResources() {
		return( resourcesNew );
	}
	
	public EngineResources getResources() {
		if( resourcesNew != null )
			return( resourcesNew );
		return( data.getResources() );
	}
	
	public EngineBuilders getTransactionBuilders() {
		return( buildersNew );
	}
	
	public EngineBuilders getBuilders() {
		if( buildersNew != null )
			return( buildersNew );
		return( data.getBuilders() );
	}
	
	public EngineLifecycles getTransactionLifecycles() {
		return( lifecyclesChange );
	}
	
	public EngineLifecycles getLifecycles() {
		if( lifecyclesChange != null )
			return( lifecyclesChange );
		return( data.getReleaseLifecycles() );
	}
	
	public EngineDirectory getTransactionDirectory() {
		return( directoryNew );
	}
	
	public EngineDirectory getDirectory() {
		if( directoryNew != null )
			return( directoryNew );
		return( data.getDirectory() );
	}

	public EngineDirectory getOldDirectory() {
		return( data.getDirectory() );
	}
	
	public EngineMirrors getTransactionMirrors() {
		return( mirrorsNew );
	}
	
	public AuthService getAuth() {
		if( authChange != null )
			return( authChange );
		return( engine.getAuth() );
	}
	
	public EngineMirrors getMirrors() {
		if( mirrorsNew != null )
			return( mirrorsNew );
		return( data.getMirrors() );
	}
	
	public EngineSettings getTransactionSettings() {
		return( settingsNew );
	}
	
	public EngineMonitoring getMonitoring() {
		return( data.getMonitoring() );
	}
	
	public EngineSettings getSettings() {
		if( settingsNew != null )
			return( settingsNew );
		return( data.getEngineSettings() );
	}
	
	public EngineBase getTransactionBase() {
		return( baseChange );
	}

	public EngineBase getEngineBase() {
		if( baseChange != null )
			return( baseChange );
		return( data.getEngineBase() );
	}

	public EngineInfrastructure getTransactionInfrastructure() {
		return( infraChange );
	}

	public EngineInfrastructure getInfrastructure() {
		if( infraChange != null )
			return( infraChange );
		return( data.getInfrastructure() );
	}

	// helpers
	public AuthResource getResource( AuthResource resource ) throws Exception {
		return( resourcesNew.getResource( resource.ID ) );
	}
	
	public ProjectBuilder getBuilder( ProjectBuilder builder ) throws Exception {
		if( builder == null )
			return( null );
		return( buildersNew.getBuilder( builder.ID ) );
	}
	
	public AppSystem getSystem( AppSystem system ) throws Exception {
		if( system == null )
			return( null );
		if( directoryNew != null )
			return( directoryNew.getSystem( system.ID ) );
		EngineDirectory directory = data.getDirectory();
		return( directory.getSystem( system.ID ) );
	}
	
	public AppProduct getProduct( AppProduct product ) throws Exception {
		if( product == null )
			return( null );
		if( directoryNew != null )
			return( directoryNew.getProduct( product.NAME ) );
		
		TransactionProduct tm = findProductTransaction( product.NAME );
		if( tm != null ) {
			if( tm.productNew != null )
				return( tm.productNew );
		}
		
		EngineDirectory directory = data.getDirectory();
		return( directory.getProduct( product.ID ) );
	}
	
	public ProductDump getDump( ProductDump dump ) throws Exception {
		if( dump == null )
			return( null );
		AppProduct productNew = getProduct( dump.dumps.product );
		return( productNew.getDump( dump.ID ) );
	}
	
	public ProductDumpMask getDumpMask( ProductDumpMask mask ) throws Exception {
		if( mask == null )
			return( null );
		ProductDump dumpNew = getDump( mask.dump );
		return( dumpNew.getDumpMask( mask.ID ) );
	}
	
	public Meta getMeta( Meta meta ) throws Exception {
		if( meta == null )
			return( null );
		EngineProduct ep = meta.getEngineProduct();
		return( ep.findSessionMeta( action , meta.getStorage() , true ) );
	}

	public MetaEnv getMetaEnv( MetaEnv env ) throws Exception {
		if( env == null )
			return( null );
		Meta meta = getMeta( env.meta );
		ProductEnvs envs = meta.getEnviroments();
		return( envs.findMetaEnv( env.NAME ) );
	}

	public MetaEnvDeployGroup getMetaEnvDeployGroup( MetaEnvDeployGroup dg ) throws Exception {
		if( dg == null )
			return( null );
		MetaEnv env = getMetaEnv( dg.env );
		return( env.findDeployGroup( dg.NAME ) );
	}

	public MetaEnvSegment getMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		if( sg == null )
			return( null );
		MetaEnv env = getMetaEnv( sg.env );
		return( env.findSegment( sg.NAME ) );
	}

	public MetaEnvStartInfo getStartInfo( MetaEnvStartInfo startInfo ) throws Exception {
		if( startInfo == null )
			return( null );
		MetaEnvSegment sg = getMetaEnvSegment( startInfo.sg );
		return( sg.getStartInfo() );
	}
	
	public MetaEnvStartGroup getStartGroup( MetaEnvStartGroup startGroup ) throws Exception {
		if( startGroup == null )
			return( null );
		MetaEnvStartInfo startInfo = getStartInfo( startGroup.startInfo );
		return( startInfo.getStartGroup( startGroup.ID ) );
	}
	
	public MetaEnvServer getMetaEnvServer( MetaEnvServer server ) throws Exception {
		if( server == null )
			return( null );
		MetaEnvSegment sg = getMetaEnvSegment( server.sg );
		return( sg.findServer( server.NAME ) );
	}

	public MetaEnvServerDeployment getMetaEnvServerDeployment( MetaEnvServerDeployment deployment ) throws Exception {
		if( deployment == null )
			return( null );
		MetaEnvServer server = getMetaEnvServer( deployment.server );
		return( server.getDeployment( deployment.ID ) );
	}

	public MetaEnvServerNode getMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		if( node == null )
			return( null );
		MetaEnvServer server = getMetaEnvServer( node.server );
		return( server.getNodeByPos( node.POS ) );
	}

	public AppProduct getTransactionProduct( AppProduct product ) throws Exception {
		AppProduct productUpdated = null;
		TransactionProduct tm = findProductTransaction( product.NAME );
		if( tm != null )
			productUpdated = tm.productNew;
		if( productUpdated == null )
			exit1( _Error.TransactionMissingProductChanges1 , "Missing changes in product=" + product.NAME , product.NAME );
		return( productUpdated );
	}
	
	public Meta[] getTransactionProductMetadataList() {
		List<Meta> list = new LinkedList<Meta>();
		for( TransactionProduct tm : productChanges.values() ) {
			Meta[] tmlist = tm.getTransactionProductMetadataList();
			for( Meta meta : tmlist )
				list.add( meta );
		}
		return( list.toArray( new Meta[0] ) );
	}

	public Meta getTransactionMetadata( Meta meta ) throws Exception {
		ProductMeta storage = getTransactionProductMetadata( meta );
		return( storage.meta );
	}
		
	public ProductMeta getTransactionProductMetadata( Meta meta ) throws Exception {
		ProductMeta storage = null;
		TransactionProduct tm = findProductTransaction( meta.name );
		if( tm != null )
			storage = tm.findTransactionProductMetadata( meta );
		if( storage == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		return( storage );
	}
	
	public Meta getTransactionMetadata( int metaId ) throws Exception {
		for( TransactionProduct tm : productChanges.values() ) {
			Meta meta = tm.findTransactionMeta( metaId );
			if( meta != null )
				return( meta );
		}
		action.exitUnexpectedState();
		return( null );
	}

	public MetaEnv getTransactionEnv( MetaEnv env ) throws Exception {
		return( getTransactionEnv( env.ID ) );
	}
	
	public MetaEnv getTransactionEnv( int envId ) throws Exception {
		for( TransactionProduct tm : productChanges.values() ) {
			MetaEnv env = tm.findTransactionEnv( envId );
			if( env != null )
				return( env );
		}
		
		action.exitUnexpectedState();
		return( null );
	}
	
	public MetaDistrDelivery getDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		Meta meta = getTransactionMetadata( delivery.meta );
		MetaDistr distr = meta.getDistr();
		return( distr.getDelivery( delivery.ID ) );
	}

	public MetaDistrBinaryItem getDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		MetaDistrDelivery delivery = getDistrDelivery( item.delivery );
		return( delivery.getBinaryItem( item.ID ) );
	}
	
	public MetaDistrConfItem getDistrConfItem( MetaDistrConfItem item ) throws Exception {
		MetaDistrDelivery delivery = getDistrDelivery( item.delivery );
		return( delivery.getConfItem( item.ID ) );
	}

	public MetaDatabaseSchema getDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		Meta meta = getTransactionMetadata( schema.meta );
		MetaDatabase database = meta.getDatabase();
		return( database.getSchema( schema.ID ) );
	}

	public MetaProductUnit getProductUnit( MetaProductUnit unit ) throws Exception {
		Meta meta = getTransactionMetadata( unit.meta );
		MetaUnits units = meta.getUnits();
		return( units.getUnit( unit.ID ) );
	}

	public MetaProductDoc getProductDoc( MetaProductDoc doc ) throws Exception {
		Meta meta = getTransactionMetadata( doc.meta );
		MetaDocs docs = meta.getDocs();
		return( docs.getDoc( doc.ID ) );
	}

	public MetaDistrComponent getDistrComponent( MetaDistrComponent comp ) throws Exception {
		Meta meta = getTransactionMetadata( comp.meta );
		MetaDistr distr = meta.getDistr();
		return( distr.getComponent( comp.ID ) );
	}

	public MetaDistrComponentItem getDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		MetaDistrComponent comp = getDistrComponent( item.comp );
		if( item.COMPITEM_TYPE == DBEnumCompItemType.BINARY )
			return( comp.getBinaryItem( item.binaryItem.NAME ) );
		if( item.COMPITEM_TYPE == DBEnumCompItemType.CONF )
			return( comp.getConfItem( item.confItem.NAME ) );
		if( item.COMPITEM_TYPE == DBEnumCompItemType.SCHEMA )
			return( comp.getSchemaItem( item.schema.NAME ) );
		if( item.COMPITEM_TYPE == DBEnumCompItemType.WSDL )
			return( comp.getWebService( item.WSDL_REQUEST ) );
		action.exitUnexpectedState();
		return( null );
	}

	public MetaSourceProjectItem getSourceProjectItem( MetaSourceProjectItem item ) throws Exception {
		MetaSourceProject project = getSourceProject( item.project );
		return( project.getItem( item.ID ) );
	}
	
	public MetaSourceProject getSourceProject( MetaSourceProject project ) throws Exception {
		Meta metaNew = getTransactionMetadata( project.meta );
		MetaSources sourceNew = metaNew.getSources();
		return( sourceNew.getProject( project.ID ) );
	}
	
	public MetaSourceProjectSet getSourceProjectSet( MetaSourceProjectSet set ) throws Exception {
		Meta metaNew = getTransactionMetadata( set.meta );
		MetaSources sourceNew = metaNew.getSources();
		return( sourceNew.getProjectSet( set.ID ) );
	}

	public MirrorRepository getMirrorRepository( MirrorRepository repo ) throws Exception {
		return( mirrorsNew.getRepository( repo.ID ) );
	}
	
	public Datacenter getDatacenter( Datacenter datacenter ) throws Exception {
		return( infraChange.getDatacenter( datacenter.ID ) );
	}
	
	public ReleaseLifecycle getLifecycle( ReleaseLifecycle lc ) throws Exception {
		return( lifecyclesChange.getLifecycle( lc.ID ) );
	}
	
	public BaseCategory getBaseCategory( BaseCategory category ) throws Exception {
		return( baseChange.getCategory( category.BASECATEGORY_TYPE ) );
	}
	
	public BaseGroup getBaseGroup( BaseGroup group ) throws Exception {
		return( baseChange.getGroup( group.ID ) );
	}
	
	public BaseItem getBaseItem( BaseItem item ) throws Exception {
		return( baseChange.getItem( item.ID ) );
	}

	public AppProductMonitoringTarget getMonitoringTarget( AppProductMonitoringTarget target ) throws Exception {
		TransactionProduct tp = findProductTransaction( target.product.NAME );
		if( tp == null )
			Common.exitUnexpected();
		
		AppProduct productUpdated = getTransactionProduct( target.product );
		
		AppProductMonitoring mon = productUpdated.getMonitoring();
		AppProductMonitoringTarget targetUpdated = mon.getTarget( target.ID );
		if( targetUpdated == null )
			Common.exitUnexpected();
		return( targetUpdated );
	}

	public AppProductMonitoringItem getMonitoringItem( AppProductMonitoringItem item ) throws Exception {
		AppProductMonitoringTarget targetUpdated = getMonitoringTarget( item.target );
		return( targetUpdated.getItem( item.ID ) );
	}
	
	public void checkSecurityFailed() {
		fail0( _Error.SecurityCheckFailed0 , "Operation is not permitted" );
	}
	
	public boolean checkSecurityServerChange( SecurityAction sa ) {
		AuthService auth = engine.getAuth();
		if( auth.checkAccessServerAction( action , sa , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecuritySpecial( SpecialRights sr ) {
		AuthService auth = engine.getAuth();
		if( auth.checkAccessSpecial( action , sr ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecurityInfrastructureChange( Network network ) {
		AuthService auth = engine.getAuth();
		if( auth.checkAccessNetworkAction( action , SecurityAction.ACTION_CONFIGURE , network , true , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecurityProductChange( AppProduct product , MetaEnv env , SecurityAction sa ) {
		AuthService auth = engine.getAuth();
		if( auth.checkAccessProductAction( action , sa , product , env , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	protected void updateRevision( AppProduct product , ProductMeta metadata , ProductMeta metadataOld ) throws Exception {
		EngineProducts products = data.getProducts();
		products.updateRevision( product , metadata , metadataOld );
	}
	
	public void deleteProductMetadata( ProductMeta metadata ) throws Exception {
		EngineProducts products = data.getProducts();
		products.deleteProductMetadata( this , metadata );
	}
	
	public void requestReplaceProductMetadata( ProductMeta storage , ProductMeta storageOld ) throws Exception {
		TransactionProduct tm = createProductTransaction( storage.getProduct() , storage.ep );
		tm.replaceProductMetadata( storage , storageOld );
	}

}
