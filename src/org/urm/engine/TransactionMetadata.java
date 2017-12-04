package org.urm.engine;

import org.urm.engine.status.EngineStatus;
import org.urm.meta.ProductMeta;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;

public class TransactionMetadata {

	TransactionBase transaction;
	
	public boolean createMetadata;
	public boolean deleteMetadata;
	public Meta sessionMeta;

	public ProductMeta metadata;
	protected ProductMeta metadataOld;
	
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
				transaction.setProductMetadata( metadataOld );
			
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
		ProductMeta sourceMetadata = meta.getStorage( transaction.action );
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
		ProductMeta sourceMetadata = meta.getStorage( transaction.action );
		if( sourceMetadata.isPrimary() ) {
			deleteMetadata = true;
			metadataOld = sourceMetadata;
			transaction.trace( "transaction product storage meta: going delete=" + sourceMetadata.objectId );
			return( true );
		}
		
		return( false );
	}

	public boolean saveProduct() throws Exception {
		if( deleteMetadata ) {
			if( metadataOld == null )
				return( true );

			deleteProduct( metadataOld );
			transaction.deleteProductMetadata( metadataOld );
			transaction.trace( "transaction product storage meta: delete=" + metadataOld.objectId );
		}
		else {
			if( metadata == null )
				return( true );
				
			transaction.setProductMetadata( metadata );
			sessionMeta.replaceStorage( transaction.action , metadata );
			transaction.trace( "transaction product storage meta: save=" + metadata.objectId );
			if( createMetadata )
				createProduct( metadata );
			else
				modifyProduct( metadataOld , metadata );
		}
		
		return( true );
	}

	public void checkTransactionMetadata( ProductMeta sourceMeta ) throws Exception {
		if( ( deleteMetadata == false && metadata == null ) || ( deleteMetadata == true && metadataOld == null ) )
			transaction.exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		if( ( deleteMetadata == false && metadata != sourceMeta ) || ( deleteMetadata == true && metadataOld != sourceMeta ) )
			transaction.exit1( _Error.InternalTransactionError1 , "Internal error: invalid transaction metadata" , "invalid transaction metadata" );
	}

	private void createProduct( ProductMeta metadata ) throws Exception {
		AppProduct product = transaction.action.findProduct( metadata.name );
		
		EngineStatus status = transaction.action.getServerStatus();
		status.createProduct( transaction.action , product , metadata );
		
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.createProduct( transaction.action , metadata.name );
	}
	
	private void deleteProduct( ProductMeta metadata ) throws Exception {
		EngineStatus status = transaction.action.getServerStatus();
		status.deleteProduct( transaction.action , metadata );
		
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.deleteProduct( transaction.action , metadata.name );
	}
	
	private void modifyProduct( ProductMeta metadataOld , ProductMeta metadataNew ) throws Exception {
		EngineStatus status = transaction.action.getServerStatus();
		status.modifyProduct( transaction.action , metadataOld , metadataNew );
		
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.modifyProduct( transaction.action , metadata.name );
	}
	
}
