package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.MetaEnvs;
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
		MetaEnvs envs = new MetaEnvs( set , set.meta );
		set.setEnvs( envs );
	}
	
	public void exportAll( ProductStorage ms ) throws Exception {
		saveEnvs( ms );
		saveMonitoring( ms );
	}

	public void loadEnvs( ProductStorage ms ) throws Exception {
		MetaEnvs envs = new MetaEnvs( set , set.meta );
		set.setEnvs( envs );
		
		ActionBase action = loader.getAction();
		for( String envFile : ms.getEnvFiles( action ) )
			loadEnvData( ms , envFile );
		
		loadMonitoring( ms );
	}
	
	public void saveEnvs( ProductStorage ms ) throws Exception {
		MetaEnvs envs = set.getEnviroments();
		for( String envName : envs.getEnvNames() ) {
			MetaEnv env = envs.findEnv( envName );
			saveEnvData( ms , env );
		}
		
		saveMonitoring( ms );
	}
	
	public void loadMonitoring( ProductStorage ms ) throws Exception {
		MetaEnvs envs = set.getEnviroments();
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
		MetaEnvs envs = set.getEnviroments();
		MetaMonitoring mon = envs.getMonitoring();
		
		mon.save( action , doc , doc.getDocumentElement() );
		ms.saveFile( action , doc , file );
	}
	
	private void loadEnvData( ProductStorage ms , String envName ) throws Exception {
		MetaEnvs envs = set.getEnviroments();
		MetaProductSettings settings = set.getSettings();
		MetaEnv env = new MetaEnv( set , settings , set.meta );
		loader.trace( "load meta env object, id=" + env.objectId );

		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getEnvConfFile( action , envName );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			env.load( action , root );
			envs.addEnv( env );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.name + ", env=" + envName , set.name , envName );
		}
	}
	
	private void saveEnvData( ProductStorage storageMeta , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		env.save( action , doc , doc.getDocumentElement() );
		String envFile = env.NAME + ".xml";
		storageMeta.saveEnvConfFile( action , doc , envFile );
	}
	
}
