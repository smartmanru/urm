package org.urm.engine;

import org.urm.meta.ServerProductMeta;
import org.urm.meta.engine.ServerMonitoring;
import org.urm.meta.product.Meta;

public class TransactionMetadata {

	TransactionBase transaction;
	
	public boolean createMetadata;
	public boolean deleteMetadata;
	public Meta sessionMeta;

	public ServerProductMeta metadata;
	protected ServerProductMeta metadataOld;
	
	public TransactionMetadata( TransactionBase transaction ) {
		this.transaction = transaction;
		
		metadata = null;
		metadataOld = null;
		createMetadata = false;
		deleteMetadata = false;
		sessionMeta = null;
	}

	public void createProduct( Meta sessionMeta ) throws Exception {
		this.sessionMeta = sessionMeta;
		createMetadata = true;
		metadata = sessionMeta.getStorage( transaction.getAction() );
	}

	public void abortTransaction( boolean save ) {
		if( metadataOld == null )
			return;

		String name = metadataOld.name;
		try {
			if( save )
				transaction.action.setProductMetadata( transaction , metadataOld );
			
			if( !deleteMetadata )
				sessionMeta.replaceStorage( transaction.action , metadataOld );
			
			createMetadata = false;
			deleteMetadata = false;
			sessionMeta = null;
			metadata = null;
			metadataOld = null;
		}
		catch( Throwable e ) {
			transaction.handle( e , "Unable to restore metadata, product=" + name );
		}
	}

	public boolean changeProduct( Meta meta ) throws Exception {
		ServerProductMeta sourceMetadata = meta.getStorage( transaction.action );
		if( sourceMetadata.isPrimary() ) {
			metadataOld = sourceMetadata;
			metadata = sourceMetadata.copy( transaction.action );
			sessionMeta = transaction.action.getProductMetadata( meta.name );
			transaction.trace( "transaction product storage meta: source=" + sourceMetadata.objectId + ", copy=" + metadata.objectId );
			if( metadata != null )
				return( true );
		}
		else
			transaction.error( "Unable to change old metadata, id=" + sourceMetadata.objectId );
		
		return( false );
	}

	public boolean deleteProduct( Meta meta ) throws Exception {
		ServerProductMeta sourceMetadata = meta.getStorage( transaction.action );
		if( sourceMetadata.isPrimary() ) {
			deleteMetadata = true;
			metadataOld = sourceMetadata;
			transaction.trace( "transaction product storage meta: going delete=" + sourceMetadata.objectId );
			return( true );
		}
		
		return( false );
	}

	public boolean saveProduct() throws Exception {
		ServerMonitoring mon = transaction.action.getServerMonitoring();
		
		if( deleteMetadata ) {
			if( metadataOld == null )
				return( true );

			mon.deleteProduct( metadataOld );
			transaction.action.deleteProductMetadata( transaction , metadataOld );
			transaction.trace( "transaction product storage meta: delete=" + metadataOld.objectId );
		}
		else {
			if( metadata == null )
				return( true );
				
			transaction.action.setProductMetadata( transaction , metadata );
			sessionMeta.replaceStorage( transaction.action , metadata );
			transaction.trace( "transaction product storage meta: save=" + metadata.objectId );
			if( createMetadata )
				mon.createProduct( metadata );
			else
				mon.modifyProduct( metadataOld , metadata );
		}
		
		return( true );
	}

	public void checkTransactionMetadata( ServerProductMeta sourceMeta ) throws Exception {
		if( ( deleteMetadata == false && metadata == null ) || ( deleteMetadata == true && metadataOld == null ) )
			transaction.exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		if( ( deleteMetadata == false && metadata != sourceMeta ) || ( deleteMetadata == true && metadataOld != sourceMeta ) )
			transaction.exit1( _Error.InternalTransactionError1 , "Internal error: invalid transaction metadata" , "invalid transaction metadata" );
	}
	
}
