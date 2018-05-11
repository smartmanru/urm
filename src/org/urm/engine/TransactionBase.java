package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.RunError;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.db.core.DBEnums.*;
import org.urm.engine.TransactionMetadata.TransactionMetadataEnv;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.ObjectMeta;
import org.urm.engine.properties.ObjectProperties;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.engine.BaseCategory;
import org.urm.meta.engine.BaseGroup;
import org.urm.meta.engine.BaseItem;
import org.urm.meta.engine.Datacenter;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.AuthResource;
import org.urm.meta.engine.EngineBase;
import org.urm.meta.engine.EngineBuilders;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineInfrastructure;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.MirrorRepository;
import org.urm.meta.engine.EngineMirrors;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.Network;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.engine.EngineLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineAuth.SpecialRights;
import org.urm.meta.engine.ReleaseLifecycle;
import org.urm.meta.env.MetaDump;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvSegment;
import org.urm.meta.env.MetaEnvServer;
import org.urm.meta.env.MetaEnvServerNode;
import org.urm.meta.env.ProductEnvs;
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
import org.urm.meta.product.MetaSources;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.product.MetaUnits;
import org.urm.meta.product.ProductMeta;

public class TransactionBase extends EngineObject {

	public Engine engine;
	public ActionInit action;
	public RunError error;
	private EngineData data;
	
	private DBConnection connection;
	private boolean USEDATABASE;
	private boolean IMPORT;
	private boolean CHANGEDATABASECORE;
	private boolean CHANGEDATABASEAUTH;
	
	// changed without copy
	protected EngineAuth authChange;
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
	
	private Map<String,TransactionMetadata> productMeta;
	private Map<Integer,TransactionMetadata> productMetaById;
	
