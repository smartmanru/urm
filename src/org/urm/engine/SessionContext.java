package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;
import org.urm.engine.meta.Meta;
import org.urm.engine.storage.LocalFolder;
import org.urm.engine.storage.UrmStorage;

public class SessionContext {

	public ServerEngine engine;
	public RunContext clientrc;
	public RunContext execrc;
	public int sessionId;
	
	public String ENV;
	public String DC;

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
	
	private ServerAuthContext login;
	
	public SessionContext( ServerEngine engine , RunContext clientrc , int sessionId , boolean client ) {
		this.engine = engine;
		this.clientrc = clientrc;
		this.sessionId = sessionId;
		this.client = client;
		
		this.execrc = engine.execrc;
		this.ENV = clientrc.envName;
		this.DC = clientrc.dcName;
		
		timestamp = Common.getNameTimeStamp();
	}
	
	public void setLoginAuth( ServerAuthContext login ) {
		this.login = login;
	}
	
	public ServerAuthContext getLoginAuth() {
		return( login );
	}
	
	public void setServerLayout( CommandOptions options ) throws Exception {
		installPath = clientrc.installPath;
		if( installPath.isEmpty() )
			Common.exit0( _Error.MasterpathEmpty0 , "masterpath is empty" );
		
		productName = "";
		productDir = "";
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
		etcPath = Common.getPath( installPath , "etc" );
		
		productPath = "";
		proxyPath = "";
	}

	public void setServerSystemProductLayout( ActionBase action , String name , String path ) throws Exception {
		setServerInternalProductLayout( action , name , path );
	}
	
	private void setServerInternalProductLayout( ActionBase action , String name , String path ) throws Exception {
		product = true;
		
		if( path.isEmpty() )
			Common.exit1( _Error.MissingProductFolder1 , "missing product folder" , name );
		
		this.productName = name;
		this.productDir = path;
		
		UrmStorage storage = action.artefactory.getUrmStorage();
		LocalFolder products = storage.getServerProductsFolder( action );
		LocalFolder product = products.getSubFolder( action , path );
		
		productPath = product.folderPath;
		etcPath = product.getFolderPath( action , UrmStorage.ETC_PATH );
		proxyPath = product.getFolderPath( action , UrmStorage.MASTER_PATH );
		
		ServerLoader loader = engine.getLoader();
		Meta meta = loader.createMetadata( this );
		action.context.setMeta( meta );
	}
	
	public void clearServerProductLayout() {
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
		
		ServerDirectory directory = serverAction.actionInit.getDirectory();
		ServerProduct product = directory.getProduct( name ); 
		setServerInternalProductLayout( serverAction , product.NAME , product.PATH );
	}
	
	public void setServerRemoteProductLayout( ActionBase serverAction ) throws Exception {
		SessionContext serverSession = serverAction.session;
		
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		ServerDirectory directory = serverAction.actionInit.getDirectory();
		ServerProduct product = directory.getProduct( clientrc.product ); 
		setServerInternalProductLayout( serverAction , product.NAME , product.PATH );
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		offline = true;
		standalone = true;
		product = true;
		
		productPath = clientrc.installPath;
		if( productPath.isEmpty() )
			Common.exit0( _Error.ProductpathEmpty0 , "productpath is empty" );
		
		productName = "";
		productDir = "";
		masterPath = Common.getPath( productPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );

		etcPath = Common.getPath( productPath , "etc" );
		proxyPath = Common.getPath( productPath , "master" );
	}
	
}
