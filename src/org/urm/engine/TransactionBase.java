package org.urm.engine;

import java.util.HashMap;
import java.util.Map;

import org.urm.common.RunError;
import org.urm.engine.action.ActionInit;
import org.urm.meta.ServerObject;
import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerAuthResource;
import org.urm.meta.engine.ServerBase;
import org.urm.meta.engine.ServerBuilders;
import org.urm.meta.engine.ServerDirectory;
import org.urm.meta.engine.ServerInfrastructure;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.engine.ServerProduct;
import org.urm.meta.engine.ServerProjectBuilder;
import org.urm.meta.engine.ServerResources;
import org.urm.meta.engine.ServerSettings;
import org.urm.meta.engine.ServerSystem;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaEnvDC;
import org.urm.meta.product.MetaEnvServer;
import org.urm.meta.product.MetaEnvServerNode;

public class TransactionBase extends ServerObject {

	public ServerEngine engine;
	public ActionInit action;
	public RunError error;
	
	public ServerInfrastructure infra;
	public ServerBase base;
	
	public ServerSettings settings;
	public ServerResources resources;
	public ServerBuilders builders;
	public ServerDirectory directory;

	protected ServerSettings settingsOld;
	protected ServerResources resourcesOld;
	protected ServerBuilders buildersOld;
	protected ServerDirectory directoryOld;

	private Map<String,TransactionMetadata> productMeta;
	
	public TransactionBase( ServerEngine engine , ActionInit action ) {
		super( null );
		this.engine = engine;
		this.action = action;
		
		resources = null;
		builders = null;
		directory = null;
		settings = null;
		infra = null;
		base = null;
		
		productMeta = new HashMap<String,TransactionMetadata>(); 
		engine.serverAction.trace( "transaction created id=" + objectId );
	}
	
	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( this ) )
				return( false );
		
			action.setTransaction( this );
			resources = null;
			builders = null;
			directory = null;
			settings = null;
			resourcesOld = null;
			buildersOld = null;
			directoryOld = null;
			settingsOld = null;
			infra = null;
			base = null;
			
			return( true );
		}
	}

	public void abortTransaction( boolean save ) {
		synchronized( engine ) {
			if( !continueTransaction() )
				return;
			
			try {
				if( resourcesOld != null ) {
					if( save )
						action.setResources( this , resourcesOld );
					resourcesOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( buildersOld != null ) {
					if( save )
						action.setBuilders( this , buildersOld );
					buildersOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore resources" );
			}
			
			try {
				if( directoryOld != null ) {
					if( save )
						action.setDirectory( this , directoryOld );
					directoryOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore directory" );
			}
			
			try {
				if( settingsOld != null ) {
					if( save )
						action.setServerSettings( this , settingsOld );
					settingsOld = null;
				}
			}
			catch( Throwable e ) {
				handle( e , "unable to restore settings" );
			}
			
			try {
				for( TransactionMetadata meta : productMeta.values() )
					meta.abortTransaction( save );
				productMeta.clear();
			}
			catch( Throwable e ) {
				handle( e , "unable to restore metadata" );
			}
			
			engine.abortTransaction( this );
		}
		
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
				res = saveResources();
			if( res )
				res = saveBuilders();
			if( res )
				res = saveDirectory();
			if( res )
				res = saveSettings();
			if( res )
				res = saveInfrastructure();
			if( res )
				res = saveBase();
			if( res )
				res = saveMetadata();
			
			if( res ) {
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
		action.trace( s );
	}
	
	public boolean changeInfrastructure( ServerInfrastructure sourceInfrastructure ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( infra != null )
					return( true );
				
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
			action.saveInfrastructure( this );
			trace( "transaction server infrastructure: save done" );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save infrastructure" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeBase( ServerBase sourceBase ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( base != null )
					return( true );
				
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
			action.saveBase( this );
			trace( "transaction server basse: save done" );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save base data" );
		}

		abortTransaction( true );
		return( false );
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
					resourcesOld = action.getActiveResources();
					if( sourceResources == resourcesOld ) {
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
		
		try {
			action.setResources( this , resources );
			trace( "transaction resources: save=" + resources.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save resources" );
		}

		abortTransaction( true );
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
					buildersOld = action.getActiveBuilders();
					if( sourceBuilders == buildersOld ) {
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
		
		try {
			action.setBuilders( this , builders );
			trace( "transaction builders: save=" + builders.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save builders" );
		}

		abortTransaction( true );
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
					directoryOld = action.getActiveDirectory();
					if( sourceDirectory == directoryOld ) {
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
		
		try {
			action.setDirectory( this , directory );
			trace( "transaction directory: save=" + directory.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save directory" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeMonitoring( ServerMonitoring sourceMonitoring ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
				return( true );
			}
			catch( Throwable e ) {
				handle( e , "unable to change settings" );
			}
			
			abortTransaction( false );
			return( false );
		}
	}
	
	public boolean changeSettings( ServerSettings sourceSettings ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( settings != null )
					return( true );
				
				settingsOld = action.getActiveServerSettings();
				if( sourceSettings == settingsOld ) {
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
			action.setServerSettings( this , settings );
			trace( "transaction server settings: save=" + settings.objectId );
			return( true );
		}
		catch( Throwable e ) {
			handle( e , "unable to save settings" );
		}

		abortTransaction( true );
		return( false );
	}

	public boolean changeMetadata( Meta meta ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );

				TransactionMetadata tm = productMeta.get( meta.name ); 
				if( tm != null )
					return( true );
				
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

	protected void checkTransactionBase() throws Exception {
		checkTransaction();
		if( base == null )
			exit( _Error.TransactionMissingBaseChanges0 , "Missing base changes" , null );
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

	protected void checkTransactionMonitoring() throws Exception {
		checkTransaction();
	}

	protected void checkTransactionMetadata( ServerProductMeta sourceMeta ) throws Exception {
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
	
	public Meta findTransactionSessionProductMetadata( String productName ) {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			return( null );
		
		return( tm.sessionMeta );
	}
	
	public ServerInfrastructure getTransactionInfrastructure() {
		return( infra );
	}

	private void addTransactionMeta( Meta meta , TransactionMetadata tm ) {
		productMeta.put( meta.name , tm );
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
	
	public MetaEnv getMetaEnv( MetaEnv env ) throws Exception {
		ServerProductMeta metadata = getTransactionMetadata( env.meta );
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

	public ServerProductMeta getTransactionMetadata( Meta meta ) throws Exception {
		TransactionMetadata tm = productMeta.get( meta.name );
		if( tm == null )
			exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		return( tm.metadata );
	}
	
	protected void createProductMetadata( ServerDirectory directory , ServerProduct product ) throws Exception {
		TransactionMetadata tm = productMeta.get( product.NAME );
		if( tm != null )
			action.exitUnexpectedState();
		
		Meta meta = action.createProductMetadata( this , directory , product );
		tm = new TransactionMetadata( this );
		tm.createProduct( meta );
		addTransactionMeta( meta , tm );
	}

	public Meta getTransactionProductMetadata( String productName ) throws Exception {
		TransactionMetadata tm = productMeta.get( productName );
		if( tm == null )
			action.exitUnexpectedState();
		return( tm.metadata.meta );
	}
	
}
