package org.urm.engine;

import org.urm.common.RunError;
import org.urm.engine.action.ActionInit;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaEnv;
import org.urm.engine.meta.MetaEnvDC;
import org.urm.engine.meta.MetaEnvServer;
import org.urm.engine.registry.ServerAuthResource;
import org.urm.engine.registry.ServerBuilders;
import org.urm.engine.registry.ServerDirectory;
import org.urm.engine.registry.ServerProduct;
import org.urm.engine.registry.ServerProjectBuilder;
import org.urm.engine.registry.ServerResources;
import org.urm.engine.registry.ServerSystem;

public class TransactionBase extends ServerObject {

	public ServerEngine engine;
	public ServerLoader loader;
	public ActionInit action;
	public RunError error;
	
	protected ServerResources resources;
	protected ServerBuilders builders;
	protected ServerDirectory directory;
	protected ServerSettings settings;
	protected ServerProductMeta metadata;

	protected ServerResources resourcesOld;
	protected ServerBuilders buildersOld;
	protected ServerDirectory directoryOld;
	protected ServerSettings settingsOld;
	protected ServerProductMeta metadataOld;
	
	public boolean createMetadata;
	public boolean deleteMetadata;
	protected Meta sessionMeta;
	
	public TransactionBase( ServerEngine engine ) {
		super( null );
		this.engine = engine;
		this.loader = engine.getLoader();
		
		resources = null;
		builders = null;
		directory = null;
		settings = null;
		metadata = null;
		createMetadata = false;
		deleteMetadata = false;
		sessionMeta = null;
		engine.serverAction.trace( "transaction created id=" + objectId );
	}
	
	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( this ) )
				return( false );
		
			resources = null;
			builders = null;
			directory = null;
			settings = null;
			metadata = null;
			resourcesOld = null;
			buildersOld = null;
			directoryOld = null;
			settingsOld = null;
			metadataOld = null;
			createMetadata = false;
			deleteMetadata = false;
			sessionMeta = null;
			return( true );
		}
	}

	public void abortTransaction() {
		synchronized( engine ) {
			if( !continueTransaction() )
				return;
			
			try {
				if( resourcesOld != null ) {
					loader.setResources( this , resourcesOld );
					resourcesOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( buildersOld != null ) {
					loader.setBuilders( this , buildersOld );
					buildersOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( directoryOld != null ) {
					loader.setDirectory( this , directoryOld );
					directoryOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore directory" );
			}
			
			try {
				if( settingsOld != null ) {
					loader.setSettings( this , settingsOld );
					settingsOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore settings" );
			}
			
			try {
				if( metadataOld != null ) {
					loader.setMetadata( this , metadataOld );
					metadataOld = null;
					createMetadata = false;
					deleteMetadata = false;
					sessionMeta = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore metadata" );
			}
			
			engine.abortTransaction( this );
		}
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
				res = saveResources();
			if( res )
				res = saveBuilders();
			if( res )
				res = saveDirectory();
			if( res )
				res = saveSettings();
			if( res )
				res = saveMetadata();
			
			if( res ) {
				if( !engine.commitTransaction( this ) )
					return( false );
				return( true );
			}

			abortTransaction();
			return( false );
		}
	}
	
	public boolean continueTransaction() {
		if( engine.getTransaction() != this )
			return( false );
		
		return( true );
	}

	public void handle( Throwable e , String s ) {
		if( action != null )
			action.log( s , e );
		if( e.getClass() == RunError.class )
			error = ( RunError )e;
		else
			error = new RunError( _Error.InternalTransactionError1 , "Internal transaction error: " + s , new String[] { s } );
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
		if( engine.serverAction != null )
			engine.serverAction.info( s );
		else {
			System.out.println( "transaction (info): " + s );
		}
	}
	
	public void debug( String s ) {
		if( action != null )
			action.debug( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.debug( s );
		else {
			System.out.println( "transaction (debug): " + s );
		}
	}
	
	public void error( String s ) {
		if( action != null )
			action.error( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.error( s );
		else {
			System.out.println( "transaction (error): " + s );
		}
	}
	
	public void trace( String s ) {
		if( action != null )
			action.trace( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.trace( s );
		else {
			System.out.println( "transaction (trace): " + s );
		}
	}
	
	public ServerResources getTransactionResources() {
		return( resources );
	}
	
	public ServerBuilders getTransactionBuilders() {
		return( builders );
	}
	
	public ServerDirectory getTransactionDirectory() {
		return( directory );
	}
	
	public ServerSettings getTransactionSettings() {
		return( settings );
	}
	
	public ServerProductMeta getTransactionMetadata() {
		return( metadata );
	}
	
	public Meta getTransactionSessionMetadata() {
		return( sessionMeta );
	}
	
	public ServerResources getResources() {
		if( resources != null )
			return( resources );
		return( engine.getResources() );
	}
	
	public ServerBuilders getBuilders() {
		if( builders != null )
			return( builders );
		return( engine.getBuilders() );
	}
	
	public ServerDirectory getDirectory() {
		if( directory != null )
			return( directory );
		return( engine.getDirectory() );
	}
	
	public ServerSettings getSettings() {
		if( settings != null )
			return( settings );
		return( engine.getSettings() );
	}
	
	public boolean changeResources( ServerResources sourceResources ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( resources != null )
					return( true );
				
				if( !engine.isRunning() )
					error( "unable to change resources, server is stopped" );
				else {
					if( sourceResources == loader.getResources() ) {
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
			
			abortTransaction();
			return( false );
		}
	}

	private boolean saveResources() {
		if( !continueTransaction() )
			return( false );
		
		if( resources == null )
			return( true );
		
		try {
			resourcesOld = loader.getResources();
			loader.setResources( this , resources );
			trace( "transaction resources: save=" + resources.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save resources" );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeBuilders( ServerBuilders sourceBuilders ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( builders != null )
					return( true );
				
				if( !engine.isRunning() )
					error( "unable to change builders, server is stopped" );
				else {
					if( sourceBuilders == loader.getBuilders() ) {
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
			
			abortTransaction();
			return( false );
		}
	}

	private boolean saveBuilders() {
		if( !continueTransaction() )
			return( false );
		
		if( builders == null )
			return( true );
		
		try {
			buildersOld = loader.getBuilders();
			loader.setBuilders( this , builders );
			trace( "transaction builders: save=" + builders.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save builders" );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeDirectory( ServerDirectory sourceDirectory ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( directory != null )
					return( true );
				
				if( !engine.isRunning() )
					error( "unable to change directory, server is stopped" );
				else {
					if( sourceDirectory == loader.getDirectory() ) {
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
			
			abortTransaction();
			return( false );
		}
	}

	private boolean saveDirectory() {
		if( !continueTransaction() )
			return( false );
		
		if( directory == null )
			return( true );
		
		try {
			directoryOld = loader.getDirectory();
			loader.setDirectory( this , directory );
			trace( "transaction directory: save=" + directory.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save directory" );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeSettings( ServerSettings sourceSettings ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( settings != null )
					return( true );
				
				if( sourceSettings == loader.getSettings() ) {
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
			
			abortTransaction();
			return( false );
		}
	}

	private boolean saveSettings() {
		if( !continueTransaction() )
			return( false );
		
		if( settings == null )
			return( true );
		
		try {
			settingsOld = loader.getSettings();
			loader.setSettings( this , settings );
			trace( "transaction server settings: save=" + settings.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save settings" );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeMetadata( ActionInit action , Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( metadata != null )
					return( true );
				
				ServerProductMeta sourceMetadata = meta.getStorage( action );
				if( sourceMetadata.isPrimary() ) {
					this.action = action;
					metadataOld = sourceMetadata;
					metadata = sourceMetadata.copy( action );
					sessionMeta = meta;
					trace( "transaction product storage meta: source=" + sourceMetadata.objectId + ", copy=" + metadata.objectId );
					if( metadata != null )
						return( true );
				}
				else
					error( "Unable to change old metadata, id=" + sourceMetadata.objectId );
			}
			catch( Throwable e ) {
				handle( e , "unable to save metadata" );
			}
			
			abortTransaction();
			return( false );
		}
	}
	
	public boolean deleteMetadata( ActionInit action , Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( metadata != null )
					return( true );
				
				ServerProductMeta sourceMetadata = meta.getStorage( action );
				if( sourceMetadata.isPrimary() ) {
					deleteMetadata = true;
					metadataOld = sourceMetadata;
					trace( "transaction product storage meta: going delete=" + sourceMetadata.objectId );
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to save metadata" );
			}
			
			abortTransaction();
			return( false );
		}
	}
	
	private boolean saveMetadata() {
		if( !continueTransaction() )
			return( false );

		try {
			if( deleteMetadata ) {
				if( metadataOld == null )
					return( true );
					
				loader.deleteMetadata( this , metadataOld );
				trace( "transaction product storage meta: delete=" + metadataOld.objectId );
			}
			else {
				if( metadata == null )
					return( true );
					
				loader.setMetadata( this , metadata );
				sessionMeta.setStorage( metadata );
				trace( "transaction product storage meta: save=" + metadata.objectId );
			}
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save metadata" );
		}

		abortTransaction();
		return( false );
	}
	
	private void checkTransaction() throws Exception {
		if( !continueTransaction() )
			exit( _Error.TransactionAborted0 , "Transaction is aborted" , null );
	}

	protected void checkTransactionResources() throws Exception {
		checkTransaction();
		if( resources == null )
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

	protected void checkTransactionSettings() throws Exception {
		checkTransaction();
		if( settings == null )
			exit( _Error.TransactionMissingSettingsChanges0 , "Missing settings changes" , null );
	}

	protected void checkTransactionMetadata( ServerProductMeta sourceMeta ) throws Exception {
		checkTransaction();
		if( metadata == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		if( sourceMeta != metadata )
			exit1( _Error.InternalTransactionError1 , "Internal error: invalid transaction metadata" , "invalid transaction metadata" );
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

	// helpers
	public ServerAuthResource getResource( ServerAuthResource resource ) throws Exception {
		return( resources.getResource( resource.NAME ) );
	}
	
	public ServerProjectBuilder getBuilder( ServerProjectBuilder builder ) throws Exception {
		return( builders.getBuilder( builder.NAME ) );
	}
	
	public ServerSystem getSystem( ServerSystem system ) throws Exception {
		return( directory.getSystem( system.NAME ) );
	}
	
	public ServerProduct getProduct( ServerProduct product ) throws Exception {
		return( directory.getProduct( product.NAME ) );
	}
	
	public Meta getMeta() {
		return( metadata.meta );
	}
	
	public MetaEnv getMetaEnv( MetaEnv env ) throws Exception {
		return( metadata.findEnvironment( env.ID ) );
	}

	public MetaEnvDC getMetaEnvDC( MetaEnvDC dc ) throws Exception {
		MetaEnv env = getMetaEnv( dc.env );
		return( env.findDC( dc.NAME ) );
	}

	public MetaEnvServer getMetaEnvServer( MetaEnvServer server ) throws Exception {
		MetaEnvDC dc = getMetaEnvDC( server.dc );
		return( dc.findServer( server.NAME ) );
	}

}
