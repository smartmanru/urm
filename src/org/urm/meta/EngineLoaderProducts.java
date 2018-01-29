package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.engine.DBEngineAuth;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineAuth;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.engine.EngineSettings;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;

public class EngineLoaderProducts {

	private EngineLoader loader;
	private EngineData data;
	public RunContext execrc;
	public Engine engine;

	public EngineLoaderProducts( EngineLoader loader , EngineData data ) {
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
		context.create( settings , urm.getProductHome( action , product ) );
		
		ProductMeta set = product.storage;
		EngineProducts products = data.getProducts();
		set = new ProductMeta( products , product );
		set.setPrimary( true );
		set.setMatched( true );
		
		try {
			// create in database
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.createdbAll( context );
	
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.createAll();
	
			// create folders
			MetadataStorage ms = action.artefactory.getMetadataStorage( action , set.meta );
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
	
			// create distributive
			DistRepository.createInitialRepository( action , set.meta , forceClearDist );
			
			// add product
			product.setStorage( set );
			addProduct( set );
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
			
			if( !matchProductMirrors( product ) )
				continue;
			
			ProductContext context = findContext( product , products );
			if( context == null || ( update == false && context.MATCHED == false ) ) {
				skipProduct( product , context );
				trace( "skip load product name=" + name );
				continue;
			}
			
			context.setProduct( product );
			loadProduct( product , context , false , update );
		}
	}

	public void skipProduct( AppProduct product , ProductContext context ) throws Exception {
		EngineProducts products = data.getProducts();
		ProductMeta storage = new ProductMeta( products , product );
		if( context != null )
			storage.setContext( context );
		product.setStorage( storage );
		products.addProductSkipped( storage );
	}
	
	public void importProduct( AppProduct product , boolean includingEnvironments ) throws Exception {
		DBConnection c = loader.getConnection();
		
		EngineProducts products = data.getProducts();
		trace( "reload settings, product=" + product.NAME + " ..." );
		
		if( !matchProductMirrors( product ) )
			Common.exit1( _Error.InvalidProductMirros1 , "Invalid product mirror repositories, product=" + product.NAME , product.NAME );

		EngineAuth auth = engine.getAuth();
		DBEngineAuth.deleteProductAccess( c , auth , product );
		
		ProductMeta storage = product.storage;
		if( storage.isExists() )
			DBProductData.dropProductData( c , storage );
		
		synchronized( products ) {
			ProductContext context = new ProductContext( product , false );
			ProductMeta storageNew = loadProduct( product , context , true , true );
			if( storageNew == null )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );

			if( storage != null )
				products.unloadProduct( storage );
			
			if( !storageNew.MATCHED )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );
		}
	}
	
	public void saveProductMetadata( String productName ) throws Exception {
		ActionBase action = loader.getAction();
		EngineProducts products = data.getProducts();
		ProductMeta storage = products.findProductStorage( productName );
		if( storage == null )
			action.exitUnexpectedState();

		saveAll( storage );
	}
	
	public void exportProductMetadata( AppProduct product ) throws Exception {
		EngineProducts products = data.getProducts();
		ProductMeta storage = products.findProductStorage( product.NAME );
		if( storage == null )
			Common.exitUnexpected();

		exportAll( storage );
	}
	
	public void saveAll( ProductMeta set ) throws Exception {
		ActionBase action = loader.getAction();
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , set.meta );
		
		EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
		ldm.saveDesignDocs( ms );
		
		EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
		lde.saveEnvs( ms );
	}
	
	public void exportAll( ProductMeta set ) throws Exception {
		ActionBase action = loader.getAction();
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , set.meta );
		
		EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
		ldm.exportAll( ms );

		EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
		lde.exportAll( ms );
	}
	
	private boolean addProduct( ProductMeta set ) {
		EngineProducts products = data.getProducts();
		products.addProduct( set );
		return( true );
	}

	private boolean matchProductMirrors( AppProduct product ) {
		// match to mirrors
		EngineMatcher matcher = loader.getMatcher();
		if( !matcher.matchProductMirrors( product ) )
			return( false );

		return( true );
	}
	
	private ProductMeta loadProduct( AppProduct product , ProductContext context , boolean importxml , boolean update ) {
		EngineProducts products = data.getProducts();
		ProductMeta set = new ProductMeta( products , product );
		set.setPrimary( true );
		set.setMatched( true );
		set.setContext( context ); 
		
		ActionBase action = loader.getAction();
		try {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder meta = urm.getProductCoreMetadataFolder( action , product );
			
			if( meta.checkExists( action ) ) {
				LocalFolder home = urm.getProductCoreMetadataFolder( action , product );
				context.create( loader.getSettings() , home );
				set.setContext( context );
				
				MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
				if( importxml )
					importxmlAll( set , storageMeta , context );
				else
					loaddbAll( set , storageMeta , context );

				EngineMatcher matcher = loader.getMatcher();
				if( !matcher.matchProduct( loader , product , set , update ) )
					trace( "match failed for product=" + product.NAME );
				else
					trace( "successfully matched product=" + product.NAME );
			}
			else
				Common.exitUnexpected();
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + product.NAME );
			set.setMatched( false );
		}
		
		product.setStorage( set );
		addProduct( set );
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
	
	private void importxmlAll( ProductMeta set , MetadataStorage ms , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.importxmlAll( ms , context );
		
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.loadEnvs( ms );
			ldm.loadDesignDocs( ms );
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , set );
			ldr.loadReleases( set , true );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.name , set.name );
		}
	}
	
	private void loaddbAll( ProductMeta set , MetadataStorage ms , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.loaddbAll( context );

			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.loadEnvs( ms );
			ldm.loadDesignDocs( ms );
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , set );
			ldr.loadReleases( set , false );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.name , set.name );
		}
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

}
