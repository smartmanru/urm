package org.urm.engine.transaction;

import java.util.LinkedList;
import java.util.List;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.CallService;
import org.urm.engine.StateService;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineMonitoring;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;

public class TransactionProduct {

	TransactionBase transaction;
	AppProduct product;
	AppProduct productNew;
	EngineProduct ep;

	protected boolean deleteProduct;
	protected boolean createProduct;
	protected boolean changeProduct;
	protected boolean importProduct;
	private List<TransactionMetadata> productMeta;
	
	public TransactionProduct( TransactionBase transaction , AppProduct product , EngineProduct ep ) {
		this.transaction = transaction;
		this.product = product;
		this.ep = ep;
		
		productMeta = new LinkedList<TransactionMetadata>();
		deleteProduct = false;
		changeProduct = false;
		createProduct = false;
		importProduct = false;
	}

	public void abortTransaction( boolean save ) {
		for( TransactionMetadata meta :	productMeta )
			meta.abortTransaction( save );
		
		productMeta.clear();
	}

	public Meta createProductMetadata( ProductMeta storage ) throws Exception {
		ActionBase action = transaction.getAction();
		Meta meta = ep.createSessionMeta( action , storage );
		
		TransactionMetadata tm = new TransactionMetadata( this , ep );
		if( !tm.createProduct( meta ) )
			Common.exitUnexpected();
			
		addTransactionMeta( tm );
		return( meta );
	}
	
	private void addTransactionMeta( TransactionMetadata tm ) {
		productMeta.add( tm );
	}

	private TransactionMetadata findTransactionMeta( Meta meta ) {
		for( TransactionMetadata tm : productMeta ) {
			if( tm.metadata != null && tm.metadata.ID == meta.getId() )
				return( tm );
			if( tm.metadataOld != null && tm.metadataOld.ID == meta.getId() )
				return( tm );
		}
		return( null );
	}
	
	public Meta findTransactionMeta( int metaId ) {
		for( TransactionMetadata tm : productMeta ) {
			if( tm.metadata != null && tm.metadata.ID == metaId )
				return( tm.metadata.meta );
			if( tm.metadataOld != null && tm.metadataOld.ID == metaId )
				return( tm.metadataOld.meta );
		}
		return( null );
	}
	
	public MetaEnv findTransactionEnv( int envId ) {
		for( TransactionMetadata tm : productMeta ) {
			for( TransactionMetadataEnv tme : tm.getTransactionEnvs() ) {
				if( tme.env.ID == envId )
					return( tme.env );
			}
		}
		return( null );
	}
	
	public boolean importProduct() {
		TransactionMetadata tm = new TransactionMetadata( this , ep );
		if( !tm.importProduct( product ) )
			return( false );
		
		importProduct = true;
		addTransactionMeta( tm );
		return( true );
	}

	public boolean importEnv( MetaEnv env ) throws Exception {
		// should exist product transaction operation
		TransactionMetadata tm = findTransactionMeta( env.meta );
		if( tm == null )
			Common.exitUnexpected();
		
		return( tm.importEnv( env ) );
	}

	public boolean recreateMetadata( Meta meta ) throws Exception {
		// should be first product transaction operation
		TransactionMetadata tm = findTransactionMeta( meta );
		if( tm != null )
			Common.exitUnexpected();
		
		tm = new TransactionMetadata( this , ep );
		if( tm.recreateProduct( meta ) ) {
			addTransactionMeta( tm );
			return( true );
		}
		return( false );
	}	

	public boolean deleteMetadata( Meta meta ) throws Exception {
		// should be first product transaction operation
		TransactionMetadata tm = findTransactionMeta( meta );
		if( tm != null )
			Common.exitUnexpected();
		
		EngineProduct ep = meta.getEngineProduct();
		tm = new TransactionMetadata( this , ep );
		if( tm.deleteProduct( meta ) ) {
			addTransactionMeta( tm );
			return( true );
		}
		return( false );
	}

	public boolean deleteProduct() throws Exception {
		EngineProductRevisions revisions = ep.getRevisions();
		for( ProductMeta storage : revisions.getRevisions() ) {
			if( !deleteMetadata( storage.meta ) )
				Common.exitUnexpected();
		}

		deleteProduct = true;
		return( true );
	}

	public boolean checkChangeMetadata( Meta meta ) {
		TransactionMetadata tm = findTransactionMeta( meta );
		if( tm == null )
			return( false );
		return( tm.checkChangeProduct() );
	}

