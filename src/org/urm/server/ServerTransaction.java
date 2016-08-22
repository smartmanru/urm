package org.urm.server;

import org.urm.common.ExitException;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionInit;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaProductVersion;

public class ServerTransaction {

	public ServerEngine engine;
	public ServerLoader loader;
	public ActionInit metadataAction;
	
	public ServerRegistry registry;
	public ServerProductMeta metadata;

	private ServerRegistry registryOld;
	private ServerProductMeta metadataOld;
	public boolean createMetadata;
	public boolean deleteMetadata;
	
	public ServerTransaction( ServerEngine engine ) {
		this.engine = engine;
		this.loader = engine.getLoader();
		
		registry = null;
		metadata = null;
		createMetadata = false;
		deleteMetadata = false;
	}

	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( this ) )
				return( false );
		
			registry = null;
			metadata = null;
			registryOld = null;
			metadataOld = null;
			createMetadata = false;
			deleteMetadata = false;
			return( true );
		}
	}

	public void abortTransaction() {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return;
				
				if( registryOld != null ) {
					loader.setRegistry( this , registryOld );
					registryOld = null;
				}
			}
			catch( Throwable e ) {
				log( "unable to restore registry" , e );
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
				log( "unable to restore metadata" , e );
			}
			
			engine.abortTransaction( this );
			stopAction();
		}
	}

	private void stopAction() {
		try {
			if( metadataAction != null )
				engine.finishAction( metadataAction );
			metadataAction = null;
		}
		catch( Throwable e ) {
			log( "unable to restore metadata" , e );
		}
	}
	
	public boolean commitTransaction() {
		synchronized( engine ) {
			if( !continueTransaction() )
				return( false );

			boolean res = true;
			if( res )
				res = saveRegisty();
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

	public void log( String s , Throwable e ) {
		if( metadataAction != null )
			metadataAction.log( s , e );
		else
		if( engine.serverAction != null )
			engine.serverAction.log( s , e );
		else {
			System.out.println( "transaction: " + s );
			e.printStackTrace();
		}
	}
	
	public void info( String s ) {
		if( metadataAction != null )
			metadataAction.info( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.info( s );
		else {
			System.out.println( "transaction (info): " + s );
		}
	}
	
	public void debug( String s ) {
		if( metadataAction != null )
			metadataAction.debug( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.debug( s );
		else {
			System.out.println( "transaction (debug): " + s );
		}
	}
	
	public void error( String s ) {
		if( metadataAction != null )
			metadataAction.error( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.error( s );
		else {
			System.out.println( "transaction (error): " + s );
		}
	}
	
	public void trace( String s ) {
		if( metadataAction != null )
			metadataAction.trace( s );
		else
		if( engine.serverAction != null )
			engine.serverAction.trace( s );
		else {
			System.out.println( "transaction (trace): " + s );
		}
	}
	
	public boolean changeRegistry( ServerRegistry sourceRegistry ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( registry != null )
					return( true );
				
				if( sourceRegistry == loader.getRegistry() ) {
					registry = sourceRegistry.copy();
					if( registry != null )
						return( true );
				}
			}
			catch( Throwable e ) {
				log( "unable to change registry" , e );
			}
			
			abortTransaction();
			return( false );
		}
	}

	private boolean saveRegisty() {
		if( !continueTransaction() )
			return( false );
		
		if( registry == null )
			return( true );
		
		try {
			registryOld = loader.getRegistry();
			loader.setRegistry( this , registry );
			return( true );
		}
		catch( Throwable e ) {
			log( "unable to save registry" , e );
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
					metadataAction = engine.createTemporaryAction( "meta" );
					metadata = sourceMetadata.storage.copy( metadataAction );
					if( metadata != null )
						return( true );
				}
			}
			catch( Throwable e ) {
				log( "unable to save registry" , e );
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
					metadataAction = engine.createTemporaryAction( "meta" );
					return( true );
				}
			}
			catch( Throwable e ) {
				log( "unable to save registry" , e );
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
			log( "unable to save metadata" , e );
		}

		abortTransaction();
		return( false );
	}
	
	private void checkTransaction() throws Exception {
		if( !continueTransaction() )
			exit( "transaction is aborted" );
	}

	private void checkTransactionRegistry() throws Exception {
		checkTransaction();
		if( registry == null )
			exit( "missing registry changes" );
	}

	private void checkTransactionMetadata() throws Exception {
		checkTransaction();
		if( metadata == null )
			exit( "missing metadata changes" );
	}

	private void checkTransactionAll() throws Exception {
		checkTransactionRegistry();
		checkTransactionMetadata();
	}

	public void exit( String msg ) throws Exception {
		throw new ExitException( msg );
	}
	
	// helpers
	public ServerSystem getSystem( ServerSystem system ) throws Exception {
		return( registry.getSystem( system.NAME ) );
	}
	
	public ServerProduct getProduct( ServerProduct product ) throws Exception {
		return( registry.getProduct( product.NAME ) );
	}
	
	// transactional operations
	public void addSystem( ServerSystem system ) throws Exception {
		checkTransactionRegistry();
		registry.addSystem( this , system );
	}
	
	public void modifySystem( ServerSystem system , ServerSystem systemNew ) throws Exception {
		checkTransactionRegistry();
		system.modifySystem( this , systemNew );
	}

	public void deleteSystem( ServerSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionRegistry();
		for( String productName : system.getProducts() ) {
			ServerProduct product = system.getProduct( productName );
			metadataAction.artefactory.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		}
		registry.deleteSystem( this , system );
	}
	
	public void createProduct( ServerProduct product ) throws Exception {
		checkTransactionRegistry();
		metadataAction = engine.createTemporaryAction( "meta" );
		metadataAction.artefactory.createProductResources( this , product );
		createMetadata = true;
		registry.createProduct( this , product );
		metadata = loader.createMetadata( this , registry , product );
	}
	
	public void modifyProduct( ServerProduct product , ServerProduct productNew ) throws Exception {
		checkTransactionRegistry();
		product.modifyProduct( this , productNew );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransactionAll();
		metadataAction.artefactory.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		registry.deleteProduct( this , product );
		metadata = null;
	}

	public void setRegistryServerProperties( PropertySet props ) throws Exception {
		checkTransactionRegistry();
		registry.setRegistryServerProperties( this , props );
	}
	
	public void setProductVersion( MetaProductVersion version ) throws Exception {
		checkTransactionMetadata();
		metadata.setVersion( this , version );
	}
	
}
