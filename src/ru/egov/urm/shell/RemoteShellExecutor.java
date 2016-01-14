package ru.egov.urm.shell;

import ru.egov.urm.run.ActionBase;

public class RemoteShellExecutor extends ShellExecutor {
	Account account;

	public RemoteShellExecutor( String name , ShellExecutorPool pool , Account account , String rootPath ) {
		super( name , pool , rootPath );
		this.account = account;
	}

	public void start( ActionBase action ) throws Exception {
		ProcessBuilder builder;
		String keyFile = action.context.KEYNAME;
		if( !keyFile.isEmpty() )
			builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN , "-i " , keyFile );
		else
			builder = new ProcessBuilder( "ssh" , "-T" , account.HOSTLOGIN );
		super.createProcess( action , builder , rootPath );
	}
	
}