	public boolean changeMetadata( Meta meta ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( meta );
		if( tm == null ) {
			tm = new TransactionMetadata( this , ep );
			addTransactionMeta( tm );
		}
		return( tm.changeProduct( meta ) );
	}

	public boolean checkChangeEnv( MetaEnv env ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( env.meta );
		if( tm == null )
			return( false );
		return( tm.checkChangeEnv( env ) );
	}
	
	public boolean changeEnv( MetaEnv env ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( env.meta );
		if( tm == null ) {
			tm = new TransactionMetadata( this , ep );
			addTransactionMeta( tm );
		}
		
		if( tm.changeEnv( env ) )
			return( true );
		return( false );
	}

	public boolean checkTransactionMetadata( ProductMeta sourceMeta ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( sourceMeta.meta );
		if( tm == null )
			return( false );
		tm.checkTransactionMetadata( sourceMeta );
		return( true );
	}
	
	public boolean checkTransactionEnv( MetaEnv env ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( env.meta );
		if( tm == null )
			return( false );
		tm.checkTransactionEnv( env );
		return( true );
	}

	public boolean checkTransactionProduct( AppProduct product ) throws Exception {
		if( changeProduct == false || product != productNew )
			return( false );
		return( true );
	}
	
	public Meta[] getTransactionProductMetadataList() {
		List<Meta> list = new LinkedList<Meta>();
		for( TransactionMetadata tm : productMeta ) {
			if( tm.sessionMeta != null )
				list.add( tm.sessionMeta );
		}
		return( list.toArray( new Meta[0] ) );
	}

	public ProductMeta findTransactionProductMetadata( Meta meta ) throws Exception {
		TransactionMetadata tm = findTransactionMeta( meta );
		if( tm != null )
			return( tm.metadata );
		return( null );
	}

	public void replaceProductMetadata( ProductMeta storage , ProductMeta storageOld ) throws Exception {
		if( storageOld == null )
			Common.exitUnexpected();
		
		TransactionMetadata tm = findTransactionMeta( storage.meta );
		if( tm == null )
			tm = findTransactionMeta( storageOld.meta );
		
		if( tm == null ) {
			tm = new TransactionMetadata( this , ep );
			addTransactionMeta( tm );
		}
		
		tm.replaceProduct( storage , storageOld );
	}

	public boolean createProduct() {
		createProduct = true;
		return( true );
	}
	
	public boolean changeProduct() {
		changeProduct = true;
		productNew = product.copy( transaction.getDirectory() , product.system );
		return( true );
	}
	
	public boolean commitTransaction() throws Exception {
		if( createProduct )
			createProductFinish( product );
		else
		if( importProduct )
			importProductFinish( product );
		else
		if( changeProduct )
			changeProductFinish( productNew );
		
		for( TransactionMetadata tm : productMeta ) {
			if( !tm.commitTransaction() )
				return( false );
		}
		
		if( deleteProduct )
			deleteProductFinish( product );
		
		return( true );
	}

	private void createProductFinish( AppProduct product ) throws Exception {
		StateService status = transaction.action.getEngineStatus();
		status.createProduct( transaction , product );
		EngineMonitoring mon = transaction.action.getEngineMonitoring();
		mon.transactionCommitCreateProduct( transaction , product );
		CallService jmx = transaction.engine.jmx;
		jmx.addProduct( product );
	}
	
	private void importProductFinish( AppProduct product ) throws Exception {
		EngineDirectory directory = product.directory;
		directory.addMatchedProduct( product );
		
		StateService status = transaction.action.getEngineStatus();
		EngineMonitoring mon = transaction.action.getEngineMonitoring();
		CallService jmx = transaction.engine.jmx;

		status.deleteProduct( transaction , product );
		mon.transactionCommitDeleteProduct( transaction , product );
		status.createProduct( transaction , product );
		mon.transactionCommitCreateProduct( transaction , product );
		
		jmx.addProduct( product );
	}
	
	private void changeProductFinish( AppProduct product ) throws Exception {
		EngineDirectory directory = product.directory;
		directory.replaceProduct( product );
		
		EngineMonitoring mon = transaction.action.getEngineMonitoring();

		mon.transactionCommitDeleteProduct( transaction , product );
		mon.transactionCommitCreateProduct( transaction , product );
	}
	
	private void deleteProductFinish( AppProduct product ) throws Exception {
		StateService status = transaction.action.getEngineStatus();
		status.deleteProduct( transaction , product );
		EngineMonitoring mon = transaction.action.getEngineMonitoring();
		mon.transactionCommitDeleteProduct( transaction , product );
		CallService jmx = transaction.engine.jmx;
		jmx.deleteProduct( product );
	}
	
}
