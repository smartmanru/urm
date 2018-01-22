package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.db.engine.DBEngineDirectory;
import org.urm.db.product.DBMeta;
import org.urm.db.product.DBProductData;
import org.urm.engine.Engine;
import org.urm.engine.dist.DistRepository;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.MetadataStorage;
import org.urm.engine.storage.UrmStorage;
import org.urm.meta.engine.AppProduct;
import org.urm.meta.engine.EngineDirectory;
import org.urm.meta.engine.EngineProducts;
import org.urm.meta.product.MetaDesignDiagram;
import org.urm.meta.product.MetaEnv;
import org.urm.meta.product.MetaProductSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EngineLoaderProducts {

	public static String XML_ROOT_ENV = "environment";
	public static String XML_ROOT_DESIGN = "design";

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
	
	public void saveAll( ProductMeta set ) throws Exception {
		ActionBase action = loader.getAction();
		MetadataStorage ms = action.artefactory.getMetadataStorage( action , set.meta );
		
		EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
		ldm.saveAll( ms );
		
		for( String envName : set.getEnvironmentNames() ) {
			MetaEnv env = set.findEnvironment( envName );
			saveEnvData( set , ms , env );
		}
		for( String diagramName : set.getDiagramNames() ) {
			MetaDesignDiagram diagram = set.findDiagram( diagramName );
			saveDesignData( set , ms , diagram );
		}
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
					loaddbAll( set , context );

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
	
	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product ) throws Exception {
		loader.log( msg ,  e );
		Common.exit1( error , msg , product );
	}
	
	private void setLoadFailed( ActionBase action , int error , Throwable e , String msg , String product , String item ) throws Exception {
		loader.log( msg ,  e );
		Common.exit2( error , msg , product , item );
	}
	
	private void importxmlAll( ProductMeta set , MetadataStorage storageMeta , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.importxmlAll( storageMeta , context );
		
			for( String envFile : storageMeta.getEnvFiles( action ) )
				loadEnvData( set , storageMeta , envFile );
			for( String designFile : storageMeta.getDesignFiles( action ) )
				loadDesignData( set , storageMeta , designFile );
			
			loadReleases( set , true );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.name , set.name );
		}
	}
	
	private void loaddbAll( ProductMeta set , ProductContext context ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			EngineLoaderMeta ldm = new EngineLoaderMeta( loader , set );
			ldm.loaddbAll( context );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductMeta1 , e , "unable to load metadata, product=" + set.name , set.name );
		}
	}
	
	private void loadEnvData( ProductMeta set , MetadataStorage storageMeta , String envName ) throws Exception {
		MetaProductSettings settings = set.getSettings();
		MetaEnv env = new MetaEnv( set , settings , set.meta );
		loader.trace( "load meta env object, id=" + env.objectId );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = storageMeta.getEnvConfFile( action , envName );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			env.load( action , root );
			set.addEnv( env );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.name + ", env=" + envName , set.name , envName );
		}
	}
	
	private void loadDesignData( ProductMeta set , MetadataStorage storageMeta , String diagramName ) throws Exception {
		MetaDesignDiagram diagram = new MetaDesignDiagram( set , set.meta );
		
		ActionBase action = loader.getAction();
		try {
			// read
			String filePath = storageMeta.getDesignFile( action , diagramName );
			action.debug( "read design definition file " + filePath + "..." );
			Document doc = action.readXmlFile( filePath );
			Node root = doc.getDocumentElement();
			diagram.load( action , root );
			set.addDiagram( diagram );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductDiagram2 , e , "unable to load design metadata, product=" + set.name + ", diagram=" + diagramName , set.name , diagramName );
		}
	}

	private void loadReleases( ProductMeta set , boolean importxml ) throws Exception {
		ActionBase action = loader.getAction();
		
		try {
			DistRepository repo = DistRepository.loadDistRepository( action , set.meta , importxml );
			set.setReleases( repo );
		}
		catch( Throwable e ) {
			setLoadFailed( action , _Error.UnableLoadProductReleases1 , e , "unable to load release repository, product=" + set.name , set.name );
		}
	}
	
	private void saveEnvData( ProductMeta set , MetadataStorage storageMeta , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		String envFile = env.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
	private void saveDesignData( ProductMeta set , MetadataStorage storageMeta , MetaDesignDiagram diagram ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_DESIGN );
		diagram.save( action , doc , doc.getDocumentElement() );
		String diagramFile = diagram.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , diagramFile );
	}
	
	public void trace( String s ) {
		loader.trace( s );
	}

}