	public TransactionBase( Engine engine , EngineData data , ActionInit action ) {
		super( null );
		this.engine = engine;
		this.data = data;
		this.action = action;
		
		USEDATABASE = false;
		IMPORT = false;
		CHANGEDATABASECORE = false;
		CHANGEDATABASEAUTH = false;
		
		productMeta = new HashMap<String,TransactionMetadata>();
		productMetaById = new HashMap<Integer,TransactionMetadata>();
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
					engine.trace( "remove old mirrors object, id=" + mirrorsOld.objectId );
					mirrorsOld.deleteObject();
					mirrorsOld = null;
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
			for( TransactionMetadata meta : productMeta.values() )
				meta.abortTransaction( save );
			productMeta.clear();
			productMetaById.clear();
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
	
	public boolean startImport() throws Exception {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( IMPORT )
					return( true );

				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
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
	
	public boolean changeAuth( EngineAuth sourceAuth ) {
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

	public Meta createProductMetadata( ProductMeta storage ) throws Exception {
		TransactionMetadata tm = productMeta.get( storage.name );
		
		// should be first product transaction operation
		if( tm != null )
			action.exitUnexpectedState();
		
		Meta meta = data.createSessionProductMetadata( this , storage );
		tm = new TransactionMetadata( this );
		if( !tm.createProduct( meta ) )
			Common.exitUnexpected();
			
		addTransactionMeta( meta.getId() , meta.name , tm );
		return( meta );
	}

	public boolean importProduct( AppProduct product ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( product.NAME );
				
				// should be first product transaction operation
				if( tm != null )
					action.exitUnexpectedState();
				
				if( !checkSecurityProductChange( product.storage.meta , null ) )
					return( false );
				
				tm = new TransactionMetadata( this );
				if( tm.importProduct( product ) ) {
					useDatabase();
					addTransactionMeta( product.storage.ID , product.NAME , tm );
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
	
	public boolean importEnv( MetaEnv env ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( env.meta.name );
				
				// should exist product transaction operation
				if( tm == null )
					action.exitUnexpectedState();
				
				if( !checkSecurityProductChange( env.meta , env ) )
					return( false );
				
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
	
	public boolean recreateMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( meta.name );
				
				// should be first product transaction operation
				if( tm != null )
					action.exitUnexpectedState();
				
				if( !checkSecurityProductChange( meta , null ) )
					return( false );
				
				tm = new TransactionMetadata( this );
				if( tm.recreateProduct( meta ) ) {
					useDatabase();
					addTransactionMeta( meta.getId() , meta.name , tm );
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
	
	public boolean deleteMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				TransactionMetadata tm = productMeta.get( meta.name );
				
				// should be first product transaction operation
				if( tm != null )
					action.exitUnexpectedState();
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				tm = new TransactionMetadata( this );
				if( tm.deleteProduct( meta ) ) {
					useDatabase();
					addTransactionMeta( meta.getId() , meta.name , tm );
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to save metadata" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean changeMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( meta.name ); 
				if( tm != null ) {
					if( tm.checkChangeProduct() )
						return( true );
				}
				
				if( !checkSecurityProductChange( meta , null ) )
					return( false );
				
				if( tm == null ) {
					tm = new TransactionMetadata( this );
					addTransactionMeta( meta.getId() , meta.name , tm );
				}
				
				if( tm.changeProduct( meta ) ) {
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
	
	public boolean changeEnv( MetaEnv env ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				Meta meta = env.meta;
				TransactionMetadata tm = productMeta.get( meta.name ); 
				if( tm != null ) {
					if( tm.checkChangeEnv( env ) )
						return( true );
				}
				
				if( !checkSecurityProductChange( env.meta , env ) )
					return( false );
				
				if( tm == null ) {
					tm = new TransactionMetadata( this );
					addTransactionMeta( meta.getId() , meta.name , tm );
				}
				
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
			for( TransactionMetadata tm : productMeta.values() ) {
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
		TransactionMetadata meta = productMeta.get( sourceMeta.name );
		if( meta == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		
		meta.checkTransactionMetadata( sourceMeta );
	}

	protected void checkTransactionEnv( MetaEnv env ) throws Exception {
		checkTransaction();
		TransactionMetadata meta = productMeta.get( env.meta.name );
		if( meta == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		
		meta.checkTransactionEnv( env );
	}

	protected void checkTransactionMetadata( String productName ) throws Exception {
		if( productMeta.get( productName ) == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
	}

	protected void checkTransactionCustomProperty( ObjectProperties ops ) throws Exception {
		ObjectMeta meta = ops.getMeta();
		PropertyEntity entity = meta.getCustomEntity();
		
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.RC_CUSTOM || 
			entity.PARAMENTITY_TYPE == DBEnumParamEntityType.ENGINE_CUSTOM ) {
			checkTransactionSettings();
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.SYSTEM_CUSTOM ) {
			checkTransactionDirectory( directoryNew );
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.PRODUCT_CUSTOM ) {
			Meta productMeta = getTransactionMetadata( ops.ownerId );
			if( productMeta == null )
				exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		}
		else
		if( entity.PARAMENTITY_TYPE == DBEnumParamEntityType.BASEITEM_CUSTOM ) {
			checkTransactionBase();
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
	
	public EngineAuth getAuth() {
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

	public Meta findTransactionSessionProductMetadata( String productName ) {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			return( null );
		
		return( tm.sessionMeta );
	}
	
	public Meta findTransactionSessionProductMetadata( int metaId ) {
		TransactionMetadata tm = productMetaById.get( metaId );
		if( tm == null )
			return( null );
		
		return( tm.sessionMeta );
	}
	
	private void addTransactionMeta( Integer metaId , String name , TransactionMetadata tm ) {
		productMeta.put( name , tm );
		if( metaId != null )
			productMetaById.put( metaId , tm );
	}

	// helpers
	public AuthResource getResource( AuthResource resource ) throws Exception {
		return( resourcesNew.getResource( resource.ID ) );
	}
	
	public ProjectBuilder getBuilder( ProjectBuilder builder ) throws Exception {
		return( buildersNew.getBuilder( builder.ID ) );
	}
	
	public AppSystem getSystem( AppSystem system ) throws Exception {
		if( directoryNew != null )
			return( directoryNew.getSystem( system.ID ) );
		EngineDirectory directory = data.getDirectory();
		return( directory.getSystem( system.ID ) );
	}
	
	public AppProduct getProduct( AppProduct product ) throws Exception {
		if( directoryNew != null )
			return( directoryNew.getProduct( product.NAME ) );
		EngineDirectory directory = data.getDirectory();
		return( directory.getProduct( product.ID ) );
	}
	
	public Meta getMeta( Meta meta ) throws Exception {
		return( action.getActiveProductMetadata( meta.name ) );
	}

	public Meta getMeta( AppProduct product ) throws Exception {
		return( action.getActiveProductMetadata( product.NAME ) );
	}

	public MetaEnv getMetaEnv( MetaEnv env ) throws Exception {
		ProductMeta metadata = getTransactionProductMetadata( env.meta );
		ProductEnvs envs = metadata.getEnviroments();
		return( envs.findMetaEnv( env.NAME ) );
	}

	public MetaEnvSegment getMetaEnvSegment( MetaEnvSegment sg ) throws Exception {
		MetaEnv env = getMetaEnv( sg.env );
		return( env.findSegment( sg.NAME ) );
	}

	public MetaEnvServer getMetaEnvServer( MetaEnvServer server ) throws Exception {
		MetaEnvSegment sg = getMetaEnvSegment( server.sg );
		return( sg.findServer( server.NAME ) );
	}

	public MetaEnvServerNode getMetaEnvServerNode( MetaEnvServerNode node ) throws Exception {
		MetaEnvServer server = getMetaEnvServer( node.server );
		return( server.getNodeByPos( node.POS ) );
	}

	public Meta[] getTransactionProductMetadataList() {
		TransactionMetadata[] tm = productMeta.values().toArray( new TransactionMetadata[0] );
		Meta[] meta = new Meta[ tm.length ];
		for( int k = 0; k < tm.length; k++ )
			meta[ k ] = tm[k].sessionMeta;
		return( meta );
	}

	public ProductMeta getTransactionProductMetadata( Meta meta ) throws Exception {
		TransactionMetadata tm = productMeta.get( meta.name );
		if( tm == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		return( tm.metadata );
	}
	
	public Meta getTransactionMetadata( Meta meta ) throws Exception {
		return( getTransactionMetadata( meta.name ) );
	}

	public Meta getTransactionMetadata( AppProduct product ) throws Exception {
		return( getTransactionMetadata( product.NAME ) );
	}

	public Meta getTransactionMetadata( String productName ) throws Exception {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			action.exitUnexpectedState();
		return( tm.metadata.meta );
	}

	public Meta getTransactionMetadata( int metaId ) throws Exception {
		TransactionMetadata tm = productMetaById.get( metaId );
		if( tm == null )
			action.exitUnexpectedState();
		return( tm.metadata.meta );
	}

	public MetaEnv getTransactionEnv( int envId ) throws Exception {
		for( TransactionMetadata tm : productMeta.values() ) {
			for( TransactionMetadataEnv tme : tm.getTransactionEnvs() ) {
				if( tme.env.ID == envId )
					return( tme.env );
			}
		}
		
		action.exitUnexpectedState();
		return( null );
	}
	
	public MetaDistrDelivery getDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		Meta meta = getTransactionMetadata( delivery.meta.name );
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
		Meta meta = getTransactionMetadata( schema.meta.name );
		MetaDatabase database = meta.getDatabase();
		return( database.getSchema( schema.ID ) );
	}

	public MetaProductUnit getProductUnit( MetaProductUnit unit ) throws Exception {
		Meta meta = getTransactionMetadata( unit.meta.name );
		MetaUnits units = meta.getUnits();
		return( units.getUnit( unit.ID ) );
	}

	public MetaProductDoc getProductDoc( MetaProductDoc doc ) throws Exception {
		Meta meta = getTransactionMetadata( doc.meta.name );
		MetaDocs docs = meta.getDocs();
		return( docs.getDoc( doc.ID ) );
	}

	public MetaDistrComponent getDistrComponent( MetaDistrComponent comp ) throws Exception {
		Meta meta = getTransactionMetadata( comp.meta.name );
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
		Meta metaNew = getTransactionMetadata( project.meta.name );
		MetaSources sourceNew = metaNew.getSources();
		return( sourceNew.getProject( project.ID ) );
	}
	
	public MetaSourceProjectSet getSourceProjectSet( MetaSourceProjectSet set ) throws Exception {
		Meta metaNew = getTransactionMetadata( set.meta.name );
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

	public MetaDump getDump( MetaDump dump ) throws Exception {
		Meta meta = getTransactionMetadata( dump.meta );
		MetaDatabase db = meta.getDatabase();
		if( dump.EXPORT )
			return( db.findExportDump( dump.NAME ) );
		return( db.findImportDump( dump.NAME ) );
	}
	
	public void checkSecurityFailed() {
		fail0( _Error.SecurityCheckFailed0 , "Operation is not permitted" );
	}
	
	public boolean checkSecurityServerChange( SecurityAction sa ) {
		EngineAuth auth = engine.getAuth();
		if( auth.checkAccessServerAction( action , sa , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecuritySpecial( SpecialRights sr ) {
		EngineAuth auth = engine.getAuth();
		if( auth.checkAccessSpecial( action , sr ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecurityInfrastructureChange( Network network ) {
		EngineAuth auth = engine.getAuth();
		if( auth.checkAccessNetworkAction( action , SecurityAction.ACTION_CONFIGURE , network , true , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public boolean checkSecurityProductChange( Meta meta , MetaEnv env ) {
		EngineAuth auth = engine.getAuth();
		if( auth.checkAccessProductAction( action , SecurityAction.ACTION_CONFIGURE , meta , env , false ) )
			return( true );
		
		checkSecurityFailed();
		return( false );
	}

	public void setProductMetadata( ProductMeta metadata ) throws Exception {
		data.setProductMetadata( this , metadata );
	}
	
	public void deleteProductMetadata( ProductMeta metadata ) throws Exception {
		data.deleteProductMetadata( this , metadata );
	}
	
	public void replaceProductMetadata( ProductMeta storage ) throws Exception {
		TransactionMetadata tm = productMeta.get( storage.name );
		if( tm == null )
			action.exitUnexpectedState();
		
		tm.replaceProduct( storage );
	}

}
