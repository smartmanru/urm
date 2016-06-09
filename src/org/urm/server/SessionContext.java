package org.urm.server;

import org.urm.common.Common;
import org.urm.common.ExitException;
import org.urm.common.RunContext;
import org.urm.common.action.CommandOptions;

public class SessionContext {

	public RunContext cmdrc;
	public String ENV;
	public String DC;
	public boolean executorFailed;

	public String installPath = "";
	public String masterPath = "";
	public String productPath = "";
	public String binPath = "";
	public String etcPath = "";
	public String proxyPath = "";
	
	public SessionContext( RunContext cmdrc ) {
		this.cmdrc = cmdrc;
		this.ENV = cmdrc.envName;
		this.DC = cmdrc.dcName;
		
		executorFailed = false;
	}
	
	public void exit( String msg ) throws Exception {
		throw new ExitException( msg );
	}
	
	public void setServerLayout( CommandOptions options ) throws Exception {
		installPath = cmdrc.installPath;
		if( installPath.isEmpty() )
			exit( "masterpath is empty" );
		
		masterPath = Common.getPath( installPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );
		
		productPath = "";
		etcPath = "";
		proxyPath = "";
	}
	
	public void setServerProductLayout( String product ) throws Exception {
		if( product.isEmpty() )
			exit( "unknown product" );
		
		productPath = Common.getPath( installPath , "products" , product );
		etcPath = Common.getPath( productPath , "etc" );
		proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void setServerClientLayout( CommandOptions options , SessionContext serverSession ) throws Exception {
		installPath = serverSession.installPath;
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		setServerProductLayout( cmdrc.productName );
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		productPath = cmdrc.installPath;
		if( productPath.isEmpty() )
			exit( "productpath is empty" );
		
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
