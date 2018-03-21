package org.urm.meta;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.env.DBMetaEnv;
import org.urm.db.env.DBMetaMonitoring;
import org.urm.engine.storage.ProductStorage;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
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

	public void createAll( boolean forceClearMeta ) throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
	}
	
	public void exportxmlAll( ProductStorage ms ) throws Exception {
		exportxmlEnvs( ms );
		exportxmlMonitoring( ms );
	}

	public void importxmlAll( ProductStorage ms , boolean update ) throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
		
		importxmlEnvs( ms , update , envs );
		importxmlMonitoring( ms , envs );
	}
	
	private void importxmlEnvs( ProductStorage ms , boolean update , ProductEnvs envs ) throws Exception {
		ActionBase action = loader.getAction();
		for( String envFile : ms.getEnvFiles( action ) )
			importxmlEnvData( ms , envFile );
		
		for( MetaEnv env : envs.getEnvs() ) {
			DBMetaEnv.matchBaseline( loader , set , env );
			env.refreshProperties();
		}

		for( MetaEnv env : envs.getEnvs() ) {
			EngineMatcher matcher = loader.getMatcher();
			if( !matcher.matchEnv( loader , set , env , update ) )
				loader.trace( "match failed for env=" + env.NAME );
			else
				loader.trace( "successfully matched env=" + env.NAME );
		}
	}
	
	private void importxmlMonitoring( ProductStorage ms , ProductEnvs envs ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getMonitoringConfFile( action );
			action.debug( "read monitoring definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			// monitoring settings
			DBMetaMonitoring.importxml( loader , set , envs , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductMonitoring1 , e , "unable to import monitoring metadata, product=" + set.name , set.name );
		}
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
		
		DBMetaEnv.loaddbProductEnvs( loader , set , envs );
		DBMetaMonitoring.loaddbProductMonitoring( loader , set , envs );
	}
	
	public void exportxmlMonitoring( ProductStorage ms ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getMonitoringConfFile( action );
		action.debug( "export product monitoring file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_MONITORING );
		Element root = doc.getDocumentElement();
		
		DBMetaMonitoring.exportxml( loader , set , doc , root );
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
