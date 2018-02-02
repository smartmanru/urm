package org.urm.engine;

import org.urm.engine.jmx.EngineJmx;
import org.urm.engine.status.EngineStatus;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.EngineMonitoring;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class TransactionMetadata {

	TransactionBase transaction;
	
	public boolean createMetadata;
	public boolean changeMetadata;
	public boolean deleteMetadata;
	public boolean recreateMetadata;
	public boolean importMetadata;
	
	public Meta sessionMeta;
	public AppProduct product;
	public ProductMeta metadata;
	protected ProductMeta metadataOld;

	private boolean matchedBeforeImport; 
	
	public TransactionMetadata( TransactionBase transaction ) {
		this.transaction = transaction;
		
		createMetadata = false;
		changeMetadata = false;
		deleteMetadata = false;
		recreateMetadata = false;
		importMetadata = false;
		matchedBeforeImport = false;
	}

	public void createProduct( Meta sessionMeta ) throws Exception {
		this.sessionMeta = sessionMeta;
		createMetadata = true;
		metadata = sessionMeta.getStorage();
	}

	public boolean importProduct( AppProduct product ) throws Exception {
		this.product = product;
		importMetadata = true;
		matchedBeforeImport = product.isMatched();
		metadataOld = product.storage;
		return( true );
	}

	public boolean changeProduct( Meta meta ) throws Exception {
		ProductMeta storage = meta.getStorage();
		if( storage.isPrimary() ) {
			changeMetadata = true;
			metadataOld = storage;
			AppSystem system = storage.product.system;
			metadata = storage.copy( transaction.action , storage.products , storage.product , system.getParameters() );
			sessionMeta = transaction.action.getProductMetadata( meta.name );
			sessionMeta.replaceStorage( transaction.action , metadata );
			transaction.trace( "transaction product storage meta: source=" + storage.objectId + ", copy=" + metadata.objectId );
			if( metadata != null )
				return( true );
		}
		else
			transaction.error( "Unable to change old metadata, id=" + storage.objectId );
		
		return( false );
	}

	public boolean recreateProduct( Meta meta ) throws Exception {
		ProductMeta storage = meta.getStorage();
		if( storage.isPrimary() ) {
			recreateMetadata = true;
			metadataOld = storage;
			metadata = null;
			sessionMeta = transaction.action.getProductMetadata( meta.name );
			transaction.trace( "transaction recreate product storage meta=" + storage.objectId );
			return( true );
		}
		else
			transaction.error( "Unable to change old metadata, id=" + storage.objectId );
		
		return( false );
	}

	public boolean deleteProduct( Meta meta ) throws Exception {
		ProductMeta sourceMetadata = meta.getStorage();
		if( sourceMetadata.isPrimary() ) {
			deleteMetadata = true;
			metadataOld = sourceMetadata;
			transaction.trace( "transaction product storage meta: going delete=" + sourceMetadata.objectId );
			return( true );
		}
		
		return( false );
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
			changeMetadata = false;
			deleteMetadata = false;
			recreateMetadata = false;
			importMetadata = false;
			
			sessionMeta = null;
			metadata = null;
			metadataOld = null;
		}
		catch( Throwable e ) {
			transaction.handle( e , "Unable to restore metadata, product=" + name );
		}
	}

	public boolean commitTransaction() throws Exception {
		if( deleteMetadata ) {
			if( metadataOld == null )
				return( false );

			AppProduct product = metadataOld.product;
			deleteProductFinish( product , metadataOld );
			transaction.deleteProductMetadata( metadataOld );
			transaction.trace( "transaction product storage meta: delete=" + metadataOld.objectId );
		}
		else
		if( createMetadata || changeMetadata || recreateMetadata ) {
			if( metadata == null )
				return( false );
				
			AppProduct product = metadata.product;
			transaction.setProductMetadata( metadata );
			product.setStorage( metadata );
			sessionMeta.replaceStorage( transaction.action , metadata );
			transaction.trace( "transaction product storage meta: save=" + metadata.objectId );
			
			if( createMetadata )
				createProductFinish( product );
			else
				modifyProductFinish( product , metadataOld , metadata );
		}
		else
		if( importMetadata ) {
			importProductFinish();
		}
		
		return( true );
	}

	public void checkTransactionMetadata( ProductMeta sourceMeta ) throws Exception {
		if( ( deleteMetadata == false && metadata == null ) || ( deleteMetadata == true && metadataOld == null ) )
			transaction.exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		if( ( deleteMetadata == false && metadata != sourceMeta ) || ( deleteMetadata == true && metadataOld != sourceMeta ) )
			transaction.exit1( _Error.InternalTransactionError1 , "Internal error: invalid transaction metadata" , "invalid transaction metadata" );
	}

	private void createProductFinish( AppProduct product ) throws Exception {
		EngineStatus status = transaction.action.getServerStatus();
		status.createProduct( transaction , product );
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.transactionCommitCreateProduct( transaction , product );
		EngineJmx jmx = transaction.engine.jmxController;
		jmx.addProduct( product );
	}
	
	private void importProductFinish() throws Exception {
		EngineStatus status = transaction.action.getServerStatus();
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		EngineJmx jmx = transaction.engine.jmxController;

		if( matchedBeforeImport ) {
			status.deleteProduct( transaction , metadataOld );
			mon.transactionCommitDeleteProduct( transaction , product );
		}
		
		if( product.isMatched() ) {
			status.createProduct( transaction , product );
			mon.transactionCommitCreateProduct( transaction , product );
			
			if( !matchedBeforeImport )
				jmx.addProduct( product );
		}
		else {
			if( matchedBeforeImport )
				jmx.deleteProduct( product );
		}
	}
	
	private void deleteProductFinish( AppProduct product , ProductMeta metadata ) throws Exception {
		EngineStatus status = transaction.action.getServerStatus();
		status.deleteProduct( transaction , metadata );
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.transactionCommitDeleteProduct( transaction , product );
		EngineJmx jmx = transaction.engine.jmxController;
		jmx.deleteProduct( product );
	}
	
	private void modifyProductFinish( AppProduct product , ProductMeta metadataOld , ProductMeta metadataNew ) throws Exception {
		product.setStorage( metadataNew );
		EngineStatus status = transaction.action.getServerStatus();
		status.modifyProduct( transaction , metadataOld , metadataNew );
		EngineMonitoring mon = transaction.action.getServerMonitoring();
		mon.transactionCommitModifyProduct( transaction , product );
	}
	
	public boolean replaceProduct( ProductMeta storage ) throws Exception {
		metadata = storage;
		transaction.trace( "transaction recreate product storage meta=" + storage.objectId );
		return( true );
	}

}
