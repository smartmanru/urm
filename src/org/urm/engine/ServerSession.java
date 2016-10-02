package org.urm.engine;

import org.urm.action.ActionBase;
import org.urm.common.Common;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;

public class ServerSession {

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
	public String binPath = "";
	
	private ServerAuthContext login;
	
	public ServerSession( ServerEngine engine , RunContext clientrc , int sessionId , boolean client ) {
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
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
	}

	public void setServerOfflineProductLayout( ActionBase serverAction , CommandOptions options , String name ) throws Exception {
		offline = true;
		
		setServerLayout( options );
		product = true;
		productName = name;
	}
	
	public void setServerRemoteProductLayout( ActionBase serverAction ) throws Exception {
		ServerSession serverSession = serverAction.session;
		
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		product = true;
		productName = clientrc.product;
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		offline = true;
		standalone = true;
		product = true;
		
		product = true;
		productName = clientrc.product;
	}
	
}
