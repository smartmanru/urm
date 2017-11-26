package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.RunError;
import org.urm.db.DBConnection;
import org.urm.engine.action.ActionInit;
import org.urm.meta.EngineData;
import org.urm.meta.EngineLoader;
import org.urm.meta.EngineObject;
import org.urm.meta.ProductMeta;
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
import org.urm.meta.engine.Product;
import org.urm.meta.engine.ProjectBuilder;
import org.urm.meta.engine.EngineReleaseLifecycles;
import org.urm.meta.engine.EngineResources;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineAuth.SecurityAction;
import org.urm.meta.engine.EngineAuth.SpecialRights;
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
	protected EngineData data;
	protected EngineLoader loader;
	
	private DBConnection connection;
	private boolean CHANGEDATABASE;
	private boolean SERVERVERSIONUPDATE;
	
	public EngineInfrastructure infra;
	public EngineReleaseLifecycles lifecycles;
	public EngineBase base;
	
	public EngineSettings settings;
	public EngineResources resources;
	public EngineBuilders builders;
	public EngineDirectory directory;
	public EngineMirrors mirrors;

	protected EngineSettings settingsOld;
	protected EngineResources resourcesOld;
	protected EngineBuilders buildersOld;
	protected EngineDirectory directoryOld;
	protected EngineMirrors mirrorsOld;
	private boolean saveRegistry;

	private Map<String,TransactionMetadata> productMeta;
	
	public TransactionBase( Engine engine , ActionInit action ) {
		super( null );
		this.engine = engine;
		this.action = action;
		
		data = engine.getData();
		loader = new EngineLoader( engine , data , action );
		
		CHANGEDATABASE = false;
		SERVERVERSIONUPDATE = false;
		
		settings = null;
		infra = null;
		lifecycles = null;
		base = null;
		
		resources = null;
		builders = null;
		directory = null;
		mirrors = null;
		saveRegistry = false;
		
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
			settings = null;
			infra = null;
			lifecycles = null;
			base = null;

			resources = null;
			builders = null;
			directory = null;
			mirrors = null;
			
			settingsOld = null;
			
			resourcesOld = null;
			buildersOld = null;
			directoryOld = null;
			mirrorsOld = null;
			saveRegistry = false;
			
			return( true );
		}
	}

	public void abortTransaction( boolean save ) {
		synchronized( engine ) {
			if( !continueTransaction() )
				return;
			
			try {
				if( settingsOld != null ) {
					if( save ) {
						loader.setSettings( settingsOld );
					}
					settingsOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore settings" );
			}

			try {
				if( resourcesOld != null ) {
					if( save ) {
						data.setResources( this , resourcesOld );
					}
					resourcesOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( buildersOld != null ) {
					if( save )
						data.setBuilders( this , buildersOld );
					buildersOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( directoryOld != null ) {
					if( save )
						data.setDirectory( this , directoryOld );
					directoryOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore directory" );
			}
			
			try {
				if( mirrorsOld != null ) {
					if( save )
						data.setMirrors( this , mirrorsOld );
					mirrorsOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore mirrors" );
			}
			
			try {
				if( saveRegistry ) {
					EngineData data = engine.getData();
					EngineLoader loader = new EngineLoader( engine , data , action );
					loader.commitRegistry();
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore registry" );
			}
			
			try {
				for( TransactionMetadata meta : productMeta.values() )
					meta.abortTransaction( save );
				productMeta.clear();
			}
			catch( Throwable e ) {
				handle( e , "unable to restore metadata" );
			}
			
			try {
				if( connection != null )
					connection.close( false );
			}
			catch( Throwable e ) {
				handle( e , "unable to rollback database changes" );
			}
			
			engine.abortTransaction( this );
		}
		
		saveRegistry = false;
		action.clearTransaction();
	}

	public ActionInit getAction() throws Exception {
		return( action );
	}
	
	public boolean commitTransaction() {
		synchronized( engine ) {
			if( !continueTransaction() )
				return( false );

			boolean res = true;
			if( res )
				res = saveSettings();
			
			if( res )
				res = saveResources();
			if( res )
				res = saveBuilders();
			if( res )
				res = saveDirectory();
			if( res )
				res = saveMirrors();
			try {
				if( saveRegistry ) {
					EngineData data = engine.getData();
					EngineLoader loader = new EngineLoader( engine , data , action );
					loader.commitRegistry();
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to save registry" );
			}
			
			if( res )
				res = saveInfrastructure();
			if( res )
				res = saveReleaseLifecycles();
			if( res )
				res = saveBase();
			
			if( res )
				res = saveMetadata();
			
			if( res ) {
				if( connection != null )
					connection.close( true );
				
				if( !engine.commitTransaction( this ) )
					return( false );
				
				action.clearTransaction();
				return( true );
			}

			abortTransaction( true );
			return( false );
		}
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
		EngineData data = engine.getData();
		EngineDB db = data.getDatabase();
		connection = db.getConnection( action );
	}
	
	public void changeEngineDatabase() throws Exception {
		changeDatabase();
		if( SERVERVERSIONUPDATE )
			return;
		
		SERVERVERSIONUPDATE = true;
		int version = connection.getNextCoreVersion();
		trace( "core version update, version=" + version );
	}
	
	public boolean changeInfrastructure( EngineInfrastructure sourceInfrastructure , Network network ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( infra != null )
					return( true );

				if( network == null ) {
					if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
						return( false );
				}
				else {
					if( !checkSecurityInfrastructureChange( network ) )
						return( false );
				}
				
				changeEngineDatabase();
				infra = sourceInfrastructure;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change infrastructure" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	private boolean saveInfrastructure() {
		if( !continueTransaction() )
			return( false );
		
		if( infra == null )
			return( true );
		
		try {
			EngineData data = engine.getData();
			EngineLoader loader = new EngineLoader( engine , data , action );
			loader.commitInfrastructure();
			trace( "transaction server infrastructure: save done" );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save infrastructure" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeReleaseLifecycles( EngineReleaseLifecycles sourceLifecycles ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( lifecycles != null )
					return( true );

				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				changeEngineDatabase();
				lifecycles = sourceLifecycles;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change release lifecycles" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	private boolean saveReleaseLifecycles() {
		if( !continueTransaction() )
			return( false );
		
		if( lifecycles == null )
			return( true );
		
		try {
			EngineData data = engine.getData();
			EngineLoader loader = new EngineLoader( engine , data , action );
			loader.commitReleaseLifecycles();
			trace( "transaction server release lifecycles: save done" );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save release lifecycles" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeBase( EngineBase sourceBase , SpecialRights sr ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( base != null )
					return( true );
				
				if( !checkSecuritySpecial( sr ) )
					return( false );
				
				changeEngineDatabase();
				base = sourceBase;
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change base" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}

	private boolean saveBase() {
		if( !continueTransaction() )
			return( false );
		
		if( base == null )
			return( true );
		
		try {
			EngineData data = engine.getData();
			EngineLoader loader = new EngineLoader( engine , data , action );
			loader.commitBase();
			trace( "transaction server base: save done" );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save base data" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeResources( EngineResources sourceResources ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( resources != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change resources, server is stopped" );
				else {
					resourcesOld = action.getActiveResources();
					if( sourceResources == resourcesOld ) {
						changeEngineDatabase();
						resources = sourceResources.copy();
						if( resources != null ) {
							trace( "transaction resources: source=" + sourceResources.objectId + ", copy=" + resources.objectId );
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

	private boolean saveResources() {
		if( !continueTransaction() )
			return( false );
		
		if( resources == null )
			return( true );
		
		saveRegistry = true;
		try {
			data.setResources( this , resources );
			trace( "transaction resources: save=" + resources.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save resources" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeBuilders( EngineBuilders sourceBuilders ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( builders != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change builders, server is stopped" );
				else {
					buildersOld = action.getActiveBuilders();
					if( sourceBuilders == buildersOld ) {
						changeEngineDatabase();
						builders = sourceBuilders.copy();
						if( builders != null ) {
							trace( "transaction builders: source=" + sourceBuilders.objectId + ", copy=" + builders.objectId );
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

	private boolean saveBuilders() {
		if( !continueTransaction() )
			return( false );
		
		if( builders == null )
			return( true );
		
		saveRegistry = true;
		try {
			data.setBuilders( this , builders );
			trace( "transaction builders: save=" + builders.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save builders" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeDirectory( EngineDirectory sourceDirectory , boolean critical ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( directory != null )
					return( true );
				
				SecurityAction mode = ( critical )? SecurityAction.ACTION_ADMIN : SecurityAction.ACTION_CONFIGURE;
				if( !checkSecurityServerChange( mode ) )
					return( false );
				
				if( !engine.isRunning() )
					error( "unable to change directory, server is stopped" );
				else {
					directoryOld = action.getActiveDirectory();
					if( sourceDirectory == directoryOld ) {
						changeEngineDatabase();
						directory = sourceDirectory.copy();
						if( directory != null ) {
							trace( "transaction directory: source=" + sourceDirectory.objectId + ", copy=" + directory.objectId );
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

	private boolean saveDirectory() {
		if( !continueTransaction() )
			return( false );
		
		if( directory == null )
			return( true );
		
		saveRegistry = true;
		try {
			data.setDirectory( this , directory );
			trace( "transaction directory: save=" + directory.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save directory" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeMirrors( EngineMirrors sourceMirrors ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( mirrors != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
					return( false );
				
				mirrorsOld = action.getActiveMirrors();
				if( sourceMirrors == mirrorsOld ) {
					changeEngineDatabase();
					mirrors = sourceMirrors.copy();
					if( mirrors != null ) {
						trace( "transaction mirrors: source=" + sourceMirrors.objectId + ", copy=" + mirrors.objectId );
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

	private boolean saveMirrors() {
		if( !continueTransaction() )
			return( false );
		
		if( mirrors == null )
			return( true );
		
		saveRegistry = true;
		try {
			data.setMirrors( this , mirrors );
			trace( "transaction mirrors: save=" + mirrors.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save mirrors" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeMonitoring( EngineMonitoring sourceMonitoring ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_MONITOR ) )
					return( false );
				
				changeEngineDatabase();
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
					
				if( settings != null )
					return( true );
				
				if( !checkSecurityServerChange( SecurityAction.ACTION_ADMIN ) )
					return( false );
				
				settingsOld = action.getActiveServerSettings();
				if( sourceSettings == settingsOld ) {
					changeEngineDatabase();
					settings = sourceSettings.copy();
					trace( "transaction server settings: source=" + sourceSettings.objectId + ", copy=" + settings.objectId );
					if( settings != null )
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

	private boolean saveSettings() {
		if( !continueTransaction() )
			return( false );
		
		if( settings == null )
			return( true );
		
		try {
			EngineData data = engine.getData();
			EngineLoader loader = new EngineLoader( engine , data , action );
			loader.setSettings( settings );
			trace( "transaction server settings: save=" + settings.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save settings" );
		}

		abortTransaction( true );
		return( false );
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
	
	private boolean saveMetadata() {
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

	protected void checkTransactionInfrastructure() throws Exception {
		checkTransaction();
		if( infra == null )
			exit( _Error.TransactionMissingInfrastructureChanges0 , "Missing infrastructure changes" , null );
	}

	protected void checkTransactionReleaseLifecycles() throws Exception {
		checkTransaction();
		if( lifecycles == null )
			exit( _Error.TransactionMissingReleaseLifecyclesChanges0 , "Missing release lifecycles changes" , null );
	}

	protected void checkTransactionBase() throws Exception {
		checkTransaction();
		if( base == null )
			exit( _Error.TransactionMissingBaseChanges0 , "Missing base changes" , null );
	}

	protected void checkTransactionResources( EngineResources sourceResources ) throws Exception {
		checkTransaction();
		if( resources == null || resources != sourceResources )
			exit( _Error.TransactionMissingResourceChanges0 , "Missing resources changes" , null );
	}

	protected void checkTransactionBuilders() throws Exception {
		checkTransaction();
		if( builders == null )
			exit( _Error.TransactionMissingBuildersChanges0 , "Missing builders changes" , null );
	}

	protected void checkTransactionDirectory() throws Exception {
		checkTransaction();
		if( directory == null )
			exit( _Error.TransactionMissingDirectoryChanges0 , "Missing directory changes" , null );
	}

	protected void checkTransactionMirrors( EngineMirrors sourceMirrors ) throws Exception {
		checkTransaction();
		changeEngineDatabase();
		if( sourceMirrors == null || mirrors != sourceMirrors )
			exit( _Error.TransactionMissingMirrorsChanges0 , "Missing mirrors changes" , null );
	}

	protected void checkTransactionSettings() throws Exception {
		checkTransaction();
		if( settings == null )
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
	
	public EngineResources getTransactionResources() {
		return( resources );
	}
	
	public EngineBuilders getTransactionBuilders() {
		return( builders );
	}
	
	public EngineReleaseLifecycles getTransactionLifecycles() {
		return( lifecycles );
	}
	
	public EngineDirectory getTransactionDirectory() {
		return( directory );
	}
	
	public EngineMirrors getTransactionMirrors() {
		return( mirrors );
	}
	
	public EngineSettings getTransactionSettings() {
		return( settings );
	}
	
	public Meta findTransactionSessionProductMetadata( String productName ) {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			return( null );
		
		return( tm.sessionMeta );
	}
	
	public EngineInfrastructure getTransactionInfrastructure() {
		return( infra );
	}

	private void addTransactionMeta( Meta meta , TransactionMetadata tm ) {
		productMeta.put( meta.name , tm );
	}

	// helpers
	public AuthResource getResource( AuthResource resource ) throws Exception {
		return( resources.getResource( resource.NAME ) );
	}
	
	public ProjectBuilder getBuilder( ProjectBuilder builder ) throws Exception {
		return( builders.getBuilder( builder.NAME ) );
	}
	
	public AppSystem getSystem( AppSystem system ) throws Exception {
		return( directory.getSystem( system.NAME ) );
	}
	
	public Product getProduct( Product product ) throws Exception {
		return( directory.getProduct( product.NAME ) );
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
	
	protected Meta createProductMetadata( Product product ) throws Exception {
		TransactionMetadata tm = productMeta.get( product.NAME );
		if( tm != null )
			action.exitUnexpectedState();
		
		if( !checkSecurityServerChange( SecurityAction.ACTION_CONFIGURE ) )
			action.exitUnexpectedState();
		
		Meta meta = action.createProductMetadata( this , product );
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
		return( mirrors.getRepository( repo.NAME ) );
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

}
