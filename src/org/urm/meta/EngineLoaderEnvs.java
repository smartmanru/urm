package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.env.DBMetaEnv;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.env.MetaMonitoring;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
	
	public void exportxmlAll( ProductStorage ms ) throws Exception {
		exportxmlEnvs( ms );
		exportxmlMonitoring( ms );
	}

	public void importxmlAll( ProductStorage ms ) throws Exception {
		importxmlEnvs( ms );
		importxmlMonitoring( ms );
	}
	
	public void importxmlEnvs( ProductStorage ms ) throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
		
		ActionBase action = loader.getAction();
		for( String envFile : ms.getEnvFiles( action ) )
			importxmlEnvData( ms , envFile );
		
		importxmlMonitoring( ms );
	}
	
	private void exportxmlEnvs( ProductStorage ms ) throws Exception {
		ProductEnvs envs = set.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findMetaEnv( envName );
			exportEnvData( ms , env );
		}
	}

	public void loaddbAll() throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
	}
	
	public void loaddbEnvs() throws Exception {
	}
	
	public void importxmlMonitoring( ProductStorage ms ) throws Exception {
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

	public void exportxmlMonitoring( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getMonitoringConfFile( action );
		action.debug( "export product monitoring file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		ProductEnvs envs = set.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
		
		mon.save( action , doc , doc.getDocumentElement() );
		ms.saveFile( action , doc , file );
	}
	
	private void importxmlEnvData( ProductStorage ms , String envFile ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getEnvFilePath( action , envFile );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaEnv.importxml( loader , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.name + ", env file=" + envFile , set.name , envFile );
		}
	}
	
	private void exportEnvData( ProductStorage ms , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getEnvConfFile( action , env.NAME );
		action.debug( "export environment file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		Element root = doc.getDocumentElement();
		
		DBMetaEnv.exportxml( loader , set , env , doc , root );
		ms.saveDoc( doc , file );
	}
	
}
