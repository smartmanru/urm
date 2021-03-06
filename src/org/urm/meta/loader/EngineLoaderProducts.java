package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.DBConnection;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.DataService;
import org.urm.engine.data.EngineDirectory;
import org.urm.engine.data.EngineProducts;
import org.urm.engine.data.EngineSettings;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.products.EngineProductRevisions;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.ProductStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.product.ProductContext;
import org.urm.meta.product.ProductMeta;
import org.urm.meta.system.AppProduct;

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
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.getEngineProduct( product );
		
		ProductMeta set = null;
		try {
			set = createProductRevision( product , "initial" , forceClearMeta );
	
			// create folders
			ProductStorage ms = action.artefactory.getMetadataStorage( action , product );
			LocalFolder homeFolder = ms.getHomeFolder( action );
			homeFolder.recreateThis( action );
			
			LocalFolder folder = ms.getMetaFolder( action );
			folder.ensureExists( action );
			folder = ms.getEnvConfFolder( action );
			folder.ensureExists( action );

			// create distributives
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.createDistributives( forceClearDist );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to create metadata, product=" + product.NAME );
			if( set != null ) {
				set.meta.deleteObject();
				set.deleteObject();
			}
			return( null );
		}
		
		return( set );
	}
	
	public ProductMeta createProductRevision( AppProduct product , String name , boolean forceClearMeta ) throws Exception {
		return( createProductRevision( product , name , null , forceClearMeta ) );
	}

	public ProductMeta copyProductRevision( AppProduct product , String name , ProductMeta src ) throws Exception {
		return( createProductRevision( product , name , src , false ) );
	}
	
	public ProductMeta createProductRevision( AppProduct product , String name , ProductMeta src , boolean forceClearMeta ) throws Exception {
		ActionBase action = loader.getAction();
		EngineSettings settings = loader.getSettings();
		UrmStorage urm = action.artefactory.getUrmStorage();
		ProductContext context = new ProductContext( product , settings , urm.getProductHome( action , product ) );
		
		EngineProduct ep = product.getEngineProduct();
		ProductMeta set = new ProductMeta( engine , ep );
		set.setMatched( true );
		set.setRevision( name );
		
		try {
			// create in database
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			if( src != null )
				ldm.copydbAll( context , src );
			else
				ldm.createdbAll( context );
	
			// create releases repository
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.createReleases( set , forceClearMeta );
			
			// create environments
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.createAll( forceClearMeta );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to create revision, product=" + product.NAME + ", revision=" + name );
			if( set != null ) {
				set.meta.deleteObject();
				set.deleteObject();
			}
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

	public ProductMeta importProduct( AppProduct product , String revision , boolean includingEnvironments ) throws Exception {
		DBConnection c = loader.getConnection();
		
		EngineProducts products = data.getProducts();
		trace( "reload settings, product=" + product.NAME + " ..." );
		
		if( !matchProductMirrors( product ) )
			Common.exit1( _Error.InvalidProductMirros1 , "Invalid product mirror repositories, product=" + product.NAME , product.NAME );

		EngineProduct ep = products.getEngineProduct( product );
		EngineProductRevisions epr = ep.getRevisions();
		ProductMeta storageOld = epr.getDraftRevision();
		if( storageOld != null && !storageOld.REVISION.equals( revision ) )
			Common.exit1( _Error.NeedCompleteDraft1 , "Please complete draft before import, revision=" + storageOld.REVISION , storageOld.REVISION );
		
		storageOld = epr.findRevision( revision );
		if( storageOld != null ) {
			if( !storageOld.isDraft() )
				Common.exit1( _Error.ImportCompletedRevision1 , "Cannot import over completed revision=" + revision , revision );
			
			if( includingEnvironments ) {
				EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , storageOld );
				lde.dropEnvs();
			}
				
			// rename revision to allow create new one with the same name
			TransactionBase transaction = loader.getTransaction();
			DBMeta.hideRevision( transaction , storageOld );
		}
		
		ProductMeta storageNew = importProduct( product , true , revision , includingEnvironments , storageOld );
		if( storageNew == null )
			Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );

		if( !storageNew.MATCHED )
			Common.exit1( _Error.UnusableProductMetadata1 , "Unable to load product metadata, product=" + product.NAME , product.NAME );

		if( storageOld != null )
			DBProductData.dropProductData( c , storageOld );
		
		return( storageNew );
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
	
	private ProductMeta importProduct( AppProduct product , boolean update , String revision , boolean includingEnvironments , ProductMeta storageOld ) {
		EngineProducts products = data.getProducts();
		EngineProduct ep = products.findEngineProduct( product );
		
		ProductMeta storage = new ProductMeta( engine , ep );
		storage.setMatched( true );
		storage.setRevision( revision );
		
		ActionBase action = loader.getAction();
		try {
			if( storageOld == null && ep.findRevision( revision ) != null )
				Common.exit1( _Error.FinalRevisionExists1 , "Cannot replace finalized revision=" + revision , revision );
			
			TransactionBase transaction = loader.getTransaction();
			if( storageOld == null )
				transaction.createProductMetadata( product , storage );
			else
				transaction.requestReplaceProductMetadata( storage , storageOld );
			
			UrmStorage urm = action.artefactory.getUrmStorage();
			LocalFolder meta = urm.getProductCoreMetadataFolder( action , product );
			
			if( meta.checkExists( action ) ) {
				LocalFolder home = urm.getProductHome( action , product );
				ProductContext context = new ProductContext( product , loader.getSettings() , home );
				
				ProductStorage ms = action.artefactory.getMetadataStorage( action , product );
				importxmlAll( ep , storage , storageOld , ms , context , update , includingEnvironments );

				EngineMatcher matcher = loader.getMatcher();
				if( !matcher.matchProduct( loader , product , storage , update ) )
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
			storage.setMatched( false );
		}
		
		return( storage );
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
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to load revision metadata, product=" + product.NAME + ", revision=" + set.REVISION );
			set.setMatched( false );
		}
	
		try {
			ep.addProductMeta( set );
		}
		catch( Throwable e ) {
			action.handle( e );
			action.error( "unable to add revision metadata, product=" + product.NAME + ", revision=" + set.REVISION );
		}
	}

	private void loadProductDistributives( AppProduct product , ProductContext context , boolean update , EngineProduct ep ) throws Exception {
		EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
		ldr.loadDistributives( false );
	}
	
	private void importxmlAll( EngineProduct ep , ProductMeta set , ProductMeta setOld , ProductStorage ms , ProductContext context , boolean update , boolean includingEnvironments ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.importxmlAll( ms , context );
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.loadReleasesByImport( set , setOld );
			ldr.loadDistributives( true );
		
			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			if( includingEnvironments )
				lde.importxmlAll( ep , ms , update );
			else
 				lde.loaddbAll();
				
			ldm.loadDesignDocs( ms );
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
			
			EngineLoaderReleases ldr = new EngineLoaderReleases( loader , ep );
			ldr.loadReleases( set );

			EngineLoaderEnvs lde = new EngineLoaderEnvs( loader , set );
			lde.loaddbAll();
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.NAME , set.NAME );
		}
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

}
