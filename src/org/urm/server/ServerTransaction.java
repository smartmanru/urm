package org.urm.server;

import org.urm.server.action.ActionBase;
import org.urm.server.meta.FinalLoader;
import org.urm.server.meta.FinalMetaProduct;
import org.urm.server.meta.FinalMetaStorage;
import org.urm.server.meta.FinalMetaSystem;
import org.urm.server.meta.FinalRegistry;
import org.urm.server.meta.Metadata;

public class ServerTransaction {

	public ActionBase action;
	public ServerEngine engine;
	public FinalLoader loader;
	
	public FinalRegistry registry;
	public FinalMetaStorage metadata;

	private FinalRegistry registryOld;
	private FinalMetaStorage metadataOld;
	
	public ServerTransaction( ActionBase action ) {
		this.action = action;
		this.engine = action.engine;
		this.loader = action.actionInit.getMetaLoader();
		
		registry = null;
		metadata = null;
	}

	public boolean startTransaction() {
		synchronized( engine ) {
			if( !engine.startTransaction( action , this ) )
				return( false );
		
			registry = null;
			metadata = null;
			registryOld = null;
			metadataOld = null;
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

	public boolean changeRegistry( FinalRegistry sourceRegistry ) {
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
	
	public boolean changeMetadata( Metadata sourceMetadata ) {
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
				action.log( e );
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
			metadataOld = loader.getMetaStorage( metadata.meta.product.CONFIG_PRODUCT );
			loader.setMetadata( this , metadata );
			return( true );
		}
		catch( Throwable e ) {
			action.log( "unable to save metadata" , e );
		}

		try {
			loader.setMetadata( this , metadataOld );
		}
		catch( Throwable e ) {
			action.log( "unable to restore metadata" , e );
		}
		
		abortTransaction();
		return( false );
	}
	
	private void checkTransaction() throws Exception {
		if( !continueTransaction() )
			action.exitUnexpectedState();
	}
	
	// transactional operations
	public void addSystem( FinalMetaSystem system ) throws Exception {
		checkTransaction();
		registry.addSystem( this , system );
	}
	
	public void modifySystem( FinalMetaSystem system , FinalMetaSystem systemNew ) throws Exception {
		checkTransaction();
		system.modifySystem( this , systemNew );
	}

	public void deleteSystem( FinalMetaSystem system , boolean fsDeleteFlag , boolean vcsDeleteFlag , boolean logsDeleteFlag ) throws Exception {
		checkTransaction();
		action.artefactory.deleteSystemResources( this , system , fsDeleteFlag , vcsDeleteFlag , logsDeleteFlag );
		registry.deleteSystem( this , system );
	}
	
	public void addProduct( FinalMetaProduct product ) throws Exception {
		checkTransaction();
		registry.createProduct( this , product );
		metadata = loader.createMetadata( this , registry , product );
	}
	
	public void modifyProduct( FinalMetaProduct product , FinalMetaProduct productNew ) throws Exception {
		checkTransaction();
		product.modifyProduct( this , productNew );
	}

}
