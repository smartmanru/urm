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

	public String masterPath;
	public String binPath;
	public String productPath;
	public String etcPath;
	public String proxyPath;
	
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
		masterPath = cmdrc.productPath;
		if( masterPath.isEmpty() )
			exit( "masterpath is empty" );
		
		binPath = Common.getPath( masterPath , "bin" );
		
		productPath = "";
		etcPath = cmdrc.etcPath;
		proxyPath = cmdrc.proxyPath;
	}
	
	public void setServerClientLayout( CommandOptions options , SessionContext serverSession ) throws Exception {
		masterPath = serverSession.masterPath;
		binPath = serverSession.binPath;
		
		if( cmdrc.productName.isEmpty() )
			exit( "unknown product" );
		
		productPath = Common.getPath( masterPath , "products" , cmdrc.productName );
		if( !serverSession.etcPath.isEmpty() )
			etcPath = Common.getPath( serverSession.etcPath , cmdrc.productName );
		else
			etcPath = Common.getPath( productPath , "etc" );
			
		if( !serverSession.proxyPath.isEmpty() )
			etcPath = Common.getPath( serverSession.proxyPath , cmdrc.productName );
		else
			proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void setStandaloneLayout( CommandOptions options ) throws Exception {
		productPath = cmdrc.productPath;
		if( productPath.isEmpty() )
			exit( "prouctpath is empty" );
		
		masterPath = Common.getPath( productPath , "master" );
		binPath = Common.getPath( masterPath , "bin" );

		productPath = masterPath;
		etcPath = cmdrc.etcPath;
		if( etcPath.isEmpty() )
			etcPath = Common.getPath( productPath , "etc" );
		
		proxyPath = cmdrc.proxyPath;
		if( proxyPath.isEmpty() )
			proxyPath = Common.getPath( productPath , "master" );
	}
	
	public void setFailed() {
		executorFailed = true;
	}
	
	public boolean isFailed() {
		return( executorFailed );
	}
	
}
