package org.urm.engine.products;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.DataService;
import org.urm.engine.Engine;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.dist.DistRepository;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;

public class EngineProduct {

	public Engine engine;
	
	private DataService data;
	protected EngineProducts products;
	
	public int productId;
	public String productName;
	
	private boolean skipped;
	
	private EngineProductReleases releases;
	private EngineProductEnvs envs;
	private EngineProductRevisions revisions;
	private EngineProductSessions sessions;

	public EngineProduct( DataService data , EngineProducts products , AppProduct product ) {
		this.engine = products.engine;
		this.data = data;
		this.products = products;
		this.productId = product.ID;
		this.productName = product.NAME;
		
		releases = new EngineProductReleases( this );
		envs = new EngineProductEnvs( this );
		revisions = new EngineProductRevisions( this );
		sessions = new EngineProductSessions( this );
	}
	
	public boolean isSkipped() {
		return( skipped );
	}

	public ProductMeta findDraftRevision() {
		return( revisions.getDraftRevision() );
	}
	
	public EngineProductReleases getReleases() {
		return( releases );
	}
	
	public EngineProductEnvs getEnvs() {
		return( envs );
	}
	
	public EngineProductRevisions getRevisions() {
		return( revisions );
	}

	public EngineProductSessions getSessions() {
		return( sessions );
	}

 	public AppProduct getProduct() throws Exception {
 		AppProduct product = findProduct();
 		if( product == null )
 			Common.exitUnexpected();
 		return( product );
 	}
	
 	public AppProduct findProduct() {
		EngineDirectory directory = data.getDirectory();
		return( directory.findProduct( productId ) );
	}	
	
	public void addSessionMeta( Meta meta ) {
		sessions.addSessionMeta( meta );
	}
	
	public void releaseSessionMeta( Meta meta ) {
		sessions.releaseSessionMeta( meta );
	}

	public boolean isReferencedBySessions( ProductMeta storage ) {
		return( sessions.isReferencedBySessions( storage ) );
	}
	
	public DistRepository getDistRepository() {
		return( releases.getDistRepository() );
	}
	
	public void setDistRepository( DistRepository repo ) {
		releases.setDistRepository( repo );
	}

	public void addProductMeta( ProductMeta storage ) throws Exception {
		revisions.addProductMeta( storage );
	}
	
	public void setSkipped() {
		skipped = true;
	}

	public void unloadProduct() {
		for( ProductMeta storage : revisions.getRevisions() )
			unloadProduct( storage );
	}
	
	public void unloadProduct( ProductMeta storage ) {
		revisions.unloadProduct( storage );
		unloadProductData( storage );
	}
	
	public void unloadProductData( ProductMeta storage ) {
		storage.setPrimary( false );
		if( !sessions.isReferencedBySessions( storage ) ) {
			storage.meta.deleteObject();
			storage.deleteObject();
		}
	}

	public void updateRevision( ProductMeta storage ) throws Exception {
		revisions.updateRevision( storage );
	}
	
	public ProductMeta findRevision( String revision ) {
		return( revisions.findRevision( revision ) );
	}
	
	public Meta createSessionMeta( ActionBase action , ProductMeta storage ) throws Exception {
		return( sessions.createSessionProductMetadata( action , storage ) );
	}

	public Meta findSessionRevision( ActionBase action , String revision ) {
		ProductMeta storage = revisions.findRevision( revision );
		return( sessions.findSessionProductMetadata( action , storage , true ) );
	}

	public Meta findSessionRevision( ActionBase action , int metaId ) {
		ProductMeta storage = revisions.findRevision( metaId );
		return( sessions.findSessionProductMetadata( action , storage , true ) );
	}

	public Meta getSessionMeta( ActionBase action , ProductMeta storage , boolean primary ) throws Exception {
		return( sessions.getSessionProductMetadata( action , storage , primary ) );
	}

	public Meta findSessionMeta( ActionBase action , ProductMeta storage , boolean create ) {
		return( sessions.findSessionProductMetadata( action , storage , create ) );
	}

	public void releaseSessionMeta( ActionBase action , Meta meta ) throws Exception {
		sessions.releaseSessionProductMetadata( action , meta );
	}
	
}
