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
		EngineSettings settings = loader.getSettings();
		UrmStorage urm = action.artefactory.getUrmStorage();
		ProductContext context = new ProductContext( product , settings , urm.getProductHome( action , product ) );
		
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.getEngineProduct( product );
		ProductMeta set = new ProductMeta( ep );
		set.setMatched( true );
		set.setRevision( "initial" );
		
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
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.createReleases( set , forceClearMeta );
			ldr.createDistributives( forceClearDist );
			
			// add product
			ep.addProductMeta( set );
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
		
		EngineDirectory directory = loader.getDirectory();
		for( String name : directory.getAllProductNames( null ) ) {
			AppProduct product = directory.findProduct( name );
			
			if( !matchProductMirrors( product ) ) {
				trace( "skip load not mirrored product name=" + name );
				continue;
			}
			
			if( loadProduct( product , update ) )
				directory.addMatchedProduct( product );
		}
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
		if( storage != null ) {
			if( includingEnvironments )
				DBEnvData.dropEnvData( c , storage );
				
			if( storage.isExists() )
				DBProductData.dropProductData( c , storage );
		}
		
		synchronized( products ) {
			ProductMeta storageNew = importProduct( product , true , includingEnvironments , storage );
			if( storageNew == null )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );

			if( !storageNew.MATCHED )
				Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );
			
			EngineDirectory directory = product.directory;
			directory.addMatchedProduct( product );
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
	
	private ProductMeta importProduct( AppProduct product , boolean update , boolean includingEnvironments , ProductMeta setOld ) {
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.findEngineProduct( product );
		
		ProductMeta set = new ProductMeta( ep );
		set.setMatched( true );
		set.setRevision( "initial" );
		
		ActionBase action = loader.getAction();
		try {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder meta = urm.getProductCoreMetadataFolder( action , product );
			
			if( meta.checkExists( action ) ) {
				LocalFolder home = urm.getProductHome( action , product );
				ProductContext context = new ProductContext( product , loader.getSettings() , home );
				
				ProductStorage storageMeta = action.artefactory.getMetadataStorage( action , product );
				importxmlAll( ep , set , storageMeta , context , update , includingEnvironments );

				EngineMatcher matcher = loader.getMatcher();
				if( !matcher.matchProduct( loader , product , set , update ) )
					trace( "match failed for product=" + product.NAME );
				else {
					products.updateRevision( product , set );
					if( setOld != null )
						products.unloadProduct( setOld );
					trace( "successfully matched product=" + product.NAME );
				}
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
		
		return( set );
	}
	
	private boolean loadProduct( AppProduct product , boolean update ) {
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.findEngineProduct( product );
		
		ActionBase action = loader.getAction();
		try {
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder home = urm.getProductHome( action , product );
			ProductContext context = new ProductContext( product , loader.getSettings() , home );
			
			ProductMeta[] sets = DBMeta.loaddbMeta( loader , ep );
			for( ProductMeta set : sets )
				loadProductRevision( product , context , update , ep , set );
			
			loadProductDistributives( product , context , update , ep );
			return( true );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + product.NAME );
			return( false );
		}
	}

	private void loadProductRevision( AppProduct product , ProductContext context , boolean update , EngineProduct ep , ProductMeta set ) {
		if( update == false && !set.MATCHED ) {
			trace( "skip loading mismatched product=" + product.NAME + ", revision=" + set.REVISION );
			return;
		}
		
		ActionBase action = loader.getAction();
		try {
			set.setMatched( true );
			loaddbAll( ep , set , context );
	
			EngineMatcher matcher = loader.getMatcher();
			if( !matcher.matchProduct( loader , product , set , update ) )
				trace( "match failed for product=" + product.NAME + ", revision=" + set.REVISION );
			else
				trace( "successfully matched product=" + product.NAME + ", revision=" + set.REVISION );
			
			ep.addProductMeta( set );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load metadata, product=" + product.NAME );
			set.setMatched( false );
		}
	}

	private void loadProductDistributives( AppProduct product , ProductContext context , boolean update , EngineProduct ep ) throws Exception {
		EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
		ldr.loadDistributives( false );
	}
	
	private void importxmlAll( EngineProduct ep , ProductMeta set , ProductStorage ms , ProductContext context , boolean update , boolean includingEnvironments ) throws Exception {
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
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.loadReleases( set , true );
			ldr.loadDistributives( true );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	private void loaddbAll( EngineProduct ep , ProductMeta set , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.loaddbAll( context );

			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.loaddbAll();
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
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
