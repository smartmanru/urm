package org.urm.server;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.Meta;

public class ServerTransaction {

	public ActionBase action;
	public ServerEngine engine;
	public ServerLoader loader;
	
	public ServerRegistry registry;
	public ServerProductMeta metadata;

	private ServerRegistry registryOld;
	private ServerProductMeta metadataOld;
	public boolean createMetadata;
	
	public ServerTransaction( ActionBase action ) {
		this.action = action;
		this.engine = action.engine;
		this.loader = action.actionInit.getMetaLoader();
		
		registry = null;
		metadata = null;
		createMetadata = false;
	}

	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( action , this ) )
				return( false );
		
			registry = null;
			metadata = null;
			registryOld = null;
			metadataOld = null;
			createMetadata = false;
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
				action.log( "unable to restore registry" , e );
			}
			
			try {
				if( metadataOld != null ) {
					loader.setMetadata( this , metadataOld );
					metadataOld = null;
					createMetadata = false;
				}
			}
			catch( Throwable e ) {
				action.log( "unable to restore metadata" , e );
			}
			
			engine.abortTransaction( this );
		}
	}
	
	public boolean commitTransaction() {
		synchronized( engine ) {
			if( !continueTransaction() )
				return( false );

			boolean res = true;
			if( res )
				res = saveMetadata();
			if( res )
				res = saveRegisty();
			
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

	public boolean changeRegistry( ServerRegistry sourceRegistry ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( registry != null )
					return( true );
				
				if( sourceRegistry == loader.getRegistry() ) {
					registry = sourceRegistry.copy( action );
					if( registry != null )
						return( true );
				}
			}
			catch( Throwable e ) {
				action.log( e );
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
			action.log( "unable to save registry" , e );
		}

		abortTransaction();
		return( false );
	}

	public boolean changeMetadata( Meta sourceMetadata ) {
		synchronized( engine ) {
			try {
				if( !continueTransaction() )
					return( false );
					
				if( metadata != null )
					return( true );
				
				if( sourceMetadata.storage == loader.getMetaStorage( sourceMetadata.product.CONFIG_PRODUCT ) ) {
					metadata = sourceMetadata.storage.copy( action );
					if( metadata != null )
						return( true );
				}
			}
			catch( Throwable e ) {
				action.log( "unable to save registry" , e );
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
				metadataOld = loader.getMetaStorage( metadata.meta.product.CONFIG_PRODUCT );
			loader.setMetadata( this , metadata );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "unable to save metadata" , e );
		}

		abortTransaction();
		return( false );
	}
	
	private void checkTransaction() throws Exception {
		if( !continueTransaction() )
			action.exitUnexpectedState();
	}
	
	// helpers
	public ServerSystem getNewSystem( ServerSystem system ) throws Exception {
		return( registry.getSystem( action , system.NAME ) );
	}
	
	public ServerProduct getNewProduct( ServerProduct product ) throws Exception {
		return( registry.getProduct( action , product.NAME ) );
	}
	
	// transactional operations
	public void addSystem( ServerSystem system ) throws Exception {
		checkTransaction();
		registry.addSystem( this , system );
	}
	
	public void modifySystem( ServerSystem system , ServerSystem systemNew ) throws Exception {
		checkTransaction();
		system.modifySystem( this , systemNew );
	}

	public void deleteSystem( ServerSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransaction();
		action.artefactory.deleteSystemResources( this , system , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		registry.deleteSystem( this , system );
	}
	
	public void addProduct( ServerProduct product ) throws Exception {
		checkTransaction();
		createMetadata = true;
		registry.createProduct( this , product );
		metadata = loader.createMetadata( this , registry , product );
	}
	
	public void modifyProduct( ServerProduct product , ServerProduct productNew ) throws Exception {
		checkTransaction();
		product.modifyProduct( this , productNew );
	}

	public void deleteProduct( ServerProduct product , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransaction();
		action.artefactory.deleteProductResources( this , product , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		registry.deleteProduct( this , product );
	}
	
}
