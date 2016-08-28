package org.urm.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.urm.common.Common;
import org.urm.common.ConfReader;
import org.urm.common.PropertySet;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.MetaDatabase;
import org.urm.server.meta.MetaDesign;
import org.urm.server.meta.MetaDistr;
import org.urm.server.meta.MetaEnv;
import org.urm.server.meta.MetaMonitoring;
import org.urm.server.meta.MetaProductSettings;
import org.urm.server.meta.MetaSource;
import org.urm.server.meta.Meta;
import org.urm.server.meta.MetaProductVersion;
import org.urm.server.storage.MetadataStorage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ServerProductMeta {

	public ServerLoader loader;
	public String name;
	public SessionContext session;
	
	public Meta meta;
	
	private MetaProductVersion version;
	private MetaProductSettings product;
	private MetaDatabase database;
	private MetaDistr distr;
	private MetaSource sources;
	private MetaMonitoring mon;
	
	private Map<String,MetaEnv> envs;
	private Map<String,MetaDesign> designFiles;
	
	public static String XML_ROOT_VERSION = "version";
	public static String XML_ROOT_PRODUCT = "product";
	public static String XML_ROOT_DISTR = "distributive";
	public static String XML_ROOT_DB = "database";
	public static String XML_ROOT_SRC = "sources";
	public static String XML_ROOT_MONITORING = "monitoring";
	public static String XML_ROOT_ENV = "environment";

	public boolean loadFailed;
	
	public ServerProductMeta( ServerLoader loader , String name , SessionContext session ) {
		this.loader = loader;
		this.name = name;
		this.session = session;
		
		meta = new Meta( this , session );
		designFiles = new HashMap<String,MetaDesign>();
		envs = new HashMap<String,MetaEnv>();
		
		loadFailed = false;
	}

	public synchronized ServerProductMeta copy( ActionBase action ) throws Exception {
		ServerProductMeta r = new ServerProductMeta( loader , name , session );
		r.meta = new Meta( r , session );
		if( version != null ) {
			r.version = version.copy( action , r.meta );
			if( r.version.loadFailed )
				r.loadFailed = true;
		}
		if( product != null ) {
			r.product = product.copy( action , r.meta );
			if( r.product.isLoadFailed() )
				r.loadFailed = true;
		}
		if( database != null ) {
			r.database = database.copy( action , r.meta );
			if( r.database.loadFailed )
				r.loadFailed = true;
		}
		if( distr != null ) {
			r.distr = distr.copy( action , r.meta );
			if( r.distr.loadFailed )
				r.loadFailed = true;
		}
		if( sources != null ) {
			r.sources = sources.copy( action , r.meta );
			if( r.sources.loadFailed )
				r.loadFailed = true;
		}
		if( mon != null ) {
			r.mon = mon.copy( action , r.meta );
			if( r.mon.loadFailed )
				r.loadFailed = true;
		}
		for( String envKey : envs.keySet() ) {
			MetaEnv env = envs.get( envKey );
			MetaEnv re = env.copy( action , r.meta );
			r.envs.put( envKey , re );
			if( re.loadFailed )
				r.loadFailed = true;
		}
		for( String designFile : designFiles.keySet() ) {
			MetaDesign design = designFiles.get( designFile );
			MetaDesign rd = design.copy( action , r.meta );
			r.designFiles.put( designFile , rd );
			if( rd.loadFailed )
				r.loadFailed = true;
		}
		
		return( r );
	}

	public void setLoadFailed( ActionBase action , String msg ) {
		loadFailed = true;
		action.error( msg );
	}
	
	private void setLoadFailed( ActionBase action , Throwable e , String msg ) {
		loadFailed = true;
		action.log( e );
		action.error( msg );
	}
	
	public synchronized MetaProductVersion loadVersion( ActionBase action , MetadataStorage storageMeta ) {
		if( version != null )
			return( version );
		
		version = new MetaProductVersion( meta );
		meta.setVersion( version );

		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getVersionConfFile( action );
				action.debug( "read product version file " + file + "..." );
				Document doc = ConfReader.readXmlFile( action.session.execrc , file );
				Node root = doc.getDocumentElement();
				version.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load version metadata, product=" + name );
				version.createFailed();
			}
		}
		
		return( version );
	}
	
	public synchronized MetaProductSettings loadProduct( ActionBase action , MetadataStorage storageMeta ) {
		if( product != null )
			return( product );
		
		PropertySet execprops = null;
		if( action.engine.execrc.standaloneMode ) {
			execprops = new PropertySet( "execrc" , null );
			try {
				action.engine.execrc.getProperties( execprops );
			}
			catch( Throwable e ) {
				action.log( e );
			}
		}
		else {
			ServerRegistry registry = loader.getRegistry();
			execprops = registry.serverContext.execprops;
		}
		
		product = new MetaProductSettings( meta , execprops );
		meta.setProduct( product );

		if( !loadFailed ) {
			try {
				ServerProductContext productContext = new ServerProductContext( meta );
				productContext.create( action , version );
				
				// read
				String file = storageMeta.getProductConfFile( action );
				action.debug( "read product definition file " + file + "..." );
				Document doc = ConfReader.readXmlFile( action.session.execrc , file );
				Node root = doc.getDocumentElement();
				product.load( action , productContext , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load settings metadata, product=" + name );
			}
		}
		
		return( product );
	}
	
	public synchronized MetaDatabase loadDatabase( ActionBase action , MetadataStorage storageMeta ) {
		if( database != null )
			return( database );
		
		database = new MetaDatabase( meta );
		meta.setDatabase( database );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getDatabaseFile( action );
				action.debug( "read database definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				database.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load database metadata, product=" + name );
				database.setLoadFailed();
			}
		}
		
		return( database );
	}
	
	public synchronized MetaDistr loadDistr( ActionBase action , MetadataStorage storageMeta ) {
		if( distr != null )
			return( distr );
		
		distr = new MetaDistr( meta );
		meta.setDistr( distr );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getDistrFile( action );
				action.debug( "read distributive definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				distr.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load distributive metadata, product=" + name );
				distr.setLoadFailed();
			}
		}
		
		return( distr );
	}

	public synchronized MetaSource loadSources( ActionBase action , MetadataStorage storageMeta ) {
		if( sources != null )
			return( sources );
		
		sources = new MetaSource( meta );
		meta.setSources( sources );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getSourceConfFile( action );
				action.debug( "read source definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				sources.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load source metadata, product=" + name );
				sources.setLoadFailed();
			}
		}
		
		return( sources );
	}
	
	public synchronized MetaMonitoring loadMonitoring( ActionBase action , MetadataStorage storageMeta ) {
		if( mon != null )
			return( mon );
		
		mon = new MetaMonitoring( meta );
		
		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getMonitoringFile( action );
				action.debug( "read monitoring definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				mon.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load monitoring metadata, product=" + name );
				mon.setLoadFailed();
			}
		}
		
		return( mon );
	}
	
	public synchronized MetaEnv loadEnvData( ActionBase action , MetadataStorage storageMeta , String envName ) {
		MetaEnv env = envs.get( envName );
		if( env != null )
			return( env );
		
		env = new MetaEnv( meta );
		envs.put( envName , env );

		if( !loadFailed ) {
			try {
				// read
				String file = storageMeta.getEnvFile( action , envName );
				action.debug( "read environment definition file " + file + "..." );
				Document doc = action.readXmlFile( file );
				Node root = doc.getDocumentElement();
				env.load( action , root );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load environment metadata, product=" + name + ", env=" + envName );
				env.setLoadFailed();
			}
		}
		
		return( env );
	}
	
	public synchronized MetaDesign loadDesignData( ActionBase action , MetadataStorage storageMeta , String fileName ) {
		MetaDesign design = designFiles.get( fileName );
		if( design != null )
			return( design );
		
		design = new MetaDesign( meta );
		
		if( !loadFailed ) {
			try {
				// read
				String filePath = storageMeta.getDesignFile( action , fileName );
				action.debug( "read design definition file " + filePath + "..." );
				Document doc = action.readXmlFile( filePath );
				Node root = doc.getDocumentElement();
				design.load( action , root );
				designFiles.put( fileName , design );
			}
			catch( Throwable e ) {
				setLoadFailed( action , e , "unable to load design metadata, product=" + name + ", file=" + fileName );
				design.setLoadFailed();
			}
		}
		
		return( design );
	}

	public synchronized void loadAll( ActionBase action , MetadataStorage storageMeta ) {
		loadVersion( action , storageMeta );
		loadProduct( action , storageMeta );
		loadDatabase( action , storageMeta );
		loadDistr( action , storageMeta );
		loadSources( action , storageMeta );
		loadMonitoring( action , storageMeta );
		
		action.meta.setVersion( version );
		action.meta.setProduct( product );
		action.meta.setDatabase( database );
		action.meta.setDistr( distr );
		action.meta.setSources( sources );
		
		if( loadFailed )
			return;
		
		try {
			for( String envFile : storageMeta.getEnvFiles( action ) )
				loadEnvData( action , storageMeta , envFile );
			for( String designFile : storageMeta.getDesignFiles( action ) )
				loadDesignData( action , storageMeta , designFile );
		}
		catch( Throwable e ) {
			setLoadFailed( action , e , "unable to load metadata, product=" + name );
		}
	}
	
	public synchronized String[] getEnvironments() throws Exception {
		List<String> names = new LinkedList<String>();
		for( MetaEnv env : envs.values() )
			names.add( env.ID );
		Collections.sort( names );
		return( names.toArray( new String[0] ) );
	}

	public synchronized MetaEnv getEnvironment( String envId ) throws Exception {
		for( MetaEnv env : envs.values() ) {
			if( env.ID.equals( envId ) )
				return( env );
		}
		return( null );
	}

	public synchronized void createInitial( ActionBase action , ServerRegistry registry ) throws Exception {
		createInitialVersion( action , registry );
		createInitialProduct( action , registry , registry.serverContext.execprops );
		createInitialDatabase( action , registry );
		createInitialDistr( action , registry );
		createInitialSources( action , registry );
		createInitialMonitoring( action , registry );
	}

	private void createInitialVersion( ActionBase action , ServerRegistry registry ) throws Exception {
		version = new MetaProductVersion( meta );
		meta.setVersion( version );
		version.create( action , registry );
		action.meta.setVersion( version );
	}
	
	private void createInitialProduct( ActionBase action , ServerRegistry registry , PropertySet execprops ) throws Exception {
		product = new MetaProductSettings( meta , execprops );
		meta.setProduct( product );
		
		ServerProductContext productContext = new ServerProductContext( meta );
		productContext.create( action , version );
		
		product.create( action , registry , productContext );
		action.meta.setProduct( product );
	}
	
	private void createInitialDatabase( ActionBase action , ServerRegistry registry ) throws Exception {
		database = new MetaDatabase( meta );
		meta.setDatabase( database );
		database.createInitial( action , registry );
		action.meta.setDatabase( database );
	}
	
	private void createInitialDistr( ActionBase action , ServerRegistry registry ) throws Exception {
		distr = new MetaDistr( meta );
		meta.setDistr( distr );
		distr.createInitial( action , registry );
		action.meta.setDistr( distr );
	}
	
	private void createInitialSources( ActionBase action , ServerRegistry registry ) throws Exception {
		sources = new MetaSource( meta );
		meta.setSources( sources );
		sources.createInitial( action , registry );
		action.meta.setSources( sources );
	}
	
	private void createInitialMonitoring( ActionBase action , ServerRegistry registry ) throws Exception {
		mon = new MetaMonitoring( meta );
		mon.createInitial( action , registry );
	}
	
	public void saveAll( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		saveVersion( action , storageMeta );
		saveProduct( action , storageMeta );
		saveDatabase( action , storageMeta );
		saveDistr( action , storageMeta );
		saveSources( action , storageMeta );
		saveMonitoring( action , storageMeta );
		
		for( String envName : envs.keySet() )
			saveEnvData( action , storageMeta , envName );
		for( String designFile : designFiles.keySet() )
			saveDesignData( action , storageMeta , designFile );
	}
	
	public void saveVersion( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_VERSION );
		version.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveVersionConfFile( action , doc );
	}
	
	public void saveProduct( ActionBase action , MetadataStorage storageMeta ) throws Exception {
		Document doc = Common.xmlCreateDoc( XML_ROOT_PRODUCT );
		product.save( action , doc , doc.getDocumentElement() );
		storageMeta.saveProductConfFile( action , doc );
	}
	
	public void saveDatabase( ActionBase action , MetadataStorage storageMeta ) throws Exception {
	}
	
	public void saveDistr( ActionBase action , MetadataStorage storageMeta ) throws Exception {
	}
	
	public void saveSources( ActionBase action , MetadataStorage storageMeta ) throws Exception {
	}
	
	public void saveMonitoring( ActionBase action , MetadataStorage storageMeta ) throws Exception {
	}
	
	public void saveEnvData( ActionBase action , MetadataStorage storageMeta , String envName ) throws Exception {
	}
	
	public void saveDesignData( ActionBase action , MetadataStorage storageMeta , String designFile ) throws Exception {
	}
	
	public void setVersion( ServerTransaction transaction , MetaProductVersion version ) throws Exception {
		this.version = version;
	}
	
}
