package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineProducts;

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
	}
	
	public void importProduct( AppProduct product , boolean includingEnvironments ) throws Exception {
		EngineProducts products = data.getProducts();
		trace( "reload settings, product=" + product.NAME + " ..." );
		
		if( !matchProductMirrors( product ) )
			Common.exit1( _Error.InvalidProductMirros1 , "Invalid product mirror repositories, product=" + product.NAME , product.NAME );

		ProductMeta storage = product.storage;
		if( storage.isExists() )
			DBProductData.dropProductData( loader , storage );
		
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
		lde.saveMonitoring( ms );
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
		ProductMeta set = products.createPrimaryMeta( product , context );
		
		ActionBase action = loader.getAction();
		try {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder meta = urm.getProductCoreMetadataFolder( action , product.NAME );
			
			if( meta.checkExists( action ) ) {
				LocalFolder home = urm.getProductCoreMetadataFolder( action , product.NAME );
				context.create( loader.getSettings() , home );
				set.setContext( context );
				
				MetadataStorage storageMeta = action.artefactory.getMetadataStorage( action , set.meta );
				if( importxml )
					importxmlAll( set , storageMeta , context );
				else
					loaddbAll( set , storageMeta , context );

				if( !DBEngineDirectory.matchProduct( loader , product , set , update ) )
					trace( "match failed for product=" + product.NAME );
				else
					trace( "successfully matched product=" + product.NAME );
				
				product.setStorage( set );
				addProduct( set );
			}
			else
				Common.exitUnexpected();
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + product.NAME );
			set.meta.deleteObject();
			set.deleteObject();
			return( null );
		}
		
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
