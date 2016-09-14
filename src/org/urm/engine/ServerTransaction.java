package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.RunError;
import org.urm.common.PropertySet;
import org.urm.engine.action.ActionInit;
import org.urm.engine.meta.Meta;
import org.urm.engine.meta.MetaProductVersion;
import org.urm.engine.meta.Meta.VarBUILDMODE;

public class ServerTransaction {

	public ServerEngine engine;
	public ServerLoader loader;
	public ActionInit action;
	public RunError error;
	
	private ServerResources resources;
	private ServerBuilders builders;
	private ServerDirectory directory;
	private ServerSettings settings;
	private ServerProductMeta metadata;

	private ServerResources resourcesOld;
	private ServerBuilders buildersOld;
	private ServerDirectory directoryOld;
	private ServerSettings settingsOld;
	private ServerProductMeta metadataOld;
	public boolean createMetadata;
	public boolean deleteMetadata;
	
	public ServerTransaction( ServerEngine engine ) {
		this.engine = engine;
		this.loader = engine.getLoader();
		
		resources = null;
		builders = null;
		directory = null;
		settings = null;
		metadata = null;
		createMetadata = false;
		deleteMetadata = false;
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
				handle( "unable to restore resources" , e );
			}
			
			try {
				if( buildersOld != null ) {
					loader.setBuilders( this , buildersOld );
					buildersOld = null;
				}
			}
			catch( Throwable e ) {
				handle( "unable to restore resources" , e );
			}
			
			try {
				if( directoryOld != null ) {
					loader.setDirectory( this , directoryOld );
					directoryOld = null;
				}
			}
			catch( Throwable e ) {
				handle( "unable to restore directory" , e );
			}
			
			try {
				if( settingsOld != null ) {
					loader.setSettings( this , settingsOld );
					settingsOld = null;
				}
			}
			catch( Throwable e ) {
				handle( "unable to restore settings" , e );
			}
			
			try {
				if( metadataOld != null ) {
					loader.setMetadata( this , metadataOld );
					metadataOld = null;
					createMetadata = false;
					deleteMetadata = false;
				}
			}
			catch( Throwable e ) {
				handle( "unable to restore metadata" , e );
			}
			
