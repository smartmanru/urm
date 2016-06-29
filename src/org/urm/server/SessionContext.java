package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;

public class SessionContext {

	public ServerEngine engine;
	public RunContext clientrc;
	public RunContext execrc;
	public int sessionId;
	
	public String ENV;
	public String DC;
	public boolean executorFailed;

	public boolean product = false;
	public boolean standalone = false;
	public String timestamp;
	
	public String installPath = "";
	public String masterPath = "";
	public String productDir = "";
	public String productPath = "";
	public String binPath = "";
	public String etcPath = "";
	public String proxyPath = "";
	
	public SessionContext( ServerEngine engine , RunContext clientrc , int sessionId ) {
		this.engine = engine;
		this.clientrc = clientrc;
		this.sessionId = sessionId;
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
		standalone = false;
		product = false;
		
		installPath = clientrc.installPath;
		if( installPath.isEmpty() )
			exit( "masterpath is empty" );
		
		productDir = "";
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
		
		productPath = "";
		etcPath = "";
		proxyPath = "";
	}
	
	public void setServerProductLayout( String productDir ) throws Exception {
		standalone = false;
		product = true;
		
		if( productDir.isEmpty() )
			exit( "missing product folder" );
		
		this.productDir = productDir;
		productPath = Common.getPath( installPath , "products" , productDir );
		etcPath = Common.getPath( productPath , "etc" );
		proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void setServerClientLayout( SessionContext serverSession ) throws Exception {
		productDir = clientrc.productDir;
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		setServerProductLayout( clientrc.productDir );
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		standalone = true;
		product = true;
		
		productPath = clientrc.installPath;
		if( productPath.isEmpty() )
			exit( "productpath is empty" );
		
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
