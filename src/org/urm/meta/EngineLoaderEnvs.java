package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.env.DBMetaEnv;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.product.Meta;
import org.urm.meta.product.MetaProductSettings;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EngineLoaderEnvs {

	public static String XML_ROOT_ENV = "environment";
	public static String XML_ROOT_MONITORING = "monitoring";
	
	public EngineLoader loader;
	public ProductMeta set;
	public Meta meta;
	
	public EngineLoaderEnvs( EngineLoader loader , ProductMeta set ) {
		this.loader = loader;
		this.set = set;
		this.meta = set.meta;
	}

	public void createAll() throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
	}
	
	public void exportAll( ProductStorage ms ) throws Exception {
		exportEnvs( ms );
		saveMonitoring( ms );
	}

	public void importxmlEnvs( ProductStorage ms ) throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
		
		ActionBase action = loader.getAction();
		for( String envFile : ms.getEnvFiles( action ) )
			importxmlEnvData( ms , envFile );
		
		loadMonitoring( ms );
	}
	
	public void exportEnvs( ProductStorage ms ) throws Exception {
		ProductEnvs envs = set.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findEnv( envName );
			exportEnvData( ms , env );
		}
	}

	public void loaddbEnvs() throws Exception {
	}
	
	public void loadMonitoring( ProductStorage ms ) throws Exception {
		ProductEnvs envs = set.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
		
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getMonitoringConfFile( action );
			action.debug( "read monitoring definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			// monitoring settings
			mon.load( action , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to import monitoring metadata, product=" + set.name , set.name );
		}
	}

	public void saveMonitoring( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getMonitoringConfFile( action );
		action.debug( "export product monitoring file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		ProductEnvs envs = set.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
		
		mon.save( action , doc , doc.getDocumentElement() );
		ms.saveFile( action , doc , file );
	}
	
	private void importxmlEnvData( ProductStorage ms , String envName ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getEnvConfFile( action , envName );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaEnv.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.name + ", env=" + envName , set.name , envName );
		}
	}
	
	private void exportEnvData( ProductStorage storageMeta , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		String envFile = env.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
}