			engine.abortTransaction( this );
			stopAction();
		}
	}

	public ActionInit getAction() throws Exception {
		if( action == null )
			action = engine.createTemporaryAction( "transaction" );
		return( action );
	}
	
	private void stopAction() {
		try {
			if( action != null )
				engine.finishAction( action );
			action = null;
		}
		catch( Throwable e ) {
			handle( "unable to restore metadata" , e );
		}
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
				stopAction();
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

	public void handle( String s , Throwable e ) {
		action.log( s , e );
		if( e.getClass() == RunError.class )
			error = ( RunError )e;
		else
			error = new RunError( _Error.InternalTransactionError1 , "Internal transaction error: " + s , new String[] { s } );
	}
	
	public void handle0( Throwable e , int errorCode , String msg ) throws RunError {
		action.error( msg );
		error = new RunError( e , errorCode , msg , null );
		throw error;
	}
	
	public void handle1( Throwable e , int errorCode , String msg , String param1 ) throws RunError {
		action.error( msg );
		error = new RunError( e , errorCode , msg , new String[] { param1 } );
		throw error;
	}
	
	public void handle2( Throwable e , int errorCode , String msg , String param1 , String param2 ) throws RunError {
		action.error( msg );
		error = new RunError( e , errorCode , msg , new String[] { param1 , param2 } );
		throw error;
	}
	
	public void handle3( Throwable e , int errorCode , String msg , String param1 , String param2 , String param3 ) throws RunError {
		action.error( msg );
		error = new RunError( errorCode , msg , new String[] { param1 , param2 , param3 } );
		throw error;
	}
	
	public void handle4( Throwable e , int errorCode , String msg , String param1 , String param2 , String param3 , String param4 ) throws RunError {
		action.error( msg );
		error = new RunError( errorCode , msg , new String[] { param1 , param2 , param3 , param4 } );
		throw error;
	}
	
	public String getError() {
		if( error != null )
			return( error.errorMessage );
		return( "" );
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
						if( resources != null )
							return( true );
					}
					else
						error( "unable to change old resources" );
				}
			}
			catch( Throwable e ) {
				handle( "unable to change resources" , e );
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
			return( true );
		}
		catch( Throwable e ) {
			handle( "unable to save resources" , e );
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
						if( builders != null )
							return( true );
					}
					else
						error( "unable to change old builders" );
				}
			}
			catch( Throwable e ) {
				handle( "unable to change builders" , e );
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
			return( true );
		}
		catch( Throwable e ) {
			handle( "unable to save builders" , e );
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
						if( directory != null )
							return( true );
					}
					else
						error( "unable to change old directory" );
				}
			}
			catch( Throwable e ) {
				handle( "unable to change directory" , e );
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
			return( true );
		}
		catch( Throwable e ) {
			handle( "unable to save directory" , e );
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
					if( settings != null )
						return( true );
				}
				else
					error( "unable to change old settings" );
			}
			catch( Throwable e ) {
				handle( "unable to change settings" , e );
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
			return( true );
		}
		catch( Throwable e ) {
			handle( "unable to save settings" , e );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeMetadata( ServerProduct product , Meta sourceMetadata ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( metadata != null )
					return( true );
				
				if( sourceMetadata.storage == loader.findMetaStorage( product.NAME ) ) {
					ActionBase za = getAction();
					metadata = sourceMetadata.storage.copy( za );
					if( metadata != null )
						return( true );
				}
				else
					error( "unable to change old metadata" );
			}
			catch( Throwable e ) {
				handle( "unable to save metadata" , e );
			}
			
			abortTransaction();
			return( false );
		}
	}
	
	public boolean deleteMetadata( ServerProduct product , Meta sourceMetadata ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( metadata != null )
					return( true );
				
				if( sourceMetadata.storage == loader.findMetaStorage( product.NAME ) ) {
					deleteMetadata = true;
					metadata = sourceMetadata.storage;
					return( true );
				}
			}
			catch( Throwable e ) {
				handle( "unable to save metadata" , e );
			}
			
			abortTransaction();
			return( false );
		}
	}
	
	private boolean saveMetadata() {
		if( !continueTransaction() )
			return( false );

		if( metadata == null )
			return( true );
			
		try {
			if( !createMetadata )
				metadataOld = loader.findMetaStorage( metadata.name );
			if( deleteMetadata )
				loader.deleteMetadata( this , metadata );
			else
				loader.setMetadata( this , metadata );
			return( true );
		}
		catch( Throwable e ) {
			handle( "unable to save metadata" , e );
		}

		abortTransaction();
		return( false );
	}
	
	private void checkTransaction() throws Exception {
		if( !continueTransaction() )
			exit( _Error.TransactionAborted0 , "transaction is aborted" , null );
	}

	private void checkTransactionResources() throws Exception {
		checkTransaction();
		if( resources == null )
			exit( _Error.TransactionMissingResourceChanges0 , "missing resources changes" , null );
	}

	private void checkTransactionBuilders() throws Exception {
		checkTransaction();
		if( builders == null )
			exit( _Error.TransactionMissingBuildersChanges0 , "missing builders changes" , null );
	}

	private void checkTransactionDirectory() throws Exception {
		checkTransaction();
		if( directory == null )
			exit( _Error.TransactionMissingDirectoryChanges0 , "missing directory changes" , null );
	}

	private void checkTransactionSettings() throws Exception {
		checkTransaction();
		if( settings == null )
			exit( _Error.TransactionMissingSettingsChanges0 , "missing settings changes" , null );
	}

	private void checkTransactionMetadata() throws Exception {
		checkTransaction();
		if( metadata == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "missing metadata changes" , null );
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
	
	// transactional operations
	public void createResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.createResource( this , res );
	}
	
	public void updateResource( ServerAuthResource res , ServerAuthResource resNew ) throws Exception {
		checkTransactionResources();
		res.updateResource( this , resNew );
	}
	
	public void deleteResource( ServerAuthResource res ) throws Exception {
		checkTransactionResources();
		resources.deleteResource( this , res );
	}
	
	public void createBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.createBuilder( this , builder );
	}
	
	public void updateBuilder( ServerProjectBuilder builder , ServerProjectBuilder builderNew ) throws Exception {
		checkTransactionBuilders();
		builder.updateBuilder( this , builderNew );
	}
	
	public void deleteBuilder( ServerProjectBuilder builder ) throws Exception {
		checkTransactionBuilders();
		builders.deleteBuilder( this , builder );
	}
	
	public void addSystem( ServerSystem system ) throws Exception {
		checkTransactionDirectory();
		directory.addSystem( this , system );
	}
	
	public void modifySystem( ServerSystem system , ServerSystem systemNew ) throws Exception {
		checkTransactionDirectory();
		system.modifySystem( this , systemNew );
	}

	public void deleteSystem( ServerSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			ActionBase za = getAction();
			za.artefactory.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		directory.deleteSystem( this , system );
	}
	
	public void createProduct( ServerProduct product ) throws Exception {
		checkTransactionDirectory();
		ActionBase za = getAction();
		za.artefactory.createProductResources( this , product );
		createMetadata = true;
		directory.createProduct( this , product );
		metadata = loader.createMetadata( this , directory , product );
	}
	
	public void modifyProduct( ServerProduct product , ServerProduct productNew ) throws Exception {
		checkTransactionDirectory();
		product.modifyProduct( this , productNew );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionDirectory();
		checkTransactionMetadata();
		ActionBase za = getAction();
		za.artefactory.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		directory.deleteProduct( this , product );
		metadata = null;
	}

	public void setServerProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setServerProperties( this , props );
	}
	
	public void setProductDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductDefaultsProperties( this , props );
	}
	
	public void setProductBuildCommonDefaultsProperties( PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildCommonDefaultsProperties( this , props );
	}
	
	public void setProductBuildModeDefaultsProperties( VarBUILDMODE mode , PropertySet props ) throws Exception {
		checkTransactionSettings();
		settings.setProductBuildModeDefaultsProperties( this , mode , props );
	}
	
	public void setProductVersion( MetaProductVersion version ) throws Exception {
		checkTransactionMetadata();
		metadata.setVersion( this , version );
	}

}
