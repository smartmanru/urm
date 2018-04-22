package org.urm.engine.transaction;

import java.util.LinkedList;
import java.util.List;

import org.urm.engine.StateService;
import org.urm.engine._Error;
import org.urm.engine.action.ActionInit;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductSessions;
import org.urm.meta.engine.AppSystem;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class TransactionMetadata {

	enum CHANGETYPE {
		NOTHING ,
		CREATE ,
		RECREATE ,
		CHANGE ,
		DELETE ,
		IMPORT
	};
	
	TransactionBase transaction;
	TransactionProduct transactionProduct;
	EngineProduct ep;
	
	CHANGETYPE productType;
	
	public Meta sessionMeta;
	public AppProduct product;
	public ProductMeta metadata;
	protected ProductMeta metadataOld;

	private List<TransactionMetadataEnv> transactionEnvs;
	
	public TransactionMetadata( TransactionProduct transaction , EngineProduct ep ) {
		this.transactionProduct = transaction;
		this.transaction = transaction.transaction;
		this.ep = ep;
		
		transactionEnvs = new LinkedList<TransactionMetadataEnv>();
	}

	public TransactionMetadataEnv[] getTransactionEnvs() {
		return( transactionEnvs.toArray( new TransactionMetadataEnv[ 0 ] ) );
	}
	
	public boolean createProduct( Meta sessionMeta ) throws Exception {
		if( productType != null ) {
			transaction.error( "Should be first product operation" );
			return( false );
		}
		
		productType = CHANGETYPE.CREATE;
		this.sessionMeta = sessionMeta;
		metadata = sessionMeta.getStorage();
		return( true );
	}

	public boolean importProduct( AppProduct product ) {
		if( productType != null )
			return( false );
		
		productType = CHANGETYPE.IMPORT;
		this.product = product;
		
		EngineProduct ep = product.findEngineProduct();
		metadata = ep.findDraftRevision();
		return( true );
	}

	public boolean recreateProduct( Meta meta ) throws Exception {
		if( productType != null ) {
			transaction.error( "Should be first product operation" );
			return( false );
		}
		
		ProductMeta storage = meta.getStorage();
		if( storage.isExists() && !storage.isPrimary() ) {
			transaction.error( "Unable to change old metadata, id=" + storage.objectId );
			return( false );
		}
			
		productType = CHANGETYPE.RECREATE;
		metadataOld = storage;
		metadata = null;
		transaction.trace( "transaction recreate product storage meta=" + storage.objectId );
		return( true );
	}

	public boolean deleteProduct( Meta meta ) throws Exception {
		if( productType != null ) {
			transaction.error( "Should be first product operation" );
			return( false );
		}
		
		ProductMeta storage = meta.getStorage();
		if( !storage.isPrimary() ) {
			transaction.error( "Unable to change old metadata, id=" + storage.objectId );
			return( false );
		}
		
		productType = CHANGETYPE.DELETE;
		metadataOld = storage;
		transaction.trace( "transaction product storage meta: going delete=" + storage.objectId );
		return( true );
	}

	public boolean changeProduct( Meta meta ) throws Exception {
		return( changeProduct( meta , false ) );
	}
	
	private boolean changeProduct( Meta meta , boolean env ) throws Exception {
		if( productType != null && productType != CHANGETYPE.NOTHING && productType != CHANGETYPE.CHANGE ) {
			transaction.error( "Unable to change product when reconstruction" );
			return( false );
		}
		
		ProductMeta storage = meta.getStorage();
		if( !storage.isPrimary() ) {
			transaction.error( "Unable to change old metadata, id=" + storage.objectId );
			return( false );
		}

		if( productType != null ) {
			if( productType == CHANGETYPE.NOTHING && env == false )
				productType = CHANGETYPE.CHANGE;
			return( true );
		}
		
		productType = ( env )? CHANGETYPE.NOTHING : CHANGETYPE.CHANGE;
		metadataOld = storage;
		
		AppProduct product = storage.getProduct();
		AppSystem system = product.system;
		
		ActionInit action = transaction.getAction();
		metadata = storage.copy( action , system.getParameters() );
		sessionMeta = ep.findSessionMeta( action , storage , true );
		
		EngineProductSessions sessions = ep.getSessions();
		sessions.replaceStorage( transaction.action , sessionMeta , metadata );
		transaction.trace( "transaction product storage meta: source=" + storage.objectId + ", copy=" + metadata.objectId );
		return( true );
	}

	public boolean checkChangeProduct() {
		if( productType == CHANGETYPE.CHANGE )
			return( true );
		return( false );
	}
	
	public boolean changeEnv( MetaEnv env ) throws Exception {
		if( !changeProduct( env.meta , true ) )
			return( false );
		
		ProductEnvs envs = metadata.getEnviroments();
		MetaEnv envNew = envs.findMetaEnv( env );
		for( TransactionMetadataEnv tenv : transactionEnvs ) {
			if( tenv.env == envNew )
				return( true );
		}
		
		TransactionMetadataEnv tenvNew = new TransactionMetadataEnv();
		tenvNew.env = envNew;
		tenvNew.envType = CHANGETYPE.CHANGE;
		transactionEnvs.add( tenvNew );
		return( true );
	}

	public boolean importEnv( MetaEnv env ) throws Exception {
		for( TransactionMetadataEnv tenv : transactionEnvs ) {
			if( tenv.env == env )
				return( true );
		}
		
		TransactionMetadataEnv tenvNew = new TransactionMetadataEnv();
		tenvNew.env = env;
		tenvNew.envType = CHANGETYPE.IMPORT;
		transactionEnvs.add( tenvNew );
		return( true );
	}

	public boolean checkChangeEnv( MetaEnv env ) throws Exception {
		if( productType != null && productType != CHANGETYPE.NOTHING && productType != CHANGETYPE.CHANGE ) {
			transaction.error( "Unable to change product environment when reconstruction" );
			return( false );
		}

		for( TransactionMetadataEnv tenv : transactionEnvs ) {
			if( tenv.env.ID == env.ID )
				return( true );
		}
		return( false );
	}

	public void abortTransaction( boolean save ) {
		if( metadataOld == null )
			return;

		String name = metadataOld.NAME;
		try {
			if( save ) {
				EngineDirectory directory = transaction.getDirectory();
				AppProduct product = directory.getProduct( ep.productId );
				transaction.updateRevision( product , metadataOld , null );
			}
			
			if( productType != CHANGETYPE.DELETE && sessionMeta != null ) {
				EngineProductSessions sessions = ep.getSessions();
				sessions.replaceStorage( transaction.action , sessionMeta , metadataOld );
			}
			
			productType = CHANGETYPE.NOTHING;
			
			sessionMeta = null;
			metadata = null;
			metadataOld = null;
		}
		catch( Throwable e ) {
			transaction.handle( e , "Unable to restore metadata, product=" + name );
		}
	}

	public boolean commitTransaction() throws Exception {
		if( productType == CHANGETYPE.DELETE ) {
			if( metadataOld == null )
				return( false );

			transaction.deleteProductMetadata( metadataOld );
			transaction.trace( "transaction product storage meta: delete=" + metadataOld.objectId );
		}
		else {
			if( metadata == null )
				return( false );
				
			EngineDirectory directory = transaction.getDirectory();
			AppProduct product = directory.getProduct( ep.productId );
			transaction.updateRevision( product , metadata , metadataOld );
			
			if( sessionMeta != null ) {
				EngineProductSessions sessions = ep.getSessions();
				sessions.replaceStorage( transaction.action , sessionMeta , metadata );
			}
			transaction.trace( "transaction product storage meta: save=" + metadata.objectId );
			
			if( productType != CHANGETYPE.CREATE )
				modifyProductFinish( product , metadataOld , metadata );
		}
		
		return( true );
	}

	public void checkTransactionMetadata( ProductMeta sourceMeta ) throws Exception {
		if( ( productType != CHANGETYPE.DELETE && metadata == null ) || ( productType == CHANGETYPE.DELETE && metadataOld == null ) )
			transaction.exit( _Error.TransactionMissingMetadataChanges0 , "Missing metadata changes" , null );
		if( ( productType != CHANGETYPE.DELETE && metadata != sourceMeta ) || ( productType == CHANGETYPE.DELETE && metadataOld != sourceMeta ) )
			transaction.exit1( _Error.InternalTransactionError1 , "Internal error: invalid transaction metadata" , "invalid transaction metadata" );
	}

	public void checkTransactionEnv( MetaEnv sourceEnv ) throws Exception {
		for( TransactionMetadataEnv tenv : transactionEnvs ) {
			if( tenv.env == sourceEnv )
				return;
		}
		
		transaction.exit( _Error.TransactionMissingMetadataChanges0 , "Missing environment changes" , null );
	}

	private void modifyProductFinish( AppProduct product , ProductMeta metadataOld , ProductMeta metadataNew ) throws Exception {
		StateService status = transaction.action.getEngineStatus();
		status.modifyProduct( transaction , metadataOld , metadataNew );
		EngineMonitoring mon = transaction.action.getEngineMonitoring();
		mon.transactionCommitModifyProduct( transaction , product );
	}
	
	public boolean replaceProduct( ProductMeta storage , ProductMeta storageOld ) throws Exception {
		metadata = storage;
		metadataOld = storageOld;
		productType = CHANGETYPE.RECREATE;
		transaction.trace( "transaction recreate product storage meta=" + storage.objectId );
		return( true );
	}

}
