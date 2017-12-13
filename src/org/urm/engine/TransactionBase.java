package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.RunError;
import org.urm.common.action.CommandMethodMeta.SecurityAction;
import org.urm.db.DBConnection;
import org.urm.db.EngineDB;
import org.urm.engine.action.ActionInit;
import org.urm.engine.properties.EngineEntities;
import org.urm.engine.properties.PropertyEntity;
import org.urm.meta.EngineData;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
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
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaDatabase;
import org.urm.meta.product.MetaDatabaseSchema;
import org.urm.meta.product.MetaDistr;
import org.urm.meta.product.MetaDistrBinaryItem;
import org.urm.meta.product.MetaDistrComponent;
import org.urm.meta.product.MetaDistrComponentItem;
import org.urm.meta.product.MetaDistrComponentWS;
import org.urm.meta.product.MetaDistrConfItem;
import org.urm.meta.product.MetaDistrDelivery;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvSegment;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;
import org.urm.meta.product.MetaSource;
import org.urm.meta.product.MetaSourceProject;
import org.urm.meta.product.MetaSourceProjectItem;
import org.urm.meta.product.MetaSourceProjectSet;
import org.urm.meta.Types.*;

public class TransactionBase extends EngineObject {

	public Engine engine;
	public ActionInit action;
	public RunError error;
	private EngineData data;
	
	private DBConnection connection;
	private boolean CHANGEDATABASE;
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
	
