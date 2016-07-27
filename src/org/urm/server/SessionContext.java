package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;
import org.urm.server.action.ActionBase;
import org.urm.server.meta.FinalMetaLoader;
import org.urm.server.meta.MetaEngineProduct;

public class SessionContext {

	public ServerEngine engine;
	public RunContext clientrc;
	public RunContext execrc;
	public int sessionId;
	
	public String ENV;
	public String DC;
	public boolean executorFailed;

	public boolean offline = false;
	public boolean product = false;
	public boolean standalone = false;
	public boolean client = false;
	public String timestamp;
	
	public String installPath = "";
	public String masterPath = "";
	public String productName = "";
	public String productDir = "";
	public String productPath = "";
	public String binPath = "";
	public String etcPath = "";
	public String proxyPath = "";
	
	public SessionContext( ServerEngine engine , RunContext clientrc , int sessionId , boolean client ) {
		this.engine = engine;
		this.clientrc = clientrc;
		this.sessionId = sessionId;
		this.client = client;
		
		this.execrc = engine.execrc;
		this.ENV = clientrc.envName;
		this.DC = clientrc.dcName;
		
		executorFailed = false;
		timestamp = Common.getNameTimeStamp();
	}
	
	public void exit( String msg ) throws Exception {
		throw new ExitException( msg );
	}
	
	public void setServerLayout( CommandOptions options ) throws Exception {
		installPath = clientrc.installPath;
		if( installPath.isEmpty() )
			exit( "masterpath is empty" );
		
		productName = "";
		productDir = "";
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
		etcPath = Common.getPath( installPath , "etc" );
		
		productPath = "";
		proxyPath = "";
	}

	public void setServerSystemProductLayout( String name , String path ) throws Exception {
		setServerInternalProductLayout( name , path );
	}
	
	private void setServerInternalProductLayout( String name , String path ) throws Exception {
		product = true;
		
		if( path.isEmpty() )
			exit( "missing product folder" );
		
		this.productName = name;
		this.productDir = path;
		
		productPath = Common.getPath( installPath , path );
		etcPath = Common.getPath( productPath , "etc" );
		proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void clearServerProductLayout() throws Exception {
		product = false;
		
		etcPath = Common.getPath( installPath , "etc" );
		
		productName = "";
		productDir = "";
		productPath = "";
		proxyPath = "";
	}
	
	public void setServerOfflineProductLayout( ActionBase serverAction , CommandOptions options , String name ) throws Exception {
		offline = true;
		
		setServerLayout( options );
		
		FinalMetaLoader loader = engine.metaLoader;
		MetaEngineProduct product = loader.getProductMeta( serverAction , name ); 
		setServerInternalProductLayout( product.NAME , product.PATH );
	}
	
	public void setServerRemoteProductLayout( ActionBase serverAction ) throws Exception {
		SessionContext serverSession = serverAction.session;
		
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		FinalMetaLoader loader = engine.metaLoader;
		MetaEngineProduct product = loader.getProductMeta( serverAction , clientrc.product ); 
		setServerInternalProductLayout( product.NAME , product.PATH );
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		offline = true;
		standalone = true;
		product = true;
		
		productPath = clientrc.installPath;
		if( productPath.isEmpty() )
			exit( "productpath is empty" );
		
		productName = "";
		productDir = "";
		masterPath = Common.getPath( productPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );

		etcPath = Common.getPath( productPath , "etc" );
		proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void setFailed() {
		executorFailed = true;
	}
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
}
