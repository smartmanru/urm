package org.urm.meta.loader;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.db.DBConnection;
import org.urm.db.env.DBEnvData;
import org.urm.db.env.DBMetaEnv;
import org.urm.engine.products.EngineProduct;
import org.urm.engine.storage.ProductStorage;
import org.urm.engine.transaction.TransactionBase;
import org.urm.meta.env.MetaEnv;
import org.urm.meta.env.ProductEnvs;
import org.urm.meta.product.Meta;
import org.urm.meta.product.ProductMeta;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class EngineLoaderEnvs {

	public static String XML_ROOT_ENV = "environment";
	
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
	}

	public void importxmlAll( EngineProduct ep , ProductStorage ms , boolean update ) throws Exception {
		ProductEnvs envs = new ProductEnvs( set , set.meta );
		set.setEnvs( envs );
		
		importxmlEnvs( ep , ms , update , envs );
	}
	
	private void importxmlEnvs( EngineProduct ep , ProductStorage ms , boolean update , ProductEnvs envs ) throws Exception {
		ActionBase action = loader.getAction();
		for( String envFile : ms.getEnvFiles( action ) )
			importxmlEnvData( ep , ms , envFile );
		
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
	}
	
	private void importxmlEnvData( EngineProduct ep , ProductStorage ms , String envFile ) throws Exception {
		ActionBase action = loader.getAction();
		try {
			// read
			String file = ms.getEnvFilePath( action , envFile );
			action.debug( "read environment definition file " + file + "..." );
			Document doc = action.readXmlFile( file );
			Node root = doc.getDocumentElement();
			
			DBMetaEnv.importxml( loader , ep , set , root );
		}
		catch( Throwable e ) {
			loader.setLoadFailed( action , _Error.UnableLoadProductEnvironment2 , e , "unable to load environment metadata, product=" + set.NAME + ", env file=" + envFile , set.NAME , envFile );
		}
	}
	
	private void exportEnvData( ProductStorage ms , MetaEnv env ) throws Exception {
		ActionBase action = loader.getAction();
		String file = ms.getEnvConfFile( action , env.NAME );
		action.debug( "export environment file " + file + "..." );
		Document doc = Common.xmlCreateDoc( XML_ROOT_ENV );
		Element root = doc.getDocumentElement();
		
		DBMetaEnv.exportxml( loader , set , env , doc , root );
		ProductStorage.saveDoc( doc , file );
	}

	public void dropEnvs() throws Exception {
		DBConnection c = loader.getConnection();
		DBEnvData.dropEnvData( c , set );
		
		ProductEnvs envs = set.getEnviroments();
		TransactionBase transaction = loader.getTransaction();
		for( MetaEnv env : envs.getEnvs() )
			envs.deleteEnv( transaction , env );
	}
	
}