	public TransactionBase( Engine engine , EngineData data , ActionInit action ) {
		super( null );
		this.engine = engine;
		this.data = data;
		this.action = action;
		
		CHANGEDATABASE = false;
		CHANGEDATABASECORE = false;
		CHANGEDATABASEAUTH = false;
		
		productMeta = new HashMap<String,TransactionMetadata>();
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
				res = saveProducts();
			
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
		}
		catch( Throwable e ) {
			handle( e , "unable to restore metadata" );
		}
	}
	
	public ActionInit getAction() throws Exception {
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
	
	public void changeDatabase() throws Exception {
		if( CHANGEDATABASE )
			return;
		
		CHANGEDATABASE = true;
		EngineDB db = data.getDatabase();
		connection = db.getConnection( action );
	}
	
	private void changeDatabaseCore() throws Exception {
		changeDatabase();
		if( CHANGEDATABASECORE )
			return;
		
		CHANGEDATABASECORE = true;
		int version = connection.getNextCoreVersion();
		trace( "core update, new version=" + version );
	}

	private void changeDatabaseAuth() throws Exception {
		changeDatabase();
		if( CHANGEDATABASEAUTH )
			return;
		
		CHANGEDATABASEAUTH = true;
		int version = connection.getNextLocalVersion();
		trace( "auth update, new version=" + version );
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

	public boolean changeReleaseLifecycles( EngineLifecycles sourceLifecycles ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( lifecyclesChange != null )
					return( true );

				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
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

	public boolean changeBuilders( EngineBuilders sourceBuilders ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( buildersNew != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
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
						changeDatabase();
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

	public boolean changeMonitoring( EngineMonitoring sourceMonitoring ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_MONITOR ) )
					return( false );
				
				changeDatabase();
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change settings" );
			}
			
			abortTransaction( false );
			return( false );
		}
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

	public boolean changeMetadata( Meta meta , MetaEnv env ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( meta.name ); 
				if( tm != null )
					return( true );
				
				if( !checkSecurityProductChange( meta , env ) )
					return( false );
				
				tm = new TransactionMetadata( this );
				if( tm.changeProduct( meta ) ) {
					addTransactionMeta( meta , tm );
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
	
	public boolean deleteMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				TransactionMetadata tm = productMeta.get( meta.name );
				if( tm != null )
					action.exitUnexpectedState();
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				tm = new TransactionMetadata( this );
				if( tm.deleteProduct( meta ) ) {
					addTransactionMeta( meta , tm );
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
	
	private boolean saveProducts() {
		if( !continueTransaction() )
			return( false );

		try {
			boolean failed = false;
			for( TransactionMetadata tm : productMeta.values() ) {
				if( !tm.saveProduct() ) {
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

	protected void checkTransactionMetadata( String productName ) throws Exception {
		if( productMeta.get( productName ) == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
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
	
	public EngineMirrors getTransactionMirrors() {
		return( mirrorsNew );
	}
	
	public EngineMirrors getMirrors() {
		if( mirrorsNew != null )
			return( mirrorsNew );
		return( data.getMirrors() );
	}
	
	public EngineSettings getTransactionSettings() {
		return( settingsNew );
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
	
	private void addTransactionMeta( Meta meta , TransactionMetadata tm ) {
		productMeta.put( meta.name , tm );
	}

	// helpers
	public AuthResource getResource( AuthResource resource ) throws Exception {
		return( resourcesNew.getResource( resource.NAME ) );
	}
	
	public ProjectBuilder getBuilder( ProjectBuilder builder ) throws Exception {
		return( buildersNew.getBuilder( builder.NAME ) );
	}
	
	public AppSystem getSystem( AppSystem system ) throws Exception {
		return( directoryNew.getSystem( system.NAME ) );
	}
	
	public AppProduct getProduct( AppProduct product ) throws Exception {
		return( directoryNew.getProduct( product.NAME ) );
	}
	
	public Meta getMeta( Meta meta ) throws Exception {
		return( action.getActiveProductMetadata( meta.name ) );
	}

	public MetaEnv getMetaEnv( MetaEnv env ) throws Exception {
		ProductMeta metadata = getTransactionMetadata( env.meta );
		return( metadata.findEnvironment( env.NAME ) );
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
		return( server.getNode( action , node.POS ) );
	}

	public Meta[] getTransactionProductMetadataList() {
		TransactionMetadata[] tm = productMeta.values().toArray( new TransactionMetadata[0] );
		Meta[] meta = new Meta[ tm.length ];
		for( int k = 0; k < tm.length; k++ )
			meta[ k ] = tm[k].sessionMeta;
		return( meta );
	}

	public ProductMeta getTransactionMetadata( Meta meta ) throws Exception {
		TransactionMetadata tm = productMeta.get( meta.name );
		if( tm == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		return( tm.metadata );
	}
	
	protected Meta createProductMetadata( AppProduct product ) throws Exception {
		TransactionMetadata tm = productMeta.get( product.NAME );
		if( tm != null )
			action.exitUnexpectedState();
		
		if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
			action.exitUnexpectedState();
		
		EngineSettings settings = getSettings();
		Meta meta = data.createProductMetadata( this , settings , product );
		tm = new TransactionMetadata( this );
		tm.createProduct( meta );
		addTransactionMeta( meta , tm );
		return( meta );
	}

	public Meta getTransactionProductMetadata( String productName ) throws Exception {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			action.exitUnexpectedState();
		return( tm.metadata.meta );
	}

	public MetaDistrDelivery getDistrDelivery( MetaDistrDelivery delivery ) throws Exception {
		Meta meta = getTransactionProductMetadata( delivery.meta.name );
		MetaDistr distr = meta.getDistr( action );
		return( distr.getDelivery( action , delivery.NAME ) );
	}

	public MetaDistrBinaryItem getDistrBinaryItem( MetaDistrBinaryItem item ) throws Exception {
		MetaDistrDelivery delivery = getDistrDelivery( item.delivery );
		return( delivery.getBinaryItem( action , item.KEY ) );
	}
	
	public MetaDistrConfItem getDistrConfItem( MetaDistrConfItem item ) throws Exception {
		MetaDistrDelivery delivery = getDistrDelivery( item.delivery );
		return( delivery.getConfItem( action , item.KEY ) );
	}

	public MetaDatabaseSchema getDatabaseSchema( MetaDatabaseSchema schema ) throws Exception {
		Meta meta = getTransactionProductMetadata( schema.meta.name );
		MetaDatabase database = meta.getDatabase( action );
		return( database.getSchema( action , schema.SCHEMA ) );
	}

	public MetaDistrComponent getDistrComponent( MetaDistrComponent comp ) throws Exception {
		Meta meta = getTransactionProductMetadata( comp.meta.name );
		MetaDistr distr = meta.getDistr( action );
		return( distr.getComponent( action , comp.NAME ) );
	}

	public MetaDistrComponentItem getDistrComponentItem( MetaDistrComponentItem item ) throws Exception {
		MetaDistrComponent comp = getDistrComponent( item.comp );
		if( item.type == VarCOMPITEMTYPE.BINARY )
			return( comp.getBinaryItem( action , item.NAME ) );
		if( item.type == VarCOMPITEMTYPE.CONF )
			return( comp.getConfItem( action , item.NAME ) );
		if( item.type == VarCOMPITEMTYPE.SCHEMA )
			return( comp.getSchemaItem( action , item.NAME ) );
		action.exitUnexpectedState();
		return( null );
	}

	public MetaDistrComponentWS getDistrComponentService( MetaDistrComponentWS service ) throws Exception {
		MetaDistrComponent comp = getDistrComponent( service.comp );
		return( comp.getWebService( action , service.NAME ) );
	}

	public MetaSourceProjectItem getSourceProjectItem( MetaSourceProjectItem item ) throws Exception {
		MetaSourceProject project = getSourceProject( item.project );
		return( project.getItem( action , item.ITEMNAME ) );
	}
	
	public MetaSourceProject getSourceProject( MetaSourceProject project ) throws Exception {
		Meta metaNew = getTransactionProductMetadata( project.meta.name );
		MetaSource sourceNew = metaNew.getSources( action );
		return( sourceNew.getProject( action , project.NAME ) );
	}
	
	public MetaSourceProjectSet getSourceProjectSet( MetaSourceProjectSet set ) throws Exception {
		Meta metaNew = getTransactionProductMetadata( set.meta.name );
		MetaSource sourceNew = metaNew.getSources( action );
		return( sourceNew.getProjectSet( action , set.NAME ) );
	}

	public MirrorRepository getMirrorRepository( MirrorRepository repo ) throws Exception {
		return( mirrorsNew.getRepository( repo.NAME ) );
	}
	
	public Datacenter getDatacenter( Datacenter datacenter ) throws Exception {
		return( infraChange.getDatacenter( datacenter.NAME ) );
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
	
}
