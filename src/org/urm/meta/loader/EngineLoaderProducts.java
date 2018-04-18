package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.env.DBEnvData;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.AuthService;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.ProductStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;

public class EngineLoaderProducts {

	private EngineLoader loader;
	private DataService data;
	public RunContext execrc;
	public Engine engine;

	public EngineLoaderProducts( EngineLoader loader , DataService data ) {
		this.loader = loader;
		this.data = data;
		this.execrc = loader.execrc;
		this.engine = loader.engine;
	}
	
	public ProductMeta createProductMetadata( AppProduct product , boolean forceClearMeta , boolean forceClearDist ) throws Exception {
		ActionBase action = loader.getAction();
		ProductContext context = new ProductContext( product , false );
		EngineSettings settings = loader.getSettings();
		UrmStorage urm = action.artefactory.getUrmStorage();
		context.create( product , settings , urm.getProductHome( action , product ) );
		
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.getEngineProduct( product );
		ProductMeta set = new ProductMeta( ep );
		set.setMatched( true );
		
		try {
			// create in database
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.createdbAll( context );
	
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.createAll( forceClearMeta );
	
			// create folders
			ProductStorage ms = action.artefactory.getMetadataStorage( action , product );
			LocalFolder homeFolder = ms.getHomeFolder( action );
			if( homeFolder.checkExists( action ) ) {
				if( !forceClearMeta ) {
					String path = action.getLocalPath( homeFolder.folderPath );
					action.exit1( _Error.ProductPathAlreadyExists1 , "Product path already exists - " + path , path );
				}
				homeFolder.removeThis( action );
			}
				
			homeFolder.ensureExists( action );
			
			LocalFolder folder = ms.getMetaFolder( action );
			folder.ensureExists( action );
			folder = ms.getEnvConfFolder( action );
			folder.ensureExists( action );
	
			// create releases
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , set );
			ldr.createAll( forceClearMeta , forceClearDist );
			
			// add product
			products.setProductDraft( product , set );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to create metadata, product=" + product.NAME );
			set.meta.deleteObject();
			set.deleteObject();
			return( null );
		}
		
		return( set );
	}
	
	public void loadProducts( boolean update ) throws Exception {
		data.unloadProducts();
		
		ProductContext[] products = DBMeta.getProducts( loader );
		
		EngineDirectory directory = loader.getDirectory();
		for( String name : directory.getAllProductNames( null ) ) {
			AppProduct product = directory.findProduct( name );
			
			if( !matchProductMirrors( product ) ) {
				trace( "skip load not mirrored product name=" + name );
				continue;
			}
			
			ProductContext context = findContext( product , products );
			if( context == null || ( update == false && context.MATCHED == false ) ) {
				skipProduct( product , context );
				if( context == null )
					trace( "skip load unknown product name=" + name );
				else
					trace( "skip load mismatched product name=" + name );
				continue;
			}
			
			context.setProduct( product );
			loadProduct( product , context , false , update , true , null );
		}
	}

	public void skipProduct( AppProduct product , ProductContext context ) throws Exception {
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.getEngineProduct( product );
		
		ProductMeta storage = new ProductMeta( ep );
		if( context != null )
			storage.setContext( context );
		products.addProductSkipped( storage );
	}
	
	public void importProduct( AppProduct product , boolean includingEnvironments ) throws Exception {
		DBConnection c = loader.getConnection();
		
		EngineProducts products = data.getProducts();
		trace( "reload settings, product=" + product.NAME + " ..." );
		
		if( !matchProductMirrors( product ) )
			Common.exit1( _Error.InvalidProductMirros1 , "Invalid product mirror repositories, product=" + product.NAME , product.NAME );

		AuthService auth = engine.getAuth();
		DBEngineAuth.deleteProductAccess( c , auth , product );
		
		EngineProduct ep = products.getEngineProduct( product );
		EngineProductRevisions epr = ep.getRevisions();
		ProductMeta storage = epr.getDraftRevision();
		if( storage.isExists() ) {
			if( includingEnvironments )
				DBEnvData.dropEnvData( c , storage );
				
			if( storage.isExists() )
				DBProductData.dropProductData( c , storage );
		}
		
		synchronized( products ) {
			ProductContext context = new ProductContext( product , false );
			ProductMeta storageNew = loadProduct( product , context , true , true , includingEnvironments , storage );
			if( storageNew == null )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );

			if( !storageNew.MATCHED )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );
		}
	}
	
	public void exportProductMetadata( AppProduct product ) throws Exception {
		EngineProductRevisions epr = product.findRevisions();
		if( epr == null )
			Common.exitUnexpected();
		
		ProductMeta storage = epr.getDraftRevision();
		if( storage == null )
			Common.exitUnexpected();

		exportAll( storage );
	}
	
	public void exportAll( ProductMeta set ) throws Exception {
		ActionBase action = loader.getAction();
		AppProduct product = set.getProduct();
		ProductStorage ms = action.artefactory.getMetadataStorage( action , product );
		
		EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
		ldm.exportxmlAll( ms );

		EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
		lde.exportxmlAll( ms );
	}
	
	private boolean matchProductMirrors( AppProduct product ) {
		// match to mirrors
		EngineMatcher matcher = loader.getMatcher();
		if( !matcher.matchProductMirrors( product ) )
			return( false );

		return( true );
	}
	
	private ProductMeta loadProduct( AppProduct product , ProductContext context , boolean importxml , boolean update , boolean includingEnvironments , ProductMeta setOld ) {
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.findEngineProduct( product );
		
		ProductMeta set = new ProductMeta( ep );
		set.setMatched( true );
		set.setContext( context ); 
		
		ActionBase action = loader.getAction();
		try {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder meta = urm.getProductCoreMetadataFolder( action , product );
			
			if( meta.checkExists( action ) ) {
				LocalFolder home = urm.getProductHome( action , product );
				context.create( product , loader.getSettings() , home );
				set.setContext( context );
				
				ProductStorage storageMeta = action.artefactory.getMetadataStorage( action , product );
				if( importxml )
					importxmlAll( set , storageMeta , context , update , includingEnvironments );
				else
					loaddbAll( set , storageMeta , context );

				EngineMatcher matcher = loader.getMatcher();
				if( !matcher.matchProduct( loader , product , set , update ) )
					trace( "match failed for product=" + product.NAME );
				else
					trace( "successfully matched product=" + product.NAME );
			}
			else {
				String path = meta.getLocalPath( action );
				Common.exit1( _Error.MissingProductFolder1 , "missing product folder=" + path , path );
			}
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + product.NAME );
			set.setMatched( false );
		}
		
		if( setOld != null )
			products.unloadProduct( setOld );
		products.setProductDraft( product , set );
		return( set );
	}
	
	private ProductContext findContext( AppProduct product , ProductContext[] products ) {
		for( ProductContext context : products ) {
			if( context.PRODUCT_ID == product.ID )
				return( context );
			if( context.NAME.equals( product.NAME ) )
				return( context );
		}
		return( null );
	}
	
	private void importxmlAll( ProductMeta set , ProductStorage ms , ProductContext context , boolean update , boolean includingEnvironments ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.importxmlAll( ms , context );
		
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			if( includingEnvironments )
				lde.importxmlAll( ms , update );
			else
 				lde.loaddbAll();
				
			ldm.loadDesignDocs( ms );
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , set );
			ldr.loadReleases( set , true );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void loaddbAll( ProductMeta set , ProductStorage ms , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.loaddbAll( context );

			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.loaddbAll();
			
			ldm.loadDesignDocs( ms );
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , set );
			ldr.loadReleases( set , false );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

}