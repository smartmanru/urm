package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;

public class RemoteShellExecutor extends ShellExecutor {
	Account hl;

	public RemoteShellExecutor( String name , ShellExecutorPool pool , Account hostLogin , String rootPath ) {
		super( name , pool , rootPath );
		this.hl = hostLogin;
	}
	
	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			builder = new ProcessBuilder( "ssh" , "-T" , hl.HOSTLOGIN , "-i " , keyFile );
		else
			builder = new ProcessBuilder( "ssh" , "-T" , hl.HOSTLOGIN );
		super.createProcess( action , builder , rootPath );
	}
	
}
